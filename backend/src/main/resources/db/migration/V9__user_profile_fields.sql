ALTER TABLE users
    ADD COLUMN date_of_birth  DATE,
    ADD COLUMN gender         VARCHAR(20) CHECK (gender IN ('FEMALE', 'MALE', 'OTHER')),
    ADD COLUMN last_login_at  TIMESTAMPTZ,
    ADD COLUMN token_version  INTEGER NOT NULL DEFAULT 0;

CREATE INDEX idx_users_role ON users(role);
