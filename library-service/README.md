# Library Service

Spring Boot microservice providing the Library Portal for book borrowing and loan management.

## Technology Stack
- Java 21
- Spring Boot 3.x
- Spring Security (session-based auth, BCrypt PIN)
- Spring Data JPA + Flyway
- MySQL 8
- Thymeleaf + Bootstrap 5

## Port
`8083`

## Database
MySQL — `librarydb`

Tables created automatically by Flyway on startup:
- `accounts` — library accounts (linked to student IDs)
- `books` — book catalogue with availability
- `loans` — borrow/return records

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `LIBRARY_DB_URL` | `jdbc:mysql://localhost:3308/librarydb` | MySQL connection URL |
| `LIBRARY_DB_USER` | `root` | MySQL username |
| `LIBRARY_DB_PASSWORD` | `root` | MySQL password |
| `FINANCE_SERVICE_URL` | `http://localhost:8082` | Finance service API URL |

## Running Locally

```bash
./mvnw spring-boot:run
```

Or open in IntelliJ IDEA and run `LibraryServiceApplication`.

## API Endpoints (consumed by Student Service)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/accounts` | Create library account |
| GET | `/api/accounts/:studentId/loans/active-count` | Count active loans |

## Key Features
- Separate PIN-based login (Student ID + 6-digit PIN)
- Book browsing and borrowing (14-day loan period)
- Overdue fine calculation (£1.00 per day)
- Fine invoice posted to Finance service on overdue return
- Admin panel for adding books via Open Library API (ISBN lookup)
- Active loan count exposed to Student service for graduation check

## Default Login
- Student ID: e.g. `STU-00001`
- Default PIN: `000000` (must be changed on first login)
- Admin: `admin` / `123456`
