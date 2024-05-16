\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.53.0', '5.54.0');

INSERT INTO iris.system_attribute (name, value)
    VALUES ('vid_max_duration_sec', '0');

-- Add base column to resource_type
ALTER TABLE iris.resource_type ADD COLUMN base BOOLEAN;
UPDATE iris.resource_type SET base = false;
UPDATE iris.resource_type SET base = true
    WHERE name IN ('action_plan', 'alert_config', 'beacon', 'camera',
                   'controller', 'detector', 'dms', 'gate_arm', 'incident',
                   'lcs', 'parking_area', 'permission', 'ramp_meter',
                   'system_attribute', 'toll_zone', 'weather_sensor');
ALTER TABLE iris.resource_type ALTER COLUMN base SET NOT NULL;

CREATE FUNCTION iris.resource_is_base(VARCHAR(16)) RETURNS BOOLEAN AS
    $resource_is_base$
SELECT EXISTS (
    SELECT 1
    FROM iris.resource_type
    WHERE name = $1 AND base = true
);
$resource_is_base$ LANGUAGE sql;

-- Delete permissions that are not for "base" resources
DELETE FROM iris.permission WHERE resource_n NOT IN (
    'action_plan', 'alert_config', 'beacon', 'camera', 'controller',
    'detector', 'dms', 'gate_arm', 'incident', 'lcs', 'parking_area',
    'permission', 'ramp_meter', 'system_attribute', 'toll_zone',
    'weather_sensor'
);

ALTER TABLE iris.permission ADD CONSTRAINT base_resource_ck
    CHECK (iris.resource_is_base(resource_n));

COMMIT;
