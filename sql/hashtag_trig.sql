\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add shared hashtag_trig function
CREATE FUNCTION iris.hashtag_trig() RETURNS TRIGGER AS
    $hashtag_trig$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        IF (TG_OP != 'INSERT') THEN
            DELETE FROM iris.hashtag
            WHERE resource_n = TG_ARGV[0] AND name = OLD.name;
        END IF;
        IF (TG_OP != 'DELETE') THEN
            INSERT INTO iris.hashtag (resource_n, name, hashtag)
            SELECT TG_ARGV[0], NEW.name, iris.parse_tags(NEW.notes);
        END IF;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$hashtag_trig$ LANGUAGE plpgsql;

DROP TRIGGER camera_hashtag_trig ON iris._camera;
DROP TRIGGER beacon_hashtag_trig ON iris._beacon;
DROP TRIGGER gps_hashtag_trig ON iris._gps;
DROP TRIGGER dms_hashtag_trig ON iris._dms;
DROP TRIGGER gate_arm_array_hashtag_trig ON iris._gate_arm_array;
DROP TRIGGER lane_marking_hashtag_trig ON iris._lane_marking;
DROP TRIGGER ramp_meter_hashtag_trig ON iris._ramp_meter;
DROP TRIGGER weather_sensor_hashtag_trig ON iris._weather_sensor;

DROP FUNCTION iris.camera_hashtag();
DROP FUNCTION iris.beacon_hashtag();
DROP FUNCTION iris.gps_hashtag();
DROP FUNCTION iris.dms_hashtag();
DROP FUNCTION iris.gate_arm_array_hashtag();
DROP FUNCTION iris.lane_marking_hashtag();
DROP FUNCTION iris.ramp_meter_hashtag();
DROP FUNCTION iris.weather_sensor_hashtag();

CREATE TRIGGER camera_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._camera
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('camera');

CREATE TRIGGER beacon_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._beacon
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('beacon');

CREATE TRIGGER gps_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gps
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('gps');

CREATE TRIGGER dms_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._dms
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('dms');

CREATE TRIGGER gate_arm_array_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('gate_arm_array');

CREATE TRIGGER lane_marking_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lane_marking
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('lane_marking');

CREATE TRIGGER ramp_meter_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('ramp_meter');

CREATE TRIGGER weather_sensor_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('weather_sensor');

-- NOTE: this is a new trigger
CREATE TRIGGER video_monitor_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('video_monitor');

-- NOTE: since trigger is new, force hashtags to be created
UPDATE iris.video_monitor SET notes = notes || ' ';
UPDATE iris.video_monitor SET notes = trim(notes);

COMMIT;
