\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.31.0', '5.32.0');

INSERT INTO iris.system_attribute (name, value)
    VALUES ('weather_sensor_event_purge_days', '90');

-- Remove batch from permission index
DROP INDEX iris.permission_role_resource_n_batch_idx;
CREATE UNIQUE INDEX permission_role_resource_n_idx
    ON iris.permission (role, resource_n);

-- Add new permissions for administrator
COPY iris.permission (role, resource_n, access_n) FROM stdin;
administrator	dms	4
administrator	flow_stream	4
administrator	gate_arm	4
administrator	gate_arm_array	4
administrator	gps	4
administrator	lcs_array	4
administrator	lcs_indication	4
\.

CREATE TRIGGER gate_arm_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

DROP TRIGGER gate_arm_array_notify_trig ON iris._gate_arm_array;

CREATE OR REPLACE FUNCTION iris.gate_arm_array_notify() RETURNS TRIGGER AS
    $gate_arm_array_notify$
BEGIN
    IF (NEW.arm_state IS DISTINCT FROM OLD.arm_state) THEN
        NOTIFY gate_arm_array, 'arm_state';
    ELSIF (NEW.interlock IS DISTINCT FROM OLD.interlock) THEN
        NOTIFY gate_arm_array, 'interlock';
    ELSE
        NOTIFY gate_arm_array;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gate_arm_array_notify$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_notify_trig
    AFTER UPDATE ON iris._gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_notify();

CREATE TRIGGER gate_arm_array_table_notify_trig
    AFTER INSERT OR DELETE ON iris._gate_arm_array
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER lcs_indication_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lcs_indication
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER lcs_array_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lcs_array
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE FUNCTION iris.gps_notify() RETURNS TRIGGER AS
    $gps_notify$
BEGIN
    IF (NEW.lat IS DISTINCT FROM OLD.lat) THEN
        NOTIFY gps, 'lat';
    ELSIF (NEW.lon IS DISTINCT FROM OLD.lon) THEN
        NOTIFY gps, 'lon';
    ELSIF (NEW.latest_sample IS DISTINCT FROM OLD.latest_sample) THEN
        NOTIFY gps, 'latest_sample';
    ELSIF (NEW.latest_poll IS DISTINCT FROM OLD.latest_poll) THEN
        NOTIFY gps, 'latest_poll';
    ELSE
        NOTIFY gps;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gps_notify$ LANGUAGE plpgsql;

CREATE TRIGGER gps_notify_trig
    AFTER UPDATE ON iris._gps
    FOR EACH ROW EXECUTE PROCEDURE iris.gps_notify();

CREATE TRIGGER gps_table_notify_trig
    AFTER INSERT OR DELETE ON iris._gps
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER flow_stream_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._flow_stream
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE INDEX ON event.weather_sensor_sample (weather_sensor);

COMMIT;
