\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.24.0'
	WHERE name = 'database_version';

-- add system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('map_extent_name_initial', 'Home');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('speed_limit_min_mph', '45');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('speed_limit_default_mph', '55');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('speed_limit_max_mph', '75');
