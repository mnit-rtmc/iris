\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.122.0'
	WHERE name = 'database_version';

UPDATE iris.system_attribute SET name = 'dms_aws_read_time'
	WHERE name = 'dms_aws_read_offset';
