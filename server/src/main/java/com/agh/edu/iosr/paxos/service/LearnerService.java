package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.google.common.util.concurrent.AtomicLongMap;
import org.springframework.stereotype.Service;

@Service
public class LearnerService {

    private final Server server;
    private final AtomicLongMap<String> acceptedValueOccurrences;
    private final AcceptorService acceptorService;

    public LearnerService(Server server, AcceptorService acceptorService) {
        this.server = server;
        this.acceptedValueOccurrences = AtomicLongMap.create();
        this.acceptorService = acceptorService;
    }

//    for testing purposes
    public AtomicLongMap<String> getAcceptedValueOccurrences() {
        return acceptedValueOccurrences;
    }

    public void learn(AcceptedProposal acceptedProposal) {
        String value = acceptedProposal.getValue();
        long occurrencesCount = acceptedValueOccurrences.incrementAndGet(value);

        if (occurrencesCount == server.getHalfReplicasCount() + 1) {
            server.setValue(value);
            acceptorService.clearAcceptedProposalOnCommitWithHigherOrEqualNumber(acceptedProposal.getSequenceNumber());
        } else if (occurrencesCount == server.getReplicasCount()) {
            acceptedValueOccurrences.getAndAdd(value, -server.getReplicasCount());
        }
    }
}
