package com.agh.edu.iosr.paxos;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RestController
public class ClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    @Value("#{'${replicas}'.split(',')}")
    private List<String> replicaAddresses;
    @Autowired
    private AsyncRestTemplate asyncCaller;

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public ResponseEntity<String> read() {
        final List<Future<ResponseEntity<String>>> calls = replicaAddresses.stream()
                .map(address -> asyncCaller.exchange(address + "/read", HttpMethod.GET, null, String.class))
                .collect(Collectors.toList());
        final int halfCallsCount = calls.size() / 2;
        final Map<String, Integer> responseCounts = new HashMap<>();
        final Iterator<Future<ResponseEntity<String>>> cyclingIterator = Iterables.cycle(calls).iterator();

        while (responseCounts.size() <= halfCallsCount && cyclingIterator.hasNext()) {
            final Future<ResponseEntity<String>> call = cyclingIterator.next();

            if (call.isCancelled()) { // timeout
                cyclingIterator.remove();
            } else if (call.isDone()) {
                try {
                    final ResponseEntity<String> response = call.get();

                    if (response.getStatusCode() == HttpStatus.OK) {
                        responseCounts.compute(response.getBody(), (key, oldValue) -> oldValue == null ? 1 : oldValue + 1);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Exception: {}", e.getMessage(), e);
                }
                cyclingIterator.remove();
            }
        }

        return responseCounts.entrySet().stream()
                .sorted((c1, c2) -> c2.getValue().compareTo(c1.getValue()))
                .findFirst()
                .map(responseCount -> {
                    if (responseCount.getValue() > halfCallsCount) {
                        return ResponseEntity.ok(responseCount.getKey());
                    }
                    return errorResponse("No majority in the nodes' responses.");
                })
                .orElse(errorResponse("All the nodes did not respond."));
    }

    @RequestMapping(value = "/write", method = RequestMethod.POST)
    public void write(@RequestBody String value) {

    }

    private static ResponseEntity<String> errorResponse(String response) {
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
