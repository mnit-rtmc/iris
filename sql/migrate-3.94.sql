\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.94.0' WHERE name = 'database_version';

CREATE TABLE iris.map_extent (
	name VARCHAR(20) PRIMARY KEY,
	easting INTEGER NOT NULL,
	east_off INTEGER NOT NULL,
	northing INTEGER NOT NULL,
	north_off INTEGER NOT NULL
);
