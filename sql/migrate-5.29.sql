\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.28.0', '5.29.0');

-- Special-case 'msg_user' changes in dms_notify_trig
DROP TRIGGER dms_notify_trig ON iris._dms;

CREATE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
    $dms_notify$
BEGIN
    IF (NEW.msg_user IS DISTINCT FROM OLD.msg_user) THEN
        NOTIFY dms, 'msg_user';
    ELSIF (NEW.msg_sched IS DISTINCT FROM OLD.msg_sched) THEN
        NOTIFY dms, 'msg_sched';
    ELSIF (NEW.msg_current IS DISTINCT FROM OLD.msg_current) THEN
        NOTIFY dms, 'msg_current';
    ELSIF (NEW.expire_time IS DISTINCT FROM OLD.expire_time) THEN
        NOTIFY dms, 'expire_time';
    ELSE
        NOTIFY dms;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE TRIGGER dms_notify_trig
    AFTER UPDATE ON iris._dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_notify();

COMMIT;
