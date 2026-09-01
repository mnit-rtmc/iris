\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Delete unimplemented action conditions
UPDATE iris.phase_action
    SET condition = 4
    WHERE condition > 4;
UPDATE iris.action_condition
    SET description = 'alarm'
    WHERE id = 4;

DELETE FROM iris.action_condition WHERE id > 4;

COMMIT;
