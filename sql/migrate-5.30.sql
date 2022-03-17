\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.29.0', '5.30.0');

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

-- Can't use iris.table_notify due to underscore (_weather_sensor)
CREATE FUNCTION iris.weather_sensor_table_notify() RETURNS TRIGGER AS
    $weather_sensor_table_notify$
BEGIN
    NOTIFY weather_sensor;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_table_notify$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_table_notify_trig
    AFTER INSERT OR DELETE ON iris._weather_sensor
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.weather_sensor_table_notify();

COMMIT;
