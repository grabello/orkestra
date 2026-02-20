resource "aws_dynamodb_table" "tenants" {
  name         = "${var.env_prefix}_orkestra_tenants"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "tenantId"

  attribute {
    name = "tenantId"
    type = "S"
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "tenants"
  })
}

resource "aws_dynamodb_table" "job_definitions" {
  name         = "${var.env_prefix}_orkestra_job_definitions"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"
  range_key    = "sk"

  attribute {
    name = "pk"
    type = "S"
  }
  attribute {
    name = "sk"
    type = "S"
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "job_definitions"
  })
}

resource "aws_dynamodb_table" "executions" {
  name         = "${var.env_prefix}_orkestra_executions"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"
  range_key    = "sk"

  attribute {
    name = "pk"
    type = "S"
  }
  attribute {
    name = "sk"
    type = "S"
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "executions"
  })
}

resource "aws_dynamodb_table" "due_work" {
  name         = "${var.env_prefix}_orkestra_due_work"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"
  range_key    = "sk"

  attribute {
    name = "pk"
    type = "S"
  }

  attribute {
    name = "sk"
    type = "S"
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "due_work"
  })
}
