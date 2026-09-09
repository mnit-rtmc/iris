\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

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

COMMIT;
