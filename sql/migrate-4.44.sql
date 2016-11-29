\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.44.0'
	WHERE name = 'database_version';

UPDATE iris.meter_algorithm SET description = 'SZM (obsolete)' WHERE id = 2;
