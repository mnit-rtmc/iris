\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.121.0'
	WHERE name = 'database_version';

INSERT INTO iris.comm_protocol VALUES (15, 'OSi ORG-815');
