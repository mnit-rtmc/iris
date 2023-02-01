\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add VSL (variable speed limit) device purpose
INSERT INTO iris.device_purpose (id, description) VALUES ('7', 'VSL');

COMMIT;
