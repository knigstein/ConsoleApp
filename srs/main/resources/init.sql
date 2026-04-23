DROP TABLE IF EXISTS study_groups CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Sequence for auto-increment IDs
DROP SEQUENCE IF EXISTS users_id_seq;
CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 1;

DROP SEQUENCE IF EXISTS study_groups_id_seq;
CREATE SEQUENCE study_groups_id_seq START WITH 1 INCREMENT BY 1;

-- Users table
CREATE TABLE users (
    id INTEGER DEFAULT nextval('users_id_seq') PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Study groups table
CREATE TABLE study_groups (
    id INTEGER DEFAULT nextval('study_groups_id_seq') PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    coordinates_x INTEGER NOT NULL,
    coordinates_y DOUBLE PRECISION NOT NULL,
    creation_date DATE NOT NULL,
    students_count INTEGER NOT NULL,
    expelled_students BIGINT,
    transferred_students INTEGER NOT NULL,
    semester VARCHAR(20),
    admin_name VARCHAR(255) NOT NULL,
    admin_birthday TIMESTAMP NOT NULL,
    admin_eye_color VARCHAR(20),
    admin_nationality VARCHAR(20),
    owner_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_study_groups_owner ON study_groups(owner_id);
CREATE INDEX idx_users_login ON users(login);