package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.service.Proposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/proposer")
public class ProposerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);

    @Value("#{'${server.port}'}")
    private String port;
    private Proposer proposer;

    public ProposerController(Proposer proposer) {
        this.proposer = proposer;
    }

    @RequestMapping(value = "/write/{value}", method = RequestMethod.GET)
    public ResponseEntity<String> saveValue(@PathVariable("value") String value) {
        LOGGER.debug("Server with port: {} received value: {} to save", port, value);
        proposer.processSaveRequest(value);
        return ResponseEntity.ok("Server with port: "+ port + " received value: " + value + " to save");
    }
}
