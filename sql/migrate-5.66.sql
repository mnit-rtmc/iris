\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.65.0', '5.66.0');

UPDATE iris.meter_fault
    SET description = 'missing state'
    WHERE id = 3;

COMMIT;
