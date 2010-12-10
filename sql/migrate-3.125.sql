\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

ALTER TABLE iris._detector ALTER COLUMN r_node SET NOT NULL;

UPDATE iris.system_attribute SET value = '3.125.0'
	WHERE name = 'database_version';
