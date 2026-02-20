locals {
  env_prefix = "local"
  tags = {
    project = "orkestra"
    env     = "local"
  }
}

module "dynamodb" {
  source     = "../../modules/dynamodb"
  env_prefix = local.env_prefix
  tags       = local.tags
}

module "sqs" {
  source     = "../../modules/sqs"
  env_prefix = local.env_prefix
  tags       = local.tags
}
