\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.77.0', '5.78.0');

-- Change comm protocol 31 to RTMS Echo
UPDATE iris.comm_protocol
    SET description = 'RTMS Echo vlog'
    WHERE id = 31;

COMMIT;
