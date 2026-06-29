/**
 * invoiceService.js — Business logic for invoices.
 *
 * Responsible for: creating invoices (UUID reference generation),
 * retrieving invoices by reference, checking graduation eligibility,
 * and processing payments.
 *
 * UUID chosen for referenceNumber so identifiers are globally unique
 * without a sequence generator, which suits the stateless/document model.
 */

// Use Node.js built-in crypto.randomUUID() — available since Node 15.6,
// avoids the uuid package's ESM-only v14 incompatibility with Jest/CommonJS.
const { randomUUID } = require('crypto');

class InvoiceService {
    /**
     * @param {import('../repositories/invoiceRepository')} invoiceRepository
     *
     * Constructor injection mirrors Spring Boot DI pattern —
     * repository supplied rather than instantiated internally.
     */
    constructor(invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Create a new OUTSTANDING invoice for a student.
     * Generates a UUID-based referenceNumber returned to the caller.
     *
     * @param {{ studentId: string, amount: number, description: string }} data
     * @returns {Promise<{referenceNumber, studentId, amount, description, status}>}
     */
    async createInvoice({ studentId, amount, description }) {
        if (!studentId) throw new Error('studentId is required');
        if (amount === undefined || amount === null) throw new Error('amount is required');
        if (!description) throw new Error('description is required');

        const referenceNumber = randomUUID();
        const invoice = await this.invoiceRepository.create({
            referenceNumber,
            studentId: studentId.trim(),
            amount: parseFloat(amount),
            description: description.trim(),
            status: 'OUTSTANDING',
        });

        return {
            referenceNumber: invoice.referenceNumber,
            studentId: invoice.studentId,
            amount: invoice.amount,
            description: invoice.description,
            status: invoice.status,
        };
    }

    /**
     * Retrieve an invoice by its reference number.
     * Returns null if not found (callers decide how to respond).
     *
     * @param {string} reference
     * @returns {Promise<object | null>}
     */
    async getInvoice(reference) {
        if (!reference) throw new Error('reference is required');
        return this.invoiceRepository.findByReference(reference);
    }

    /**
     * Check whether a student has any outstanding invoices.
     * Used by the Student service graduation eligibility endpoint.
     *
     * @param {string} studentId
     * @returns {Promise<{studentId: string, hasOutstanding: boolean}>}
     */
    async getBalance(studentId) {
        if (!studentId) throw new Error('studentId is required');
        const hasOutstanding = await this.invoiceRepository.hasOutstanding(studentId);
        return { studentId, hasOutstanding };
    }

    /**
     * Mark an invoice as PAID.
     * Throws if the invoice does not exist.
     * Returns the updated invoice so the portal can display the new status.
     *
     * @param {string} reference
     * @returns {Promise<object>}
     */
    async payInvoice(reference) {
        if (!reference) throw new Error('reference is required');

        const invoice = await this.invoiceRepository.findByReference(reference);
        if (!invoice) throw new Error(`Invoice not found: ${reference}`);

        if (invoice.status === 'PAID') {
            // Return as-is — idempotent; caller can check status field
            return invoice;
        }

        return this.invoiceRepository.markAsPaid(reference);
    }
}

module.exports = InvoiceService;
