output "tenants_table_name" {
  value = aws_dynamodb_table.tenants.name
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
