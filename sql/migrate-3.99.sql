\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.99.0' WHERE name = 'database_version';

CREATE TABLE iris.holiday (
	name VARCHAR(32) PRIMARY KEY,
	month INTEGER NOT NULL,
	day INTEGER NOT NULL,
	week INTEGER NOT NULL,
	weekday INTEGER NOT NULL,
	shift INTEGER NOT NULL,
	period INTEGER NOT NULL
);

INSERT INTO iris.holiday (name, month, day, week, weekday, shift, period)
	(SELECT name, month, day, week, weekday, shift, period FROM holiday);

DROP TABLE holiday;
