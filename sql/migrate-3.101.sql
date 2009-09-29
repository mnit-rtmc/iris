\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.101.0' WHERE name = 'database_version';

INSERT INTO iris.lane_use_indication (id, description)
	VALUES (14, 'HOV / HOT begins');

CREATE TABLE iris.system_attribute (
	name VARCHAR(32) PRIMARY KEY,
	value VARCHAR(64) NOT NULL
);

INSERT INTO iris.system_attribute (name, value)
	(SELECT name, value FROM system_attribute);

DROP TABLE system_attribute;
