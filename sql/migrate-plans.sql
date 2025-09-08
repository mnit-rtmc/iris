\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add selectable to plan phase table
ALTER TABLE iris.plan_phase ADD COLUMN selectable BOOLEAN;
UPDATE iris.plan_phase SET selectable = true;
ALTER TABLE iris.plan_phase ALTER COLUMN selectable SET NOT NULL;

-- Allow phase hold time to be NULL
ALTER TABLE iris.plan_phase ALTER COLUMN hold_time DROP NOT NULL;
UPDATE iris.plan_phase SET hold_time = NULL WHERE hold_time < 1;
ALTER TABLE iris.plan_phase ADD CONSTRAINT plan_phase_hold_time_check
    CHECK (hold_time >= 1 AND hold_time <= 600);

COMMIT;
