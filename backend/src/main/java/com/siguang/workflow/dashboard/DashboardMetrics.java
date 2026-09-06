package com.siguang.workflow.dashboard;

import com.siguang.workflow.request.RequestStatus;
import java.util.Map;

public record DashboardMetrics(
        long totalRequests,
        long pendingReview,
        long approved,
        long rejected,
        long completed,
        Map<RequestStatus, Long> statusSummary) {
}
