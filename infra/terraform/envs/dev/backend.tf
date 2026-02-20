terraform {
  backend "s3" {
    bucket         = "REPLACE_ME"
    key            = "orkestra/dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "REPLACE_ME"
    encrypt        = true
  }
}
