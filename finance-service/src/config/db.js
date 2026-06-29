/**
 * db.js — Mongoose connection configuration.
 *
 * Reads the MongoDB URI from the MONGO_URI environment variable so that
 * the connection string is never hardcoded (12-factor app principle).
 * Falls back to a local default only for developer convenience.
 */

const mongoose = require('mongoose');

/**
 * Connect to MongoDB using the URI supplied via environment variable.
 * Retries are handled automatically by the Mongoose driver.
 *
 * @returns {Promise<void>}
 */
async function connectDB() {
    const uri = process.env.MONGO_URI || 'mongodb://localhost:27017/financedb';

    try {
        await mongoose.connect(uri);
        console.log(`[DB] MongoDB connected: ${uri}`);
    } catch (err) {
        console.error('[DB] MongoDB connection error:', err.message);
        // Exit so Docker can restart the container once the DB is ready
        process.exit(1);
    }
}

module.exports = connectDB;
