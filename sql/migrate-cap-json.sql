\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Change comm protocol 23 to CAP-NWS
UPDATE iris.comm_protocol
    SET description = 'CAP-NWS'
    WHERE id = 23;

UPDATE iris.comm_protocol
    SET description = 'CAP-IPAWS'
    WHERE id = 42;

COMMIT;
