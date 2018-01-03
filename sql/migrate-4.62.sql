\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.61.0', '4.62.0');

-- insert camera_kbd_panasonic_enable system attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_kbd_panasonic_enable', 'false');

COMMIT;
