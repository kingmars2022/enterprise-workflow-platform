# Terraform AWS Infrastructure

This folder contains a starter AWS infrastructure template for the Enterprise Workflow Management Platform.

It creates:

- ECR repositories for backend and frontend Docker images
- VPC, public subnets, route table, and internet gateway
- ECS cluster
- RDS PostgreSQL instance
- S3 bucket and CloudFront distribution for frontend assets

The ECS service/task-definition resources are intentionally left as a next step because real deployments require account-specific IAM roles, container image tags, domain choices, and secret management decisions.

## Commands

```bash
terraform init
terraform plan -var="db_password=CHANGE_ME"
terraform apply -var="db_password=CHANGE_ME"
```

Do not commit real secrets or Terraform state files.
