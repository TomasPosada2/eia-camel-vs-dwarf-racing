# The Great EIA Camel vs. Dwarf Racing System

Backend information system for the fictional EIA Camel vs. Dwarf Racing League. Built for the Backend Development course at Universidad EIA.

> This README currently documents **Persona 1's** delivery (project architecture, security, users, competitors and audit log). Sections for Teams/Races (Persona 2), Registrations/Results/Docker (Persona 3) and the frontend will be completed by the rest of the team as those modules land.

## Team

| Persona | Scope |
|---|---|
| Persona 1 | Architecture setup, Security (JWT + roles), Users, Competitors, Audit Log, global exception handling |
| Persona 2 | Teams, Races |
| Persona 3 | Registrations, Results, Standings, Database/Docker finalization |

## Architecture

Layered Spring Boot backend under `backend/src/main/java/com/eia/racing`:

```
config/       Security, OpenAPI (Swagger) and startup data seeding configuration
security/     JWT generation/validation, authentication filter, UserDetails adapter
controller/   REST controllers (HTTP only, no business logic)
service/      Business rules and orchestration
repository/   Spring Data JPA repositories
model/        JPA entities and enums
dto/          Request/response contracts (entities are never exposed directly)
mapper/       Entity <-> DTO conversion
exception/    Custom exceptions + centralized @RestControllerAdvice error handling
```

## Technologies

- Java 21, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Validation, Spring Security
- JWT (jjwt), BCrypt password hashing
- PostgreSQL (Docker), H2 (tests only)
- springdoc-openapi (Swagger UI) for manual API testing
- Maven

## Database model (Persona 1 scope)

- **User**: id, email (unique), password (BCrypt hash), fullName, role (`ADMIN`, `RACE_ORGANIZER`, `VIEWER`), enabled, createdAt.
- **Competitor**: id, name, nickname (unique), competitorType (`DWARF`, `CAMEL`, `MEDIUM`, `OTHER`), dateOfBirth/approximateAge, weight, height, countryOrigin, status (`ACTIVE`, `INJURED`, `SUSPENDED`, `RETIRED`), registrationDate, `teamId` (plain FK column, to be turned into a JPA relationship once Persona 2 creates the `Team` entity), victories/defeats/completedRaces (updated later by the Results module).
- **AuditLog**: id, username, action, entityType, entityId, timestamp, description, previousValue, newValue.

Roles are stored as an enum column on `User` rather than a separate `Role` table — a deliberate simplification for this project's scope.

## Security strategy

- Stateless JWT authentication. Access tokens expire quickly (15 min by default); refresh tokens last longer (7 days) and are only accepted by `POST /api/auth/refresh`.
- Passwords hashed with BCrypt, never returned by any endpoint.
- Authorization enforced with `@PreAuthorize` at the controller layer based on Spring Security roles (`ROLE_ADMIN`, `ROLE_RACE_ORGANIZER`, `ROLE_VIEWER`).
- Missing/invalid token → `401 Unauthorized`. Authenticated but insufficient role → `403 Forbidden`. Both return the same structured JSON error body as the rest of the API (no stack traces).

### Roles and permissions (implemented so far)

| Role | Competitors | Users | Audit log |
|---|---|---|---|
| ADMIN | full CRUD | manage (list/get/update role/status) | view |
| RACE_ORGANIZER | read only | — | — |
| VIEWER | read only | — | — |

## Running locally

### 1. Start PostgreSQL

```bash
cp .env.example .env
docker compose up -d
```

### 2. Run the backend

```bash
cd backend
mvn spring-boot:run
```

Requires JDK 21 and Maven 3.9+ installed locally (no Maven Wrapper is committed yet — feel free to run `mvn -N wrapper:wrapper` once and commit the result if the team prefers `./mvnw`).

The API starts on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

### Environment variables

| Variable | Purpose |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection |
| `JWT_SECRET` | HMAC signing key for JWTs |
| `JWT_EXPIRATION` | Access token TTL (ms) |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL (ms) |
| `API_CORS_ORIGINS` | Comma-separated list of allowed frontend origins |

## Testing

```bash
cd backend
mvn test
```

Tests run against an in-memory H2 database (`application-test.yml`), never against the persistent PostgreSQL instance.

## Sample users (seeded on first startup)

| Email | Role | Password |
|---|---|---|
| admin@eia.edu.co | ADMIN | Passw0rd! |
| organizer@eia.edu.co | RACE_ORGANIZER | Passw0rd! |
| viewer@eia.edu.co | VIEWER | Passw0rd! |

5 dwarfs, 2 camels and 2 medium competitors are also seeded automatically.

## Sample API requests

Register:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"newuser@eia.edu.co","password":"Passw0rd!","fullName":"New User"}'
```

Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@eia.edu.co","password":"Passw0rd!"}'
```

Create a competitor (requires ADMIN access token):
```bash
curl -X POST http://localhost:8080/api/competitors \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Byte","nickname":"Byte","competitorType":"CAMEL","weight":480,"height":2.1,"countryOrigin":"Colombia"}'
```

List/filter competitors:
```bash
curl "http://localhost:8080/api/competitors?type=DWARF&status=ACTIVE&page=0&size=10" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## Known limitations

- Teams, Races, Registrations, Results and Standings are not implemented yet (Persona 2 / Persona 3 scope).
- `Competitor.teamId` has no JPA relationship until the `Team` entity exists.
- `docker-compose.yml` currently only provisions PostgreSQL; the backend/frontend containers and the final Dockerfile are pending (Persona 3).
- No refresh-token revocation/blacklist — a refresh token remains valid until it expires.

## Future improvements

- Move roles into a dedicated `Role` entity if fine-grained/custom roles become necessary.
- Add rate limiting and refresh-token rotation.
- Add CSV/PDF export for competitor and results data.
