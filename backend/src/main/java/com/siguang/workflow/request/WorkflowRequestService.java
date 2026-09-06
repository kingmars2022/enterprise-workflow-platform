package com.siguang.workflow.request;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowRequestService {
    private final WorkflowRequestRepository repository;

    public WorkflowRequestService(WorkflowRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WorkflowRequestResponse create(CreateWorkflowRequest command) {
        WorkflowRequest request = new WorkflowRequest(
                command.title().trim(),
                parseEnum(RequestType.class, command.type(), "type"),
                command.sourceSystem().trim(),
                command.targetSystem().trim(),
                parseEnum(RequestPriority.class, command.priority(), "priority"),
                command.description().trim());
        return WorkflowRequestResponse.from(repository.save(request));
    }

    @Transactional(readOnly = true)
    public List<WorkflowRequestResponse> findAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(WorkflowRequest::getUpdatedAt).reversed())
                .map(WorkflowRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkflowRequestResponse findById(Long id) {
        return WorkflowRequestResponse.from(findEntity(id));
    }

    @Transactional
    public WorkflowRequestResponse transition(Long id, TransitionWorkflowRequest command) {
        WorkflowRequest request = findEntity(id);
        RequestStatus nextStatus = nextStatus(request.getStatus(), command.action());
        request.transitionTo(
                nextStatus,
                command.actor().trim(),
                command.comment() == null || command.comment().isBlank()
                        ? "workflow action submitted"
                        : command.comment().trim());
        return WorkflowRequestResponse.from(repository.save(request));
    }

    @Transactional(readOnly = true)
    public Map<RequestStatus, Long> statusSummary() {
        Map<RequestStatus, Long> summary = new EnumMap<>(RequestStatus.class);
        for (RequestStatus status : RequestStatus.values()) {
            summary.put(status, repository.countByStatus(status));
        }
        return summary;
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    private WorkflowRequest findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("request not found: " + id));
    }

    private RequestStatus nextStatus(RequestStatus current, String action) {
        String normalized = action.trim().toUpperCase();
        if ("REJECT".equals(normalized)) {
            if (current == RequestStatus.COMPLETED) {
                throw new IllegalStateException("completed requests cannot be rejected");
            }
            return RequestStatus.REJECTED;
        }
        if (!"ADVANCE".equals(normalized) && !"APPROVE".equals(normalized)) {
            throw new IllegalArgumentException("action must be ADVANCE, APPROVE, or REJECT");
        }
        return switch (current) {
            case DRAFT -> RequestStatus.SUBMITTED;
            case SUBMITTED -> RequestStatus.IN_REVIEW;
            case IN_REVIEW -> RequestStatus.APPROVED;
            case APPROVED -> RequestStatus.COMPLETED;
            case REJECTED, COMPLETED -> current;
        };
    }

    private <T extends Enum<T>> T parseEnum(Class<T> type, String value, String field) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid " + field + ": " + value);
        }
    }
}
