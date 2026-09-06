import { useEffect, useMemo, useState } from "react";
import {
  ArrowRight,
  CheckCircle2,
  ClipboardList,
  Clock3,
  FilePlus2,
  GitPullRequestArrow,
  RefreshCw,
  Search,
  ShieldCheck,
  XCircle
} from "lucide-react";
import {
  advanceRequest,
  createRequest,
  getMetrics,
  listRequests,
  rejectRequest
} from "./api";
import type {
  CreateWorkflowPayload,
  DashboardMetrics,
  RequestPriority,
  RequestStatus,
  RequestType,
  WorkflowRequest
} from "./types";

const boardStatuses: RequestStatus[] = [
  "SUBMITTED",
  "IN_REVIEW",
  "APPROVED",
  "COMPLETED",
  "REJECTED"
];

const statusLabels: Record<RequestStatus, string> = {
  DRAFT: "Draft",
  SUBMITTED: "Submitted",
  IN_REVIEW: "In Review",
  APPROVED: "Approved",
  REJECTED: "Rejected",
  COMPLETED: "Completed"
};

const requestTypes: RequestType[] = [
  "PURCHASE",
  "ACCESS_REQUEST",
  "CUSTOMER_UPDATE",
  "INVOICE",
  "IT_SERVICE"
];

const priorities: RequestPriority[] = ["LOW", "MEDIUM", "HIGH"];

const initialPayload: CreateWorkflowPayload = {
  title: "Route customer profile update to ERP",
  type: "CUSTOMER_UPDATE",
  sourceSystem: "crm",
  targetSystem: "erp",
  priority: "HIGH",
  description: "Customer profile changes must sync before invoice generation."
};

export function App() {
  const [requests, setRequests] = useState<WorkflowRequest[]>([]);
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [payload, setPayload] = useState<CreateWorkflowPayload>(initialPayload);
  const [selected, setSelected] = useState<WorkflowRequest | null>(null);
  const [query, setQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<RequestStatus | "ALL">("ALL");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function load() {
    setError("");
    const [nextMetrics, nextRequests] = await Promise.all([getMetrics(), listRequests()]);
    setMetrics(nextMetrics);
    setRequests(nextRequests);
    setSelected((current) => {
      if (!current) {
        return nextRequests[0] ?? null;
      }
      return nextRequests.find((request) => request.id === current.id) ?? nextRequests[0] ?? null;
    });
  }

  useEffect(() => {
    load().catch((err) => setError(err.message));
  }, []);

  const filteredRequests = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    return requests.filter((request) => {
      const matchesStatus = statusFilter === "ALL" || request.status === statusFilter;
      const searchable = [
        request.title,
        request.type,
        request.priority,
        request.sourceSystem,
        request.targetSystem,
        request.description
      ]
        .join(" ")
        .toLowerCase();
      return matchesStatus && (!normalized || searchable.includes(normalized));
    });
  }, [query, requests, statusFilter]);

  const groupedRequests = useMemo(() => {
    return boardStatuses.reduce<Record<RequestStatus, WorkflowRequest[]>>(
      (groups, status) => {
        groups[status] = filteredRequests.filter((request) => request.status === status);
        return groups;
      },
      {
        DRAFT: [],
        SUBMITTED: [],
        IN_REVIEW: [],
        APPROVED: [],
        REJECTED: [],
        COMPLETED: []
      }
    );
  }, [filteredRequests]);

  const completionRate = useMemo(() => {
    if (!metrics?.totalRequests) {
      return 0;
    }
    return Math.round(((metrics.completed ?? 0) / metrics.totalRequests) * 100);
  }, [metrics]);

  async function submitRequest(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    try {
      const created = await createRequest(payload);
      setSelected(created);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    } finally {
      setLoading(false);
    }
  }

  async function transition(id: number, mode: "advance" | "reject") {
    setLoading(true);
    try {
      const updated = mode === "advance" ? await advanceRequest(id) : await rejectRequest(id);
      setSelected(updated);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Transition failed");
    } finally {
      setLoading(false);
    }
  }

  const selectedClosed = selected ? ["COMPLETED", "REJECTED"].includes(selected.status) : true;

  return (
    <main className="workflow-console">
      <aside className="workspace-rail" aria-label="Workflow workspace navigation">
        <div className="brand-mark">
          <GitPullRequestArrow size={24} />
        </div>
        <nav>
          <button className="rail-button active" type="button" title="Workflow board">
            <ClipboardList size={20} />
          </button>
          <button className="rail-button" type="button" title="Approvals">
            <ShieldCheck size={20} />
          </button>
          <button className="rail-button" type="button" title="Activity">
            <Clock3 size={20} />
          </button>
        </nav>
      </aside>

      <section className="command-panel">
        <div className="panel-title">
          <p>Operations Workspace</p>
          <h1>Workflow Intake</h1>
        </div>

        <form className="intake-form" onSubmit={submitRequest}>
          <label>
            Request title
            <input
              value={payload.title}
              onChange={(event) => setPayload({ ...payload, title: event.target.value })}
              required
            />
          </label>

          <div className="field-pair">
            <label>
              Type
              <select
                value={payload.type}
                onChange={(event) => setPayload({ ...payload, type: event.target.value as RequestType })}
              >
                {requestTypes.map((type) => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </select>
            </label>
            <label>
              Priority
              <select
                value={payload.priority}
                onChange={(event) => setPayload({ ...payload, priority: event.target.value as RequestPriority })}
              >
                {priorities.map((priority) => (
                  <option key={priority} value={priority}>{priority}</option>
                ))}
              </select>
            </label>
          </div>

          <div className="field-pair">
            <label>
              Source
              <input
                value={payload.sourceSystem}
                onChange={(event) => setPayload({ ...payload, sourceSystem: event.target.value })}
                required
              />
            </label>
            <label>
              Target
              <input
                value={payload.targetSystem}
                onChange={(event) => setPayload({ ...payload, targetSystem: event.target.value })}
                required
              />
            </label>
          </div>

          <label>
            Business context
            <textarea
              value={payload.description}
              onChange={(event) => setPayload({ ...payload, description: event.target.value })}
              required
            />
          </label>

          <button className="submit-button" disabled={loading} type="submit">
            <FilePlus2 size={18} />
            Create Workflow
          </button>
        </form>

        <div className="ops-summary">
          <SummaryItem label="Total" value={metrics?.totalRequests ?? 0} />
          <SummaryItem label="Pending" value={metrics?.pendingReview ?? 0} />
          <SummaryItem label="Rejected" value={metrics?.rejected ?? 0} />
          <SummaryItem label="Complete" value={`${completionRate}%`} />
        </div>
      </section>

      <section className="board-shell">
        <header className="board-header">
          <div>
            <p className="eyebrow">Java / Spring Boot / PostgreSQL / CI/CD</p>
            <h2>Enterprise Workflow Command Center</h2>
          </div>
          <button className="refresh-button" onClick={() => load()} type="button">
            <RefreshCw size={17} />
            Refresh
          </button>
        </header>

        {error && <div className="error-banner">{error}</div>}

        <div className="toolbar">
          <label className="search-box">
            <Search size={17} />
            <input
              placeholder="Search by title, system, priority, or request type"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </label>
          <select
            aria-label="Filter by workflow status"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as RequestStatus | "ALL")}
          >
            <option value="ALL">All statuses</option>
            {boardStatuses.map((status) => (
              <option key={status} value={status}>{statusLabels[status]}</option>
            ))}
          </select>
        </div>

        <div className="board-grid" aria-label="Workflow status board">
          {boardStatuses.map((status) => (
            <WorkflowLane
              key={status}
              requests={groupedRequests[status]}
              selectedId={selected?.id}
              status={status}
              onSelect={setSelected}
            />
          ))}
        </div>
      </section>

      <aside className="inspector-panel">
        {selected ? (
          <>
            <div className="inspector-header">
              <span className={`status-pill ${selected.status.toLowerCase()}`}>
                {statusLabels[selected.status]}
              </span>
              <h2>{selected.title}</h2>
              <p>{selected.description}</p>
            </div>

            <div className="route-card">
              <span>{selected.sourceSystem}</span>
              <ArrowRight size={18} />
              <span>{selected.targetSystem}</span>
            </div>

            <dl className="metadata-grid">
              <div>
                <dt>Type</dt>
                <dd>{selected.type}</dd>
              </div>
              <div>
                <dt>Priority</dt>
                <dd>{selected.priority}</dd>
              </div>
              <div>
                <dt>Created</dt>
                <dd>{formatDate(selected.createdAt)}</dd>
              </div>
              <div>
                <dt>Updated</dt>
                <dd>{formatDate(selected.updatedAt)}</dd>
              </div>
            </dl>

            <div className="inspector-actions">
              <button
                className="advance-button"
                disabled={loading || selectedClosed}
                onClick={() => transition(selected.id, "advance")}
                type="button"
              >
                <CheckCircle2 size={18} />
                Advance
              </button>
              <button
                className="reject-button"
                disabled={loading || selectedClosed}
                onClick={() => transition(selected.id, "reject")}
                type="button"
              >
                <XCircle size={18} />
                Reject
              </button>
            </div>

            <section className="audit-panel">
              <h3>Audit Trail</h3>
              <div className="timeline">
                {selected.auditEvents.map((event) => (
                  <article className="timeline-item" key={event.id}>
                    <div className="timeline-dot" />
                    <div>
                      <strong>{event.action}</strong>
                      <p>{event.message}</p>
                      <span>{event.actor} · {formatDate(event.createdAt)}</span>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          </>
        ) : (
          <div className="empty-inspector">
            <ClipboardList size={34} />
            <h2>No request selected</h2>
            <p>Create or select a workflow request to inspect lifecycle details.</p>
          </div>
        )}
      </aside>
    </main>
  );
}

function WorkflowLane({
  requests,
  selectedId,
  status,
  onSelect
}: {
  requests: WorkflowRequest[];
  selectedId?: number;
  status: RequestStatus;
  onSelect: (request: WorkflowRequest) => void;
}) {
  return (
    <section className={`workflow-lane lane-${status.toLowerCase()}`}>
      <header>
        <span>{statusLabels[status]}</span>
        <strong>{requests.length}</strong>
      </header>
      <div className="lane-stack">
        {requests.map((request) => (
          <button
            className={`work-card ${selectedId === request.id ? "selected" : ""}`}
            key={request.id}
            onClick={() => onSelect(request)}
            type="button"
          >
            <div>
              <span className={`priority-dot ${request.priority.toLowerCase()}`} />
              <strong>{request.title}</strong>
            </div>
            <p>{request.sourceSystem} to {request.targetSystem}</p>
            <footer>
              <span>{request.type}</span>
              <time>{formatDate(request.updatedAt)}</time>
            </footer>
          </button>
        ))}
        {requests.length === 0 && <div className="lane-empty">No requests</div>}
      </div>
    </section>
  );
}

function SummaryItem({ label, value }: { label: string; value: number | string }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function formatDate(value: string) {
  return new Date(value).toLocaleString([], {
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}
