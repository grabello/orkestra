# Architecture (Draft)

- Spring Boot backend (single service)
- DynamoDB for persistent execution state
- SQS for async work dispatch
- Tenant + API key auth (JWT)

More docs will be added under docs/architecture and docs/decisions.
