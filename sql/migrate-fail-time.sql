\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Fix controller `fail_time` as primary attribute
CREATE OR REPLACE FUNCTION iris.controller_notify() RETURNS TRIGGER AS
    $controller_notify$
BEGIN
    IF (NEW.drop_id IS DISTINCT FROM OLD.drop_id) OR
       (NEW.comm_link IS DISTINCT FROM OLD.comm_link) OR
       (NEW.cabinet_style IS DISTINCT FROM OLD.cabinet_style) OR
       (NEW.condition IS DISTINCT FROM OLD.condition) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.status IS DISTINCT FROM OLD.status) OR
       (NEW.fail_time IS DISTINCT FROM OLD.fail_time)
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
