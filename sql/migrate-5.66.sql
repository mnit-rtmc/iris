\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.65.0', '5.66.0');

UPDATE iris.meter_fault
    SET description = 'missing state'
    WHERE id = 3;

UPDATE iris.comm_protocol
    SET description = 'Central Park'
    WHERE id = 21;

ALTER TABLE iris.controller ALTER COLUMN password TYPE VARCHAR;
ALTER TABLE iris.controller ADD CONSTRAINT controller_password_check
    CHECK (LENGTH(password) < 128);

COMMIT;
