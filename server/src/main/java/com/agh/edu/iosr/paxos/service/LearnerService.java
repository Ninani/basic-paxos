package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.google.common.util.concurrent.AtomicLongMap;
import org.springframework.stereotype.Service;

@Service
public class LearnerService {

    private final Server server;
    private final AtomicLongMap<String> occurrences;
    private final AcceptorService acceptorService;

    public LearnerService(Server server, AcceptorService acceptorService) {
        this.server = server;
        this.occurrences = AtomicLongMap.create();
        this.acceptorService = acceptorService;
    }

    public void learn(AcceptedProposal acceptedProposal) {
        String value = acceptedProposal.getValue();
        long halfReplicasCountPlusOne = server.getHalfReplicasCount() + 1;

        if (occurrences.incrementAndGet(value) == halfReplicasCountPlusOne) {
            occurrences.getAndAdd(value, -halfReplicasCountPlusOne);

            server.setValue(value);
            acceptorService.clearAcceptedProposalOnCommitWithHigherOrEqualNumber(acceptedProposal.getSequenceNumber());
        }
    }
}
