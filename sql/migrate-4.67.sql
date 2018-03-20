\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.66.0', '4.67.0');

-- Drop old views
DROP VIEW quick_message_view;
DROP VIEW dms_message_view;
DROP VIEW dms_toll_zone_view;
DROP VIEW dms_action_view;

-- Add sign_config column to quick_message
ALTER TABLE iris.quick_message ADD COLUMN sign_config VARCHAR(12)
	REFERENCES iris.sign_config;

-- Add prefix_page column to quick_message
ALTER TABLE iris.quick_message ADD COLUMN prefix_page BOOLEAN;
UPDATE iris.quick_message SET prefix_page = name IN (
	SELECT quick_message
	FROM iris.dms_action
	WHERE a_priority = 2
	AND quick_message IS NOT NULL
);
ALTER TABLE iris.quick_message ALTER COLUMN prefix_page SET NOT NULL;

-- Replace a_priority and r_priority with msg_priority on dms_action
ALTER TABLE iris.dms_action ADD COLUMN msg_priority INTEGER;
UPDATE iris.dms_action SET msg_priority = r_priority;
ALTER TABLE iris.dms_action ALTER COLUMN msg_priority SET NOT NULL;
ALTER TABLE iris.dms_action DROP COLUMN a_priority;
ALTER TABLE iris.dms_action DROP COLUMN r_priority;

-- Add prefix_page column to sign_message
ALTER TABLE iris.sign_message ADD COLUMN prefix_page BOOLEAN;
UPDATE iris.sign_message SET prefix_page = (a_priority = 2);
ALTER TABLE iris.sign_message ALTER COLUMN prefix_page SET NOT NULL;

-- Replace a_priority and r_priority with msg_priority on sign_message
ALTER TABLE iris.sign_message ADD COLUMN msg_priority INTEGER;
UPDATE iris.sign_message SET msg_priority = r_priority;
ALTER TABLE iris.sign_message ALTER COLUMN msg_priority SET NOT NULL;
ALTER TABLE iris.sign_message DROP COLUMN a_priority;
ALTER TABLE iris.sign_message DROP COLUMN r_priority;

-- Create dms_action_view
CREATE VIEW dms_action_view AS
	SELECT name, action_plan, sign_group, phase, quick_message,
	       beacon_enabled, msg_priority
	FROM iris.dms_action;
GRANT SELECT ON dms_action_view TO PUBLIC;

-- Create dms_toll_zone_view
CREATE VIEW dms_toll_zone_view AS
    SELECT dms, state, toll_zone, action_plan, dms_action_view.quick_message
    FROM dms_action_view
    JOIN iris.dms_sign_group
    ON dms_action_view.sign_group = dms_sign_group.sign_group
    JOIN iris.quick_message
    ON dms_action_view.quick_message = quick_message.name
    JOIN iris.quick_message_toll_zone
    ON dms_action_view.quick_message = quick_message_toll_zone.quick_message;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

-- Create dms_message_view
CREATE VIEW dms_message_view AS
	SELECT d.name, cc.description AS condition, multi, beacon_enabled,
	       prefix_page, msg_priority, iris.sign_msg_sources(source)
	       AS sources, duration, deploy_time, owner
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Create quick_message_view
CREATE VIEW quick_message_view AS
	SELECT name, sign_group, sign_config, prefix_page, multi
	FROM iris.quick_message;
GRANT SELECT ON quick_message_view TO PUBLIC;

COMMIT;
