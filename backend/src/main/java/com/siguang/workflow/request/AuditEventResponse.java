package com.siguang.workflow.request;

import java.time.Instant;

public record AuditEventResponse(
        Long id,
        String action,
        String actor,
        String message,
        Instant createdAt) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getAction(),
                event.getActor(),
                event.getMessage(),
                event.getCreatedAt());
    }
}
