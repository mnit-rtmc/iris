\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.51.0'
	WHERE name = 'database_version';

-- Add comm_idle_disconnect system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_idle_disconnect_dms_sec', '-1');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_idle_disconnect_modem_sec', '20');
