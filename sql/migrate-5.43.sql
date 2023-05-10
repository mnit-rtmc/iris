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
UPDATE iris.permission SET hashtag = batch;
ALTER TABLE iris.permission DROP COLUMN batch;

COMMIT;
