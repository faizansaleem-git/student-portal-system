-- V2__seed_data.sql
-- Library Service seed data
-- PINs are BCrypt hashes:
--   "123456" hash (admin)
--   "000000" hash (students — first login forces PIN change)

INSERT INTO accounts (student_id, hashed_pin, is_first_login) VALUES
('admin',     '$2a$12$1eALyIYjLJWmXrIFnXn7/OBaGPBFR3uVVl5nC6E0eTPE0gZMJA/7m', 0),
('STU-00001', '$2a$12$k8vZmN5nEuJdz7KL9tCnZOCH0y.TlxJt0lWfF4iCJiZaOm/RXEaXG', 1),
('STU-00002', '$2a$12$k8vZmN5nEuJdz7KL9tCnZOCH0y.TlxJt0lWfF4iCJiZaOm/RXEaXG', 1),
('STU-00003', '$2a$12$k8vZmN5nEuJdz7KL9tCnZOCH0y.TlxJt0lWfF4iCJiZaOm/RXEaXG', 1);

INSERT INTO books (isbn, title, author, total_copies, available_copies) VALUES
('9780132350884', 'Clean Code',                           'Robert C. Martin',    4, 4),
('9780201633610', 'Design Patterns',                      'Gang of Four',        3, 3),
('9780596007126', 'Head First Design Patterns',           'Eric Freeman',        3, 3),
('9780131103627', 'The C Programming Language',           'Brian W. Kernighan',  3, 3),
('9780201485677', 'The Pragmatic Programmer',             'Andrew Hunt',         4, 4),
('9780321125217', 'Domain-Driven Design',                 'Eric Evans',          3, 3),
('9780134685991', 'Effective Java',                       'Joshua Bloch',        4, 4),
('9780131495050', 'Working Effectively with Legacy Code', 'Michael C. Feathers', 3, 3),
('9780201485694', 'Refactoring',                          'Martin Fowler',       3, 3),
('9781491950357', 'Building Microservices',               'Sam Newman',          4, 4),
('9780596517748', 'JavaScript: The Good Parts',           'Douglas Crockford',   3, 3),
('9781491910726', 'You Don''t Know JS',                   'Kyle Simpson',        3, 3),
('9780596806750', 'JavaScript: The Definitive Guide',     'David Flanagan',      3, 3),
('9780321714114', 'The Clean Coder',                      'Robert C. Martin',    3, 3),
('9780137081073', 'The Art of Agile Development',         'James Shore',         3, 3),
('9780134494166', 'Clean Architecture',                   'Robert C. Martin',    3, 3),
('9780321127426', 'Patterns of Enterprise Application',   'Martin Fowler',       3, 3),
('9780321146533', 'Test Driven Development',              'Kent Beck',           3, 3),
('9780596009205', 'Head First Java',                      'Kathy Sierra',        4, 4),
('9780135957059', 'Modern Software Engineering',          'David Farley',        3, 3);

-- 3 loans borrowed 20 days ago (overdue — will generate fines when returned)
-- accounts row order: id=1 admin, id=2 STU-00001, id=3 STU-00002, id=4 STU-00003
-- books row order: id=1 Clean Code, id=7 Effective Java, id=10 Building Microservices
INSERT INTO loans (account_id, book_id, borrowed_at, returned_at, is_overdue) VALUES
(2, 1,  DATE_SUB(NOW(), INTERVAL 20 DAY), NULL, 0),
(3, 7,  DATE_SUB(NOW(), INTERVAL 20 DAY), NULL, 0),
(4, 10, DATE_SUB(NOW(), INTERVAL 20 DAY), NULL, 0);

-- Reduce available_copies for the 3 active loans
UPDATE books SET available_copies = available_copies - 1 WHERE id IN (1, 7, 10);
