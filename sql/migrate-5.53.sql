\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.52.0', '5.53.0');

-- Adds system-attribute entry for camera_latest_ptz_enable.
-- This allows enabling a camera tooltip that will show
-- which user last attempted a PTZ or preset-recall on a
-- camera and how long ago it was attempted.
INSERT INTO iris.system_attribute (name, value)
    VALUES ('camera_latest_ptz_enable', 'false');

-- Adds system-attribute entry for dms_message_tooltip_enable.
-- This allows enabling a DMS tooltip that will show the current message on
-- a sign and who posted it.
INSERT INTO iris.system_attribute (name, value)
    VALUES ('dms_message_tooltip_enable', 'false');

COMMIT;
