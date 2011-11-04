\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.137.0'
	WHERE name = 'database_version';

ALTER TABLE iris.holiday DROP COLUMN period;
