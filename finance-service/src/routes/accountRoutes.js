/**
 * accountRoutes.js — Express router for account REST endpoints.
 *
 * All routes delegate to AccountController; no logic here.
 * Dependencies (service, repository, model) are wired at app startup
 * using constructor injection and passed in via the factory function below.
 */

const express = require('express');

/**
 * Create and return the accounts router with dependencies injected.
 *
 * @param {import('../controllers/accountController')} accountController
 * @returns {express.Router}
 */
function createAccountRouter(accountController) {
    const router = express.Router();

    // POST /api/accounts — create a finance account for a student
    router.post('/', accountController.createAccount);

    return router;
}

module.exports = createAccountRouter;
