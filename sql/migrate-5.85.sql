\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.84.0', '5.85.0');

-- Change alarm base resource **BACK** to comm_config
UPDATE iris.resource_type SET base = 'comm_config' WHERE name = 'alarm';

INSERT INTO iris.action_condition (id, description)
VALUES (6, 'alarm');

-- Remove hold_time and next_phase from plan_phase
ALTER TABLE iris.plan_phase DROP COLUMN hold_time;
ALTER TABLE iris.plan_phase DROP COLUMN next_phase;

COMMIT;
