package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.messages.PrepareResponse;
import com.agh.edu.iosr.paxos.service.Acceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/acceptor")
public class AcceptorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptorController.class);

    @Value("#{'${server.port}'}")
    private String myPort;
    private Acceptor acceptor;

    public AcceptorController(Acceptor acceptor) {
        this.acceptor = acceptor;
    }

    @GetMapping(value = "/prepare/{value}/{sequence}")
    public PrepareResponse getPrepareRequest(
            @PathVariable("value") String value, @PathVariable("sequence") long sequenceNumber) {
        LOGGER.debug("Received prepare request with value {} and number {}", value, sequenceNumber);
        PrepareResponse prepareResponse = acceptor.processPrepareRequest(value, sequenceNumber);
        LOGGER.debug("Status: {} (value: {}, seqNumber: {})",
                prepareResponse.getStatus(), prepareResponse.getValue(), prepareResponse.getSequenceNumber());
        return prepareResponse;
    }

}
