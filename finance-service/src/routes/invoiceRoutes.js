/**
 * invoiceRoutes.js — Express router for invoice REST endpoints
 * and Finance Portal web UI routes.
 */

const express = require('express');

/**
 * @param {import('../controllers/invoiceController')} invoiceController
 * @returns {express.Router}
 */
function createInvoiceRouter(invoiceController) {
    const router = express.Router();

    // REST API routes (consumed by Student and Library services)
    router.post('/', invoiceController.createInvoice);
    router.get('/:reference', invoiceController.getInvoice);
    router.post('/:reference/pay', invoiceController.payInvoice);

    return router;
}

/**
 * @param {import('../controllers/invoiceController')} invoiceController
 * @returns {express.Router}
 */
function createPortalRouter(invoiceController) {
    const router = express.Router();

    // Finance Portal web UI routes
    router.get('/', invoiceController.showPortal);
    router.post('/lookup', invoiceController.lookupInvoice);
    router.post('/pay/:reference', invoiceController.processPayment);

    return router;
}

module.exports = { createInvoiceRouter, createPortalRouter };
