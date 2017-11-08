package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AcceptorService {

    private final AtomicLong minimumProposalNumber;
    private final AtomicReference<AcceptedProposal> acceptedProposal;

    public AcceptorService() {
        this.minimumProposalNumber = new AtomicLong();
        this.acceptedProposal = new AtomicReference<>();
    }

//    added for testing purposes
    public AcceptorService(long minimumProposalNumber, AcceptedProposal acceptedProposal) {
        this.minimumProposalNumber = new AtomicLong(minimumProposalNumber);
        this.acceptedProposal = new AtomicReference<>(acceptedProposal);
    }

    public PrepareResponse prepare(PrepareRequest prepareRequest) {
        long prepareNumber = prepareRequest.getSequenceNumber();

        if (minimumProposalNumber.updateAndGet(n -> prepareNumber > n ? prepareNumber : n) == prepareNumber) {
            return new PrepareResponse(true, acceptedProposal.get());
        } else {
            return new PrepareResponse(false);
        }
    }

    public AcceptResponse accept(AcceptRequest acceptRequest) {
        long acceptNumber = acceptRequest.getSequenceNumber();
        long minimumNumber = minimumProposalNumber.updateAndGet(n -> acceptNumber >= n ? acceptNumber : n);

        if (minimumNumber == acceptNumber)
            acceptedProposal.set(new AcceptedProposal(acceptNumber, acceptRequest.getValue()));

        return new AcceptResponse(minimumNumber);
    }
}
