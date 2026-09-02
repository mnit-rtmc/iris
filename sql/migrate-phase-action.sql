\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

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

COMMIT;
