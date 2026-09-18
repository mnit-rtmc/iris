\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Insert date-time action condition
INSERT INTO iris.action_condition (id, description)
    VALUES (5, 'alarm');
UPDATE iris.phase_action SET condition = 5 WHERE condition = 4;
UPDATE iris.action_condition SET description = 'RWIS threshold' WHERE id = 4;
UPDATE iris.phase_action SET condition = 4 WHERE condition = 3;
UPDATE iris.action_condition SET description = 'traffic threshold' WHERE id = 3;
UPDATE iris.phase_action SET condition = 3 WHERE condition = 2;
UPDATE iris.action_condition SET description = 'date-time' WHERE id = 2;
UPDATE iris.phase_action SET condition = 2
    WHERE condition = 1 AND params LIKE '%T%';

ALTER TABLE iris.phase_action ADD
    CONSTRAINT day_ck CHECK ((day_plan IS NULL) OR (condition != 2));

-- Add symbol to action_condition
ALTER TABLE iris.action_condition ADD COLUMN symbol VARCHAR;
UPDATE iris.action_condition SET symbol = '⏳' WHERE id = 0;
UPDATE iris.action_condition SET symbol = '⏰' WHERE id = 1;
UPDATE iris.action_condition SET symbol = '🗓️' WHERE id = 2;
UPDATE iris.action_condition SET symbol = '🚗' WHERE id = 3;
UPDATE iris.action_condition SET symbol = '🌦️' WHERE id = 4;
UPDATE iris.action_condition SET symbol = '🔔' WHERE id = 5;
ALTER TABLE iris.action_condition ALTER COLUMN symbol SET NOT NULL;

COMMIT;
