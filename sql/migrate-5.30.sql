\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.29.0', '5.30.0');

-- Handle NOTIFY for tables starting with underscore
CREATE OR REPLACE FUNCTION iris.table_notify() RETURNS TRIGGER AS
    $table_notify$
BEGIN
    PERFORM pg_notify(LTRIM(TG_TABLE_NAME, '_'), '');
    RETURN NULL; -- AFTER trigger return is ignored
END;
$table_notify$ LANGUAGE plpgsql;

-- Use table_notify for camera_table_notify_trig
DROP TRIGGER camera_table_notify_trig ON iris._camera;
DROP FUNCTION iris.camera_table_notify();
CREATE TRIGGER camera_table_notify_trig
    AFTER INSERT OR DELETE ON iris._camera
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Use table_notify for alarm_table_notify_trig
DROP TRIGGER alarm_notify_trig ON iris._alarm;
DROP FUNCTION iris.alarm_table_notify();
CREATE TRIGGER alarm_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._alarm
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Use table_notify for dms_table_notify_trig
DROP TRIGGER dms_table_notify_trig ON iris._dms;
DROP FUNCTION iris.dms_table_notify();
CREATE TRIGGER dms_table_notify_trig
    AFTER INSERT OR DELETE ON iris._dms
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Special-case 'settings' / 'sample' changes in weather_sensor_notify_trig
DROP TRIGGER weather_sensor_notify_trig ON iris._weather_sensor;

CREATE OR REPLACE FUNCTION iris.weather_sensor_notify() RETURNS TRIGGER AS
    $weather_sensor_notify$
BEGIN
    IF (NEW.settings IS DISTINCT FROM OLD.settings) THEN
        NOTIFY weather_sensor, 'settings';
    ELSIF (NEW.sample IS DISTINCT FROM OLD.sample) THEN
        NOTIFY weather_sensor, 'sample';
    ELSE
        NOTIFY weather_sensor;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_notify$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_notify_trig
    AFTER UPDATE ON iris._weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_notify();

CREATE TRIGGER weather_sensor_table_notify_trig
    AFTER INSERT OR DELETE ON iris._weather_sensor
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Rename geo_loc.notify_tag to resource_n
ALTER TABLE iris.geo_loc
    ADD COLUMN resource_n VARCHAR(16) REFERENCES iris.resource_type;
UPDATE iris.geo_loc SET resource_n = notify_tag::VARCHAR(16);

DROP TRIGGER geo_loc_notify_trig ON iris.geo_loc;
CREATE OR REPLACE FUNCTION iris.geo_loc_notify() RETURNS TRIGGER AS
    $geo_loc_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        IF (OLD.resource_n IS NOT NULL) THEN
            PERFORM pg_notify(OLD.resource_n, OLD.name);
        END IF;
    ELSIF (NEW.resource_n IS NOT NULL) THEN
        PERFORM pg_notify(NEW.resource_n, NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$geo_loc_notify$ LANGUAGE plpgsql;

CREATE TRIGGER geo_loc_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.geo_loc
    FOR EACH ROW EXECUTE PROCEDURE iris.geo_loc_notify();

ALTER TABLE iris.geo_loc DROP COLUMN notify_tag;

COPY iris.permission (role, resource_n, access_n) FROM stdin;
administrator	weather_sensor	4
\.

COMMIT;
