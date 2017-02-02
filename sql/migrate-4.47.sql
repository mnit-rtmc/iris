\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.47.0'
	WHERE name = 'database_version';

-- delete camera_auth_ system attributes
DELETE FROM iris.system_attribute WHERE name = 'camera_auth_password';
DELETE FROM iris.system_attribute WHERE name = 'camera_auth_username';
