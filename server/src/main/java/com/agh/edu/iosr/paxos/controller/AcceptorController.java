package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import com.agh.edu.iosr.paxos.service.AcceptorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AcceptorController {

    private final AcceptorService acceptorService;

    public AcceptorController(AcceptorService acceptorService) {
        this.acceptorService = acceptorService;
    }

    @PostMapping(value = "/prepare")
    public ResponseEntity<PrepareResponse> prepare(@RequestBody PrepareRequest prepareRequest) {
        return ResponseEntity.ok(acceptorService.prepare(prepareRequest));
    }

    @PostMapping(value = "/accept")
    public ResponseEntity<AcceptResponse> accept(@RequestBody AcceptRequest acceptRequest) {
        return ResponseEntity.ok(acceptorService.accept(acceptRequest));
    }
}
