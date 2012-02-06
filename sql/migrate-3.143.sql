\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.143.0'
	WHERE name = 'database_version';

INSERT INTO iris.meter_algorithm (id, description)
	VALUES (3, 'K Adaptive Metering');
