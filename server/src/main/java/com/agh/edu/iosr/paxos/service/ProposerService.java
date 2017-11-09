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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
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

        List<PrepareResponse> promises = prepare(sequenceNumber);

        if (promises.size() <= server.getHalfReplicasCount()) {
            propose(value);
        }

        value = updateValueIfNecessary(value, promises.stream().map(PrepareResponse::getAcceptedProposal));

        List<AcceptResponse> acceptResponses = accept(sequenceNumber, value);

        if (acceptResponses.size() <= server.getHalfReplicasCount() || acceptResponses.stream().anyMatch(rs -> rs.getSequenceNumber() > sequenceNumber)) {
            propose(value);
        }
    }

    private List<PrepareResponse> prepare(long sequenceNumber) {
        PrepareRequest prepareRequest = new PrepareRequest(sequenceNumber);

        return getResponses("/prepare", prepareRequest, PrepareResponse.class, PrepareResponse::getAnswer);
    }

    private String updateValueIfNecessary(String value, Stream<AcceptedProposal> acceptedProposals) {
        return acceptedProposals
                .filter(Objects::nonNull)
                .sorted((p1, p2) -> Long.compare(p2.getSequenceNumber(), p1.getSequenceNumber()))
                .findFirst()
                .map(AcceptedProposal::getValue)
                .orElse(value);
    }

    private List<AcceptResponse> accept(long sequenceNumber, String value) {
        AcceptRequest acceptRequest = new AcceptRequest(sequenceNumber, value);

        return getResponses("/accept", acceptRequest, AcceptResponse.class, rs -> true);
    }

    private <RQ, RS> List<RS> getResponses(String endpoint, RQ request, Class<RS> responseType, Predicate<RS> responseCondition) {
        Iterator<Future<ResponseEntity<RS>>> callsCyclingIterator = sendAsyncPostRequestsToReplicas(endpoint, request, responseType);
        List<RS> responses = new ArrayList<>();

        while (responses.size() <= server.getHalfReplicasCount() && callsCyclingIterator.hasNext()) {
            Future<ResponseEntity<RS>> call = callsCyclingIterator.next();

            if (call.isCancelled()) {
                callsCyclingIterator.remove();
                LOGGER.info("One of the " + endpoint + " calls timed out.");
            } else if (call.isDone()) {
                try {
                    ResponseEntity<RS> response = call.get();

                    if (response.getStatusCode() == HttpStatus.OK) {
                        RS responseBody = response.getBody();

                        if (responseCondition.test(responseBody)) {
                            responses.add(responseBody);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("{}", e.getMessage(), e);
                }
                callsCyclingIterator.remove();
            }
        }

        return responses;
    }

    private <RQ, RS> Iterator<Future<ResponseEntity<RS>>> sendAsyncPostRequestsToReplicas(String endpoint, RQ requestBody, Class<RS> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RQ> request = new HttpEntity<>(requestBody, headers);

        List<Future<ResponseEntity<RS>>> calls = server.getReplicasAddresses().stream()
                .map(address -> asyncCaller.exchange(address + endpoint, HttpMethod.POST, request, responseType))
                .collect(Collectors.toList());

        return Iterables.cycle(calls).iterator();
    }
}
