\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.102.0'
	WHERE name = 'database_version';

INSERT INTO iris.lane_type (id, description, dcode)
	VALUES (16, 'Shoulder', 'D');
