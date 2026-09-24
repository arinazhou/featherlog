# FeatherLog

A REST API for tracking the health of pet budgies: weigh-ins, trend analysis, recurring care schedules, and automatic health alerts.

I built this for my blue budgie. Budgies hide illness very well. By the time a small bird *looks* sick, it has often been unwell for a while, and one of the few early signs you can measure is **gradual weight loss**. A 35 g budgie losing 3–4 g is already a 10% drop, and you won't see that by eye. FeatherLog turns daily kitchen-scale weigh-ins into early warnings and keeps the everyday care routine (water, food, cage cleaning, vet visits) on schedule.

## Features

- **JWT authentication.** Stateless bearer tokens signed with HS256 through Spring Security's OAuth2 resource server, with BCrypt-hashed passwords.
- **Per-user data isolation.** Every bird-scoped query filters on the owner. Another user's bird returns `404`, not `403`, so bird IDs don't leak.
- **Weight trend analysis** (`WeightTrendAnalyzer`):
  - *Baseline* is the **median** of weigh-ins from 7–30 days ago. The median holds up against one-off readings taken right after a meal.
  - *Recent* is the mean of the last 7 days.
  - `WEIGHT_DROP` fires at ≥5% below baseline (WARNING) and ≥10% below (CRITICAL).
  - `RAPID_WEIGHT_CHANGE` fires when two consecutive readings less than 72 h apart differ by ≥7%.
  - `OUT_OF_RANGE` fires when the latest reading falls outside the bird's target range (default 30–40 g).
- **Alert de-duplication.** A bad week of weigh-ins opens one alert, not one per reading.
- **Care schedules.** Each new bird gets a default budgie routine: daily water and food, greens every 2 days, weekly cage clean, cuttlebone check, nail/beak check, and a yearly vet exam. Completing a task reschedules it from the completion time.
- **Scheduled reminders.** A cron job (every 15 minutes) raises `CARE_OVERDUE` alerts. It is idempotent, and completing the task resolves its alert.
- **Health dashboard endpoint.** Weight status, open alerts, and overdue task count in one call.
- **Production basics.** Flyway migrations, PostgreSQL, RFC 7807 `ProblemDetail` errors with per-field validation messages, paginated history, OpenAPI/Swagger UI, Actuator health, Docker Compose, and GitHub Actions CI.

## Tech stack

Java 21 · Spring Boot 3.5 (Web, Data JPA, Security, Validation, Actuator) · PostgreSQL 16 · Flyway · springdoc-openapi · JUnit 5 / MockMvc / AssertJ · H2 (tests) · Docker

## Architecture

```
controller  →  service  →  repository  →  PostgreSQL
                  │
                  ├── WeightTrendAnalyzer   (pure logic, no Spring/JPA, unit tested)
                  ├── AlertService          (dedup + resolve)
                  └── CareReminderJob       (@Scheduled)
```

Code is organized by feature (`auth`, `bird`, `weight`, `care`, `alert`), not by layer. A `java.time.Clock` bean is injected everywhere time matters, including JWT expiry validation, so tests can move time forward ("30 hours later, is the water change overdue?") without sleeping.

### Data model

```
app_user 1─* bird 1─* weight_entry
                 1─* care_task 1─* health_alert
                 1─* health_alert
```

`care_task.next_due_at` is denormalized and indexed with `active`, so the reminder job finds overdue tasks in a single indexed query instead of computing due dates in memory.

## Running locally

With Docker (API and Postgres):

```bash
docker compose up --build
```

Or run Postgres in Docker and the app from your IDE or terminal:

```bash
docker compose up -d db
./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

Run the tests (no database needed; they use in-memory H2 in PostgreSQL mode):

```bash
./mvnw test
```

## API walkthrough

```bash
# Register and grab a token
TOKEN=$(curl -s -X POST localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"me@example.com","password":"password123","displayName":"Me"}' | jq -r .accessToken)

# Add a bird (species and weight range default to typical budgie values)
curl -s -X POST localhost:8080/api/birds -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Sky","colorMutation":"Sky Blue","sex":"MALE"}'

# Log a morning weigh-in; the response includes the fresh assessment and any new alerts
curl -s -X POST localhost:8080/api/birds/1/weights -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"grams":34.6}'

# One-glance health summary
curl -s localhost:8080/api/birds/1/health -H "Authorization: Bearer $TOKEN"

# See today's care tasks and mark water as done
curl -s localhost:8080/api/birds/1/care-tasks -H "Authorization: Bearer $TOKEN"
curl -s -X POST localhost:8080/api/birds/1/care-tasks/1/complete -H "Authorization: Bearer $TOKEN"
```

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Get a JWT |
| GET/POST | `/api/birds` | List or create birds |
| GET/PUT/DELETE | `/api/birds/{id}` | Read, update, or delete a bird |
| GET/POST | `/api/birds/{id}/weights` | Paged history (`?page=&size=`) or log a weigh-in |
| GET | `/api/birds/{id}/health` | Weight assessment, open alerts, overdue care |
| GET/POST | `/api/birds/{id}/care-tasks` | List or add recurring tasks |
| POST | `/api/birds/{id}/care-tasks/{taskId}/complete` | Mark a task done and reschedule it |
| DELETE | `/api/birds/{id}/care-tasks/{taskId}` | Deactivate a task |
| GET | `/api/alerts?open=true` | Alerts across all your birds |
| POST | `/api/alerts/{id}/resolve` | Resolve an alert |

## Testing

- `WeightTrendAnalyzerTest`: unit tests for baseline and median behavior, WARNING/CRITICAL thresholds, rapid-change detection, the target range, and ignoring future readings.
- `CareTaskTest`: scheduling rules for care tasks.
- `BirdApiTest`, `HealthMonitoringApiTest`: full-stack MockMvc tests covering auth (401/409), cross-user isolation, validation errors, a two-week weight decline that raises exactly one alert, pagination, and overdue reminders driven by a controllable clock.

## Possible next steps

- Push or email notifications for CRITICAL alerts
- Daily observation log (appetite, droppings, activity) feeding into the health status
- Molt-season awareness (weight often dips slightly during a heavy molt)
- Testcontainers-based integration tests against real PostgreSQL
