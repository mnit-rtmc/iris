\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.33.0'
	WHERE name = 'database_version';

-- Reserve incident feed comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (34, 'Incident Feed');
