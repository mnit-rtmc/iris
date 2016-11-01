\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.40.0'
	WHERE name = 'database_version';

-- delete old system attributes
DELETE FROM iris.system_attribute WHERE name = 'dms_comm_loss_minutes';
