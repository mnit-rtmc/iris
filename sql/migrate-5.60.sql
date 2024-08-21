\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.59.0', '5.60.0');

ALTER TABLE iris.sign_config DROP CONSTRAINT sign_config_module_width_check;
ALTER TABLE iris.sign_config ADD CONSTRAINT sign_config_check
    CHECK (
        module_width > 0 AND
        (pixel_width % module_width) = 0
    );

ALTER TABLE iris.sign_config DROP CONSTRAINT sign_config_module_height_check;
ALTER TABLE iris.sign_config ADD CONSTRAINT sign_config_check1
    CHECK (
        module_height > 0 AND
        (pixel_height % module_height) = 0
    );

-- Rename dms_action to device_action
DROP VIEW dms_toll_zone_view;
DROP VIEW dms_action_view;

INSERT INTO iris.resource_type (name, base) VALUES ('device_action', false);
UPDATE iris.privilege SET type_n = 'device_action' WHERE type_n = 'dms_action';
DELETE FROM iris.resource_type WHERE name = 'dms_action';

CREATE TABLE iris.device_action (
    name VARCHAR(30) PRIMARY KEY,
    action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
    phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,
    hashtag VARCHAR(16) NOT NULL,
    msg_pattern VARCHAR(20) REFERENCES iris.msg_pattern,
    msg_priority INTEGER NOT NULL,

    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$')
);

INSERT INTO iris.device_action (
    name, action_plan, phase, hashtag, msg_pattern, msg_priority
)
SELECT name, action_plan, phase, dms_hashtag, msg_pattern, msg_priority
FROM iris.dms_action;

DROP TABLE iris.dms_action;

CREATE VIEW device_action_view AS
    SELECT name, action_plan, phase, hashtag, msg_pattern, msg_priority
    FROM iris.device_action;
GRANT SELECT ON device_action_view TO PUBLIC;

CREATE VIEW dms_action_view AS
    SELECT h.name AS dms, action_plan, phase, h.hashtag, msg_pattern,
           msg_priority
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
