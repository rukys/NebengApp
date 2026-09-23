-- ==============================================================================
-- MIGRATION: 005_make_phone_nullable.sql
-- Drop NOT NULL constraint on users.phone_number to allow Google Sign-In users
-- without phone number to be saved, until they complete their profile later.
-- ==============================================================================

ALTER TABLE users ALTER COLUMN phone_number DROP NOT NULL;
