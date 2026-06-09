CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) DEFAULT 'USER' NOT NULL,
                       created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
--SEED ADMIN with pass adminPassword123
INSERT INTO users (username, email, password, role, created_at, updated_at)
VALUES (
           'admin',
           'admin@mindmirror.com',
           '$2a$10$ZjMh8g3h4.QTXXjRpXREhO/xpSIhx9Ss4o5on6Ldc7Q4ut5hY2Rgi',
           'ADMIN',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       );