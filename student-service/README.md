# Student Service

Spring Boot microservice providing the Student Portal — the main entry point for students.

## Technology Stack
- Java 21
- Spring Boot 3.x
- Spring Security (session-based auth, BCrypt)
- Spring Data JPA + Flyway
- MySQL 8
- Thymeleaf + Bootstrap 5
- JUnit 5 + Mockito (20 tests)

## Port
`8080`

## Database
MySQL — `studentdb`

Tables created automatically by Flyway on startup:
- `users` — login accounts
- `students` — student profiles
- `courses` — available courses
- `enrolments` — course enrolments with invoice references

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `STUDENT_DB_URL` | `jdbc:mysql://localhost:3307/studentdb` | MySQL connection URL |
| `STUDENT_DB_USER` | `root` | MySQL username |
| `STUDENT_DB_PASSWORD` | `root` | MySQL password |
| `FINANCE_SERVICE_URL` | `http://localhost:8082` | Finance service API URL |
| `LIBRARY_SERVICE_URL` | `http://localhost:8083` | Library service API URL |
| `FINANCE_PORTAL_URL` | `http://localhost:8082` | Finance portal browser URL |
| `LIBRARY_PORTAL_URL` | `http://localhost:8083` | Library portal browser URL |

## Running Locally

```bash
# With Maven wrapper
./mvnw spring-boot:run
```

Or open in IntelliJ IDEA and run `StudentServiceApplication`.

## Running Tests

```bash
./mvnw test
```

20 tests covering service layer and controllers.

## Key Features
- Student registration and authentication
- Course browsing and enrolment (£1,500 per course)
- Finance invoice creation on enrolment
- Library account provisioning on first login
- Graduation eligibility check (credits + finance + library)
