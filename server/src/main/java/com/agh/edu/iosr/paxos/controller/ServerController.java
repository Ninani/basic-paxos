package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.service.ProposerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);

    private final Server server;
    private final ProposerService proposerService;

    public ServerController(Server server, ProposerService proposerService) {
        this.server = server;
        this.proposerService = proposerService;
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public ResponseEntity<String> read() {
        return ResponseEntity.ok(server.getValue().get());
    }

    @RequestMapping(value = "/write/{value}", method = RequestMethod.POST)
    public void write(@PathVariable String value) {
        proposerService.propose(value);
    }
}
