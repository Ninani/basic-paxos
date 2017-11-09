package com.agh.edu.iosr.paxos;

import com.google.common.collect.ImmutableList;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RestController
public class ClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);
    private static final String NO_RESPONSE_FROM_REPLICAS = "All the replicas did not respond.";

    private final ImmutableList<String> replicasAddresses;
    private final int halfReplicasCount;
    private final AsyncRestTemplate asyncCaller;
    private final RestTemplate syncCaller;

    public ClientController(@Value("#{'${replicas}'.split(',')}") List<String> replicasAddresses,
                            AsyncRestTemplate asyncCaller,
                            RestTemplate syncCaller) {
        this.replicasAddresses = ImmutableList.copyOf(new LinkedHashSet<>(replicasAddresses));
        this.halfReplicasCount = this.replicasAddresses.size() / 2;
        this.asyncCaller = asyncCaller;
        this.syncCaller = syncCaller;
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public ResponseEntity<String> read() {
        Iterator<Future<ResponseEntity<String>>> callsCyclingIterator = doAsyncReadCallsToReplicas();
        Map<String, Integer> responseCounts = new HashMap<>();

        while (responseCounts.size() <= halfReplicasCount && callsCyclingIterator.hasNext()) {
            Future<ResponseEntity<String>> call = callsCyclingIterator.next();

            if (call.isCancelled()) {
                callsCyclingIterator.remove();
                LOGGER.info("One of the read calls timed out.");
            } else if (call.isDone()) {
                try {
                    ResponseEntity<String> response = call.get();

                    if (response.getStatusCode() == HttpStatus.OK) {
                        responseCounts.compute(response.getBody(), (rs, count) -> count == null ? 1 : count + 1);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("{}", e.getMessage(), e);
                }
                callsCyclingIterator.remove();
            }
        }

        return getHighestResponseCount(responseCounts)
                .map(responseCount -> responseCount.getValue() > halfReplicasCount ?
                        ResponseEntity.ok(responseCount.getKey()) :
                        errorResponse("No majority in the replicas' responses."))
                .orElse(errorResponse(NO_RESPONSE_FROM_REPLICAS));
    }

    @RequestMapping(value = "/write/{value}", method = RequestMethod.POST)
    public ResponseEntity<String> write(@PathVariable String value) {
        for (String address : replicasAddresses) {
            try {
                syncCaller.exchange(address + "/write/" + value, HttpMethod.POST, null, String.class);
                return ResponseEntity.ok("Successfully requested a write of value: " + value + ".");
            } catch (RestClientException e) {
                LOGGER.info("Write call to " + address + " failed.");
            }
        }
        return errorResponse(NO_RESPONSE_FROM_REPLICAS);
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
