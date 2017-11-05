package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.messages.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.Status;
import com.agh.edu.iosr.paxos.messages.PromiseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Acceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Acceptor.class);
    private Server server;

    public Acceptor(Server server) {
        this.server = server;
    }

    public PromiseResponse processPrepareRequest(long sequenceNumber) {
        PromiseResponse promiseResponse;

        if (server.compareAndUpdateSeqNumber(sequenceNumber)) {
            LOGGER.debug("Value ACCEPTED, seq number: {}", sequenceNumber);
            promiseResponse = new PromiseResponse(sequenceNumber, Status.ACCEPTED);
        } else {
            LOGGER.debug("Value DENIED, seq number: {}", sequenceNumber);
            promiseResponse = new PromiseResponse(server.getSeqNumber(), Status.DENIED);
        }

        return promiseResponse;
    }

    public AcceptResponse processAcceptRequest(long sequenceNumber, String value) {
//        check if sequence number is greater or equal to the saved one
//        create and return AcceptResponse for proposer
//        send AcceptResponse to Learners
        AcceptResponse acceptResponse = null;
        // TODO: 11/5/17 check thread safety for servers seqNumber
        if(server.getSeqNumber() <= sequenceNumber) acceptResponse = new AcceptResponse(Status.ACCEPTED);
        else acceptResponse = new AcceptResponse(Status.DENIED);
        return acceptResponse;
    }

}
