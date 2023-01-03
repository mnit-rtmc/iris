\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Update protocols to make one for NDOT beacons
UPDATE iris.comm_protocol SET description = 'NDOT Beacon'
    WHERE id=24;

COMMIT;
