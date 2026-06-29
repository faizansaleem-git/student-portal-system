/**
 * accountController.js — HTTP handlers for Finance account endpoints.
 *
 * Controllers contain NO business logic — they delegate to the service
 * layer and translate HTTP request/response objects.
 * This mirrors the Controller → Service → Repository layered architecture
 * used in the Spring Boot microservices.
 */

class AccountController {
    /**
     * @param {import('../services/accountService')} accountService
     */
    constructor(accountService) {
        this.accountService = accountService;
        // Bind methods so they retain correct `this` when used as route handlers
        this.createAccount = this.createAccount.bind(this);
    }

    /**
     * POST /api/accounts
     * Body: { studentId }
     *
     * Called by Student service on first-ever enrolment to open
     * a finance account for the student.
     *
     * Responds with 201 Created on success, 409 if account exists (treated
     * as success by the service — idempotent), 400 for validation errors.
     */
    async createAccount(req, res) {
        try {
            const { studentId } = req.body;
            if (!studentId) {
                return res.status(400).json({ error: 'studentId is required' });
            }
            const result = await this.accountService.createAccount(studentId);
            return res.status(201).json(result);
        } catch (err) {
            console.error('[AccountController] createAccount error:', err.message);
            return res.status(500).json({ error: 'Failed to create account', detail: err.message });
        }
    }
}

module.exports = AccountController;
