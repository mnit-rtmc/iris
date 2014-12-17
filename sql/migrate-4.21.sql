\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.21.0'
	WHERE name = 'database_version';

-- delete insecure system attributes
DELETE FROM iris.system_attribute WHERE name = 'sample_archive_directory';
