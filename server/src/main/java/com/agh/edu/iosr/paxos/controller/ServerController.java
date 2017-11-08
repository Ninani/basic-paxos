package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
public class ServerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);

    private final Server server;
    private final AsyncRestTemplate asyncCaller;

    public ServerController(Server server, AsyncRestTemplate asyncCaller) {
        this.server = server;
        this.asyncCaller = asyncCaller;
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public ResponseEntity<String> read() {
        return ResponseEntity.ok(server.getValue().get());
    }

    @RequestMapping(value = "/write/{value}", method = RequestMethod.POST)
    public ResponseEntity<String> write(@PathVariable String value) {
        long sequenceNumber = server.incrementAndGetSequenceNumber();

        PrepareRequest prepareRequest = new PrepareRequest(sequenceNumber);
        List<PrepareResponse> promises = getResponses("/prepare", prepareRequest, PrepareResponse.class, PrepareResponse::isAnswerPositive);

        if (promises.size() <= server.getHalfReplicasCount()) {
            return errorResponse("Failure (no majority in the prepare responses).");
        }

        String newValue = promises.stream().sorted().findFirst()
                .map(promiseWithHighestNumberAcceptedValue -> promiseWithHighestNumberAcceptedValue.getAcceptedProposal().getValue())
                .orElse(value);
        AcceptRequest acceptRequest = new AcceptRequest(sequenceNumber, newValue);
        List<AcceptResponse> acceptResponses = getResponses("/accept", acceptRequest, AcceptResponse.class, rs -> true);

        if (acceptResponses.size() <= server.getHalfReplicasCount()) {
            return errorResponse("Failure (no majority in the accept responses).");
        }

        long highestAcceptorNumber = acceptResponses.stream().sorted().findFirst().get().getSequenceNumber();

        if (highestAcceptorNumber > sequenceNumber) {
            return errorResponse("Failure (at least one acceptor had accepted a proposal with a higher sequence number).");
        }

        server.getReplicasAddresses().forEach(address -> asyncCaller.exchange(address + "/learn/" + newValue, HttpMethod.POST, null, String.class));
        server.setValue(newValue); // in case the async learn request to the server itself will not finish before read()

        return read();
    }

    private <RQ, RS> List<RS> getResponses(String endPoint, RQ request, Class<RS> responseType, Predicate<RS> responseCondition) {
        Iterator<Future<ResponseEntity<RS>>> callsCyclingIterator = sendAsyncPostRequestsToReplicas(endPoint, request, responseType);
        List<RS> responses = new ArrayList<>();

        while (responses.size() <= server.getHalfReplicasCount() && callsCyclingIterator.hasNext()) {
            Future<ResponseEntity<RS>> call = callsCyclingIterator.next();

            if (call.isCancelled()) {
                callsCyclingIterator.remove();
                LOGGER.info("One of the " + endPoint + " calls timed out.");
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

    private static ResponseEntity<String> errorResponse(String response) {
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
