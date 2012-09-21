\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.150.0'
	WHERE name = 'database_version';

ALTER TABLE iris.i_user ADD COLUMN password VARCHAR(64);
UPDATE iris.i_user SET password = '';
ALTER TABLE iris.i_user ALTER COLUMN password SET NOT NULL;
