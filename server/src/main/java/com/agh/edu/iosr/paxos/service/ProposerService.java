package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProposerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProposerService.class);

    private final Server server;
    private final AsyncRestTemplate asyncCaller;

    public ProposerService(Server server, AsyncRestTemplate asyncCaller) {
        this.server = server;
        this.asyncCaller = asyncCaller;
    }

    public void propose(String value) {
        long sequenceNumber = server.incrementAndGetSequenceNumber();

        LOGGER.info("Proposing value " + value + " with sequence number " + sequenceNumber);

        Map<String, PrepareResponse> prepareResponses = prepare(sequenceNumber);

        List<PrepareResponse> promises = prepareResponses.values().stream().filter(PrepareResponse::getAnswer).collect(Collectors.toList());

        if (promises.size() <= server.getHalfReplicasCount()) {
            LOGGER.info("Got minority of promises - retrying value " + value);
            propose(value);
            return;
        }

        String newValue = updateValueIfNecessary(value, promises.stream().map(PrepareResponse::getAcceptedProposal));

        Map<String, AcceptResponse> acceptResponses = accept(sequenceNumber, newValue);

        if (acceptResponses.size() <= server.getHalfReplicasCount()) {
            LOGGER.info("Got minority of accept responses - retrying value " + newValue);
            propose(newValue);
        } else if (acceptResponses.values().stream().anyMatch(rs -> rs.getSequenceNumber() > sequenceNumber)) {
            LOGGER.info("One accept response had a larger sequence number than " + sequenceNumber + " - retrying value " + newValue);
            propose(newValue);
        }

        if (!newValue.equals(value)) {
            LOGGER.info("Value " + value + " had to be overriden with value " + newValue + " - retrying value " + value);
            propose(value);
        }

        LOGGER.info("Done proposing value " + value);
    }

    Map<String, PrepareResponse> prepare(long sequenceNumber) {
        PrepareRequest prepareRequest = new PrepareRequest(sequenceNumber);

        return getResponses("prepare", prepareRequest, PrepareResponse.class);
    }

    String updateValueIfNecessary(String value, Stream<AcceptedProposal> acceptedProposals) {
        return acceptedProposals
                .filter(Objects::nonNull)
                .sorted((p1, p2) -> Long.compare(p2.getSequenceNumber(), p1.getSequenceNumber()))
                .findFirst()
                .map(AcceptedProposal::getValue)
                .orElse(value);
    }

    Map<String, AcceptResponse> accept(long sequenceNumber, String value) {
        AcceptRequest acceptRequest = new AcceptRequest(sequenceNumber, value);

        return getResponses("accept", acceptRequest, AcceptResponse.class);
    }

    private <RQ, RS> Map<String, RS> getResponses(String endpoint, RQ request, Class<RS> responseType) {
        Iterator<Future<ResponseEntity<RS>>> callsCyclingIterator = sendAsyncPostRequestsToReplicas(endpoint, request, responseType);
        Map<String, RS> responses = new HashMap<>();

        while (responses.size() <= server.getHalfReplicasCount() && callsCyclingIterator.hasNext()) {
            Future<ResponseEntity<RS>> call = callsCyclingIterator.next();

            if (call.isCancelled()) {
                callsCyclingIterator.remove();
                LOGGER.info("One of the " + endpoint + " calls timed out.");
            } else if (call.isDone()) {
                try {
                    ResponseEntity<RS> response = call.get();

                    if (response.getStatusCode() == HttpStatus.OK) {
                        responses.put(response.getHeaders().get("port").get(0), response.getBody());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("{}", e.getMessage(), e);
                }
                callsCyclingIterator.remove();
            }
        }

        LOGGER.info("Got " + responses.size() + "/" + server.getReplicasCount() + " " + endpoint + " responses: " + responses);

        return responses;
    }

    private <RQ, RS> Iterator<Future<ResponseEntity<RS>>> sendAsyncPostRequestsToReplicas(String endpoint, RQ requestBody, Class<RS> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("port", server.getPort());
        HttpEntity<RQ> request = new HttpEntity<>(requestBody, headers);

        List<Future<ResponseEntity<RS>>> calls = server.getReplicasAddresses().stream()
                .map(address -> asyncCaller.exchange(address + "/" + endpoint, HttpMethod.POST, request, responseType))
                .collect(Collectors.toList());

        return Iterables.cycle(calls).iterator();
    }
}
