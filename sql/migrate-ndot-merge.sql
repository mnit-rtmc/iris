\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add Road Surface direction used for many camera presets in Nebraska
INSERT INTO iris.direction (id, direction, dir) VALUES (7, 'SUR', 'SUR');

COMMIT;
