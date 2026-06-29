-- Admin user: username=admin, password=admin123
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN');

-- 12 university courses across departments
INSERT INTO courses (code, title, department, credits, description) VALUES
('CS101', 'Introduction to Computer Science', 'Computer Science', 20, 'Fundamentals of programming, algorithms, and computational thinking.'),
('CS201', 'Data Structures and Algorithms', 'Computer Science', 20, 'Arrays, linked lists, trees, graphs, sorting and searching algorithms.'),
('CS301', 'Software Engineering', 'Computer Science', 20, 'Software development lifecycle, design patterns, and agile methodologies.'),
('CS401', 'Microservices Architecture', 'Computer Science', 20, 'Service-oriented architecture, REST APIs, Docker, and cloud deployment.'),
('ENG101', 'Engineering Mathematics', 'Engineering', 20, 'Calculus, linear algebra, and differential equations for engineers.'),
('ENG201', 'Structural Engineering', 'Engineering', 20, 'Principles of stress, strain, and structural analysis of beams and frames.'),
('ENG301', 'Electrical Circuits', 'Engineering', 20, 'DC and AC circuit analysis, Kirchhoff laws, and network theorems.'),
('ENG401', 'Thermodynamics', 'Engineering', 20, 'Laws of thermodynamics, heat transfer, and energy conversion systems.'),
('BUS101', 'Business Management', 'Business', 20, 'Introduction to management principles, organisational behaviour, and strategy.'),
('BUS201', 'Financial Accounting', 'Business', 20, 'Double-entry bookkeeping, financial statements, and accounting standards.'),
('HIS101', 'World History', 'History', 20, 'Survey of major civilisations and events from antiquity to the modern era.'),
('HIS201', 'Modern British History', 'History', 20, 'Britain from the Industrial Revolution to the present day.');
