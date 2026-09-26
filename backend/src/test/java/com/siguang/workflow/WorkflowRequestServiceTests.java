package com.siguang.workflow;

import com.siguang.workflow.request.AuditEventResponse;
import com.siguang.workflow.request.CreateWorkflowRequest;
import com.siguang.workflow.request.RequestNotFoundException;
import com.siguang.workflow.request.RequestStatus;
import com.siguang.workflow.request.TransitionWorkflowRequest;
import com.siguang.workflow.request.WorkflowRequestRepository;
import com.siguang.workflow.request.WorkflowRequestResponse;
import com.siguang.workflow.request.WorkflowRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(WorkflowRequestService.class)
class WorkflowRequestServiceTests {
    @Autowired
    private WorkflowRequestService service;

    @Autowired
    private WorkflowRequestRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void createsAndAdvancesWorkflowRequest() {
        var created = createRequest();

        assertThat(created.status()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(repository.count()).isEqualTo(1);

        var advanced = service.transition(
                created.id(),
                new TransitionWorkflowRequest("ADVANCE", "reviewer", "validated business fields"));

        assertThat(advanced.status()).isEqualTo(RequestStatus.IN_REVIEW);
        assertThat(advanced.auditEvents()).hasSize(2);
    }

    @Test
    void advancesThroughFullLifecycle() {
        var created = createRequest();

        assertThat(advance(created.id()).status()).isEqualTo(RequestStatus.IN_REVIEW);
        assertThat(advance(created.id()).status()).isEqualTo(RequestStatus.APPROVED);
        assertThat(advance(created.id()).status()).isEqualTo(RequestStatus.COMPLETED);
    }

    @Test
    void rejectsCompletedRequestAdvance() {
        var created = createRequest();
        advance(created.id());
        advance(created.id());
        advance(created.id());

        assertThatThrownBy(() -> advance(created.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("completed requests cannot be advanced");
        assertThat(service.findById(created.id()).auditEvents()).hasSize(4);
    }

    @Test
    void rejectsRejectedRequestAdvance() {
        var created = createRequest();
        reject(created.id());

        assertThatThrownBy(() -> advance(created.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rejected requests cannot be advanced");
    }

    @Test
    void rejectsRepeatedRejection() {
        var created = createRequest();
        assertThat(reject(created.id()).status()).isEqualTo(RequestStatus.REJECTED);

        assertThatThrownBy(() -> reject(created.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rejected requests cannot be rejected");
        assertThat(service.findById(created.id()).auditEvents()).hasSize(2);
    }

    @Test
    void rejectsCompletedRequestRejection() {
        var created = createRequest();
        advance(created.id());
        advance(created.id());
        advance(created.id());

        assertThatThrownBy(() -> reject(created.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("completed requests cannot be rejected");
    }

    @Test
    void rejectsUnknownAction() {
        var created = createRequest();

        assertThatThrownBy(() -> service.transition(
                created.id(), new TransitionWorkflowRequest("ESCALATE", "reviewer", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("action must be");
    }

    @Test
    void throwsNotFoundForMissingRequest() {
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessage("request not found: 999");
        assertThatThrownBy(() -> advance(999L))
                .isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    void returnsAuditEventsInChronologicalOrderAfterReload() {
        var created = createRequest();
        advance(created.id());
        advance(created.id());
        entityManager.flush();
        entityManager.clear();

        var reloaded = service.findById(created.id());

        assertThat(reloaded.auditEvents())
                .extracting(AuditEventResponse::message)
                .containsExactly(
                        "Request submitted",
                        "Status changed to IN_REVIEW: step",
                        "Status changed to APPROVED: step");
    }

    private WorkflowRequestResponse createRequest() {
        return service.create(new CreateWorkflowRequest(
                "Approve vendor onboarding",
                "PURCHASE",
                "procurement",
                "finance",
                "HIGH",
                "Vendor needs approval before payment setup."));
    }

    private WorkflowRequestResponse advance(Long id) {
        return service.transition(id, new TransitionWorkflowRequest("ADVANCE", "reviewer", "step"));
    }

    private WorkflowRequestResponse reject(Long id) {
        return service.transition(id, new TransitionWorkflowRequest("REJECT", "reviewer", "not valid"));
    }
}
