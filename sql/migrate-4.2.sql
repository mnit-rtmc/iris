\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.2.0'
	WHERE name = 'database_version';

DELETE FROM iris.system_attribute WHERE name = 'camera_stream_duration_secs';
