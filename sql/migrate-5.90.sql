\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.89.0', '5.90.0');

-- Move sticky + ignore_auto_fail from action_plan to device_action
DROP VIEW dms_toll_zone_view;
DROP VIEW dms_action_view;
DROP VIEW device_action_view;
DROP VIEW action_plan_view;

ALTER TABLE iris.device_action ADD COLUMN sticky BOOLEAN;
UPDATE iris.device_action AS da
   SET sticky = ap.sticky
  FROM iris.action_plan AS ap
 WHERE da.action_plan = ap.name;
ALTER TABLE iris.device_action ALTER COLUMN sticky SET NOT NULL;

ALTER TABLE iris.action_plan DROP COLUMN sticky;

ALTER TABLE iris.device_action ADD COLUMN ignore_auto_fail BOOLEAN;
UPDATE iris.device_action AS da
   SET ignore_auto_fail = ap.ignore_auto_fail
  FROM iris.action_plan AS ap
 WHERE da.action_plan = ap.name;
ALTER TABLE iris.device_action ALTER COLUMN ignore_auto_fail SET NOT NULL;

ALTER TABLE iris.action_plan DROP COLUMN ignore_auto_fail;

CREATE VIEW action_plan_view AS
    SELECT name, notes, sync_actions, active, default_phase, phase
    FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

CREATE VIEW device_action_view AS
    SELECT name, action_plan, phase, hashtag, msg_pattern, msg_priority,
           sticky, ignore_auto_fail
    FROM iris.device_action;
GRANT SELECT ON device_action_view TO PUBLIC;

CREATE VIEW dms_action_view AS
    SELECT h.name AS dms, action_plan, phase, h.hashtag, msg_pattern,
           msg_priority, sticky, ignore_auto_fail
    FROM iris.device_action da
    JOIN iris.hashtag h ON h.hashtag = da.hashtag AND resource_n = 'dms';
GRANT SELECT ON dms_action_view TO PUBLIC;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, hashtag, tz.state, toll_zone, action_plan, da.msg_pattern
    FROM dms_action_view da
    JOIN iris.msg_pattern mp
    ON da.msg_pattern = mp.name
    JOIN iris.msg_pattern_toll_zone tz
    ON da.msg_pattern = tz.msg_pattern;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

-- Insert date-time action condition
INSERT INTO iris.action_condition (id, description)
    VALUES (5, 'alarm');
UPDATE iris.phase_action SET condition = 5 WHERE condition = 4;
UPDATE iris.action_condition SET description = 'RWIS threshold' WHERE id = 4;
UPDATE iris.phase_action SET condition = 4 WHERE condition = 3;
UPDATE iris.action_condition SET description = 'traffic threshold' WHERE id = 3;
UPDATE iris.phase_action SET condition = 3 WHERE condition = 2;
UPDATE iris.action_condition SET description = 'date-time' WHERE id = 2;
UPDATE iris.phase_action SET condition = 2
    WHERE condition = 1 AND params LIKE '%T%';

ALTER TABLE iris.phase_action ADD
    CONSTRAINT day_ck CHECK ((day_plan IS NULL) OR (condition != 2));

-- Add symbol to action_condition
ALTER TABLE iris.action_condition ADD COLUMN symbol VARCHAR;
UPDATE iris.action_condition SET symbol = '⏳' WHERE id = 0;
UPDATE iris.action_condition SET symbol = '⏰' WHERE id = 1;
UPDATE iris.action_condition SET symbol = '🗓️' WHERE id = 2;
UPDATE iris.action_condition SET symbol = '🚗' WHERE id = 3;
UPDATE iris.action_condition SET symbol = '🌦️' WHERE id = 4;
UPDATE iris.action_condition SET symbol = '🔔' WHERE id = 5;
ALTER TABLE iris.action_condition ALTER COLUMN symbol SET NOT NULL;

COMMIT;
