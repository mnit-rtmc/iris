\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.47.0'
	WHERE name = 'database_version';

-- change controller password to VARCHAR(32)
ALTER TABLE iris.controller ADD COLUMN pwd VARCHAR(32);
UPDATE iris.controller SET pwd = password;
ALTER TABLE iris.controller DROP COLUMN password;
ALTER TABLE iris.controller ADD COLUMN password VARCHAR(32);
UPDATE iris.controller SET password = pwd;
ALTER TABLE iris.controller DROP COLUMN pwd;
