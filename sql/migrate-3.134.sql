\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.134.0'
	WHERE name = 'database_version';

INSERT INTO iris.system_attribute VALUES('dialup_poll_period_mins', '120');
