\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Remove alert_config as a base resource; use weather_sensor instead
DELETE FROM iris.permission WHERE base_resource = 'alert_config';

UPDATE iris.resource_type
    SET base = 'weather_sensor'
    WHERE name IN ('alert_config', 'alert_info', 'alert_message');

COMMIT;
