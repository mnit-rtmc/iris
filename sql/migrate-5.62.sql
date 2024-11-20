\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.61.0', '5.62.0');

-- Add notify channels
CREATE TRIGGER encoder_type_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.encoder_type
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TRIGGER encoder_stream_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.encoder_stream
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TRIGGER incident_detail_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON event.incident_detail
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TRIGGER inc_advice_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.inc_advice
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TRIGGER inc_descriptor_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.inc_descriptor
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TRIGGER inc_locator_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.inc_locator
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TRIGGER camera_preset_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.camera_preset
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TRIGGER road_affix_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.road_affix
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TRIGGER day_matcher_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.day_matcher
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

-- Rename iris_user to user_id in client_event
DROP VIEW client_event_view;

ALTER TABLE event.client_event RENAME TO old_client_event;

CREATE TABLE event.client_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    host_port VARCHAR(64) NOT NULL,
    user_id VARCHAR(15)
);

INSERT INTO event.client_event (event_date, event_desc, host_port, user_id)
SELECT event_date, event_desc_id, host_port, iris_user
FROM event.old_client_event;

DROP TABLE event.old_client_event;

CREATE VIEW client_event_view AS
    SELECT ev.id, event_date, ed.description, host_port, user_id
    FROM event.client_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON client_event_view TO PUBLIC;

-- Rename iris_user to user_id in gate_arm_event
DROP VIEW gate_arm_event_view;

ALTER TABLE event.gate_arm_event RENAME TO old_gate_arm_event;

CREATE TABLE event.gate_arm_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    device_id VARCHAR(20),
    user_id VARCHAR(15),
    fault VARCHAR(32)
);

INSERT INTO event.gate_arm_event (
    event_date, event_desc, device_id, user_id, fault
)
SELECT event_date, event_desc_id, device_id, iris_user, fault
FROM event.old_gate_arm_event;

DROP TABLE event.old_gate_arm_event;

CREATE VIEW gate_arm_event_view AS
    SELECT ev.id, event_date, ed.description, device_id, user_id, fault
    FROM event.gate_arm_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON gate_arm_event_view TO PUBLIC;

-- Add meter_lock_event
CREATE TABLE event.meter_lock_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    ramp_meter VARCHAR(20) NOT NULL REFERENCES iris._ramp_meter
        ON DELETE CASCADE,
    m_lock INTEGER REFERENCES iris.meter_lock,
    user_id VARCHAR(15)
);

CREATE VIEW meter_lock_event_view AS
    SELECT ev.id, event_date, ed.description, ramp_meter,
           lk.description AS m_lock, user_id
    FROM event.meter_lock_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id
    JOIN iris.meter_lock lk ON ev.m_lock = lk.id;
GRANT SELECT ON meter_lock_event_view TO PUBLIC;

INSERT INTO event.event_description (event_desc_id, description)
    VALUES (402, 'Meter LOCK');

INSERT INTO iris.event_config (name, enable_store, enable_purge, purge_days)
    VALUES ('meter_lock_event', true, false, 0);

-- Add phase and user_id to action_plan_event
DROP VIEW action_plan_event_view;

ALTER TABLE event.action_plan_event RENAME TO old_action_plan_event;

CREATE TABLE event.action_plan_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    action_plan VARCHAR(16) NOT NULL,
    phase VARCHAR(12),
    user_id VARCHAR(15)
);

INSERT INTO event.action_plan_event (
    event_date, event_desc, action_plan, user_id
)
SELECT event_date, event_desc_id, action_plan, detail
FROM event.old_action_plan_event
WHERE event_desc_id = 900 OR event_desc_id = 901;

INSERT INTO event.action_plan_event (
    event_date, event_desc, action_plan, phase
)
SELECT event_date, event_desc_id, action_plan, detail::VARCHAR(12)
FROM event.old_action_plan_event
WHERE event_desc_id = 902;

DROP TABLE event.old_action_plan_event;

CREATE VIEW action_plan_event_view AS
    SELECT ev.id, event_date, ed.description, action_plan, phase, user_id
    FROM event.action_plan_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON action_plan_event_view TO PUBLIC;

-- Add user_id to beacon_event
DROP VIEW beacon_event_view;

ALTER TABLE event.beacon_event RENAME TO old_beacon_event;

CREATE TABLE event.beacon_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    beacon VARCHAR(20) NOT NULL REFERENCES iris._beacon ON DELETE CASCADE,
    state INTEGER NOT NULL REFERENCES iris.beacon_state,
    user_id VARCHAR(15)
);

INSERT INTO event.beacon_event (event_date, beacon, state)
SELECT event_date, beacon, state
FROM event.old_beacon_event;

DROP TABLE event.old_beacon_event;

CREATE VIEW beacon_event_view AS
    SELECT be.id, event_date, beacon, bs.description AS state, user_id
    FROM event.beacon_event be
    JOIN iris.beacon_state bs ON be.state = bs.id;
GRANT SELECT ON beacon_event_view TO PUBLIC;

-- Add user_id to incident / incident_update
DROP VIEW incident_update_view;
DROP VIEW incident_view;

ALTER TABLE event.incident_update DROP CONSTRAINT incident_update_incident_fkey;

ALTER TABLE event.incident DROP CONSTRAINT incident_pkey;
ALTER TABLE event.incident DROP CONSTRAINT incident_replaces_fkey;
ALTER TABLE event.incident DROP CONSTRAINT incident_name_key;
ALTER TABLE event.incident DROP CONSTRAINT incident_detail_fkey;
ALTER TABLE event.incident DROP CONSTRAINT incident_dir_fkey;
ALTER TABLE event.incident DROP CONSTRAINT incident_event_desc_id_fkey;
ALTER TABLE event.incident DROP CONSTRAINT incident_lane_code_fkey;

ALTER TABLE event.incident_update DROP CONSTRAINT incident_update_pkey;

ALTER TABLE event.incident RENAME TO old_incident;
ALTER TABLE event.incident_update RENAME TO old_incident_update;

CREATE TABLE event.incident (
    id SERIAL PRIMARY KEY,
    name VARCHAR(16) NOT NULL UNIQUE,
    replaces VARCHAR(16) REFERENCES event.incident(name),
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    detail VARCHAR(8) REFERENCES event.incident_detail(name),
    lane_code VARCHAR(1) NOT NULL REFERENCES iris.lane_code,
    road VARCHAR(20) NOT NULL,
    dir SMALLINT NOT NULL REFERENCES iris.direction(id),
    lat double precision NOT NULL,
    lon double precision NOT NULL,
    camera VARCHAR(20),
    impact VARCHAR(20) NOT NULL,
    cleared BOOLEAN NOT NULL,
    confirmed BOOLEAN NOT NULL,
    user_id VARCHAR(15),

    CONSTRAINT impact_ck CHECK (impact ~ '^[!?\.]*$')
);

INSERT INTO event.incident (
    name, replaces, event_date, event_desc, detail, lane_code, road,
    dir, lat, lon, camera, impact, cleared, confirmed
)
SELECT name, replaces, event_date, event_desc_id, detail, lane_code, road,
       dir, lat, lon, camera, impact, cleared, confirmed
FROM event.old_incident;

DROP TABLE event.old_incident;

CREATE TRIGGER incident_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON event.incident
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE TABLE event.incident_update (
    id SERIAL PRIMARY KEY,
    incident VARCHAR(16) NOT NULL REFERENCES event.incident(name),
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    impact VARCHAR(20) NOT NULL,
    cleared BOOLEAN NOT NULL,
    confirmed BOOLEAN NOT NULL,
    user_id VARCHAR(15)
);

INSERT INTO event.incident_update (
    incident, event_date, impact, cleared, confirmed
)
SELECT incident, event_date, impact, cleared, confirmed
FROM event.old_incident_update;

DROP TABLE event.old_incident_update;

CREATE OR REPLACE FUNCTION event.incident_update_trig() RETURNS TRIGGER AS
$incident_update_trig$
BEGIN
    IF (NEW.impact IS DISTINCT FROM OLD.impact) OR
       (NEW.cleared IS DISTINCT FROM OLD.cleared)
    THEN
        INSERT INTO event.incident_update (
            incident, event_date, impact, cleared, confirmed, user_id
        ) VALUES (
            NEW.name, now(), NEW.impact, NEW.cleared, NEW.confirmed, NEW.user_id
        );
    END IF;
    RETURN NEW;
END;
$incident_update_trig$ LANGUAGE plpgsql;

CREATE TRIGGER incident_update_trigger
    AFTER INSERT OR UPDATE ON event.incident
    FOR EACH ROW EXECUTE FUNCTION event.incident_update_trig();

CREATE VIEW incident_view AS
    SELECT i.id, name, event_date, ed.description, road, d.direction,
           impact, event.incident_blocked_lanes(impact) AS blocked_lanes,
           event.incident_blocked_shoulders(impact) AS blocked_shoulders,
           cleared, confirmed, user_id, camera, lc.description AS lane_type,
           detail, replaces, lat, lon
    FROM event.incident i
    LEFT JOIN event.event_description ed ON i.event_desc = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_code lc ON i.lane_code = lc.lcode;
GRANT SELECT ON incident_view TO PUBLIC;

CREATE VIEW incident_update_view AS
    SELECT iu.id, name, iu.event_date, ed.description, road,
           d.direction, iu.impact, iu.cleared, iu.confirmed, iu.user_id,
           camera, lc.description AS lane_type, detail, replaces, lat, lon
    FROM event.incident i
    JOIN event.incident_update iu ON i.name = iu.incident
    LEFT JOIN event.event_description ed ON i.event_desc = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_code lc ON i.lane_code = lc.lcode;
GRANT SELECT ON incident_update_view TO PUBLIC;

-- Delete unused incident event descriptions
DELETE FROM event.event_description
    WHERE event_desc_id = 20 OR event_desc_id = 29;

-- Change road_affix base to incident
UPDATE iris.resource_type SET base = 'incident' WHERE name = 'road_affix';

-- Add sign_message foreign key to incidents
UPDATE iris.sign_message SET incident = NULL
    WHERE incident IS NOT NULL
    AND incident NOT IN (SELECT name FROM event.incident);
ALTER TABLE iris.sign_message
    ADD CONSTRAINT sign_message_incident_fkey FOREIGN KEY (incident)
    REFERENCES event.incident(name) ON DELETE SET NULL;

-- Change column names for r_node_transition / r_node_type LUTs
ALTER TABLE iris.r_node DROP CONSTRAINT r_node_transition_fkey;
ALTER TABLE iris.r_node DROP CONSTRAINT r_node_node_type_fkey;
DROP VIEW r_node_view;
DROP TABLE iris.r_node_transition;

CREATE TABLE iris.r_node_transition (
    id INTEGER PRIMARY KEY,
    description VARCHAR(12) NOT NULL
);

INSERT INTO iris.r_node_transition (id, description)
VALUES
    (0, 'none'),
    (1, 'loop'),
    (2, 'leg'),
    (3, 'slipramp'),
    (4, 'CD'),
    (5, 'HOV'),
    (6, 'common'),
    (7, 'flyover');

ALTER TABLE iris.r_node
    ADD CONSTRAINT r_node_transition_fkey FOREIGN KEY (transition)
    REFERENCES iris.r_node_transition;

DROP TABLE iris.r_node_type;

CREATE TABLE iris.r_node_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR(12) NOT NULL
);

INSERT INTO iris.r_node_type (id, description)
VALUES
    (0, 'station'),
    (1, 'entrance'),
    (2, 'exit'),
    (3, 'intersection'),
    (4, 'access'),
    (5, 'interchange');

ALTER TABLE iris.r_node
    ADD CONSTRAINT r_node_node_type_fkey FOREIGN KEY (node_type)
    REFERENCES iris.r_node_type;

CREATE VIEW r_node_view AS
    SELECT n.name, n.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           nt.description AS node_type, n.pickable, n.above,
           tr.description AS transition, n.lanes, n.attach_side, n.shift,
           n.active, n.station_id, n.speed_limit, n.notes
    FROM iris.r_node n
    JOIN geo_loc_view l ON n.geo_loc = l.name
    JOIN iris.r_node_type nt ON n.node_type = nt.id
    JOIN iris.r_node_transition tr ON n.transition = tr.id;
GRANT SELECT ON r_node_view TO PUBLIC;

-- Move meter_event LUTs to iris schema
DROP VIEW meter_event_view;

CREATE TABLE iris.metering_phase (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

INSERT INTO iris.metering_phase (id, description)
VALUES
    (0, 'not started'),
    (1, 'metering'),
    (2, 'flushing'),
    (3, 'stopped');

CREATE TABLE iris.meter_queue_state (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

INSERT INTO iris.meter_queue_state (id, description)
VALUES
    (0, 'unknown'),
    (1, 'empty'),
    (2, 'exists'),
    (3, 'full');

CREATE TABLE iris.meter_limit_control (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

INSERT INTO iris.meter_limit_control (id, description)
VALUES
    (0, 'passage fail'),
    (1, 'storage limit'),
    (2, 'wait limit'),
    (3, 'target minimum'),
    (4, 'backup limit');

ALTER TABLE event.meter_event RENAME TO old_meter_event;

CREATE TABLE event.meter_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    ramp_meter VARCHAR(20) NOT NULL REFERENCES iris._ramp_meter
        ON DELETE CASCADE,
    phase INTEGER NOT NULL REFERENCES iris.metering_phase,
    q_state INTEGER NOT NULL REFERENCES iris.meter_queue_state,
    q_len REAL NOT NULL,
    dem_adj REAL NOT NULL,
    wait_secs INTEGER NOT NULL,
    limit_ctrl INTEGER NOT NULL REFERENCES iris.meter_limit_control,
    min_rate INTEGER NOT NULL,
    rel_rate INTEGER NOT NULL,
    max_rate INTEGER NOT NULL,
    d_node VARCHAR(10),
    seg_density REAL NOT NULL
);

INSERT INTO event.meter_event (
    event_date, event_desc, ramp_meter, phase, q_state, q_len, dem_adj,
    wait_secs, limit_ctrl, min_rate, rel_rate, max_rate, d_node, seg_density
)
SELECT event_date, event_desc_id, ramp_meter, phase, q_state, q_len, dem_adj,
    wait_secs, limit_ctrl, min_rate, rel_rate, max_rate, d_node, seg_density
FROM event.old_meter_event;

DROP TABLE event.old_meter_event;
DROP TABLE event.meter_phase;
DROP TABLE event.meter_queue_state;
DROP TABLE event.meter_limit_control;

CREATE VIEW meter_event_view AS
    SELECT me.id, event_date, ed.description, ramp_meter,
           mp.description AS phase, qs.description AS q_state, q_len, dem_adj,
           wait_secs, lc.description AS limit_ctrl, min_rate, rel_rate,
           max_rate, d_node, seg_density
    FROM event.meter_event me
    JOIN event.event_description ed ON me.event_desc = ed.event_desc_id
    JOIN iris.metering_phase mp ON phase = mp.id
    JOIN iris.meter_queue_state qs ON q_state = qs.id
    JOIN iris.meter_limit_control lc ON limit_ctrl = lc.id;
GRANT SELECT ON meter_event_view TO PUBLIC;

-- Rework `day_matcher` / `day_plan`
ALTER TABLE iris.day_plan ADD COLUMN holidays BOOLEAN;
UPDATE iris.day_plan SET holidays = true;
ALTER TABLE iris.day_plan ALTER COLUMN holidays SET NOT NULL;

ALTER TABLE iris.day_plan_day_matcher
    DROP CONSTRAINT day_plan_day_matcher_day_matcher_fkey;

ALTER TABLE iris.day_matcher RENAME TO old_day_matcher;

CREATE TABLE iris.day_matcher (
    name VARCHAR(10) PRIMARY KEY,
    day_plan VARCHAR(10) NOT NULL REFERENCES iris.day_plan,
    month INTEGER CHECK (month >= 1 AND month <= 12),
    day INTEGER CHECK (day >= 1 AND day <= 31),
    weekday INTEGER CHECK (weekday >= 1 AND weekday <= 7),
    week INTEGER CHECK (week >= 1 AND week <= 4 OR week = -1),
    shift INTEGER CHECK (shift >= -2 AND shift <= 2 AND shift != 0),

    CONSTRAINT day_matcher_valid CHECK (
        (COALESCE(month, day, weekday, week) IS NOT NULL) AND
        (day IS NULL OR week IS NULL) AND
        (shift IS NULL OR (weekday IS NOT NULL AND week IS NOT NULL))
    )
);

INSERT INTO iris.day_matcher (
    name, day_plan, month, day, weekday, week, shift
)
(
SELECT 'dm_' || ROW_NUMBER() OVER (ORDER BY m.day_plan, day_matcher),
    m.day_plan, NULLIF(month + 1, 0), NULLIF(day, 0), NULLIF(weekday, 0),
    NULLIF(week, 0), NULLIF(shift, 0)
FROM iris.day_plan_day_matcher m
JOIN iris.old_day_matcher dm ON m.day_matcher = dm.name
WHERE month >= 0 OR day > 0 OR weekday > 0 OR week != 0
);

UPDATE iris.day_plan SET holidays = false WHERE name IN (
    SELECT m.day_plan
    FROM iris.day_plan_day_matcher m
    JOIN iris.old_day_matcher dm ON m.day_matcher = dm.name
    WHERE dm.holiday = false AND (
        month >= 0 OR day > 0 OR weekday > 0 OR week != 0
    )
);

DROP TABLE iris.day_plan_day_matcher;
DROP TABLE iris.old_day_matcher;

CREATE FUNCTION iris.day_plan_notify() RETURNS TRIGGER AS
    $day_plan_notify$
BEGIN
    NOTIFY day_plan;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$day_plan_notify$ LANGUAGE plpgsql;

CREATE TRIGGER day_plan_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.day_plan
    FOR EACH STATEMENT EXECUTE FUNCTION iris.day_plan_notify();

CREATE TRIGGER day_matcher_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.day_matcher
    FOR EACH STATEMENT EXECUTE FUNCTION iris.day_plan_notify();

CREATE VIEW day_plan_view AS
    SELECT p.name, holidays, month, day, weekday, week, shift
    FROM iris.day_plan p
    JOIN iris.day_matcher m ON m.day_plan = p.name;
GRANT SELECT ON day_plan_view TO PUBLIC;

-- Reserve protocol value for Sierra SSH GPS modem
INSERT INTO iris.comm_protocol (id, description)
    VALUES (46, 'Sierra SSH GPS');

COMMIT;
