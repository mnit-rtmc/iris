\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Delete DMS pixel system attributes
DELETE FROM iris.system_attribute WHERE name IN (
    'dms_pixel_off_limit',
    'dms_pixel_on_limit',
    'dms_pixel_test_timeout_secs'
);

COMMIT;
