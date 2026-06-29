/**
 * invoiceService.test.js — Unit tests for InvoiceService.
 *
 * All repository dependencies are mocked (no real MongoDB connection).
 * Tests verify the service-layer business logic in isolation.
 */

const InvoiceService = require('../src/services/invoiceService');

// ─── Mock repository ──────────────────────────────────────────────────────────
function makeMockRepo(overrides = {}) {
    return {
        create: jest.fn(),
        findByReference: jest.fn(),
        hasOutstanding: jest.fn(),
        markAsPaid: jest.fn(),
        ...overrides,
    };
}

describe('InvoiceService', () => {
    describe('createInvoice', () => {
        it('creates an invoice and returns referenceNumber', async () => {
            const fakeInvoice = {
                referenceNumber: 'abc-123',
                studentId: 'STU-00001',
                amount: 500,
                description: 'Enrolment: CS101',
                status: 'OUTSTANDING',
            };
            const repo = makeMockRepo({ create: jest.fn().mockResolvedValue(fakeInvoice) });
            const service = new InvoiceService(repo);

            const result = await service.createInvoice({
                studentId: 'STU-00001',
                amount: 500,
                description: 'Enrolment: CS101',
            });

            expect(repo.create).toHaveBeenCalledTimes(1);
            expect(result.referenceNumber).toBeDefined();
            expect(result.studentId).toBe('STU-00001');
            expect(result.status).toBe('OUTSTANDING');
        });

        it('throws if studentId is missing', async () => {
            const service = new InvoiceService(makeMockRepo());
            await expect(service.createInvoice({ amount: 100, description: 'test' }))
                .rejects.toThrow('studentId is required');
        });

        it('throws if amount is missing', async () => {
            const service = new InvoiceService(makeMockRepo());
            await expect(service.createInvoice({ studentId: 'STU-1', description: 'test' }))
                .rejects.toThrow('amount is required');
        });

        it('throws if description is missing', async () => {
            const service = new InvoiceService(makeMockRepo());
            await expect(service.createInvoice({ studentId: 'STU-1', amount: 100 }))
                .rejects.toThrow('description is required');
        });
    });

    describe('payInvoice', () => {
        it('marks an OUTSTANDING invoice as PAID', async () => {
            const outstanding = { referenceNumber: 'ref-1', status: 'OUTSTANDING' };
            const paid = { referenceNumber: 'ref-1', status: 'PAID' };
            const repo = makeMockRepo({
                findByReference: jest.fn().mockResolvedValue(outstanding),
                markAsPaid: jest.fn().mockResolvedValue(paid),
            });
            const service = new InvoiceService(repo);

            const result = await service.payInvoice('ref-1');

            expect(repo.markAsPaid).toHaveBeenCalledWith('ref-1');
            expect(result.status).toBe('PAID');
        });

        it('returns existing invoice without calling markAsPaid if already PAID', async () => {
            const paid = { referenceNumber: 'ref-2', status: 'PAID' };
            const repo = makeMockRepo({
                findByReference: jest.fn().mockResolvedValue(paid),
                markAsPaid: jest.fn(),
            });
            const service = new InvoiceService(repo);

            const result = await service.payInvoice('ref-2');

            expect(repo.markAsPaid).not.toHaveBeenCalled();
            expect(result.status).toBe('PAID');
        });

        it('throws if invoice does not exist', async () => {
            const repo = makeMockRepo({ findByReference: jest.fn().mockResolvedValue(null) });
            const service = new InvoiceService(repo);
            await expect(service.payInvoice('nonexistent')).rejects.toThrow('Invoice not found');
        });
    });

    describe('getBalance', () => {
        it('returns hasOutstanding: true when student has unpaid invoices', async () => {
            const repo = makeMockRepo({ hasOutstanding: jest.fn().mockResolvedValue(true) });
            const service = new InvoiceService(repo);

            const result = await service.getBalance('STU-00001');

            expect(result).toEqual({ studentId: 'STU-00001', hasOutstanding: true });
        });

        it('returns hasOutstanding: false when all invoices paid', async () => {
            const repo = makeMockRepo({ hasOutstanding: jest.fn().mockResolvedValue(false) });
            const service = new InvoiceService(repo);

            const result = await service.getBalance('STU-00001');

            expect(result).toEqual({ studentId: 'STU-00001', hasOutstanding: false });
        });

        it('throws if studentId is missing', async () => {
            const service = new InvoiceService(makeMockRepo());
            await expect(service.getBalance('')).rejects.toThrow('studentId is required');
        });
    });
});
