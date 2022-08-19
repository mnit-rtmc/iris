\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.32.0', '5.33.0');

-- Update comm_link_notify trigger
CREATE OR REPLACE FUNCTION iris.comm_link_notify() RETURNS TRIGGER AS
    $comm_link_notify$
BEGIN
    IF (NEW.connected IS DISTINCT FROM OLD.connected) THEN
        NOTIFY comm_link, 'connected';
    ELSE
        NOTIFY comm_link;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$comm_link_notify$ LANGUAGE plpgsql;
COMMIT;
