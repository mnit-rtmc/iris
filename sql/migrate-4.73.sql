\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.72.0', '4.73.0');

-- insert camera_full_screen_enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'camera_full_screen_enable';
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_full_screen_enable', 'false');

-- Drop sign_group from inc_descriptor, inc_locator and inc_advice tables
ALTER TABLE iris.inc_descriptor DROP COLUMN sign_group;
ALTER TABLE iris.inc_locator DROP COLUMN sign_group;
ALTER TABLE iris.inc_advice DROP COLUMN sign_group;

-- Add abbrev column to inc_descriptor, inc_locator and inc_advice tables
ALTER TABLE iris.inc_descriptor ADD COLUMN abbrev VARCHAR(32);
ALTER TABLE iris.inc_locator ADD COLUMN abbrev VARCHAR(32);
ALTER TABLE iris.inc_advice ADD COLUMN abbrev VARCHAR(32);

COMMIT;
