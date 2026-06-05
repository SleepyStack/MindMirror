CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
--SEED ADMIN with pass adminPassword123
INSERT INTO users(username, email, password, created_at, updated_at)
VALUES (
        'admin',
        'admin@mindmirror.com',
        '$2a$10$R7MvEGEc/F6OaefzBvSSTuN7r1A02v6FhIqN8XVE9aU0Vj6hPZbe2',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
       )