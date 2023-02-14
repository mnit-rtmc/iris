\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.39.0', '5.40.0');

-- Rename beacon_enabled to flash_beacon in sign_message
DROP VIEW dms_message_view;
DROP VIEW sign_message_view;

ALTER TABLE iris.sign_message ADD COLUMN flash_beacon BOOLEAN;
UPDATE iris.sign_message SET flash_beacon = beacon_enabled;
ALTER TABLE iris.sign_message ALTER COLUMN flash_beacon SET NOT NULL;
ALTER TABLE iris.sign_message DROP COLUMN beacon_enabled;

CREATE VIEW sign_message_view AS
    SELECT name, sign_config, incident, multi, flash_beacon, msg_priority,
           iris.sign_msg_sources(source) AS sources, owner, duration
    FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, flash_beacon,
           msg_priority, iris.sign_msg_sources(source) AS sources,
           duration, expire_time
    FROM iris.dms d
    LEFT JOIN iris.controller c ON d.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Drop beacon_enabled from in dms_action
DROP VIEW dms_toll_zone_view;
DROP VIEW dms_action_view;

ALTER TABLE iris.dms_action DROP COLUMN beacon_enabled;

CREATE VIEW dms_action_view AS
    SELECT name, action_plan, phase, dms_hashtag, msg_pattern, msg_priority
    FROM iris.dms_action;
GRANT SELECT ON dms_action_view TO PUBLIC;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, dh.hashtag, tz.state, toll_zone, action_plan, da.msg_pattern
    FROM dms_action_view da
    JOIN iris.dms_hashtag dh
    ON da.dms_hashtag = dh.hashtag
    JOIN iris.msg_pattern mp
    ON da.msg_pattern = mp.name
    JOIN iris.msg_pattern_toll_zone tz
    ON da.msg_pattern = tz.msg_pattern;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

DROP VIEW msg_pattern_view;

-- Add flash_beacon to msg_pattern
ALTER TABLE iris.msg_pattern ADD COLUMN flash_beacon BOOLEAN;
UPDATE iris.msg_pattern SET flash_beacon = false;
ALTER TABLE iris.msg_pattern ALTER COLUMN flash_beacon SET NOT NULL;

CREATE VIEW msg_pattern_view AS
    SELECT name, multi, flash_beacon, compose_hashtag
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

-- Fix msg_line hashtags
UPDATE iris.msg_line SET msg_pattern = '.2.LINE'
    WHERE msg_pattern = '.3.LINE' AND restrict_hashtag IN
(
    SELECT '#' || dms FROM dms_hashtag_view WHERE hashtag = '#TwoLine'
);

UPDATE iris.msg_line SET msg_pattern = '.3.LINE'
    WHERE msg_pattern = '.2.PAGE' AND restrict_hashtag IN
(
    SELECT '#' || dms FROM dms_hashtag_view WHERE hashtag = '#ThreeLine'
);

COMMIT;
