\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.93.0' WHERE name = 'database_version';

DROP TABLE iris.sign_message;

CREATE TABLE iris.sign_message (
	name VARCHAR(20) PRIMARY KEY,
	multi VARCHAR(256) NOT NULL,
	bitmaps text NOT NULL,
	a_priority INTEGER NOT NULL,
	r_priority INTEGER NOT NULL,
	duration INTEGER
);
