\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.24.0', '5.25.0');

-- Change existing TIMEOUT (308) gate arm events to UNKNOWN (301)
UPDATE event.gate_arm_event SET event_desc_id = 301 WHERE event_desc_id = 308;

-- Delete gate arm TIMEOUT event type
DELETE FROM event.event_description WHERE event_desc_id = 308;

-- Don't allow using the same action plan for more than one gate arm array
ALTER TABLE iris._gate_arm_array
    ADD CONSTRAINT _gate_arm_array_action_plan_key UNIQUE (action_plan);

COMMIT;
