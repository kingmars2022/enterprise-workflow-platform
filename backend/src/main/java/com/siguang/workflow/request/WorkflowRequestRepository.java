package com.siguang.workflow.request;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRequestRepository extends JpaRepository<WorkflowRequest, Long> {
    long countByStatus(RequestStatus status);
}
