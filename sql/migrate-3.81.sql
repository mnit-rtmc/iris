\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

ALTER TABLE font ADD COLUMN f_number INTEGER;
UPDATE font SET f_number = 1;
ALTER TABLE font ALTER COLUMN f_number SET NOT NULL;

UPDATE system_attribute SET value = '3.81.0' WHERE name = 'database_version';
