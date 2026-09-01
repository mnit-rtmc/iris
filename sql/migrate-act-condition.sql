\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

UPDATE iris.action_condition
    SET description = '(*unused*)'
    WHERE id = 4;

COMMIT;
