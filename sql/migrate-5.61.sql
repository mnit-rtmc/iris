\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.60.0', '5.61.0');

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

-- Change type of camera notes column
DROP VIEW camera_view;
DROP VIEW iris.camera;

ALTER TABLE iris._camera ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._camera ADD CONSTRAINT _camera_notes_check
    CHECK (LENGTH(notes) < 256);

CREATE VIEW iris.camera AS
    SELECT c.name, geo_loc, controller, pin, notes, cam_num, cam_template,
           encoder_type, enc_address, enc_port, enc_mcast, enc_channel,
           publish, video_loss
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_insert();

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_update();

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW camera_view AS
    SELECT c.name, cam_num, c.cam_template, encoder_type, et.make, et.model,
           et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
           c.publish, c.video_loss, c.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name
    LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

COMMIT;
