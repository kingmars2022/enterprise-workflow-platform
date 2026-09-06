variable "aws_region" {
  description = "AWS region for the deployment."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project resource prefix."
  type        = string
  default     = "enterprise-workflow"
}

variable "db_username" {
  description = "RDS PostgreSQL username."
  type        = string
  default     = "workflow"
}

variable "db_password" {
  description = "RDS PostgreSQL password. Use a secure value in real deployments."
  type        = string
  sensitive   = true
}
