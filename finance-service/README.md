# Finance Service

Node.js microservice providing invoice management and the Finance Portal.

## Technology Stack
- Node.js + Express
- MongoDB + Mongoose
- EJS templating + Bootstrap 5
- Jest (unit tests)

## Port
`8082`

## Database
MongoDB — `financedb`

Collections created automatically:
- `invoices` — all student invoices
- `accounts` — student finance accounts

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGO_URI` | `mongodb://localhost:27017/financedb` | MongoDB connection string |
| `PORT` | `8082` | Server port |

## Running Locally

```bash
npm install
set MONGO_URI=mongodb://localhost:27017/financedb
set PORT=8082
node src/app.js
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/invoices` | Create invoice (called by Student/Library service) |
| GET | `/api/invoices/:reference` | Get invoice by reference |
| POST | `/api/invoices/:reference/pay` | Mark invoice as paid |
| GET | `/api/accounts/:studentId/balance` | Check outstanding balance |

## Finance Portal
- `GET /portal` — Invoice lookup page
- `POST /portal/lookup` — Look up invoice by reference
- `POST /portal/pay/:reference` — Pay an invoice

## Key Features
- Invoice creation with unique reference numbers
- Payment processing
- Outstanding balance checking for graduation eligibility
- Fine invoices from Library service for overdue books
