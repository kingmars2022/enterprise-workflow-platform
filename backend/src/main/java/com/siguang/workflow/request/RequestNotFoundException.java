package com.siguang.workflow.request;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(Long id) {
        super("request not found: " + id);
    }
}
