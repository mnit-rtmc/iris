\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.150.0'
	WHERE name = 'database_version';

ALTER TABLE iris.i_user ADD COLUMN pwd_hash VARCHAR(64);
UPDATE iris.i_user SET pwd_hash = '';
ALTER TABLE iris.i_user ALTER COLUMN pwd_hash SET NOT NULL;
