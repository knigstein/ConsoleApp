DROP TABLE IF EXISTS study_group_admins CASCADE;
DROP TABLE IF EXISTS study_group_semesters CASCADE;
DROP TABLE IF EXISTS study_group_student_stats CASCADE;
DROP TABLE IF EXISTS study_group_creation_dates CASCADE;
DROP TABLE IF EXISTS study_group_coordinates CASCADE;
DROP TABLE IF EXISTS study_group_names CASCADE;
DROP TABLE IF EXISTS study_group_owners CASCADE;
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Study group owner (1:1 with study_groups)
CREATE TABLE study_group_owners (
    study_group_id INTEGER PRIMARY KEY REFERENCES study_groups(id) ON DELETE CASCADE,
    owner_id INTEGER REFERENCES users(id) ON DELETE SET NULL
);

-- Study group name (1:1 with study_groups)
CREATE TABLE study_group_names (
    study_group_id INTEGER PRIMARY KEY REFERENCES study_groups(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL
);

-- Study group coordinates (1:1 with study_groups)
CREATE TABLE study_group_coordinates (
    study_group_id INTEGER PRIMARY KEY REFERENCES study_groups(id) ON DELETE CASCADE,
    x INTEGER NOT NULL,
    y DOUBLE PRECISION NOT NULL
);

-- Study group creation date (1:1 with study_groups)
CREATE TABLE study_group_creation_dates (
    study_group_id INTEGER PRIMARY KEY REFERENCES study_groups(id) ON DELETE CASCADE,
    creation_date DATE NOT NULL
);

-- Study group student stats (1:1 with study_groups)
CREATE TABLE study_group_student_stats (
    study_group_id INTEGER PRIMARY KEY REFERENCES study_groups(id) ON DELETE CASCADE,
    students_count INTEGER NOT NULL,
    expelled_students BIGINT,
    transferred_students INTEGER NOT NULL
);

-- Study group semester (1:1 with study_groups)
CREATE TABLE study_group_semesters (
    study_group_id INTEGER PRIMARY KEY REFERENCES study_groups(id) ON DELETE CASCADE,
    semester VARCHAR(20)
);

-- Study group admin (1:1 with study_groups)
CREATE TABLE study_group_admins (
    study_group_id INTEGER PRIMARY KEY REFERENCES study_groups(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    birthday TIMESTAMP NOT NULL,
    eye_color VARCHAR(20),
    nationality VARCHAR(20)
);

-- Indexes
CREATE INDEX idx_users_login ON users(login);
CREATE INDEX idx_owner_user ON study_group_owners(owner_id);
CREATE INDEX idx_name_study_group ON study_group_names(study_group_id);
CREATE INDEX idx_coords_study_group ON study_group_coordinates(study_group_id);
CREATE INDEX idx_dates_study_group ON study_group_creation_dates(study_group_id);
CREATE INDEX idx_stats_study_group ON study_group_student_stats(study_group_id);
CREATE INDEX idx_semester_study_group ON study_group_semesters(study_group_id);
CREATE INDEX idx_admins_study_group ON study_group_admins(study_group_id);
