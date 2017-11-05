package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.messages.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.PromiseResponse;
import com.agh.edu.iosr.paxos.service.Acceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/acceptor")
public class AcceptorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptorController.class);

    @Value("#{'${server.port}'}")
    private String port;
    private Acceptor acceptor;

    public AcceptorController(Acceptor acceptor) {
        this.acceptor = acceptor;
    }

    @GetMapping(value = "/prepare/{sequence}")
    public PromiseResponse getPrepareRequest(@PathVariable("sequence") long sequenceNumber) {
        LOGGER.debug("Received prepare request with sequence number {}", sequenceNumber);
        PromiseResponse promiseResponse = acceptor.processPrepareRequest(sequenceNumber);
        LOGGER.debug("Status: {} (seqNumber: {})", promiseResponse.getStatus(), promiseResponse.getSequenceNumber());
        return promiseResponse;
    }

    @GetMapping (value = "/accept/{sequence}/{value}")
    public AcceptResponse postAcceptRequest(@PathVariable("sequence") long sequenceNumber, @PathVariable("value") String value) {
        LOGGER.debug("Received AcceptRequest with sequence number: {}, and value: {}", sequenceNumber, value);
        AcceptResponse acceptResponse = acceptor.processAcceptRequest(sequenceNumber, value);
        LOGGER.debug("Status: {} ", acceptResponse.getStatus());
        return acceptResponse;
    }

}
