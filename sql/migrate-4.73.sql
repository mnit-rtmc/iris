\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.72.0', '4.73.0');

-- insert camera_full_screen_enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'camera_full_screen_enable';
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_full_screen_enable', 'false');

COMMIT;
