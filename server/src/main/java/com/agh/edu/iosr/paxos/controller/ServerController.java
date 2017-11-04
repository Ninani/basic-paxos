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

import java.util.concurrent.atomic.AtomicReference;

@RestController
public class ServerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);

    @Value("#{'${server.port}'}")
    private String myPort;
    private final AtomicReference<String> value;
    private Proposer proposer;

    public ServerController(Proposer proposer) {;
        this.value = new AtomicReference<>("dummyValue");
        this.proposer = proposer;
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public ResponseEntity<String> read() {
        return ResponseEntity.ok(value.get());
    }

    @RequestMapping(value = "/save/{value}", method = RequestMethod.GET)
    public ResponseEntity<String> saveValue(@PathVariable("value") String value) {
        LOGGER.debug("Server with port: {} received value: {} to save", myPort, value);
//        send propose request to acceptors
        proposer.processSaveRequest(value);
        return ResponseEntity.ok("Server with port: "+ myPort + " received value: " + value + " to save");
    }

}
