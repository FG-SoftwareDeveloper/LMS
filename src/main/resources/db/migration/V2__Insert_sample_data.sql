-- Insert sample data for LMS application
-- V2__Insert_sample_data.sql

-- Insert sample users
INSERT INTO users (id, email, password_hash, first_name, last_name, username, role, is_active, is_verified, email_verified_at, last_login, created_at, updated_at) VALUES 
(1, 'admin@learnhub.com', '$2a$10$N.zmdr9k7uOCQb07YxWiguim8U0H2BG7SDv1VgvAQbyOJ0KTcnEt2', 'System', 'Administrator', 'admin', 'ADMIN', true, true, '2024-01-01 10:00:00', '2024-12-01 09:00:00', '2024-01-01 10:00:00', '2024-12-01 09:00:00'),
(2, 'john.instructor@learnhub.com', '$2a$10$8XNNTYOAdl6TK2.QNjG6jOGJTLlzMf6jNgCEf5L3LtDjgaV2zHDWm', 'John', 'Smith', 'john_instructor', 'INSTRUCTOR', true, true, '2024-01-15 14:30:00', '2024-12-01 08:45:00', '2024-01-15 14:30:00', '2024-12-01 08:45:00'),
(3, 'sarah.wilson@learnhub.com', '$2a$10$vJzLnI3mwMqKr5F8Rp2jzOeB4Hs9V6Nq1Lr8Et3Ku7Gw2Mp0Xc5Df', 'Sarah', 'Wilson', 'sarah_wilson', 'INSTRUCTOR', true, true, '2024-01-20 16:15:00', '2024-11-30 19:20:00', '2024-01-20 16:15:00', '2024-11-30 19:20:00'),
(4, 'student1@example.com', '$2a$10$mK4nL8rE2Qs6Wt9Yv3Bp0OaI5Jc7Dx1Fg8Hu2Zk9Nl6Mp3Qr4St', 'Michael', 'Johnson', 'student1', 'STUDENT', true, true, '2024-02-01 12:00:00', '2024-12-01 18:30:00', '2024-02-01 12:00:00', '2024-12-01 18:30:00'),
(5, 'emma.davis@example.com', '$2a$10$pL9sM7nR4Tv5Xu8Zw6Cq1PbJ6Kd8Ex2Gh9Iv3Zl0Om7Nq4Qr5Su', 'Emma', 'Davis', 'emma_davis', 'STUDENT', true, true, '2024-02-05 09:45:00', '2024-11-29 21:15:00', '2024-02-05 09:45:00', '2024-11-29 21:15:00'),
(6, 'alex.brown@example.com', '$2a$10$qM0tN8oS5Uw6Yv9Zx7Dr2QcK7Le9Fy3Hi0Jw4Zm1Pn8Or5Qs6Tv', 'Alex', 'Brown', 'alex_brown', 'STUDENT', true, true, '2024-02-10 11:20:00', '2024-12-01 16:45:00', '2024-02-10 11:20:00', '2024-12-01 16:45:00'),
(7, 'test.user@example.com', '$2a$10$rN1uO9pT6Vx7Zw0Ay8Es3RdL8Mf0Gz4Ij1Kx5Zn2Qo9Ps6Qt7Uw', 'Test', 'User', 'testuser', 'STUDENT', true, true, '2024-03-01 10:30:00', '2024-12-01 20:00:00', '2024-03-01 10:30:00', '2024-12-01 20:00:00');

-- Reset sequence for users
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- Insert sample courses
INSERT INTO courses (id, title, description, instructor_id, category, difficulty_level, price, duration_hours, enrollment_count, rating, thumbnail_url, image_url, is_published) VALUES
(1, 'Java Fundamentals', 'Learn the basics of Java programming with comprehensive examples and hands-on exercises.', 2, 'Programming', 'BEGINNER', 99.99, 40, 125, 4.5, '/images/courses/java-course.jpg', '/images/courses/java-course.jpg', true),
(2, 'Advanced Spring Boot', 'Master Spring Boot framework development with advanced techniques and best practices.', 2, 'Framework', 'ADVANCED', 149.99, 60, 87, 4.8, '/images/courses/spring-course.jpg', '/images/courses/spring-course.jpg', true),
(3, 'Introduction to Programming', 'Learn programming fundamentals with multiple languages and programming concepts.', 3, 'Programming', 'BEGINNER', 0.00, 30, 245, 4.3, '/images/courses/programming-course.jpg', '/images/courses/programming-course.jpg', true),
(4, 'JavaScript & Web Development', 'Build modern web applications with JavaScript, HTML, CSS and popular frameworks.', 3, 'Web Development', 'INTERMEDIATE', 79.99, 45, 189, 4.6, '/images/courses/javascript-course.jpg', '/images/courses/javascript-course.jpg', true),
(5, 'Cybersecurity Fundamentals', 'Learn essential cybersecurity concepts and protect digital assets from modern threats.', 2, 'Security', 'INTERMEDIATE', 129.99, 35, 76, 4.7, '/images/courses/security-course.jpg', '/images/courses/security-course.jpg', true),
(6, 'Mobile App Development', 'Create cross-platform mobile applications using modern frameworks and development tools.', 3, 'Mobile Development', 'ADVANCED', 159.99, 55, 98, 4.4, '/images/courses/mobile-course.jpg', '/images/courses/mobile-course.jpg', true),
(7, 'UI/UX Design Principles', 'Master user interface and user experience design with practical projects and industry insights.', 3, 'Design', 'BEGINNER', 0.00, 25, 156, 4.2, '/images/courses/design-course.jpg', '/images/courses/design-course.jpg', true),
(8, 'Project Management', 'Learn project management methodologies, tools, and leadership skills for successful project delivery.', 2, 'Management', 'INTERMEDIATE', 89.99, 40, 112, 4.5, '/images/courses/pm-course.jpg', '/images/courses/pm-course.jpg', true);

-- Reset sequence for courses
SELECT setval('courses_id_seq', (SELECT MAX(id) FROM courses));

-- Insert sample vouchers
INSERT INTO vouchers (code, discount_type, discount_amount, max_uses, valid_from, valid_until, is_active) VALUES
('WELCOME10', 'PERCENTAGE', 10.00, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', true),
('STUDENT20', 'PERCENTAGE', 20.00, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '60 days', true),
('FREESHIP', 'FIXED', 15.00, 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days', true);