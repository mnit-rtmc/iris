\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.78.0', '5.79.0');

-- Change comm protocol 31 to RTMS Echo
UPDATE iris.comm_protocol
    SET description = 'RTMS Echo'
    WHERE id = 31;

-- Add pollinator column to comm_config
DROP VIEW comm_config_view;

ALTER TABLE iris.comm_config ADD COLUMN pollinator BOOLEAN;
UPDATE iris.comm_config SET pollinator = false;
ALTER TABLE iris.comm_config ALTER COLUMN pollinator SET NOT NULL;

CREATE VIEW comm_config_view AS
    SELECT cc.name, cc.description, pollinator, cp.description AS protocol,
           timeout_ms, retry_threshold, poll_period_sec, long_poll_period_sec,
           idle_disconnect_sec, no_response_disconnect_sec
    FROM iris.comm_config cc
    JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_config_view TO PUBLIC;

COMMIT;
