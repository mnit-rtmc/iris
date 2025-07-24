\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.71.0', '5.72.0');

ALTER TABLE iris.device_action ADD CONSTRAINT device_action_msg_priority_check
    CHECK (msg_priority >= 1 AND msg_priority <= 15);

-- DROP duration from sign_event
DROP VIEW recent_sign_event_view;
DROP VIEW sign_event_view;

ALTER TABLE event.sign_event DROP COLUMN duration;

CREATE VIEW sign_event_view AS
    SELECT event_id, event_date, description, device_id,
           event.multi_message(multi) as message, multi, msg_owner
    FROM event.sign_event JOIN event.event_description
    ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
    SELECT event_id, event_date, description, device_id, message, multi,
           msg_owner
    FROM sign_event_view
    WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

COMMIT;
