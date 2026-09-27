# Terraform AWS Infrastructure

This folder deploys the Enterprise Workflow Management Platform to AWS.

It creates:

- ECR repositories for backend and frontend Docker images
- VPC with public subnets (internet gateway) and private subnets (no internet route)
- Application Load Balancer as the only public entry point
- ECS Fargate cluster with a backend service and a frontend service
- ECS Service Connect namespace so the frontend reaches the backend at `backend:8080`
- RDS PostgreSQL instance (encrypted, private subnets, not publicly accessible)
- SSM Parameter Store `SecureString` for the database password
- IAM task execution role and CloudWatch log groups
- Security groups for the load balancer, frontend, backend, and database tiers

## Architecture

```text
browser --80--> ALB --80--> frontend (nginx) --/api, 8080--> backend (Spring Boot) --5432--> RDS
                             ECS Fargate        Service Connect      ECS Fargate              private subnets
```

The frontend container serves the React build and proxies `/api` to `http://backend:8080`, which Service Connect resolves to the backend service. The browser only ever talks to the load balancer, so no CORS setup is needed.

## Network Security

| Security group | Inbound | Attached to |
|---|---|---|
| `alb` | TCP 80 from `0.0.0.0/0` | Load balancer |
| `frontend` | TCP 80 from `alb` SG only | Frontend ECS service |
| `backend` | TCP 8080 from `frontend` SG only | Backend ECS service |
| `db` | TCP 5432 from `backend` SG only | RDS instance |

ECS tasks run in the public subnets with public IPs so they can pull images from ECR and write logs without a NAT gateway. They are still unreachable from the internet because their security groups only admit the tier in front of them. RDS sits in the private subnets, which have no internet route.

## First Deployment

1. Create the infrastructure:

   ```bash
   terraform init
   terraform apply -var="db_password=CHANGE_ME"
   ```

   Terraform registers the first task definitions pointing at the `:latest` image tag. The ECR repositories are empty at this point, so the ECS tasks fail to start until step 3. This is expected.

2. Create an IAM role for GitHub Actions (OIDC) with the permissions below, and save its ARN as the `AWS_ROLE_TO_ASSUME` repository secret.

3. Run the `CI/CD` workflow on `main`, either by pushing a commit or with **Run workflow** in the Actions tab. It builds both images, registers new task definition revisions with the commit SHA tag, and waits for both services to become stable.

4. Open the application:

   ```bash
   terraform output app_url
   ```

After this, every push to `main` deploys automatically. The ECS services ignore `task_definition` changes, so a later `terraform apply` does not roll back to the `:latest` revision.

## GitHub Actions Role Permissions

The role in `AWS_ROLE_TO_ASSUME` needs:

- `ecr:GetAuthorizationToken` on `*`
- `ecr:BatchCheckLayerAvailability`, `ecr:InitiateLayerUpload`, `ecr:UploadLayerPart`, `ecr:CompleteLayerUpload`, `ecr:PutImage`, `ecr:BatchGetImage` on the two ECR repositories
- `ecs:DescribeTaskDefinition`, `ecs:RegisterTaskDefinition` on `*`
- `ecs:UpdateService`, `ecs:DescribeServices` on the two ECS services
- `iam:PassRole` on the task execution role (`terraform output` does not print it; it is named `<project_name>-task-execution`)

## Cost

This is not free-tier infrastructure. At the default sizes in `us-east-1`, expect very roughly:

| Resource | Approx. monthly cost |
|---|---|
| Application Load Balancer | ~$16 + usage |
| Fargate backend (0.5 vCPU, 1 GB) | ~$18 |
| Fargate frontend (0.25 vCPU, 0.5 GB) | ~$9 |
| RDS `db.t4g.micro` + 20 GB | ~$14 |
| Public IPv4 addresses (ALB and tasks) | ~$3.60 each |

Run `terraform destroy -var="db_password=CHANGE_ME"` when you are done experimenting. Check current AWS pricing before applying.

## Notes

- The load balancer serves plain HTTP. Add an ACM certificate and an HTTPS listener for real use.
- `skip_final_snapshot = true` means `terraform destroy` deletes the database without a backup.
- The database password is stored in Terraform state. Use a remote, encrypted state backend for anything beyond a demo.

Do not commit real secrets or Terraform state files.
