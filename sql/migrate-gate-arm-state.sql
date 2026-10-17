\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Remove unused gate arm state 4
INSERT INTO iris.gate_arm_state (id, description) VALUES (4, 'closing');
UPDATE iris._gate_arm SET arm_state = 4 WHERE arm_state = 5;
UPDATE iris.gate_arm_state SET description = 'closed' WHERE id = 5;
UPDATE iris._gate_arm SET arm_state = 5 WHERE arm_state = 6;
DELETE FROM iris.gate_arm_state WHERE id = 6;

COMMIT;
