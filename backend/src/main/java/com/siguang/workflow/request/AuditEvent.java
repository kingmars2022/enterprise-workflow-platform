package com.siguang.workflow.request;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String actor;
    private String message;
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private WorkflowRequest request;

    protected AuditEvent() {
    }

    public AuditEvent(String action, String actor, String message, WorkflowRequest request) {
        this.action = action;
        this.actor = actor;
        this.message = message;
        this.request = request;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getActor() {
        return actor;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
