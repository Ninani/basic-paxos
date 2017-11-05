package com.agh.edu.iosr.paxos.messages;
/**
 * FROM: Acceptor
 * TO: Proposer
 * TO: Learner
* */
public class AcceptResponse {
    private Status status;

    public AcceptResponse() {
    }

    public AcceptResponse(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
