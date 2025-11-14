\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Change comm protocol 31 to RTMS Echo
UPDATE iris.comm_protocol
    SET description = 'RTMS Echo vlog'
    WHERE id = 31;

COMMIT;
