INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Password is 'password' (verified BCrypt hash)
INSERT INTO users (username, email, password, full_name, is_active, created_at, updated_at) 
VALUES ('user', 'user@fintrack.com', '$2a$10$7qOFjBmPQaUw6LVSYknheuAn.drVunEO9kq3jXVrMifjyy2KSlqjq', 'Test User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) 
VALUES (
  (SELECT id FROM users WHERE username = 'user'), 
  (SELECT id FROM roles WHERE name = 'ROLE_USER')
);

-- Pre-populate some income and expense data
INSERT INTO income (user_id, amount, category, description, transaction_date, is_deleted, created_at, updated_at)
VALUES (
  (SELECT id FROM users WHERE username = 'user'), 
  5000.00, 'Salary', 'Monthly salary payment', CURRENT_DATE, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO income (user_id, amount, category, description, transaction_date, is_deleted, created_at, updated_at)
VALUES (
  (SELECT id FROM users WHERE username = 'user'), 
  450.00, 'Freelance', 'Web design contract work', CURRENT_DATE - 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO expense (user_id, amount, category, description, transaction_date, is_deleted, created_at, updated_at)
VALUES (
  (SELECT id FROM users WHERE username = 'user'), 
  120.00, 'Food', 'Weekly groceries shopping', CURRENT_DATE, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO expense (user_id, amount, category, description, transaction_date, is_deleted, created_at, updated_at)
VALUES (
  (SELECT id FROM users WHERE username = 'user'), 
  45.00, 'Bills', 'Mobile bill payment', CURRENT_DATE - 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO expense (user_id, amount, category, description, transaction_date, is_deleted, created_at, updated_at)
VALUES (
  (SELECT id FROM users WHERE username = 'user'), 
  350.00, 'Shopping', 'Bought a new office chair', CURRENT_DATE - 10, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Pre-populate budgets
INSERT INTO budget (user_id, category, limit_amount, spent_amount, start_date, end_date, is_deleted, created_at, updated_at)
VALUES (
  (SELECT id FROM users WHERE username = 'user'), 
  'Food', 500.00, 120.00, CURRENT_DATE - 15, CURRENT_DATE + 15, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO budget (user_id, category, limit_amount, spent_amount, start_date, end_date, is_deleted, created_at, updated_at)
VALUES (
  (SELECT id FROM users WHERE username = 'user'), 
  'ALL', 2000.00, 515.00, CURRENT_DATE - 15, CURRENT_DATE + 15, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
