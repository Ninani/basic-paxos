package com.agh.edu.iosr.paxos.controller;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

class ControllerUtils {
    static String describeRequest(HttpServletRequest request) {
        return "RQ " + request.getServletPath() + " from " + request.getHeader("port");
    }

    static String describeRequest(HttpServletRequest request, Object requestBody) {
        return describeRequest(request) + " [" + requestBody + "]";
    }

    static String describeResponse(HttpServletRequest request, ResponseEntity<?> response) {
        return "RS to [" + describeRequest(request) + "]: " + response;
    }

    static String describeResponse(HttpServletRequest request, Object requestBody, ResponseEntity<?> response) {
        return "RS to [" + describeRequest(request, requestBody) + "]: " + response;
    }
}
