\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.26.0'
	WHERE name = 'database_version';

-- Reserve Transcore E6 comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (32, 'TransCore E6');
