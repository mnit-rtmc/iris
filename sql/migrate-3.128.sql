\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.128.0'
	WHERE name = 'database_version';

DROP TABLE iris.map_extent;

CREATE TABLE iris.map_extent (
	name VARCHAR(20) PRIMARY KEY,
	lon real NOT NULL,
	lat real NOT NULL,
	zoom INTEGER NOT NULL
);
