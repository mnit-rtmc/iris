\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

UPDATE iris.system_attribute SET value = '4.57.0'
	WHERE name = 'database_version';

COMMIT;
