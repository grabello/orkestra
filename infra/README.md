# Infra (Local)

LocalStack provides local DynamoDB + SQS.

```bash
docker compose up -d
cd infra/terraform/envs/local
terraform init # if necessary
terraform apply
```
