package com.siguang.workflow.dashboard;

import com.siguang.workflow.request.RequestStatus;
import com.siguang.workflow.request.WorkflowRequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final WorkflowRequestService service;

    public DashboardController(WorkflowRequestService service) {
        this.service = service;
    }

    @GetMapping("/metrics")
    public DashboardMetrics metrics() {
        var summary = service.statusSummary();
        return new DashboardMetrics(
                service.count(),
                summary.get(RequestStatus.SUBMITTED) + summary.get(RequestStatus.IN_REVIEW),
                summary.get(RequestStatus.APPROVED),
                summary.get(RequestStatus.REJECTED),
                summary.get(RequestStatus.COMPLETED),
                summary);
    }
}
