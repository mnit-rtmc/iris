\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.11.0'
	WHERE name = 'database_version';

DELETE FROM iris.meter_algorithm WHERE id = 4;
