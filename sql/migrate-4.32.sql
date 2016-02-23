\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.32.0'
	WHERE name = 'database_version';

-- Reserve Control By Web comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (33, 'Control By Web');
