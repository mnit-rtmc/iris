\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.59.0', '4.60.0');

-- delete camera_blank_url system attribute
DELETE FROM iris.system_attribute WHERE name = 'camera_blank_url';

COMMIT;
