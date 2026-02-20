output "work_queue_url" {
  value = aws_sqs_queue.work_queue.url
}

output "work_queue_arn" {
  value = aws_sqs_queue.work_queue.arn
}
