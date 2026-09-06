package com.siguang.workflow.request;

import java.time.Instant;
import java.util.List;

public record WorkflowRequestResponse(
        Long id,
        String title,
        RequestType type,
        String sourceSystem,
        String targetSystem,
        RequestPriority priority,
        RequestStatus status,
        String description,
        Instant createdAt,
        Instant updatedAt,
        List<AuditEventResponse> auditEvents) {
    public static WorkflowRequestResponse from(WorkflowRequest request) {
        return new WorkflowRequestResponse(
                request.getId(),
                request.getTitle(),
                request.getType(),
                request.getSourceSystem(),
                request.getTargetSystem(),
                request.getPriority(),
                request.getStatus(),
                request.getDescription(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getAuditEvents().stream().map(AuditEventResponse::from).toList());
    }
}
