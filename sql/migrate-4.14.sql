\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.14.0'
	WHERE name = 'database_version';

INSERT INTO iris.system_attribute (name, value) VALUES ('comm_event_purge_days', '14');
