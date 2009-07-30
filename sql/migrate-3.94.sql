\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.94.0' WHERE name = 'database_version';

CREATE TABLE iris.map_extent (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES geo_loc(name)
);
