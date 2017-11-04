package com.agh.edu.iosr.paxos;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RestController
public class ClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    private final List<String> replicasAddresses;
    private final int halfReplicasCount;
    private final AsyncRestTemplate asyncCaller;

    public ClientController(@Value("#{'${replicas}'.split(',')}") List<String> replicasAddresses, AsyncRestTemplate asyncCaller) {
        this.replicasAddresses = replicasAddresses;
        this.halfReplicasCount = replicasAddresses.size() / 2;
        this.asyncCaller = asyncCaller;
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public ResponseEntity<String> read() {
        Iterator<Future<ResponseEntity<String>>> callsCyclingIterator = doAsyncReadCallsToReplicas();
        Map<String, Integer> responseCounts = new HashMap<>();

        while (responseCounts.size() <= halfReplicasCount && callsCyclingIterator.hasNext()) {
            Future<ResponseEntity<String>> call = callsCyclingIterator.next();

            if (call.isCancelled()) { // timeout
                callsCyclingIterator.remove();
            } else if (call.isDone()) {
                try {
                    ResponseEntity<String> response = call.get();
                    if (response.getStatusCode() == HttpStatus.OK) {
                        responseCounts.compute(response.getBody(), (rs, count) -> count == null ? 1 : count + 1);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Exception: {}", e.getMessage(), e);
                }
                callsCyclingIterator.remove();
            }
        }

        return getHighestResponseCount(responseCounts)
                .map(responseCount -> responseCount.getValue() > halfReplicasCount ?
                        ResponseEntity.ok(responseCount.getKey()) :
                        errorResponse("No majority in the nodes' responses."))
                .orElse(errorResponse("All the nodes did not respond."));
    }

    @RequestMapping(value = "/write", method = RequestMethod.POST)
    public void write(@PathVariable String value) {

    }

    private Iterator<Future<ResponseEntity<String>>> doAsyncReadCallsToReplicas() {
        List<Future<ResponseEntity<String>>> calls = replicasAddresses.stream()
                .map(address -> asyncCaller.exchange(address + "/read", HttpMethod.GET, null, String.class))
                .collect(Collectors.toList());

        return Iterables.cycle(calls).iterator();
    }

    private Optional<Map.Entry<String, Integer>> getHighestResponseCount(Map<String, Integer> responseCounts) {
        return responseCounts.entrySet().stream().sorted((c1, c2) -> c2.getValue().compareTo(c1.getValue())).findFirst();
    }

    private static ResponseEntity<String> errorResponse(String response) {
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
