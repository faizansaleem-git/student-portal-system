/**
 * app.js — Express application entry point for the Finance microservice.
 *
 * Architecture: constructor-based dependency injection wires the full
 * object graph at startup: Model → Repository → Service → Controller → Router.
 * This mirrors the Spring IoC container pattern and makes every layer
 * independently testable.
 *
 * Technology choice:
 *   Node.js + Express chosen for deliberate stack diversity across services.
 *   MongoDB chosen because invoice/account data is document-shaped —
 *   no relational joins needed, flexible schema, demonstrates NoSQL vs SQL
 *   contrast (Student and Library services use MySQL + JPA).
 */

const express = require('express');
const morgan = require('morgan');
const path = require('path');

const connectDB = require('./config/db');

// Models
const Account = require('./models/Account');
const Invoice = require('./models/Invoice');

// Repositories — depend on models
const AccountRepository = require('./repositories/accountRepository');
const InvoiceRepository = require('./repositories/invoiceRepository');

// Services — depend on repositories
const AccountService = require('./services/accountService');
const InvoiceService = require('./services/invoiceService');

// Controllers — depend on services
const AccountController = require('./controllers/accountController');
const InvoiceController = require('./controllers/invoiceController');

// Routers — depend on controllers
const createAccountRouter = require('./routes/accountRoutes');
const { createInvoiceRouter, createPortalRouter } = require('./routes/invoiceRoutes');

// ─── Wire the dependency graph (constructor injection) ────────────────────────
const accountRepository = new AccountRepository(Account);
const invoiceRepository = new InvoiceRepository(Invoice);

const accountService = new AccountService(accountRepository);
const invoiceService = new InvoiceService(invoiceRepository);

const accountController = new AccountController(accountService);
const invoiceController = new InvoiceController(invoiceService);

// ─── Create Express app ───────────────────────────────────────────────────────
const app = express();

// EJS as the view engine for the Finance Portal web UI
app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

// Middleware
app.use(morgan('dev'));                          // HTTP request logging
app.use(express.json());                        // Parse JSON request bodies
app.use(express.urlencoded({ extended: true })); // Parse form POST bodies

// Static assets (Bootstrap loaded from CDN — no static dir needed)

// ─── Routes ──────────────────────────────────────────────────────────────────
app.use('/api/accounts', createAccountRouter(accountController));
app.use('/api/invoices', createInvoiceRouter(invoiceController));

// Balance endpoint lives under accounts but uses invoice data
app.get('/api/accounts/:studentId/balance', invoiceController.getBalance);

// Finance Portal web UI
app.use('/portal', createPortalRouter(invoiceController));

// Redirect root to portal
app.get('/', (req, res) => res.redirect('/portal'));

// Health-check endpoint (used by Docker healthcheck and root compose)
app.get('/health', (req, res) => res.json({ status: 'UP', service: 'finance-service' }));

// 404 handler
app.use((req, res) => {
    res.status(404).json({ error: `Route not found: ${req.method} ${req.originalUrl}` });
});

// Global error handler
app.use((err, req, res, _next) => {
    console.error('[Finance] Unhandled error:', err.message);
    res.status(500).json({ error: 'Internal server error', detail: err.message });
});

// ─── Start server ─────────────────────────────────────────────────────────────
const PORT = process.env.PORT || 8082;

async function start() {
    await connectDB();
    app.listen(PORT, () => {
        console.log(`[Finance] Service running on http://localhost:${PORT}`);
        console.log(`[Finance] Portal UI:  http://localhost:${PORT}/portal`);
        console.log(`[Finance] Health:     http://localhost:${PORT}/health`);
    });
}

// Only start the server when this file is executed directly (not when imported by tests)
if (require.main === module) {
    start();
}

module.exports = app;
