package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.google.common.util.concurrent.AtomicLongMap;
import org.springframework.stereotype.Service;

@Service
public class LearnerService {

    private final Server server;
    private final AtomicLongMap<AcceptedProposal> acceptedProposalOccurrences;
    private final AcceptorService acceptorService;

    public LearnerService(Server server, AcceptorService acceptorService) {
        this.server = server;
        this.acceptedProposalOccurrences = AtomicLongMap.create();
        this.acceptorService = acceptorService;
    }

    public void learn(AcceptedProposal acceptedProposal) {
        long occurrencesCount = acceptedProposalOccurrences.incrementAndGet(acceptedProposal);

        if (occurrencesCount == server.getHalfReplicasCount() + 1) {
            server.setValue(acceptedProposal.getValue());
            acceptorService.clearAcceptedProposalOnCommitWithHigherOrEqualNumber(acceptedProposal.getSequenceNumber());
        } else if (occurrencesCount == server.getReplicasCount()) {
            acceptedProposalOccurrences.getAndAdd(acceptedProposal, -server.getReplicasCount());
        }
    }
}
