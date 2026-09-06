package com.siguang.workflow;

import com.siguang.workflow.request.CreateWorkflowRequest;
import com.siguang.workflow.request.RequestStatus;
import com.siguang.workflow.request.TransitionWorkflowRequest;
import com.siguang.workflow.request.WorkflowRequestRepository;
import com.siguang.workflow.request.WorkflowRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(WorkflowRequestService.class)
class WorkflowRequestServiceTests {
    @Autowired
    private WorkflowRequestService service;

    @Autowired
    private WorkflowRequestRepository repository;

    @Test
    void createsAndAdvancesWorkflowRequest() {
        var created = service.create(new CreateWorkflowRequest(
                "Approve vendor onboarding",
                "PURCHASE",
                "procurement",
                "finance",
                "HIGH",
                "Vendor needs approval before payment setup."));

        assertThat(created.status()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(repository.count()).isEqualTo(1);

        var advanced = service.transition(
                created.id(),
                new TransitionWorkflowRequest("ADVANCE", "reviewer", "validated business fields"));

        assertThat(advanced.status()).isEqualTo(RequestStatus.IN_REVIEW);
        assertThat(advanced.auditEvents()).hasSize(2);
    }
}
