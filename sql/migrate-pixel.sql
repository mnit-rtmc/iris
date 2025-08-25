\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

DELETE FROM iris.system_attribute WHERE name = 'dms_pixel_test_timeout_secs';

COMMIT;
