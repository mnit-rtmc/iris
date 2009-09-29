\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.101.0' WHERE name = 'database_version';

INSERT INTO iris.lane_use_indication (id, description)
	VALUES (14, 'HOV / HOT begins');
