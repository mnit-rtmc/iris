\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.42.0'
	WHERE name = 'database_version';

-- Add owner column to sign_message table
ALTER TABLE iris.sign_message ADD COLUMN owner VARCHAR(15);
