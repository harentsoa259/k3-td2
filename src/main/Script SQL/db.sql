CREATE DATABASE mini_football_db;

CREATE USER mini_football_db_manager WITH PASSWORD 'password';

-- Privilèges
GRANT ALL PRIVILEGES ON DATABASE mini_football_db TO mini_football_db_manager;
