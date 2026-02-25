## Backend module boundaries

Orkestra’s backend is a single Spring Boot service, but structured as a Gradle multi-module
project to keep domain logic, orchestration logic, and infrastructure adapters cleanly separated.

| Module | Purpose | Contains | Must NOT contain | Depends on |
|---|---|---|---|---|
| `backend/modules/orkestra-api` | **API contract module**. Defines the public request/response shapes used by the REST API. Stable “surface area”. | DTOs (requests/responses), error model (`ApiError`), API enums, pagination models | Spring annotations (`@RestController`, `@Configuration`), AWS SDK, persistence code, business logic | *(optional)* `orkestra-core` (only if sharing enums/types). Prefer no deps if possible |
| `backend/modules/orkestra-app` | **Composition root + runtime**. Wires everything together and hosts the HTTP server. | `OrkestraApplication`, Spring config, controllers, filters (correlation IDs), exception handlers, property binding, actuator setup | Domain rules/state machine logic, DynamoDB data model decisions embedded in controllers | `orkestra-api`, `orkestra-core`, `orkestra-dsl`, `orkestra-engine`, `orkestra-storage` |
| `backend/modules/orkestra-core` | **Domain model**. The truth of the business concepts and invariants. | Domain entities/value objects, core enums (ExecutionStatus, StepStatus), validation helpers that are domain-only | Spring, AWS SDK, HTTP, DynamoDB key formats, repositories | *(none)* (or very minimal pure libs only) |
| `backend/modules/orkestra-dsl` | **Workflow definition DSL**. Parses + validates YAML definitions and compiles into a normalized/validated model. | YAML parsing, schema/structure validation, DAG validation (cycle detection/topo sort), “compiled workflow” representation | Spring, AWS SDK, persistence, runtime execution | `orkestra-core` |
| `backend/modules/orkestra-engine` | **Orchestration engine**. Implements execution semantics independent of infrastructure. | State machines, step scheduling logic, retry/backoff calculations, compensation orchestration, idempotency rules, ports/interfaces (e.g., `ExecutionStore`, `WorkQueue`) | Spring, AWS SDK, DynamoDB/SQS specifics, web controllers | `orkestra-core` (and optionally `orkestra-dsl` if engine consumes compiled workflows) |
| `backend/modules/orkestra-storage` | **Infrastructure adapter**. Implements engine ports using DynamoDB (and later SQS if you add a queue module). | DynamoDB repository implementations, item mapping, conditional writes/leases, query access patterns | Spring wiring (if using composition-root approach), controllers, business rules beyond persistence concerns | `orkestra-core`, `orkestra-engine` (+ AWS SDK v2) |

### Dependency direction (rule of thumb)
- High-level modules depend on lower-level modules.
- Domain (`core`) stays pure.
- Engine (`engine`) stays infrastructure-agnostic.
- Adapters (`storage`) implement ports from `engine`.
- App (`app`) wires everything and is the only module that “knows” Spring Boot runtime details.
