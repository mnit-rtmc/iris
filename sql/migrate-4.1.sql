\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.1.0'
	WHERE name = 'database_version';

UPDATE iris.comm_protocol SET description = 'RTMS G4' WHERE id = 17;
UPDATE iris.comm_protocol SET description = 'RTMS' WHERE id = 18;
