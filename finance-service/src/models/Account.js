/**
 * Account.js — Mongoose schema for finance accounts.
 *
 * MongoDB / document model chosen for the Finance service because:
 *   - Invoice and account data is document-shaped (no relational joins needed)
 *   - Demonstrates intentional NoSQL vs SQL contrast across microservices
 *   - Flexible schema suits future extension without migrations
 *
 * Design decision: studentId is indexed and unique — one account per student.
 */

const mongoose = require('mongoose');

const accountSchema = new mongoose.Schema(
    {
        /**
         * Unique student identifier, e.g. STU-12345.
         * Supplied by the Student service when a student first enrols.
         */
        studentId: {
            type: String,
            required: [true, 'studentId is required'],
            unique: true,
            trim: true,
        },
    },
    {
        // Automatically adds createdAt and updatedAt fields
        timestamps: { createdAt: 'createdAt', updatedAt: false },
    }
);

module.exports = mongoose.model('Account', accountSchema);
