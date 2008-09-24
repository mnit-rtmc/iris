\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

CREATE TABLE traffic_device_attribute (
	name VARCHAR(32) PRIMARY KEY,
	id text NOT NULL,
	aname VARCHAR(32) NOT NULL,
	avalue VARCHAR(64) NOT NULL
);

CREATE TABLE system_attribute (
	name VARCHAR(32) PRIMARY KEY,
	value VARCHAR(64) NOT NULL
);

INSERT INTO system_attribute VALUES('database_version', '3.78.0');
