import type {
  CreateWorkflowPayload,
  DashboardMetrics,
  WorkflowRequest
} from "./types";

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    },
    ...init
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.error ?? `Request failed: ${response.status}`);
  }

  return response.json();
}

export function listRequests(): Promise<WorkflowRequest[]> {
  return request<WorkflowRequest[]>("/api/requests");
}

export function getMetrics(): Promise<DashboardMetrics> {
  return request<DashboardMetrics>("/api/dashboard/metrics");
}

export function createRequest(payload: CreateWorkflowPayload): Promise<WorkflowRequest> {
  return request<WorkflowRequest>("/api/requests", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function advanceRequest(id: number): Promise<WorkflowRequest> {
  return request<WorkflowRequest>(`/api/requests/${id}/transition`, {
    method: "POST",
    body: JSON.stringify({
      action: "ADVANCE",
      actor: "workflow-manager",
      comment: "advanced from dashboard"
    })
  });
}

export function rejectRequest(id: number): Promise<WorkflowRequest> {
  return request<WorkflowRequest>(`/api/requests/${id}/transition`, {
    method: "POST",
    body: JSON.stringify({
      action: "REJECT",
      actor: "workflow-manager",
      comment: "rejected from dashboard"
    })
  });
}
