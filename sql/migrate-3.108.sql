\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.108.0'
	WHERE name = 'database_version';
UPDATE iris.system_attribute SET name = 'dms_op_status_enable'
	WHERE name = 'dms_inter_status_enable';
