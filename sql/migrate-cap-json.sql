\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Change comm protocol 23 to CAP-JSON
UPDATE iris.comm_protocol
    SET description = 'CAP-JSON'
    WHERE id = 23;

UPDATE iris.comm_protocol
    SET description = 'CAP-XML'
    WHERE id = 42;

COMMIT;
