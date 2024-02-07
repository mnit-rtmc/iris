\set ON_ERROR_STOP

BEGIN;

ALTER SCHEMA public OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.42.0', '5.43.0');

DROP VIEW dms_attribute_view;

-- Drop modem flag from comm_config
DROP VIEW comm_link_view;
DROP VIEW comm_config_view;

ALTER TABLE iris.comm_config DROP COLUMN modem;

CREATE VIEW comm_config_view AS
    SELECT cc.name, cc.description, cp.description AS protocol,
           timeout_ms, poll_period_sec, long_poll_period_sec,
           idle_disconnect_sec, no_response_disconnect_sec
    FROM iris.comm_config cc
    JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_config_view TO PUBLIC;

CREATE VIEW comm_link_view AS
    SELECT cl.name, cl.description, uri, poll_enabled,
           cp.description AS protocol, cc.description AS comm_config,
           timeout_ms, poll_period_sec, connected
    FROM iris.comm_link cl
    JOIN iris.comm_config cc ON cl.comm_config = cc.name
    JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;

-- Rename permission `batch` to `hashtag`
ALTER TABLE iris.permission ADD COLUMN hashtag VARCHAR(16);
UPDATE iris.permission SET hashtag = NULL;
ALTER TABLE iris.permission ADD CONSTRAINT hashtag_ck
    CHECK (hashtag ~ '^#[A-Za-z0-9]+$');
ALTER TABLE iris.permission DROP COLUMN batch;

-- Add resource hashtags
CREATE TABLE iris.hashtag (
    resource_n VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    name VARCHAR(20) NOT NULL,
    hashtag VARCHAR(16) NOT NULL,

    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$')
);
ALTER TABLE iris.hashtag ADD PRIMARY KEY (resource_n, name, hashtag);

CREATE VIEW hashtag_view AS
    SELECT resource_n, name, hashtag
    FROM iris.hashtag;
GRANT SELECT ON hashtag_view TO PUBLIC;

-- Remove invalid characters from hashtags
UPDATE iris.dms_hashtag
    SET hashtag = translate(hashtag, '-_', '');
UPDATE iris.msg_pattern
    SET compose_hashtag = translate(compose_hashtag, '-_', '');
UPDATE iris.msg_line
    SET restrict_hashtag = translate(restrict_hashtag, '-_', '');
UPDATE iris.dms_action
    SET dms_hashtag = translate(dms_hashtag, '-_', '');
UPDATE iris.alert_config
    SET dms_hashtag = translate(dms_hashtag, '-_', '');
UPDATE cap.alert_info
    SET all_hashtag = translate(all_hashtag, '-_', '');
UPDATE iris.lane_use_multi
    SET dms_hashtag = translate(dms_hashtag, '-_', '');

-- Add hashtag check constraints
ALTER TABLE iris.dms_hashtag ADD
    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$');
ALTER TABLE iris.msg_pattern ADD
    CONSTRAINT hashtag_ck CHECK (compose_hashtag ~ '^#[A-Za-z0-9]+$');
ALTER TABLE iris.msg_line ADD
    CONSTRAINT hashtag_ck CHECK (restrict_hashtag ~ '^#[A-Za-z0-9]+$');
ALTER TABLE iris.dms_action ADD
    CONSTRAINT hashtag_ck CHECK (dms_hashtag ~ '^#[A-Za-z0-9]+$');
ALTER TABLE iris.alert_config ADD
    CONSTRAINT hashtag_ck CHECK (dms_hashtag ~ '^#[A-Za-z0-9]+$');
ALTER TABLE cap.alert_info ADD
    CONSTRAINT hashtag_ck CHECK (all_hashtag ~ '^#[A-Za-z0-9]+$');
ALTER TABLE iris.lane_use_multi ADD
    CONSTRAINT hashtag_ck CHECK (dms_hashtag ~ '^#[A-Za-z0-9]+$');

-- Delete dms_composer_edit_mode system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_composer_edit_mode';

-- Delete dict_*_scheme system attributes
DELETE FROM iris.system_attribute WHERE name = 'dict_allowed_scheme';
DELETE FROM iris.system_attribute WHERE name = 'dict_banned_scheme';

COMMIT;
