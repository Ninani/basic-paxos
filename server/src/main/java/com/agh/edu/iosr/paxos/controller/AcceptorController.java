package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import com.agh.edu.iosr.paxos.service.AcceptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.agh.edu.iosr.paxos.controller.ControllerUtils.describeRequest;
import static com.agh.edu.iosr.paxos.controller.ControllerUtils.describeResponse;


@RestController
public class AcceptorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptorController.class);

    private final Server server;
    private final AcceptorService acceptorService;

    public AcceptorController(Server server, AcceptorService acceptorService) {
        this.server = server;
        this.acceptorService = acceptorService;
    }

    @PostMapping(value = "/prepare")
    public ResponseEntity<PrepareResponse> prepare(@RequestBody PrepareRequest prepareRequest, HttpServletRequest request) {
        LOGGER.debug(describeRequest(request, prepareRequest));

        ResponseEntity<PrepareResponse> response = okResponse(acceptorService.prepare(prepareRequest));

        LOGGER.debug(describeResponse(request, prepareRequest, response));

        return response;
    }

    @PostMapping(value = "/accept")
    public ResponseEntity<AcceptResponse> accept(@RequestBody AcceptRequest acceptRequest, HttpServletRequest request) {
        LOGGER.debug(describeRequest(request, acceptRequest));

        ResponseEntity<AcceptResponse> response = okResponse(acceptorService.accept(acceptRequest));

        LOGGER.debug(describeResponse(request, acceptRequest, response));

        return response;
    }

    private <T> ResponseEntity<T> okResponse(T response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("port", server.getPort());

        return ResponseEntity.ok().headers(headers).body(response);
    }
}
