\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.43.0'
	WHERE name = 'database_version';

INSERT INTO iris.system_attribute (name, value)
	VALUES ('dms_comm_loss_enable', 'true');

UPDATE iris.privilege SET attr_n = 'msgUser'
	WHERE type_n = 'dms' AND attr_n = 'messageNext';
DELETE FROM iris.privilege
	WHERE type_n = 'dms' AND attr_n = 'ownerNext';
