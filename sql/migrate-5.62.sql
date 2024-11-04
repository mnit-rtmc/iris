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

COMMIT;
