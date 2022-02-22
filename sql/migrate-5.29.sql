\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.28.0', '5.29.0');

-- Special-case 'msg_user' changes in dms_notify_trig
DROP TRIGGER dms_notify_trig ON iris._dms;

CREATE OR REPLACE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
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

-- Add permission table
INSERT INTO iris.sonar_type (name) VALUES ('permission');

CREATE TABLE iris.permission (
    id SERIAL PRIMARY KEY,
    role VARCHAR(15) NOT NULL REFERENCES iris.role ON DELETE CASCADE,
    resource_n VARCHAR(16) NOT NULL REFERENCES iris.sonar_type,
    batch VARCHAR(16),
    access_n INTEGER NOT NULL,
    CONSTRAINT permission_access_n CHECK (access_n >= 1 AND access_n <= 4)
);

COPY iris.permission (role, resource_n, access_n) FROM stdin;
administrator	alarm	4
administrator	cabinet_style	4
administrator	comm_config	4
administrator	comm_link	4
administrator	controller	4
administrator	modem	4
administrator	permission	4
\.

CREATE TRIGGER permission_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.permission
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

COMMIT;
