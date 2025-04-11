\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.69.0', '5.70.0');

-- Change comm protocol 26 to Campbell Cloud
UPDATE iris.comm_protocol
    SET description = 'CampbellCloud'
    WHERE id = 26;

COMMIT;
