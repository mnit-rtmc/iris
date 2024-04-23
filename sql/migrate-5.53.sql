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

-- Don't notify DMS for all status updates (just faults)
CREATE OR REPLACE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
    $dms_notify$
BEGIN
    -- has_faults is derived from status (secondary attribute)
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.msg_current IS DISTINCT FROM OLD.msg_current) OR
       ((NEW.status->>'faults' IS NOT NULL) IS DISTINCT FROM
        (OLD.status->>'faults' IS NOT NULL))
    THEN
        NOTIFY dms;
    ELSE
        PERFORM pg_notify('dms', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

COMMIT;
