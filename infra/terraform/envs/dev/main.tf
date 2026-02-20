locals {
  env_prefix = "dev"
  tags = {
    project = "orkestra"
    env     = "dev"
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
