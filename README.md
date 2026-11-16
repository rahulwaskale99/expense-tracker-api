# expense-tracker-api

A REST API for tracking personal expenses — CRUD with filtering, bean validation, consistent error shaping, and monthly summaries aggregated in the database. Spring Boot 3 + JPA, Java 21.

Built as a personal project to practice the pieces a production backend actually needs: layered architecture, typed API contracts (records for DTOs), database-side aggregation, profiles for dev/prod, and a test pyramid (fast unit tests + real-MVC web tests).

## Features

- Full CRUD for expenses with bean validation on every input
- Filtering by category and date range
- Monthly summary: grand total + per-category totals, aggregated in SQL (interface projections — no entities loaded)
- Consistent error bodies (`timestamp`, `status`, `message`, `fieldErrors`) via a `@RestControllerAdvice`; validation failures report per-field messages
- Dev profile seeds sample data; `postgres` profile for real deployments
- Test suite: Mockito unit tests for business rules + `@WebMvcTest` for the HTTP contract

## Tech stack

| Layer     | Choice |
|-----------|--------|
| Language  | Java 21 |
| Framework | Spring Boot 3.3 (Web, Data JPA, Validation) |
| Database  | H2 in-memory (dev), PostgreSQL (prod profile) |
| Build     | Maven |
| Tests     | JUnit 5, Mockito, MockMvc |

## Getting started

```bash
mvn spring-boot:run          # starts on :8080 with H2 + sample data
```

H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:expenses`, user `sa`)

Run the tests:

```bash
mvn test
```

## API

| Method | Path                        | Description                          |
|--------|-----------------------------|--------------------------------------|
| POST   | `/api/v1/expenses`          | Create an expense (201 + Location)   |
| GET    | `/api/v1/expenses`          | List, optional `category`, `fromDate`, `toDate` |
| GET    | `/api/v1/expenses/{id}`     | Fetch one (404 if unknown)           |
| PUT    | `/api/v1/expenses/{id}`     | Replace an expense                   |
| DELETE | `/api/v1/expenses/{id}`     | Delete (204)                         |
| GET    | `/api/v1/expenses/summary`  | Monthly totals (`?month=yyyy-MM`)    |

### Examples

```bash
# create
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{"description": "Groceries", "amount": 86.40, "category": "FOOD", "date": "2026-11-15"}'

# monthly summary
curl http://localhost:8080/api/v1/expenses/summary?month=2026-11
```

```json
{
  "month": "2026-11",
  "total": 195.90,
  "byCategory": [
    { "category": "HOUSING", "total": 150.00 },
    { "category": "FOOD", "total": 45.90 }
  ]
}
```

## Design notes

- **DTOs are records**, the entity never leaves the service layer — the HTTP contract is explicit and immutable.
- **Amounts are `BigDecimal`** with column precision 12/2 — never doubles for money.
- **`date` vs `createdAt`**: the user picks the day money was spent; the record creation time is separate. Summaries aggregate over `date`, so backdated entries land in the right month.
- **Aggregation in the database**, not in Java: `sumByCategoryBetween` is a grouped JPQL query mapped onto an interface projection.
- **Branching instead of a mega-query** for optional filters — each repository method has a clear, type-safe signature.

## Roadmap

- [ ] Pagination + sorting on the list endpoint (`Pageable`)
- [ ] CSV import endpoint for bank exports
- [ ] Budgets per category with alerts when a month crosses the limit
- [ ] Recurring expenses (rent, subscriptions) generated on schedule
- [ ] Flyway migrations to replace `ddl-auto`
- [ ] JWT authentication with per-user data isolation
- [ ] OpenAPI 3 documentation (springdoc)
- [ ] Dockerfile + docker-compose (app + PostgreSQL)
- [ ] Soft delete with audit log
- [ ] Exchange-rate conversion for foreign-currency expenses
