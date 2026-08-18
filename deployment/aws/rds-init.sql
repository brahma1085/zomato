-- AWS RDS PostgreSQL Multi-Tenant Initialization Script for Zomato UC
-- Run this script once after creating the RDS PostgreSQL instance

-- Create databases for each service
CREATE DATABASE user_db;
CREATE DATABASE restaurant_db;
CREATE DATABASE review_db;
CREATE DATABASE recommendation_db;
CREATE DATABASE keycloak_db;

-- Connect to recommendation_db and enable pgvector
\c recommendation_db;
CREATE EXTENSION IF NOT EXISTS vector;

-- Grant permissions if custom application users are used
-- GRANT ALL PRIVILEGES ON DATABASE user_db TO admin;
-- GRANT ALL PRIVILEGES ON DATABASE restaurant_db TO admin;
-- GRANT ALL PRIVILEGES ON DATABASE review_db TO admin;
-- GRANT ALL PRIVILEGES ON DATABASE recommendation_db TO admin;
-- GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO admin;
