package com.siguang.workflow.request;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_requests")
public class WorkflowRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private RequestType type;

    private String sourceSystem;
    private String targetSystem;

    @Enumerated(EnumType.STRING)
    private RequestPriority priority;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<AuditEvent> auditEvents = new ArrayList<>();

    protected WorkflowRequest() {
    }

    public WorkflowRequest(
            String title,
            RequestType type,
            String sourceSystem,
            String targetSystem,
            RequestPriority priority,
            String description) {
        this.title = title;
        this.type = type;
        this.sourceSystem = sourceSystem;
        this.targetSystem = targetSystem;
        this.priority = priority;
        this.description = description;
        this.status = RequestStatus.SUBMITTED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        addAudit("CREATED", "system", "Request submitted");
    }

    public void transitionTo(RequestStatus nextStatus, String actor, String comment) {
        this.status = nextStatus;
        this.updatedAt = Instant.now();
        addAudit("STATUS_CHANGED", actor, "Status changed to " + nextStatus + ": " + comment);
    }

    public void addAudit(String action, String actor, String message) {
        auditEvents.add(new AuditEvent(action, actor, message, this));
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public RequestType getType() {
        return type;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<AuditEvent> getAuditEvents() {
        return auditEvents;
    }
}
