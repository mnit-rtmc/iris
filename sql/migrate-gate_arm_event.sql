\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- DROP user_id from gate_arm_event
DROP VIEW gate_arm_event_view;

ALTER TABLE event.gate_arm_event DROP COLUMN user_id;

CREATE VIEW gate_arm_event_view AS
    SELECT ev.id, event_date, ed.description, device_id, fault
    FROM event.gate_arm_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON gate_arm_event_view TO PUBLIC;

COMMIT;
