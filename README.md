# expense-tracker-api

A REST API for recording and reviewing personal expenses. It supports the usual create, read, update, and delete operations, filtering by category or date, and a monthly summary.

The project uses Java 21, Spring Boot 3, Spring Data JPA, and Bean Validation. H2 is used for local development; a PostgreSQL profile is included for running against a real database.

## Run it locally

You will need JDK 21 and Maven.

```bash
mvn spring-boot:run
```

The API starts on port `8080` and uses an in-memory H2 database. By default, the database starts empty. To load the sample expenses, activate the `dev` profile:

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

The sample-data seeder only runs with that profile enabled.

Run the tests:

```bash
mvn test
```

The H2 console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console).

- JDBC URL: `jdbc:h2:mem:expenses`
- Username: `sa`
- Password: leave blank

## Endpoints

All endpoints are under `/api/v1/expenses`.

| Method | Path | What it does |
|---|---|---|
| POST | `/` | Create an expense. Returns 201 and a Location header. |
| GET | `/` | List expenses. Optional filters: `category`, `fromDate`, `toDate`. |
| GET | `/{id}` | Get one expense. |
| PUT | `/{id}` | Replace an expense. |
| DELETE | `/{id}` | Delete an expense. Returns 204. |
| GET | `/summary?month=yyyy-MM` | Get totals for a month. Defaults to the current month. |

For example, create an expense:

```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{"description":"Groceries","amount":86.40,"category":"FOOD","date":"2026-11-15"}'
```

Get the summary for November:

```bash
curl "http://localhost:8080/api/v1/expenses/summary?month=2026-11"
```

The summary includes the month's total and totals grouped by category.

## How the code is organised

The controller handles HTTP requests and delegates to the service. The service contains the application rules, and the repository handles database access.

A few choices worth calling out:

- Request and response DTOs are Java records. The JPA entity is kept inside the application rather than being returned directly from the API.
- Expense amounts use `BigDecimal`, not `double`.
- The expense date is separate from the record creation time. This matters when someone enters an expense a few days late.
- Monthly totals are calculated by the database query instead of loading every expense and adding them up in Java.
- Validation and exception handling are kept in the API layer, so clients get a consistent error response.

## PostgreSQL profile

The `postgres` profile switches the datasource to PostgreSQL and disables the H2 console. Configure the connection using environment variables:

```bash
export SPRING_PROFILES_ACTIVE=postgres
export DB_URL='jdbc:postgresql://localhost:5432/expenses'
export DB_USERNAME='your-user'
export DB_PASSWORD='your-password'
mvn spring-boot:run
```

The profile uses Hibernate's `ddl-auto=validate`. The schema must already exist; Flyway migrations are not included yet.

## Current scope

This is a small single-user expense API. Authentication, per-user data isolation, pagination, and recurring expenses are not implemented yet. They are possible next steps, but the current API should not be treated as a multi-user service or deployed as-is for real financial data.

**Known date-filter limitation:** when a request includes `category` or `toDate` without `fromDate`, the current service uses `1970-01-01` as the lower date bound. Expenses dated before that are therefore omitted from those filtered results. The query behavior needs a code fix and regression tests.