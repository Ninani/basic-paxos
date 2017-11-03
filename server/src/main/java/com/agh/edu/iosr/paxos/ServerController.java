package com.agh.edu.iosr.paxos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicReference;

@RestController
public class ServerController {
    private final AtomicReference<String> value;

    public ServerController() {
        this.value = new AtomicReference<>("dummyValue");
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public ResponseEntity<String> read() {
        return ResponseEntity.ok(value.get());
    }
}
