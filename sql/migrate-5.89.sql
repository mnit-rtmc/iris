\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.88.0', '5.89.0');

-- Delete unimplemented action conditions
UPDATE iris.phase_action
    SET condition = 4
    WHERE condition > 4;
UPDATE iris.action_condition
    SET description = 'alarm'
    WHERE id = 4;

DELETE FROM iris.action_condition WHERE id > 4;

-- Convert existing time_action records to phase_action records
INSERT INTO iris.phase_action (
    name, action_plan, day_plan, to_phase, condition, params
) (
    SELECT name, action_plan, day_plan, phase, 1, 
           COALESCE(sched_date::VARCHAR || 'T', '') ||
           SUBSTRING(time_of_day::VARCHAR FOR 5)
    FROM iris.time_action
);

-- DROP time_action and related stuff
DROP VIEW meter_action_view;
DROP VIEW time_action_view;
DROP TABLE iris.time_action;
DROP FUNCTION iris.time_action_notify();

DELETE FROM iris.resource_type WHERE name = 'time_action';

-- Recreate meter_action_view using phase_action instead of time_action
CREATE VIEW meter_action_view AS
    SELECT h.name AS ramp_meter, da.action_plan, pa.to_phase, h.hashtag,
           msg_pattern, day_plan, params AS clock_time
    FROM iris.device_action da
    JOIN iris.hashtag h ON h.hashtag = da.hashtag AND resource_n = 'ramp_meter'
    JOIN iris.action_plan ap ON da.action_plan = ap.name
    LEFT JOIN iris.phase_action pa ON pa.action_plan = ap.name
    WHERE active = true
    AND pa.condition = 1
    ORDER BY ramp_meter, params;
GRANT SELECT ON meter_action_view TO PUBLIC;

-- Update day_matcher_valid CHECK
ALTER TABLE iris.day_matcher
    DROP CONSTRAINT day_matcher_valid;
ALTER TABLE iris.day_matcher
    ADD CONSTRAINT day_matcher_valid CHECK (
        (month IS NOT NULL OR day IS NOT NULL OR weekday IS NOT NULL) AND
        (week IS NULL OR (day IS NULL AND weekday IS NOT NULL)) AND
        (shift IS NULL OR
            (day IS NULL AND weekday IS NOT NULL AND week IS NOT NULL)
        )
    );

-- Delete RWIS action tag system attributes
DELETE FROM iris.system_attribute WHERE name IN (
    'rwis_auto_max_dist_miles',
    'rwis_slippery_1_percent',
    'rwis_slippery_2_degrees',
    'rwis_slippery_3_percent',
    'rwis_windy_1_kph',
    'rwis_windy_2_kph',
    'rwis_visibility_1_m',
    'rwis_visibility_2_m',
    'rwis_flooding_1_mm',
    'rwis_flooding_2_mm'
);

-- DROP dms_weather_sensor
DROP VIEW dms_weather_sensor_view;
DROP TABLE iris.dms_weather_sensor;

COMMIT;
