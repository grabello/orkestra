resource "aws_sqs_queue" "work_queue" {
  name = "${var.env_prefix}-orkestra-work-queue"

  // Keep it simple for MVP
  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600  # 4 days

  tags = merge(var.tags, {
    component = "sqs"
    queue     = "work"
  })
}
