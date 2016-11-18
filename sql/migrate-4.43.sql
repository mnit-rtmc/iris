\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.43.0'
	WHERE name = 'database_version';

INSERT INTO iris.system_attribute (name, value)
	VALUES ('dms_comm_loss_enable', 'true');
