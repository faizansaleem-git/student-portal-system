/**
 * invoiceRoutes.test.js — Integration tests for invoice REST endpoints.
 *
 * Uses mongodb-memory-server so tests run against a real (in-memory)
 * MongoDB instance without touching a real database or Docker.
 * Supertest makes real HTTP requests to the Express app.
 */

const { MongoMemoryServer } = require('mongodb-memory-server');
const mongoose = require('mongoose');
const request = require('supertest');

// We need to connect mongoose before importing app, so we build the app
// after connecting. app.js skips start() when not require.main, so importing
// it is safe — it just exports the configured Express app.
let mongod;
let app;

beforeAll(async () => {
    // Start in-memory MongoDB and connect Mongoose to it
    mongod = await MongoMemoryServer.create();
    const uri = mongod.getUri();
    process.env.MONGO_URI = uri;
    await mongoose.connect(uri);

    // Import app after env var is set so connectDB uses the test URI
    app = require('../src/app');
});

afterAll(async () => {
    await mongoose.disconnect();
    await mongod.stop();
});

afterEach(async () => {
    // Clean collections between tests for isolation
    const collections = mongoose.connection.collections;
    for (const key in collections) {
        await collections[key].deleteMany({});
    }
});

describe('POST /api/accounts', () => {
    it('creates an account and returns 201', async () => {
        const res = await request(app)
            .post('/api/accounts')
            .send({ studentId: 'STU-00001' });

        expect(res.status).toBe(201);
        expect(res.body.studentId).toBe('STU-00001');
        expect(res.body.accountId).toBeDefined();
    });

    it('returns 201 for duplicate studentId (idempotent)', async () => {
        await request(app).post('/api/accounts').send({ studentId: 'STU-00002' });
        const res = await request(app).post('/api/accounts').send({ studentId: 'STU-00002' });
        expect(res.status).toBe(201);
    });

    it('returns 400 when studentId is missing', async () => {
        const res = await request(app).post('/api/accounts').send({});
        expect(res.status).toBe(400);
    });
});

describe('POST /api/invoices', () => {
    it('creates an invoice and returns referenceNumber', async () => {
        const res = await request(app)
            .post('/api/invoices')
            .send({ studentId: 'STU-00001', amount: 500, description: 'Enrolment: CS101' });

        expect(res.status).toBe(201);
        expect(res.body.referenceNumber).toBeDefined();
        expect(res.body.status).toBe('OUTSTANDING');
        expect(res.body.studentId).toBe('STU-00001');
    });

    it('returns 400 when required fields are missing', async () => {
        const res = await request(app)
            .post('/api/invoices')
            .send({ studentId: 'STU-00001' });
        expect(res.status).toBe(400);
    });
});

describe('GET /api/invoices/:reference', () => {
    it('returns the invoice by reference', async () => {
        const created = await request(app)
            .post('/api/invoices')
            .send({ studentId: 'STU-00001', amount: 250, description: 'Test fee' });

        const ref = created.body.referenceNumber;
        const res = await request(app).get(`/api/invoices/${ref}`);

        expect(res.status).toBe(200);
        expect(res.body.referenceNumber).toBe(ref);
    });

    it('returns 404 for unknown reference', async () => {
        const res = await request(app).get('/api/invoices/does-not-exist');
        expect(res.status).toBe(404);
    });
});

describe('GET /api/accounts/:studentId/balance', () => {
    it('returns hasOutstanding: true when there are outstanding invoices', async () => {
        await request(app)
            .post('/api/invoices')
            .send({ studentId: 'STU-00003', amount: 500, description: 'Fee' });

        const res = await request(app).get('/api/accounts/STU-00003/balance');

        expect(res.status).toBe(200);
        expect(res.body.hasOutstanding).toBe(true);
    });

    it('returns hasOutstanding: false when no invoices exist', async () => {
        const res = await request(app).get('/api/accounts/STU-NONE/balance');
        expect(res.status).toBe(200);
        expect(res.body.hasOutstanding).toBe(false);
    });
});

describe('POST /api/invoices/:reference/pay', () => {
    it('marks invoice as PAID', async () => {
        const created = await request(app)
            .post('/api/invoices')
            .send({ studentId: 'STU-00004', amount: 100, description: 'Fine' });

        const ref = created.body.referenceNumber;
        const res = await request(app).post(`/api/invoices/${ref}/pay`);

        expect(res.status).toBe(200);
        expect(res.body.status).toBe('PAID');
    });

    it('returns 404 for unknown reference', async () => {
        const res = await request(app).post('/api/invoices/nonexistent/pay');
        expect(res.status).toBe(404);
    });
});
