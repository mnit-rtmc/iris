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

-- Add impact constraint to incident
ALTER TABLE event.incident ADD CONSTRAINT impact_ck
    CHECK (impact ~ '^[!?\.]*$');

-- Rename iris_user to user_id in event tables
DROP VIEW client_event_view;
DROP VIEW gate_arm_event_view;

ALTER TABLE event.client_event RENAME COLUMN iris_user TO user_id;
ALTER TABLE event.gate_arm_event RENAME COLUMN iris_user TO user_id;

CREATE VIEW client_event_view AS
    SELECT e.event_id, e.event_date, ed.description, e.host_port,
           e.user_id
    FROM event.client_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON client_event_view TO PUBLIC;

CREATE VIEW gate_arm_event_view AS
    SELECT e.event_id, e.event_date, ed.description, device_id, e.user_id,
           e.fault
    FROM event.gate_arm_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id;
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

COMMIT;
