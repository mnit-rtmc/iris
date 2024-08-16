\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.59.0', '5.60.0');

ALTER TABLE iris.sign_config DROP CONSTRAINT sign_config_module_width_check;
ALTER TABLE iris.sign_config ADD CONSTRAINT sign_config_check
    CHECK (
        module_width > 0 AND
        (pixel_width % module_width) = 0
    );

ALTER TABLE iris.sign_config DROP CONSTRAINT sign_config_module_height_check;
ALTER TABLE iris.sign_config ADD CONSTRAINT sign_config_check1
    CHECK (
        module_height > 0 AND
        (pixel_height % module_height) = 0
    );

COMMIT;
