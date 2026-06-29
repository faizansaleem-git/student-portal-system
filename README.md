# Student Portal System

A microservices-based Student Portal System built for the Software Engineering for Service Computing (SESC) module at Leeds Beckett University.

## System Architecture

The system consists of three independent microservices, each with its own database:

| Service | Technology | Port | Database |
|---|---|---|---|
| Student Service | Spring Boot + Java 21 | 8080 | MySQL |
| Finance Service | Node.js + Express | 8082 | MongoDB |
| Library Service | Spring Boot + Java 21 | 8083 | MySQL |

## Portal URLs

| Portal | URL |
|---|---|
| Student Portal | http://localhost:8080 |
| Finance Portal | http://localhost:8082/portal |
| Library Portal | http://localhost:8083/login |

## Features

### Student Service
- Student registration and login
- Browse and enrol in courses
- View enrolments with invoice references
- Profile management
- Graduation eligibility checker (credits, invoices, library loans)

### Finance Service
- Invoice creation and management
- Invoice lookup by reference number
- Payment processing
- Outstanding balance checking

### Library Service
- Book browsing and borrowing
- Loan management and returns
- Overdue fine calculation
- Admin dashboard for book management

## Running with Docker (Recommended)

### Prerequisites
- Docker Desktop installed and running

### Start all services
```bash
cd student-portal-system
docker compose up --build -d
```

### Stop all services
```bash
docker compose down
```

## Running Manually (Without Docker)

### Prerequisites
- Java 21 JDK
- IntelliJ IDEA Community Edition (with Lombok plugin)
- Node.js 20+
- MySQL 8 (port 3306, root password: root)
- MongoDB Community (port 27017)

### Step 1 — Create MySQL Databases
Run in MySQL Workbench:
```sql
CREATE DATABASE IF NOT EXISTS studentdb;
CREATE DATABASE IF NOT EXISTS librarydb;
```

### Step 2 — Run Finance Service
```cmd
cd finance-service
npm install
set MONGO_URI=mongodb://localhost:27017/financedb
set PORT=8082
node src/app.js
```

### Step 3 — Run Library Service in IntelliJ
Open `library-service` folder in IntelliJ IDEA and set these environment variables in Run Configuration:
```
LIBRARY_DB_URL=jdbc:mysql://localhost:3306/librarydb?createDatabaseIfNotExist=true&serverTimezone=UTC
LIBRARY_DB_USER=root
LIBRARY_DB_PASSWORD=root
FINANCE_SERVICE_URL=http://localhost:8082
```

### Step 4 — Run Student Service in IntelliJ
Open `student-service` folder in IntelliJ IDEA and set these environment variables in Run Configuration:
```
STUDENT_DB_URL=jdbc:mysql://localhost:3306/studentdb?createDatabaseIfNotExist=true&serverTimezone=UTC
STUDENT_DB_USER=root
STUDENT_DB_PASSWORD=root
FINANCE_SERVICE_URL=http://localhost:8082
LIBRARY_SERVICE_URL=http://localhost:8083
FINANCE_PORTAL_URL=http://localhost:8082
LIBRARY_PORTAL_URL=http://localhost:8083
```

### Startup Order
1. MySQL (Windows service — starts automatically)
2. MongoDB (Windows service — starts automatically)
3. Finance Service (CMD terminal)
4. Library Service (IntelliJ)
5. Student Service (IntelliJ) — always last

## Default Credentials

| Portal | Credentials |
|---|---|
| Student Portal | Register a new account on the login page |
| Library Portal | Student ID (e.g. STU-00001) + PIN: 000000 (change on first login) |
| Finance Portal | No login required — enter invoice reference number |

## Project Structure

```
student-portal-system/
├── docker-compose.yml          # Starts all 6 containers
├── student-service/            # Spring Boot — Student Portal
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── finance-service/            # Node.js — Finance Portal
│   ├── src/
│   ├── Dockerfile
│   └── package.json
└── library-service/            # Spring Boot — Library Portal
    ├── src/
    ├── Dockerfile
    └── pom.xml
```

## Authors
Faizan Saleem 
Faizan Zafar 
Hiral Jahlani
