package com.siguang.workflow.common;

import com.siguang.workflow.request.CreateWorkflowRequest;
import com.siguang.workflow.request.WorkflowRequestRepository;
import com.siguang.workflow.request.WorkflowRequestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final WorkflowRequestRepository repository;
    private final WorkflowRequestService service;

    public DataSeeder(WorkflowRequestRepository repository, WorkflowRequestService service) {
        this.repository = repository;
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        service.create(new CreateWorkflowRequest(
                "Route customer profile update to ERP",
                "CUSTOMER_UPDATE",
                "crm",
                "erp",
                "HIGH",
                "Customer address and billing profile must sync before invoice generation."));
        service.create(new CreateWorkflowRequest(
                "Approve cloud database access request",
                "ACCESS_REQUEST",
                "service-desk",
                "identity-platform",
                "MEDIUM",
                "Developer needs temporary read-only access for production support."));
        service.create(new CreateWorkflowRequest(
                "Process vendor invoice exception",
                "INVOICE",
                "billing",
                "finance",
                "LOW",
                "Invoice amount requires manual workflow review before posting."));
    }
}
