\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.50.0'
	WHERE name = 'database_version';

-- Add camera condition URLs
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_construction_url', '');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_out_of_service_url', '');
