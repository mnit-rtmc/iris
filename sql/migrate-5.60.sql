\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.59.0', '5.60.0');

ALTER TABLE iris.sign_config DROP CONSTRAINT sign_config_module_width_check;
ALTER TABLE iris.sign_config ADD CONSTRAINT sign_config_module_width_check
    CHECK (
        module_width > 0 AND
        module_width * (pixel_width / module_width) = pixel_width
    );

ALTER TABLE iris.sign_config DROP CONSTRAINT sign_config_module_height_check;
ALTER TABLE iris.sign_config ADD CONSTRAINT sign_config_module_height_check
    CHECK (
        module_height > 0 AND
        module_height * (pixel_height / module_height) = pixel_height
    );

COMMIT;
