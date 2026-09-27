# Enterprise Workflow Management Platform

A full-stack enterprise workflow application built with **React, TypeScript, Java, Spring Boot, PostgreSQL, Docker, GitHub Actions, and AWS deployment architecture**.

This project simulates a real internal business system where teams submit workflow requests, review them, approve or reject them, and track every status change through an audit trail.

It is designed as a portfolio project for full-stack Java, backend, enterprise application, and cloud-oriented software developer roles.

## What The Application Does

Enterprise teams often need approval workflows for internal operations. Examples include:

- IT access requests
- Purchase approvals
- Customer profile updates
- Invoice exception reviews
- Internal service requests

This application provides a complete request workflow from creation to completion:

```text
SUBMITTED -> IN_REVIEW -> APPROVED -> COMPLETED
                         |
                         v
                      REJECTED
```

Users can:

- Create a new workflow request from the web UI.
- View requests on a workflow board grouped by status.
- Search and filter requests by status, title, system, priority, or type.
- Select a request and inspect its details in a right-side inspector panel.
- Advance a request through the workflow.
- Reject a request with an audit comment.
- View operational metrics in the intake panel.
- Track audit events in a timeline generated during request creation and transitions.

## Live Demo Flow

After running the project locally:

1. Open `http://localhost:5173`.
2. Use the left intake panel to create a workflow request.
3. Review requests on the center workflow board grouped by status.
4. Search or filter requests from the toolbar.
5. Select a request card to open the right-side inspector.
6. Click `Advance` or `Reject` and watch the board, metrics, and audit timeline update.

## Architecture

```text
React + TypeScript frontend
        |
        | REST API
        v
Spring Boot backend
        |
        | Spring Data JPA
        v
PostgreSQL database
        |
        v
Workflow state machine + audit log
        |
        v
GitHub Actions CI/CD
        |
        v
AWS deployment architecture
ALB / ECS Fargate / ECR / RDS
```

## Tech Stack

| Area | Technology |
|---|---|
| Frontend | React, TypeScript, Vite, lucide-react |
| Backend | Java 17, Spring Boot, Spring Web, Spring Data JPA |
| Database | PostgreSQL |
| Validation | Jakarta Bean Validation |
| Testing | JUnit, Spring Boot Test, H2 test database |
| Local runtime | Docker Compose |
| CI/CD | GitHub Actions |
| Cloud architecture | AWS ALB, ECS Fargate, ECR, RDS, SSM Parameter Store |
| Infrastructure as Code | Terraform |

## Main Features

### Frontend

- Three-column workflow console built with React and TypeScript.
- Left-side request intake panel.
- Center Kanban-style workflow board grouped by request status.
- Search and status filter toolbar.
- Right-side request inspector with route, metadata, and actions.
- Advance and reject workflow actions.
- Audit trail timeline.
- Responsive layout for desktop and laptop screens.

### Backend

- Java 17 Spring Boot REST API.
- PostgreSQL persistence with Spring Data JPA.
- Workflow request entity with status, priority, type, route, and description.
- Audit event entity for request creation and workflow transitions.
- Workflow transition rules implemented in the service layer.
- Centralized API error handling.
- Request validation.
- Seed data for local demo.
- Backend test using Spring Boot Test and H2.

### DevOps And Cloud

- Docker Compose local environment.
- Separate frontend and backend Dockerfiles.
- GitHub Actions pipeline for backend tests and frontend build.
- Docker image build job for pull requests.
- AWS deployment workflow using GitHub OIDC, ECR, and rolling ECS deployments.
- Terraform infrastructure for ALB, ECS Fargate services, ECR, and private RDS PostgreSQL.

## Project Structure

```text
.
├── backend/
│   ├── src/main/java/com/siguang/workflow/
│   │   ├── api/
│   │   ├── common/
│   │   ├── dashboard/
│   │   └── request/
│   ├── src/main/resources/application.yml
│   ├── src/test/java/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── App.tsx
│   │   ├── api.ts
│   │   ├── main.tsx
│   │   ├── styles.css
│   │   └── types.ts
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.ts
├── infra/terraform/
├── .github/workflows/ci-cd.yml
├── docker-compose.yml
└── README.md
```

## Run Locally With Docker

### Requirements

- Docker Desktop
- Docker Compose

### Start The Application

```bash
cd /Users/siguangzhao/Documents/GitHub/my-projects/ai/Enterprise_Workflow_Management_Platform
docker compose up --build
```

### Open The Application

Frontend:

```text
http://localhost:5173
```

Backend API:

```text
http://localhost:8080
```

PostgreSQL host port:

```text
5433
```

### Stop The Application

Press `Control + C` in the terminal, then run:

```bash
docker compose down
```

To remove the local PostgreSQL data volume:

```bash
docker compose down -v
```

## Run Services Manually

Use this only if you want to run the frontend and backend outside Docker.

### Start PostgreSQL

```bash
cd /Users/siguangzhao/Documents/GitHub/my-projects/ai/Enterprise_Workflow_Management_Platform
docker compose up postgres
```

### Run Backend

Open a second terminal:

```bash
cd /Users/siguangzhao/Documents/GitHub/my-projects/ai/Enterprise_Workflow_Management_Platform/backend
mvn spring-boot:run
```

### Run Frontend

Open a third terminal:

```bash
cd /Users/siguangzhao/Documents/GitHub/my-projects/ai/Enterprise_Workflow_Management_Platform/frontend
npm install
npm run dev
```

Then open:

```text
http://localhost:5173
```

The backend connects to PostgreSQL on `localhost:5433` by default, matching the Docker Compose port mapping. Override it with `DATABASE_URL` if your database runs elsewhere.

The frontend calls the API on the same origin (`/api/...`). In development, Vite proxies `/api` to `http://localhost:8080`; set `API_PROXY_TARGET` to point it at a different backend.

If npm reports a cache permission error on macOS, use a project-local cache:

```bash
npm install --cache ../.npm-cache
npm run dev
```

## Frontend API Routing

The frontend never hardcodes the backend address. It sends requests to same-origin `/api/...`, which is forwarded to the backend:

| Environment | Forwarded by | Configure with |
|---|---|---|
| `npm run dev` / Docker Compose | Vite dev server proxy | `API_PROXY_TARGET` (default `http://localhost:8080`) |
| Frontend container image | nginx reverse proxy | `BACKEND_URL` at container start (default `http://backend:8080`) |

To call a backend on a different origin instead, build the frontend with `VITE_API_BASE_URL` (e.g. `docker build --build-arg VITE_API_BASE_URL=https://api.example.com ./frontend`) and add that frontend origin to the backend's `CORS_ALLOWED_ORIGIN`.

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/requests` | Create a workflow request |
| `GET` | `/api/requests` | List workflow requests |
| `GET` | `/api/requests/{id}` | Get one workflow request |
| `POST` | `/api/requests/{id}/transition` | Advance, approve, or reject a request |
| `GET` | `/api/dashboard/metrics` | Get dashboard metrics |

## Example API Calls

Create a request:

```bash
curl -X POST http://localhost:8080/api/requests \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Route customer profile update to ERP",
    "type": "CUSTOMER_UPDATE",
    "sourceSystem": "crm",
    "targetSystem": "erp",
    "priority": "HIGH",
    "description": "Customer profile changes must sync before invoice generation."
  }'
```

Advance a request:

```bash
curl -X POST http://localhost:8080/api/requests/1/transition \
  -H "Content-Type: application/json" \
  -d '{
    "action": "ADVANCE",
    "actor": "workflow-manager",
    "comment": "validated request fields"
  }'
```

Reject a request:

```bash
curl -X POST http://localhost:8080/api/requests/1/transition \
  -H "Content-Type: application/json" \
  -d '{
    "action": "REJECT",
    "actor": "workflow-manager",
    "comment": "missing approval evidence"
  }'
```

Get dashboard metrics:

```bash
curl http://localhost:8080/api/dashboard/metrics
```

## Verification

Backend tests:

```bash
cd /Users/siguangzhao/Documents/GitHub/my-projects/ai/Enterprise_Workflow_Management_Platform/backend
mvn test
```

Frontend dependency install, audit, and production build:

```bash
cd /Users/siguangzhao/Documents/GitHub/my-projects/ai/Enterprise_Workflow_Management_Platform/frontend
npm ci
npm audit --audit-level=moderate
npm run build
```

Docker Compose configuration check:

```bash
cd /Users/siguangzhao/Documents/GitHub/my-projects/ai/Enterprise_Workflow_Management_Platform
docker compose config
```

## CI/CD Pipeline

GitHub Actions workflow:

```text
.github/workflows/ci-cd.yml
```

Pipeline behavior:

1. On pull requests to `main`, run backend tests.
2. On pull requests to `main`, install and build the frontend.
3. On pull requests to `main`, build backend and frontend Docker images.
4. On push to `main`, authenticate to AWS using GitHub OIDC.
5. Build and push backend and frontend images to Amazon ECR, tagged with the commit SHA.
6. Register new ECS task definition revisions that use those images.
7. Deploy the backend, then the frontend, and wait for each ECS service to become stable.

The deploy job can also be started manually from the Actions tab (`workflow_dispatch`), which is how the first deployment after `terraform apply` is done.

Required GitHub secret:

```text
AWS_ROLE_TO_ASSUME
```

`AWS_ROLE_TO_ASSUME` is an IAM role that GitHub Actions assumes through OIDC. See `infra/terraform/README.md` for the permissions it needs.

## AWS Infrastructure

Terraform files are located in:

```text
infra/terraform
```

The Terraform starter creates:

- Backend ECR repository
- Frontend ECR repository
- VPC with public and private subnets
- Application Load Balancer (public entry point)
- ECS Fargate cluster with backend and frontend services, connected through ECS Service Connect
- RDS PostgreSQL instance in private subnets
- Database password in SSM Parameter Store, injected into the backend as a secret
- CloudWatch log groups and least-privilege security groups

Run Terraform:

```bash
cd /Users/siguangzhao/Documents/GitHub/my-projects/ai/Enterprise_Workflow_Management_Platform/infra/terraform
terraform init
terraform plan -var="db_password=CHANGE_ME"
terraform apply -var="db_password=CHANGE_ME"
```

Do not commit real secrets, `.env` files, or Terraform state files.

## Troubleshooting

### Docker daemon is not running

If you see:

```text
Cannot connect to the Docker daemon
```

Open Docker Desktop first, wait until Docker is running, then run:

```bash
docker compose up --build
```

### Port already in use

This project uses:

- Frontend: `5173`
- Backend: `8080`
- PostgreSQL host port: `5433`

If a port is already in use, stop the conflicting process or change the port mapping in `docker-compose.yml`.

### npm cache permission error

If npm reports permission issues under `/Users/<name>/.npm`, run frontend commands with a local cache:

```bash
npm install --cache ../.npm-cache
```

## Why This Project Is Useful For Job Applications

This project maps directly to common full-stack Java job requirements:

- React and TypeScript frontend development
- Java and Spring Boot backend development
- PostgreSQL relational database design
- REST API design
- Workflow/process-driven application logic
- Docker-based local development
- GitHub Actions CI/CD
- AWS deployment architecture
- Terraform infrastructure-as-code

It is stronger than a basic CRUD project because it includes workflow state transitions, audit logs, a Kanban-style operations console, dashboard metrics, Dockerized services, CI/CD, and cloud infrastructure planning.

## Resume Bullets

**Enterprise Workflow Management Platform** - Java, Spring Boot, React, TypeScript, PostgreSQL, Docker, GitHub Actions, AWS

- Built a full-stack enterprise workflow management system with React/TypeScript frontend and Spring Boot REST APIs.
- Implemented request lifecycle processing with workflow states, approval actions, rejection handling, audit logs, and PostgreSQL persistence.
- Added GitHub Actions CI/CD pipeline for backend testing, frontend production builds, Docker image builds, and AWS deployment preparation.
- Provisioned AWS infrastructure with Terraform: ALB, ECS Fargate services linked by Service Connect, ECR, and private RDS PostgreSQL with tiered security groups.

## Future Improvements

- Add Spring Security with JWT authentication.
- Add role-based access control for requester, reviewer, and administrator roles.
- Add database migrations with Flyway.
- Add React component tests.
- Add HTTPS with an ACM certificate and a custom domain.
- Add Camunda or Flowable for external BPM workflow orchestration.
