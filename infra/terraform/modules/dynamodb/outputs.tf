output "tenants_table_name" {
  value = aws_dynamodb_table.tenants.name
}

output "tenant_memberships_table_name" {
  value = aws_dynamodb_table.orkestra_tenant_memberships.name
}

output "platform_admins_table_name" {
  value = aws_dynamodb_table.orkestra_platform_admins.name
}

output "users_table_name" {
  value = aws_dynamodb_table.orkestra_users.name
}

output "job_definitions_table_name" {
  value = aws_dynamodb_table.job_definitions.name
}

output "executions_table_name" {
  value = aws_dynamodb_table.executions.name
}

output "due_work_table_name" {
  value = aws_dynamodb_table.due_work.name
}
