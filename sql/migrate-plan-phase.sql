\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Remove hold_time and next_phase from plan_phase
ALTER TABLE iris.plan_phase DROP COLUMN hold_time;
ALTER TABLE iris.plan_phase DROP COLUMN next_phase;

COMMIT;
