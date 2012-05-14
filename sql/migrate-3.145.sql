\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.145.0'
	WHERE name = 'database_version';

INSERT INTO iris.system_attribute (name, value) values ('msg_feed_verify', 'true');
