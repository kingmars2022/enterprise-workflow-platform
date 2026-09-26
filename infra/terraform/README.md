# Terraform AWS Infrastructure

This folder contains a starter AWS infrastructure template for the Enterprise Workflow Management Platform.

It creates:

- ECR repositories for backend and frontend Docker images
- VPC with public subnets (internet gateway) and private subnets (no internet route)
- Security groups for frontend, backend, and database tiers
- ECS cluster
- RDS PostgreSQL instance (encrypted, private subnets, not publicly accessible)
- S3 bucket and CloudFront distribution for frontend assets

## Network Security

Traffic is only allowed along the path the application actually uses:

```text
internet --80--> frontend SG (nginx) --8080--> backend SG (Spring Boot) --5432--> db SG (RDS)
```

| Security group | Inbound | Attach to |
|---|---|---|
| `frontend` | TCP 80 from `0.0.0.0/0` | Frontend ECS service |
| `backend` | TCP 8080 from `frontend` SG only | Backend ECS service |
| `db` | TCP 5432 from `backend` SG only | RDS instance |

ECS tasks run in the public subnets so they can pull images from ECR without a NAT gateway. The backend is still unreachable from the internet because its security group only admits the frontend. RDS sits in the private subnets, which have no internet route.

The ECS service/task-definition resources are intentionally left as a next step because real deployments require account-specific IAM roles, container image tags, domain choices, and secret management decisions.

## Commands

```bash
terraform init
terraform plan -var="db_password=CHANGE_ME"
terraform apply -var="db_password=CHANGE_ME"
```

Do not commit real secrets or Terraform state files.
