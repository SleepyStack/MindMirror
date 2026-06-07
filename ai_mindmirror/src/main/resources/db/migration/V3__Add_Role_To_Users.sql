ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER' NOT NULL;
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@mindmirror.com';