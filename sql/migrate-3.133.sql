\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.133.0'
	WHERE name = 'database_version';

INSERT INTO iris.comm_protocol (id, description) VALUES (16, 'Infinova D PTZ');
