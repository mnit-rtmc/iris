\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.37.0'
	WHERE name = 'database_version';

-- Add camera_auth_ system attributes
INSERT INTO iris.system_attribute('camera_auth_password', '');
INSERT INTO iris.system_attribute('camera_auth_username', '');
