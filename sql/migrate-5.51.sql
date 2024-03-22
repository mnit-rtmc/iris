\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.50.0', '5.51.0');

-- Replace resource_notify with table-specific triggers
DROP TRIGGER resource_notify_trig ON iris.hashtag;
DROP TRIGGER resource_notify_trig ON iris.geo_loc;
DROP TRIGGER resource_notify_trig ON iris.controller_io;
DROP TRIGGER resource_notify_trig ON iris.device_preset;

CREATE FUNCTION iris.hashtag_notify() RETURNS TRIGGER AS
    $hashtag_notify$
BEGIN
    PERFORM pg_notify(OLD.resource_n, '');
    RETURN NULL; -- AFTER trigger return is ignored
END;
$hashtag_notify$ LANGUAGE plpgsql;

CREATE TRIGGER hashtag_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.hashtag
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_notify();

CREATE FUNCTION iris.geo_loc_notify() RETURNS TRIGGER AS
    $geo_loc_notify$
BEGIN
    IF (NEW.roadway IS DISTINCT FROM OLD.roadway) OR
       (NEW.road_dir IS DISTINCT FROM OLD.road_dir) OR
       (NEW.cross_street IS DISTINCT FROM OLD.cross_street) OR
       (NEW.cross_dir IS DISTINCT FROM OLD.cross_dir) OR
       (NEW.cross_mod IS DISTINCT FROM OLD.cross_mod) OR
       (NEW.landmark IS DISTINCT FROM OLD.landmark)
    THEN
        PERFORM pg_notify(NEW.resource_n, '');
    ELSIF (NEW.lat IS DISTINCT FROM OLD.lat) OR
          (NEW.lon IS DISTINCT FROM OLD.lon)
    THEN
        PERFORM pg_notify(NEW.resource_n, NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$geo_loc_notify$ LANGUAGE plpgsql;

CREATE TRIGGER geo_loc_notify_trig
    AFTER UPDATE ON iris.geo_loc
    FOR EACH ROW EXECUTE FUNCTION iris.geo_loc_notify();

CREATE FUNCTION iris.controller_io_notify() RETURNS TRIGGER AS
    $controller_io_notify$
BEGIN
    IF (NEW.controller IS DISTINCT FROM OLD.controller) THEN
        PERFORM pg_notify(NEW.resource_n, '');
    ELSIF (NEW.pin IS DISTINCT FROM OLD.pin) THEN
        PERFORM pg_notify(NEW.resource_n, NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_io_notify$ LANGUAGE plpgsql;

CREATE TRIGGER controller_io_notify_trig
    AFTER UPDATE ON iris.controller_io
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_notify();

CREATE FUNCTION iris.device_preset_notify() RETURNS TRIGGER AS
    $device_preset_notify$
BEGIN
    IF (NEW.preset IS DISTINCT FROM OLD.preset) THEN
        PERFORM pg_notify(NEW.resource_n, NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$device_preset_notify$ LANGUAGE plpgsql;

CREATE TRIGGER device_preset_notify_trig
    AFTER UPDATE ON iris.device_preset
    FOR EACH ROW EXECUTE FUNCTION iris.device_preset_notify();

DROP FUNCTION iris.resource_notify();

-- Remove secondary notification channels ($1 suffix)
DROP TRIGGER road_notify_trig ON iris.road;
DROP TRIGGER r_node_notify_trig ON iris.r_node;
DROP TRIGGER comm_link_notify_trig ON iris.comm_link;
DROP TRIGGER controller_notify_trig ON iris.controller;
DROP TRIGGER camera_notify_trig ON iris._camera;
DROP TRIGGER beacon_notify_trig ON iris._beacon;
DROP TRIGGER detector_notify_trig ON iris._detector;
DROP TRIGGER gps_notify_trig ON iris._gps;
DROP TRIGGER dms_notify_trig ON iris._dms;
DROP TRIGGER gate_arm_array_notify_trig ON iris._gate_arm_array;
DROP TRIGGER parking_area_notify_trig ON iris.parking_area;
DROP TRIGGER ramp_meter_notify_trig ON iris._ramp_meter;
DROP TRIGGER tag_reader_notify_trig ON iris._tag_reader;
DROP TRIGGER video_monitor_notify_trig ON iris._video_monitor;
DROP TRIGGER weather_sensor_notify_trig ON iris._weather_sensor;

CREATE OR REPLACE FUNCTION iris.road_notify() RETURNS TRIGGER AS
    $road_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        PERFORM pg_notify('road', OLD.name);
    ELSE
        PERFORM pg_notify('road', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$road_notify$ LANGUAGE plpgsql;

CREATE TRIGGER road_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.road
    FOR EACH STATEMENT EXECUTE FUNCTION iris.road_notify();

CREATE OR REPLACE FUNCTION iris.r_node_notify() RETURNS TRIGGER AS
    $r_node_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        PERFORM pg_notify('r_node', OLD.name);
    ELSE
        PERFORM pg_notify('r_node', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$r_node_notify$ LANGUAGE plpgsql;

CREATE TRIGGER r_node_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.r_node
    FOR EACH ROW EXECUTE FUNCTION iris.r_node_notify();

CREATE OR REPLACE FUNCTION iris.comm_link_notify() RETURNS TRIGGER AS
    $comm_link_notify$
BEGIN
    NOTIFY comm_link;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$comm_link_notify$ LANGUAGE plpgsql;

CREATE TRIGGER comm_link_notify_trig
    AFTER UPDATE ON iris.comm_link
    FOR EACH ROW EXECUTE FUNCTION iris.comm_link_notify();

CREATE OR REPLACE FUNCTION iris.controller_notify() RETURNS TRIGGER AS
    $controller_notify$
BEGIN
    IF (NEW.drop_id IS DISTINCT FROM OLD.drop_id) OR
       (NEW.comm_link IS DISTINCT FROM OLD.comm_link) OR
       (NEW.cabinet_style IS DISTINCT FROM OLD.cabinet_style) OR
       (NEW.condition IS DISTINCT FROM OLD.condition) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.setup IS DISTINCT FROM OLD.setup) OR
       (NEW.fail_time IS DISTINCT FROM OLD.fail_time)
    THEN
        NOTIFY controller;
    ELSE
        PERFORM pg_notify('controller', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_notify$ LANGUAGE plpgsql;

CREATE TRIGGER controller_notify_trig
    AFTER UPDATE ON iris.controller
    FOR EACH ROW EXECUTE FUNCTION iris.controller_notify();

CREATE OR REPLACE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
    $camera_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.cam_num IS DISTINCT FROM OLD.cam_num) OR
       (NEW.publish IS DISTINCT FROM OLD.publish)
    THEN
        NOTIFY camera;
    ELSE
        PERFORM pg_notify('camera', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

CREATE TRIGGER camera_notify_trig
    AFTER UPDATE ON iris._camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_notify();

CREATE OR REPLACE FUNCTION iris.beacon_notify() RETURNS TRIGGER AS
    $beacon_notify$
BEGIN
    IF (NEW.message IS DISTINCT FROM OLD.message) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.state IS DISTINCT FROM OLD.state)
    THEN
        NOTIFY beacon;
    ELSE
        PERFORM pg_notify('beacon', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_notify$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_notify_trig
    AFTER UPDATE ON iris._beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_notify();

CREATE OR REPLACE FUNCTION iris.detector_notify() RETURNS TRIGGER AS
    $detector_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.lane_code IS DISTINCT FROM OLD.lane_code) OR
       (NEW.lane_number IS DISTINCT FROM OLD.lane_number) OR
       (NEW.abandoned IS DISTINCT FROM OLD.abandoned)
    THEN
        NOTIFY detector;
    ELSE
        PERFORM pg_notify('detector', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$detector_notify$ LANGUAGE plpgsql;

CREATE TRIGGER detector_notify_trig
    AFTER UPDATE ON iris._detector
    FOR EACH ROW EXECUTE FUNCTION iris.detector_notify();

CREATE OR REPLACE FUNCTION iris.gps_notify() RETURNS TRIGGER AS
    $gps_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        NOTIFY gps;
    ELSE
        PERFORM pg_notify('gps', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gps_notify$ LANGUAGE plpgsql;

CREATE TRIGGER gps_notify_trig
    AFTER UPDATE ON iris._gps
    FOR EACH ROW EXECUTE FUNCTION iris.gps_notify();

CREATE OR REPLACE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
    $dms_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.msg_current IS DISTINCT FROM OLD.msg_current) OR
       (NEW.status IS DISTINCT FROM OLD.status)
    THEN
        NOTIFY dms;
    ELSE
        PERFORM pg_notify('dms', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE TRIGGER dms_notify_trig
    AFTER UPDATE ON iris._dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_notify();

CREATE OR REPLACE FUNCTION iris.gate_arm_array_notify() RETURNS TRIGGER AS
    $gate_arm_array_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.arm_state IS DISTINCT FROM OLD.arm_state) OR
       (NEW.interlock IS DISTINCT FROM OLD.interlock)
    THEN
        NOTIFY gate_arm_array;
    ELSE
        PERFORM pg_notify('gate_arm_array', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gate_arm_array_notify$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_notify_trig
    AFTER UPDATE ON iris._gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_array_notify();

CREATE OR REPLACE FUNCTION iris.parking_area_notify() RETURNS TRIGGER AS
    $parking_area_notify$
BEGIN
    IF (NEW.time_stamp_static IS DISTINCT FROM OLD.time_stamp_static) THEN
        NOTIFY parking_area;
    ELSE
        PERFORM pg_notify('parking_area', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$parking_area_notify$ LANGUAGE plpgsql;

CREATE TRIGGER parking_area_notify_trig
    AFTER UPDATE ON iris.parking_area
    FOR EACH ROW EXECUTE FUNCTION iris.parking_area_notify();

CREATE OR REPLACE FUNCTION iris.ramp_meter_notify() RETURNS TRIGGER AS
    $ramp_meter_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        NOTIFY ramp_meter;
    ELSE
        PERFORM pg_notify('ramp_meter', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$ramp_meter_notify$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_notify_trig
    AFTER UPDATE ON iris._ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_notify();

CREATE OR REPLACE FUNCTION iris.tag_reader_notify() RETURNS TRIGGER AS
    $tag_reader_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        NOTIFY tag_reader;
    ELSE
        PERFORM pg_notify('tag_reader', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$tag_reader_notify$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_notify_trig
    AFTER UPDATE ON iris._tag_reader
    FOR EACH ROW EXECUTE FUNCTION iris.tag_reader_notify();

CREATE OR REPLACE FUNCTION iris.video_monitor_notify() RETURNS TRIGGER AS
    $video_monitor_notify$
BEGIN
    IF (NEW.mon_num IS DISTINCT FROM OLD.mon_num) OR
       (NEW.notes IS DISTINCT FROM OLD.notes)
    THEN
        NOTIFY video_monitor;
    ELSE
        PERFORM pg_notify('video_monitor', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$video_monitor_notify$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_notify_trig
    AFTER UPDATE ON iris._video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_notify();

CREATE OR REPLACE FUNCTION iris.weather_sensor_notify() RETURNS TRIGGER AS
    $weather_sensor_notify$
BEGIN
    IF (NEW.site_id IS DISTINCT FROM OLD.site_id) OR
       (NEW.alt_id IS DISTINCT FROM OLD.alt_id) OR
       (NEW.notes IS DISTINCT FROM OLD.notes)
    THEN
        NOTIFY weather_sensor;
    ELSE
        PERFORM pg_notify('weather_sensor', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_notify$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_notify_trig
    AFTER UPDATE ON iris._weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.weather_sensor_notify();

COMMIT;
