package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.messages.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.Status;
import com.agh.edu.iosr.paxos.messages.PromiseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class Proposer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proposer.class);

    @Value("#{'${server.port}'}")
    private String port;
    @Value("#{'${server.replicas}'.split(',')}")
    private List<String> replicaAddresses;
    private Server server;
    private final RestTemplate restTemplate;

    public Proposer(Server server, RestTemplateBuilder restTemplateBuilder) {
        this.server = server;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void processSaveRequest(String value) {
        ArrayList<CompletableFuture<PromiseResponse>> responseList = null;
//        send request to acceptors (Prepare Request)
        try {
            responseList = sendPrepareRequestsToAcceptors();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        update sequence number
        if (responseList != null) {
            try {
                // TODO: 11/5/17 examine what happens if a server is a proposer and acceptor and wants to update the value at the same time
                updateSequenceNumber(responseList);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

//        check if more than a half of responses have the status ACCEPTED (quorum accepts the value) and send acceptRequest
        ArrayList<CompletableFuture<AcceptResponse>> acceptResponseList = null;
        try {
            if (receivedAcceptanceQuorum(responseList)) {
                acceptResponseList = sendAcceptRequestsToAcceptors(value);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    // TODO: 11/5/17 read more about CompletableFuture structure
    private ArrayList<CompletableFuture<PromiseResponse>> sendPrepareRequestsToAcceptors() throws InterruptedException {
        long proposedSequenceNumber = server.incrementAndGetSeqNumber();
        ArrayList<CompletableFuture<PromiseResponse>> responseList = new ArrayList<>();
// TODO: 11/5/17 send prepareRequest only to a quorum of the servers (select them randomly)? or it is only applicable for accept request?
        for (String replica: replicaAddresses) {
            CompletableFuture<PromiseResponse> response = sendPrepareRequest(replica, proposedSequenceNumber);
            responseList.add(response);
        }
        CompletableFuture.allOf(responseList.toArray(new CompletableFuture[responseList.size()])).join();
        return responseList;
    }

    @Async
    private CompletableFuture<PromiseResponse> sendPrepareRequest(String acceptorAddress, long sequenceNumber) throws InterruptedException{
//        asynchronously send sequence number to all acceptors
        LOGGER.debug("Send prepare request from proposer on {} to acceptor on {}", port, acceptorAddress);
        String url = String.format("%s/acceptor/prepare/%s", acceptorAddress, sequenceNumber);
        PromiseResponse promiseResponse = restTemplate.getForObject(url, PromiseResponse.class);

        return CompletableFuture.completedFuture(promiseResponse);
    }

    private void updateSequenceNumber(ArrayList<CompletableFuture<PromiseResponse>> responseList) throws ExecutionException, InterruptedException {
        long highestReceivedSeqNum = -1;

        for (CompletableFuture<PromiseResponse> future: responseList) {
            if (future.get().getSequenceNumber() > highestReceivedSeqNum)
                highestReceivedSeqNum = future.get().getSequenceNumber();
        }
        server.compareAndUpdateSeqNumber(highestReceivedSeqNum);
    }

    private boolean receivedAcceptanceQuorum(ArrayList<CompletableFuture<PromiseResponse>> responseList) throws ExecutionException, InterruptedException {
        int quorumSize = 0;
        if (replicaAddresses.size()%2 == 0) quorumSize = replicaAddresses.size()/2 + 1;
        else quorumSize = replicaAddresses.size()/2 + 1;

        int numberOfAcceptedResponses = 0;
        for (CompletableFuture<PromiseResponse> future: responseList) {
            if (future.get().getStatus().equals(Status.ACCEPTED)) numberOfAcceptedResponses++;
        }

        return numberOfAcceptedResponses >= quorumSize;
    }

    // TODO: 11/5/17 send http status instead of AcceptResponse message???
    private ArrayList<CompletableFuture<AcceptResponse>> sendAcceptRequestsToAcceptors(String value) {
        ArrayList<CompletableFuture<AcceptResponse>> responseList = new ArrayList<>();
        // TODO: 11/5/17 send only to acceptors which accepted (sent Status.ACCEPTED) or to quorum - not to all
        for (String replica: replicaAddresses) {
            CompletableFuture<AcceptResponse> response = sendAcceptRequest(replica, server.getSeqNumber(), value);
            responseList.add(response);
        }
        CompletableFuture.allOf(responseList.toArray(new CompletableFuture[responseList.size()])).join();
        return responseList;
    }

    @Async
    private CompletableFuture<AcceptResponse> sendAcceptRequest(String acceptorAddress, long sequenceNumber, String value) {
        LOGGER.debug("Send accept request from proposer on {} to acceptor on {}", port, acceptorAddress);
        String url = String.format("%s/acceptor/accept/%s/%s", acceptorAddress, sequenceNumber, value);
        AcceptResponse acceptResponse = restTemplate.getForObject(url, AcceptResponse.class);

        return CompletableFuture.completedFuture(acceptResponse);
    }
}
