\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Update triggers for "comm" channel
CREATE OR REPLACE FUNCTION iris.comm_link_notify() RETURNS TRIGGER AS
    $comm_link_notify$
BEGIN
    IF (NEW.description IS DISTINCT FROM OLD.description) OR
       (NEW.uri IS DISTINCT FROM OLD.uri) OR
       (NEW.poll_enabled IS DISTINCT FROM OLD.poll_enabled) OR
       (NEW.comm_config IS DISTINCT FROM OLD.comm_config)
    THEN
        NOTIFY comm_link;
    ELSE
        PERFORM pg_notify('comm_link', NEW.name);
    END IF;
    IF (NEW.connected IS DISTINCT FROM OLD.connected) THEN
        -- notify "comm" channel on connected change
        PERFORM pg_notify('comm', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$comm_link_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.controller_notify() RETURNS TRIGGER AS
    $controller_notify$
BEGIN
    IF (NEW.drop_id IS DISTINCT FROM OLD.drop_id) OR
       (NEW.comm_link IS DISTINCT FROM OLD.comm_link) OR
       (NEW.cabinet_style IS DISTINCT FROM OLD.cabinet_style) OR
       (NEW.condition IS DISTINCT FROM OLD.condition) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.setup IS DISTINCT FROM OLD.setup)
    THEN
        NOTIFY controller;
    ELSE
        PERFORM pg_notify('controller', NEW.name);
    END IF;
    IF (NEW.comm_state IS DISTINCT FROM OLD.comm_state) THEN
        -- notify "comm" channel on comm_state change
        PERFORM pg_notify('comm', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_notify$ LANGUAGE plpgsql;

COMMIT;
