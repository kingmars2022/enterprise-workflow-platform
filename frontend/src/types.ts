export type RequestStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "IN_REVIEW"
  | "APPROVED"
  | "REJECTED"
  | "COMPLETED";

export type RequestPriority = "LOW" | "MEDIUM" | "HIGH";

export type RequestType =
  | "PURCHASE"
  | "ACCESS_REQUEST"
  | "CUSTOMER_UPDATE"
  | "INVOICE"
  | "IT_SERVICE";

export interface AuditEvent {
  id: number;
  action: string;
  actor: string;
  message: string;
  createdAt: string;
}

export interface WorkflowRequest {
  id: number;
  title: string;
  type: RequestType;
  sourceSystem: string;
  targetSystem: string;
  priority: RequestPriority;
  status: RequestStatus;
  description: string;
  createdAt: string;
  updatedAt: string;
  auditEvents: AuditEvent[];
}

export interface DashboardMetrics {
  totalRequests: number;
  pendingReview: number;
  approved: number;
  rejected: number;
  completed: number;
  statusSummary: Record<RequestStatus, number>;
}

export interface CreateWorkflowPayload {
  title: string;
  type: RequestType;
  sourceSystem: string;
  targetSystem: string;
  priority: RequestPriority;
  description: string;
}
