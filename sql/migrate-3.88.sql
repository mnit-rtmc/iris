\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.88.0' WHERE name = 'database_version';

CREATE TABLE iris.quick_message (
	name VARCHAR(20) PRIMARY KEY,
	multi VARCHAR(256) NOT NULL
);
