/**
 * accountService.js — Business logic for Finance accounts.
 *
 * Design decision: service layer classes use constructor-based dependency
 * passing to mirror the DI pattern used in the Spring Boot services.
 * This makes the service fully testable without touching Mongoose or MongoDB.
 */

class AccountService {
    /**
     * @param {import('../repositories/accountRepository')} accountRepository
     *
     * Constructor injection: dependencies declared explicitly rather than
     * imported directly, enabling test doubles to be substituted easily.
     */
    constructor(accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Create a new finance account for a student.
     * Called by Student service on first enrolment.
     *
     * Idempotent: if an account already exists for the studentId,
     * return the existing one rather than throwing a duplicate-key error.
     *
     * @param {string} studentId
     * @returns {Promise<{accountId: string, studentId: string}>}
     */
    async createAccount(studentId) {
        if (!studentId || typeof studentId !== 'string') {
            throw new Error('studentId must be a non-empty string');
        }

        // Check for existing account — return it rather than error (idempotent)
        const existing = await this.accountRepository.findByStudentId(studentId.trim());
        if (existing) {
            return { accountId: existing._id.toString(), studentId: existing.studentId };
        }

        const account = await this.accountRepository.create({ studentId: studentId.trim() });
        return { accountId: account._id.toString(), studentId: account.studentId };
    }
}

module.exports = AccountService;
