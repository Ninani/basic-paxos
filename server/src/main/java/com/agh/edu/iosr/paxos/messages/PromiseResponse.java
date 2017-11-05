package com.agh.edu.iosr.paxos.messages;
/**
 * Response for PrepareRequest
 * FROM: Acceptor
 * TO: Proposer
 * */

public class PromiseResponse {

    private long sequenceNumber;
    private Status status;

    public PromiseResponse() {
    }

    public PromiseResponse(long sequenceNumber, Status status) {
        this.sequenceNumber = sequenceNumber;
        this.status = status;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public Status getStatus() {
        return status;
    }
}
