package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


@RestController
public class AcceptorController {

    private final AtomicLong minimumProposalNumber;
    private final AtomicReference<AcceptedProposal> acceptedProposal;

    public AcceptorController() {
        this.minimumProposalNumber = new AtomicLong();
        this.acceptedProposal = new AtomicReference<>();
    }

    @PostMapping(value = "/prepare")
    public ResponseEntity<PrepareResponse> prepare(@RequestBody PrepareRequest prepareRequest) {
        long prepareNumber = prepareRequest.getSequenceNumber();

        if (minimumProposalNumber.updateAndGet(n -> prepareNumber > n ? prepareNumber : n) == prepareNumber) {
            return ResponseEntity.ok(new PrepareResponse(true, acceptedProposal.get()));
        }

        return ResponseEntity.ok(new PrepareResponse(false));
    }

    @PostMapping(value = "/accept")
    public ResponseEntity<AcceptResponse> accept(@RequestBody AcceptRequest acceptRequest) {
        long acceptNumber = acceptRequest.getSequenceNumber();
        long minimumNumber = minimumProposalNumber.updateAndGet(n -> acceptNumber >= n ? acceptNumber : n);

        if (minimumNumber == acceptNumber) {
            acceptedProposal.set(new AcceptedProposal(acceptNumber, acceptRequest.getValue()));
        }

        return ResponseEntity.ok(new AcceptResponse(minimumNumber));
    }
}