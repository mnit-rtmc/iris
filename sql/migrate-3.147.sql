\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.147.0'
	WHERE name = 'database_version';

INSERT INTO iris.system_attribute (name, value) VALUES ('client_units_si', 'true');
DELETE FROM iris.system_attribute WHERE name = 'temp_fahrenheit_enable';
