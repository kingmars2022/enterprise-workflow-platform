output "backend_ecr_repository_url" {
  value = aws_ecr_repository.backend.repository_url
}

output "frontend_ecr_repository_url" {
  value = aws_ecr_repository.frontend.repository_url
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "rds_endpoint" {
  value = aws_db_instance.postgres.address
}

output "frontend_cloudfront_domain" {
  value = aws_cloudfront_distribution.frontend.domain_name
}
