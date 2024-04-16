\set ON_ERROR_STOP

-- Adds system-attribute entry for camera_latest_ptz_enable.
-- This allows enabling a camera tooltip that will show
-- which user last attempted a PTZ or preset-recall on a
-- camera and how long ago it was attempted.

SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.system_attribute (name, value) VALUES ('camera_latest_ptz_enable', 'false');

COMMIT;
