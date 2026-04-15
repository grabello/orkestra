resource "aws_dynamodb_table" "tenants" {
  name         = "${var.env_prefix}_orkestra_tenants"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "tenantId"

  attribute {
    name = "tenantId"
    type = "S"
  }

  attribute {
    name = "slug"
    type = "S"
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "tenants"
  })

  global_secondary_index {
    name               = "slug-index"
    hash_key           = "slug"
    projection_type    = "ALL"
    on_demand_throughput {
      max_read_request_units  = 5
      max_write_request_units = 5
    }
  }
}

resource "aws_dynamodb_table" "orkestra_tenant_memberships" {
  name         = "${var.env_prefix}_orkestra_tenant_memberships"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "tenantId"
  range_key    = "userId"

  attribute {
    name = "tenantId"
    type = "S"
  }
  attribute {
    name = "userId"
    type = "S"
  }

  global_secondary_index {
    name               = "user-lookup-index"
    hash_key           = "userId"
    range_key           = "tenantId"
    projection_type    = "ALL"
    on_demand_throughput {
      max_read_request_units  = 5
      max_write_request_units = 5
    }
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "orkestra_tenant_memberships"
  })
}

resource "aws_dynamodb_table" "orkestra_users" {
  name         = "${var.env_prefix}_orkestra_users"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "userId"

  attribute {
    name = "userId"
    type = "S"
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "orkestra_users"
  })
}

resource "aws_dynamodb_table" "orkestra_platform_admins" {
  name         = "${var.env_prefix}_orkestra_platform_admins"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "userId"

  attribute {
    name = "userId"
    type = "S"
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "platform_admins"
  })
}

resource "aws_dynamodb_table" "workflow_index" {
  name         = "${var.env_prefix}_orkestra_workflow_index"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "tenantId"
  range_key    = "workflowName"

  attribute {
    name = "tenantId"
    type = "S"
  }

  attribute {
    name = "workflowName"
    type = "S"
  }

  tags = merge(var.tags, {
    component = "dynamodb"
    table     = "workflow_index"
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
