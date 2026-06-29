/**
 * Invoice.js — Mongoose schema for student invoices.
 *
 * Each invoice represents a financial obligation (e.g. course enrolment fee,
 * library fine). Status flows: OUTSTANDING → PAID.
 *
 * referenceNumber is a UUID v4 generated at creation time, used as the
 * human-readable identifier passed back to the Student service.
 */

const mongoose = require('mongoose');

const invoiceSchema = new mongoose.Schema(
    {
        /**
         * UUID-based reference, e.g. "a3f2e1d0-...".
         * Returned to callers so they can store and look it up later.
         */
        referenceNumber: {
            type: String,
            required: true,
            unique: true,
            trim: true,
        },

        /** The student this invoice belongs to. */
        studentId: {
            type: String,
            required: [true, 'studentId is required'],
            trim: true,
        },

        /** Amount in GBP, stored as a decimal number. */
        amount: {
            type: Number,
            required: [true, 'amount is required'],
            min: [0, 'amount must be non-negative'],
        },

        /** Human-readable reason, e.g. "Enrolment: CS101" or "Library fine - Clean Code". */
        description: {
            type: String,
            required: [true, 'description is required'],
            trim: true,
        },

        /**
         * Payment status. Only two valid states:
         *   OUTSTANDING — invoice not yet paid
         *   PAID        — invoice settled
         */
        status: {
            type: String,
            enum: ['OUTSTANDING', 'PAID'],
            default: 'OUTSTANDING',
        },
    },
    {
        timestamps: { createdAt: 'createdAt', updatedAt: false },
    }
);

module.exports = mongoose.model('Invoice', invoiceSchema);
