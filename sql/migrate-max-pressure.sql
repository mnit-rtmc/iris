\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add max-pressure meter algorithm value
INSERT INTO iris.meter_algorithm (id, description) VALUES
    (4, 'Max-Pressure Metering');

COMMIT;
