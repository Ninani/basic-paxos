package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import com.agh.edu.iosr.paxos.service.AcceptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
public class AcceptorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptorController.class);

    private final AcceptorService acceptorService;

    public AcceptorController(AcceptorService acceptorService) {
        this.acceptorService = acceptorService;
    }

    @PostMapping(value = "/prepare")
    public ResponseEntity<PrepareResponse> prepare(@RequestBody PrepareRequest prepareRequest, HttpServletRequest request) {
        LOGGER.info(ControllerUtils.describeRequest(request, prepareRequest));

        ResponseEntity<PrepareResponse> response = ResponseEntity.ok(acceptorService.prepare(prepareRequest));

        LOGGER.info(ControllerUtils.describeResponse(request, prepareRequest, response));

        return response;
    }

    @PostMapping(value = "/accept")
    public ResponseEntity<AcceptResponse> accept(@RequestBody AcceptRequest acceptRequest, HttpServletRequest request) {
        LOGGER.info(ControllerUtils.describeRequest(request, acceptRequest));

        ResponseEntity<AcceptResponse> response = ResponseEntity.ok(acceptorService.accept(acceptRequest));

        LOGGER.info(ControllerUtils.describeResponse(request, acceptRequest, response));

        return response;
    }
}
