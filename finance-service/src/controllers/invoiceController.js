/**
 * invoiceController.js — HTTP handlers for invoice endpoints.
 *
 * Handles REST API calls from Student and Library services as well as
 * web-form submissions from the Finance Portal UI.
 *
 * No business logic here — all logic delegated to InvoiceService.
 */

class InvoiceController {
    /**
     * @param {import('../services/invoiceService')} invoiceService
     */
    constructor(invoiceService) {
        this.invoiceService = invoiceService;
        this.createInvoice = this.createInvoice.bind(this);
        this.getInvoice = this.getInvoice.bind(this);
        this.getBalance = this.getBalance.bind(this);
        this.payInvoice = this.payInvoice.bind(this);
        this.showPortal = this.showPortal.bind(this);
        this.lookupInvoice = this.lookupInvoice.bind(this);
        this.processPayment = this.processPayment.bind(this);
    }

    /**
     * POST /api/invoices
     * Body: { studentId, amount, description }
     *
     * Creates an OUTSTANDING invoice and returns the referenceNumber.
     * Called by Student service (enrolment fee) and Library service (fine).
     */
    async createInvoice(req, res) {
        try {
            const { studentId, amount, description } = req.body;
            if (!studentId || amount === undefined || !description) {
                return res.status(400).json({
                    error: 'studentId, amount, and description are required',
                });
            }
            const result = await this.invoiceService.createInvoice({ studentId, amount, description });
            return res.status(201).json(result);
        } catch (err) {
            console.error('[InvoiceController] createInvoice error:', err.message);
            return res.status(500).json({ error: 'Failed to create invoice', detail: err.message });
        }
    }

    /**
     * GET /api/invoices/:reference
     * Returns full invoice by referenceNumber. 404 if not found.
     */
    async getInvoice(req, res) {
        try {
            const invoice = await this.invoiceService.getInvoice(req.params.reference);
            if (!invoice) {
                return res.status(404).json({ error: 'Invoice not found' });
            }
            return res.json(invoice);
        } catch (err) {
            console.error('[InvoiceController] getInvoice error:', err.message);
            return res.status(500).json({ error: 'Failed to retrieve invoice', detail: err.message });
        }
    }

    /**
     * GET /api/accounts/:studentId/balance
     * Returns { studentId, hasOutstanding: boolean }
     * Used by Student service graduation eligibility check.
     */
    async getBalance(req, res) {
        try {
            const result = await this.invoiceService.getBalance(req.params.studentId);
            return res.json(result);
        } catch (err) {
            console.error('[InvoiceController] getBalance error:', err.message);
            return res.status(500).json({ error: 'Failed to check balance', detail: err.message });
        }
    }

    /**
     * POST /api/invoices/:reference/pay
     * Marks invoice as PAID. Idempotent — safe to call on already-paid invoice.
     */
    async payInvoice(req, res) {
        try {
            const invoice = await this.invoiceService.payInvoice(req.params.reference);
            return res.json(invoice);
        } catch (err) {
            if (err.message.startsWith('Invoice not found')) {
                return res.status(404).json({ error: err.message });
            }
            console.error('[InvoiceController] payInvoice error:', err.message);
            return res.status(500).json({ error: 'Failed to pay invoice', detail: err.message });
        }
    }

    // -------------------------------------------------------------------------
    // Finance Portal web UI handlers (EJS views)
    // -------------------------------------------------------------------------

    /**
     * GET /portal
     * Render the Finance Portal. Handles ?ref= (lookup) and ?paid= (post-payment) query params
     * so all renders happen via GET — prevents browser "resend form?" prompt on reload.
     */
    async showPortal(req, res) {
        const { ref, paid, lookupError } = req.query;

        if (ref) {
            try {
                const invoice = await this.invoiceService.getInvoice(ref);
                if (!invoice) {
                    return res.render('portal', {
                        invoice: null,
                        error: `No invoice found for reference: ${ref}`,
                        success: paid === 'true' ? `Payment successful! Invoice ${ref} is now PAID.` : null,
                    });
                }
                const success = paid === 'true'
                    ? (invoice.status === 'PAID' ? `Payment successful! Invoice ${ref} is now PAID.` : null)
                    : null;
                return res.render('portal', { invoice, error: null, success });
            } catch (err) {
                console.error('[InvoiceController] showPortal lookup error:', err.message);
                return res.render('portal', { invoice: null, error: 'An error occurred. Please try again.', success: null });
            }
        }

        const error = lookupError === 'notfound' ? 'Invoice not found. Please check the reference.'
                    : lookupError === 'invalid'   ? 'Please enter a valid invoice reference (letters, numbers, and hyphens only).'
                    : lookupError === 'payfail'   ? 'Payment failed. Please try again.'
                    : null;

        res.render('portal', { invoice: null, error, success: null });
    }

    /**
     * POST /portal/lookup → redirect to GET /portal?ref=<reference>  (PRG pattern)
     */
    async lookupInvoice(req, res) {
        const { reference } = req.body;
        if (!reference || !/^[a-zA-Z0-9-]+$/.test(reference.trim())) {
            return res.redirect('/portal?lookupError=invalid');
        }
        return res.redirect(`/portal?ref=${encodeURIComponent(reference.trim())}`);
    }

    /**
     * POST /portal/pay/:reference → redirect to GET /portal?ref=<reference>&paid=true  (PRG pattern)
     */
    async processPayment(req, res) {
        const { reference } = req.params;
        try {
            await this.invoiceService.payInvoice(reference);
            return res.redirect(`/portal?ref=${encodeURIComponent(reference)}&paid=true`);
        } catch (err) {
            if (err.message.startsWith('Invoice not found')) {
                return res.redirect('/portal?lookupError=notfound');
            }
            console.error('[InvoiceController] processPayment error:', err.message);
            return res.redirect('/portal?lookupError=payfail');
        }
    }
}

module.exports = InvoiceController;
