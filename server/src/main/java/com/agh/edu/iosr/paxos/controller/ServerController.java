package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.service.ProposerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {

    private final Server server;
    private final ProposerService proposerService;

    public ServerController(Server server, ProposerService proposerService) {
        this.server = server;
        this.proposerService = proposerService;
    }

    @GetMapping(value = "/read")
    public ResponseEntity<String> read() {
        return ResponseEntity.ok(server.getValue().get());
    }

    @PostMapping(value = "/write/{value}")
    public void write(@PathVariable String value) {
        proposerService.propose(value);
    }
}
