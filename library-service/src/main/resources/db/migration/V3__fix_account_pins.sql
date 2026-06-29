-- V3__fix_account_pins.sql
-- Fix: replace incorrect BCrypt hashes in V2 seed data with verified hashes.
-- Verified with BCryptPasswordEncoder(12): VERIFY_000000=true, VERIFY_123456=true

UPDATE accounts SET hashed_pin = '$2a$12$5SAOG1sUTWDbZcvnvddgj.2mcEYa4H35V7nkcus6zrGlJ4eM0l8Wm'
WHERE student_id = 'admin';

UPDATE accounts SET hashed_pin = '$2a$12$BDS4Q53IaWsJR2Y7B5V2zOwZjoCVmY16ypVrOes5swiE.dq5BaqoK'
WHERE student_id IN ('STU-00001', 'STU-00002', 'STU-00003');
