/**
 * accountRepository.js — Data-access layer for Account documents.
 *
 * All Mongoose queries are isolated here so that services remain
 * database-agnostic and easy to unit-test by mocking this module.
 *
 * Constructor-based dependency injection pattern: the model is passed in,
 * mirroring the DI principle used in the Spring Boot services.
 */

class AccountRepository {
    /**
     * @param {import('mongoose').Model} AccountModel — the Mongoose Account model.
     *
     * Constructor injection: makes the dependency explicit and allows tests
     * to supply a mock model without touching the module system.
     */
    constructor(AccountModel) {
        this.Account = AccountModel;
    }

    /**
     * Persist a new Account document.
     * @param {{ studentId: string }} data
     * @returns {Promise<import('../models/Account')>}
     */
    async create(data) {
        return this.Account.create(data);
    }

    /**
     * Find account by studentId.
     * @param {string} studentId
     * @returns {Promise<import('../models/Account') | null>}
     */
    async findByStudentId(studentId) {
        return this.Account.findOne({ studentId });
    }
}

module.exports = AccountRepository;
