package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AcceptorService {

    private final Server server;
    private final AsyncRestTemplate asyncCaller;
    private final AtomicLong minimumProposalNumber;
    private final AtomicReference<AcceptedProposal> acceptedProposal;

    @Autowired
    public AcceptorService(Server server, AsyncRestTemplate asyncCaller) {
        this.server = server;
        this.asyncCaller = asyncCaller;
        this.minimumProposalNumber = new AtomicLong();
        this.acceptedProposal = new AtomicReference<>();
    }

    // for testing
    public AcceptorService(Server server, AsyncRestTemplate asyncCaller, long minimumProposalNumber, AcceptedProposal acceptedProposal) {
        this.server = server;
        this.asyncCaller = asyncCaller;
        this.minimumProposalNumber = new AtomicLong(minimumProposalNumber);
        this.acceptedProposal = new AtomicReference<>(acceptedProposal);
    }

    public AcceptedProposal getAcceptedProposal() {
        return acceptedProposal.get();
    }

    public PrepareResponse prepare(PrepareRequest prepareRequest) {
        long prepareNumber = prepareRequest.getSequenceNumber();

        if (minimumProposalNumber.updateAndGet(n -> prepareNumber > n ? prepareNumber : n) == prepareNumber) {
            return new PrepareResponse(true, acceptedProposal.get());
        }

        return new PrepareResponse(false);
    }

    public AcceptResponse accept(AcceptRequest acceptRequest) {
        long acceptNumber = acceptRequest.getSequenceNumber();
        long minimumNumber = minimumProposalNumber.updateAndGet(n -> acceptNumber >= n ? acceptNumber : n);

        if (minimumNumber == acceptNumber) {
            AcceptedProposal proposal = new AcceptedProposal(acceptNumber, acceptRequest.getValue());

            acceptedProposal.set(proposal);
            notifyLearners(proposal);
        }

        return new AcceptResponse(minimumNumber);
    }

    public void clearAcceptedProposalOnCommitWithHigherOrEqualNumber(long commitNumber) {
        if (commitNumber >= minimumProposalNumber.get()) {
            acceptedProposal.set(null);
        }
    }

    private void notifyLearners(AcceptedProposal acceptedProposal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("port", server.getPort());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AcceptedProposal> request = new HttpEntity<>(acceptedProposal, headers);
        server.getReplicasAddresses().forEach(address -> asyncCaller.exchange(address + "/learn/", HttpMethod.POST, request, String.class));
    }
}
