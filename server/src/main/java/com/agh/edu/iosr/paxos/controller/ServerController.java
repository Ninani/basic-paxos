package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.service.ProposerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.agh.edu.iosr.paxos.controller.ControllerUtils.describeRequest;
import static com.agh.edu.iosr.paxos.controller.ControllerUtils.describeResponse;

@RestController
public class ServerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);

    private final Server server;
    private final ProposerService proposerService;

    public ServerController(Server server, ProposerService proposerService) {
        this.server = server;
        this.proposerService = proposerService;
    }

    @GetMapping(value = "/read")
    public ResponseEntity<String> read(HttpServletRequest request) {
        LOGGER.debug(describeRequest(request));

        ResponseEntity<String> response = ResponseEntity.ok(server.getValue().get());

        LOGGER.debug(describeResponse(request, response));

        return response;
    }

    @PostMapping(value = "/write/{value}")
    public ResponseEntity<String> write(@PathVariable String value, HttpServletRequest request) {
        LOGGER.debug(describeRequest(request));

        proposerService.propose(value);

        ResponseEntity<String> response = ResponseEntity.ok("OK");

        LOGGER.debug(describeResponse(request, response));

        return response;
    }
}
