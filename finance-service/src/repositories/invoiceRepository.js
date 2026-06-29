/**
 * invoiceRepository.js — Data-access layer for Invoice documents.
 *
 * Keeps all Mongoose queries out of the service layer.
 * Constructor injection allows tests to pass a mock model.
 */

class InvoiceRepository {
    /**
     * @param {import('mongoose').Model} InvoiceModel
     */
    constructor(InvoiceModel) {
        this.Invoice = InvoiceModel;
    }

    /**
     * Persist a new Invoice document.
     * @param {object} data
     * @returns {Promise<import('../models/Invoice')>}
     */
    async create(data) {
        return this.Invoice.create(data);
    }

    /**
     * Find invoice by its UUID reference number.
     * @param {string} referenceNumber
     * @returns {Promise<import('../models/Invoice') | null>}
     */
    async findByReference(referenceNumber) {
        return this.Invoice.findOne({ referenceNumber });
    }

    /**
     * Check whether a student has any OUTSTANDING invoices.
     * Used by the graduation eligibility check.
     * @param {string} studentId
     * @returns {Promise<boolean>}
     */
    async hasOutstanding(studentId) {
        const count = await this.Invoice.countDocuments({
            studentId,
            status: 'OUTSTANDING',
        });
        return count > 0;
    }

    /**
     * Mark an invoice as PAID by its reference number.
     * @param {string} referenceNumber
     * @returns {Promise<import('../models/Invoice') | null>}
     */
    async markAsPaid(referenceNumber) {
        return this.Invoice.findOneAndUpdate(
            { referenceNumber },
            { status: 'PAID' },
            { returnDocument: 'after' }
        );
    }
}

module.exports = InvoiceRepository;
