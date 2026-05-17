-- Fix inconsistent role names in database
-- DB hiện có: USER, STORE_OWNER, ROLE_ADMIN, ROLE_MODERATOR
-- Code expects: ROLE_USER, ROLE_STORE_OWNER, ROLE_ADMIN, ROLE_MODERATOR

UPDATE roles SET name = 'ROLE_USER' WHERE name = 'USER';
UPDATE roles SET name = 'ROLE_STORE_OWNER' WHERE name = 'STORE_OWNER';

-- Verify
SELECT role_id, name, description FROM roles;
