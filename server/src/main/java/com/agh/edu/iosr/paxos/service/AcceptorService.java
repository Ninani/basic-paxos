package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
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
            String acceptValue = acceptRequest.getValue();

            acceptedProposal.set(new AcceptedProposal(acceptNumber, acceptValue));
            notifyLearners(acceptValue);
        }

        return new AcceptResponse(minimumNumber);
    }

    private void notifyLearners(String value) {
        server.getReplicasAddresses().forEach(address -> asyncCaller.exchange(address + "/learn/" + value, HttpMethod.POST, null, String.class));
    }
}
