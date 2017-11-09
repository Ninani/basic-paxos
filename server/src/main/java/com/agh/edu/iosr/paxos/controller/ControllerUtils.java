package com.agh.edu.iosr.paxos.controller;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

class ControllerUtils {
    static String describeRequest(HttpServletRequest request) {
        return "RQ " + request.getHeader("port") + " " + request.getServletPath();
    }

    static String describeRequest(HttpServletRequest request, Object requestBody) {
        return describeRequest(request) + " [" + requestBody + "]";
    }

    static String describeResponse(HttpServletRequest request, Object requestBody, ResponseEntity<?> response) {
        return "RS to " + describeRequest(request, requestBody) + ": " + response;
    }
}
