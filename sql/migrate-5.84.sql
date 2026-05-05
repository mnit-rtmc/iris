\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.83.0', '5.84.0');

-- Change alarm base resource to system_attribute
UPDATE iris.resource_type SET base = 'system_attribute' WHERE name = 'alarm';

-- Remove controller as a base resource; use comm_config instead
UPDATE iris.resource_type SET base = NULL WHERE name = 'comm_config';

UPDATE iris.permission
    SET base_resource = 'comm_config'
    WHERE base_resource = 'controller';

UPDATE iris.resource_type
    SET base = 'comm_config'
    WHERE name IN ('comm_link', 'controller', 'geo_loc', 'gps', 'modem');

-- Remove detector as a base resource; use road instead
UPDATE iris.resource_type SET base = NULL WHERE name = 'road';

UPDATE iris.permission
    SET base_resource = 'road'
    WHERE base_resource = 'detector';

UPDATE iris.resource_type
    SET base = 'road'
    WHERE name IN ('detector', 'map_extent', 'r_node');

DELETE FROM iris.resource_type WHERE name = 'station';

-- Remove alert_config as a base resource; use weather_sensor instead
DELETE FROM iris.permission WHERE base_resource = 'alert_config';

UPDATE iris.resource_type
    SET base = 'weather_sensor'
    WHERE name IN ('alert_config', 'alert_info', 'alert_message');

-- Remove modem devices
DROP VIEW modem_view;
DROP TABLE iris.modem;
DELETE FROM iris.resource_type WHERE name = 'modem';

-- Add hashtag to map extent
ALTER TABLE iris.map_extent ADD COLUMN hashtag VARCHAR(16);

ALTER TABLE iris.map_extent ADD
    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$');

-- Add phase actions
INSERT INTO iris.resource_type (name, base)
    VALUES ('phase_action', 'action_plan');

CREATE TABLE iris.action_condition (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.action_condition (id, description)
VALUES
    (0, 'hold time'),
    (1, 'clock time'),
    (2, 'traffic threshold'),
    (3, 'RWIS threshold'),
    (4, 'toll mode'),
    (5, 'alert period');

CREATE TABLE iris.phase_action (
    name VARCHAR(30) PRIMARY KEY,
    action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
    day_plan VARCHAR(10) REFERENCES iris.day_plan,
    from_phase VARCHAR(12) REFERENCES iris.plan_phase,
    to_phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,
    condition INTEGER NOT NULL REFERENCES iris.action_condition,
    params VARCHAR(32)
);

CREATE FUNCTION iris.phase_action_notify() RETURNS TRIGGER AS
    $phase_action_notify$
BEGIN
    PERFORM pg_notify('phase_action', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$phase_action_notify$ LANGUAGE plpgsql;

CREATE TRIGGER phase_action_notify_trig
    AFTER UPDATE ON iris.phase_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.phase_action_notify();

CREATE TRIGGER phase_action_table_notify_trig
    AFTER INSERT OR DELETE ON iris.phase_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW phase_action_view AS
    SELECT name, action_plan, day_plan, from_phase, to_phase, condition,
           params
    FROM iris.phase_action;
GRANT SELECT ON phase_action_view TO PUBLIC;

COMMIT;
