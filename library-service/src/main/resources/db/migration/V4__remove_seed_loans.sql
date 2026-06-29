-- V4__remove_seed_loans.sql
-- Remove the demo loans seeded in V2 (20 days old / overdue).
-- Restore available_copies for the affected books.

UPDATE books SET available_copies = total_copies WHERE id IN (1, 7, 10);
DELETE FROM loans;
