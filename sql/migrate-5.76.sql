\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.75.0', '5.76.0');

-- Update camera_preset_view
DROP VIEW camera_preset_view;
CREATE VIEW camera_preset_view AS
    SELECT cp.name, camera, preset_num, d.direction, p.name AS device
    FROM iris.camera_preset cp
    LEFT JOIN iris.direction d ON d.id = cp.direction
    LEFT JOIN iris.device_preset p ON cp.name = p.preset;
GRANT SELECT ON camera_preset_view TO PUBLIC;

COMMIT;
