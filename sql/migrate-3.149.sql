\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.149.1'
	WHERE name = 'database_version';

UPDATE iris.dms_action SET a_priority = a_priority + 1 WHERE a_priority > 1;
UPDATE iris.dms_action SET r_priority = r_priority + 1 WHERE r_priority > 1;
UPDATE iris.sign_message SET a_priority = a_priority + 1 WHERE a_priority > 1;
UPDATE iris.sign_message SET r_priority = r_priority + 1 WHERE r_priority > 1;
