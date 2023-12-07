\set ON_ERROR_STOP

BEGIN;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.46.0', '5.47.0');

-- Create trigger for word changes
CREATE TRIGGER word_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.word
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

-- Add hashtag/access constraint
ALTER TABLE iris.permission
    ADD CONSTRAINT hashtag_access_ck
    CHECK (hashtag IS NULL OR access_n != 1);

-- Drop version_id from font
DROP VIEW font_view;

ALTER TABLE iris.font DROP COLUMN version_id;

CREATE VIEW font_view AS
    SELECT name, f_number, height, width, line_spacing, char_spacing
    FROM iris.font;
GRANT SELECT ON font_view TO PUBLIC;

-- Fix error in MULTI supported tag bits
UPDATE iris.multi_tag SET tag = 'f13' WHERE bit = 26;
UPDATE iris.multi_tag SET tag = 'tr' WHERE bit = 27;
UPDATE iris.multi_tag SET tag = 'cr' WHERE bit = 28;
INSERT INTO iris.multi_tag (bit, tag) VALUES (29, 'pb');

-- Simplify NOTIFY triggers
DROP TRIGGER resource_notify_trig ON iris.hashtag;
DROP TRIGGER resource_notify_trig ON iris.geo_loc;
DROP TRIGGER resource_notify_trig ON iris.controller_io;

CREATE OR REPLACE FUNCTION iris.resource_notify() RETURNS TRIGGER AS
    $resource_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        PERFORM pg_notify(OLD.resource_n, '');
    ELSE
        PERFORM pg_notify(NEW.resource_n, '');
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$resource_notify$ LANGUAGE plpgsql;

CREATE TRIGGER resource_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.hashtag
    FOR EACH ROW EXECUTE FUNCTION iris.resource_notify();

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.geo_loc
    FOR EACH ROW EXECUTE FUNCTION iris.resource_notify();

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.controller_io
    FOR EACH ROW EXECUTE FUNCTION iris.resource_notify();

DROP TRIGGER road_class_notify_trig ON iris.road_class;
DROP TRIGGER road_class_table_notify_trig ON iris.road_class;
DROP FUNCTION iris.road_class_notify();

DROP TRIGGER road_notify_trig ON iris.road;
DROP TRIGGER road_table_notify_trig ON iris.road;

CREATE OR REPLACE FUNCTION iris.road_notify() RETURNS TRIGGER AS
    $road_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        PERFORM pg_notify('road$1', OLD.name);
    ELSE
        PERFORM pg_notify('road$1', NEW.name);
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
        PERFORM pg_notify('r_node$1', OLD.name);
    ELSE
        PERFORM pg_notify('r_node$1', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$r_node_notify$ LANGUAGE plpgsql;

DROP TRIGGER font_notify_trig ON iris.font;
DROP TRIGGER glyph_notify_trig ON iris.glyph;

CREATE OR REPLACE FUNCTION iris.comm_link_notify() RETURNS TRIGGER AS
    $comm_link_notify$
BEGIN
    IF (NEW.description IS DISTINCT FROM OLD.description) OR
       (NEW.uri IS DISTINCT FROM OLD.uri) OR
       (NEW.poll_enabled IS DISTINCT FROM OLD.poll_enabled) OR
       (NEW.comm_config IS DISTINCT FROM OLD.comm_config) OR
       (NEW.connected IS DISTINCT FROM OLD.connected)
    THEN
        NOTIFY comm_link;
    END IF;
    PERFORM pg_notify('comm_link$1', NEW.name);
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
       (NEW.setup IS DISTINCT FROM OLD.setup) OR
       (NEW.fail_time IS DISTINCT FROM OLD.fail_time)
    THEN
        NOTIFY controller;
    END IF;
    PERFORM pg_notify('controller$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
    $camera_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.cam_num IS DISTINCT FROM OLD.cam_num) OR
       (NEW.publish IS DISTINCT FROM OLD.publish)
    THEN
        NOTIFY camera;
    END IF;
    PERFORM pg_notify('camera$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.beacon_notify() RETURNS TRIGGER AS
    $beacon_notify$
BEGIN
    IF (NEW.message IS DISTINCT FROM OLD.message) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.state IS DISTINCT FROM OLD.state)
    THEN
        NOTIFY beacon;
    END IF;
    PERFORM pg_notify('beacon$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.detector_notify() RETURNS TRIGGER AS
    $detector_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        NOTIFY detector;
    END IF;
    PERFORM pg_notify('detector$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$detector_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.gps_notify() RETURNS TRIGGER AS
    $gps_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        NOTIFY gps;
    END IF;
    PERFORM pg_notify('gps$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gps_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
    $dms_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.msg_current IS DISTINCT FROM OLD.msg_current) OR
       (NEW.status IS DISTINCT FROM OLD.status)
    THEN
        NOTIFY dms;
    END IF;
    PERFORM pg_notify('dms$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.gate_arm_array_notify() RETURNS TRIGGER AS
    $gate_arm_array_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.arm_state IS DISTINCT FROM OLD.arm_state) OR
       (NEW.interlock IS DISTINCT FROM OLD.interlock)
    THEN
        NOTIFY gate_arm_array;
    END IF;
    PERFORM pg_notify('gate_arm_array$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gate_arm_array_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.parking_area_notify() RETURNS TRIGGER AS
    $parking_area_notify$
BEGIN
    IF (NEW.time_stamp_static IS DISTINCT FROM OLD.time_stamp_static) THEN
        NOTIFY parking_area;
    END IF;
    PERFORM pg_notify('parking_area$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$parking_area_notify$ LANGUAGE plpgsql;

DROP TRIGGER ramp_meter_notify_trig ON iris._ramp_meter;

CREATE OR REPLACE FUNCTION iris.ramp_meter_notify() RETURNS TRIGGER AS
    $ramp_meter_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        NOTIFY ramp_meter;
    END IF;
    PERFORM pg_notify('ramp_meter$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$ramp_meter_notify$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_notify_trig
    AFTER UPDATE ON iris._ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_notify();

CREATE TRIGGER ramp_meter_table_notify_trig
    AFTER INSERT OR DELETE ON iris._ramp_meter
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE OR REPLACE FUNCTION iris.tag_reader_notify() RETURNS TRIGGER AS
    $tag_reader_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        NOTIFY tag_reader;
    END IF;
    PERFORM pg_notify('tag_reader$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$tag_reader_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.video_monitor_notify() RETURNS TRIGGER AS
    $video_monitor_notify$
BEGIN
    IF (NEW.mon_num IS DISTINCT FROM OLD.mon_num) OR
       (NEW.notes IS DISTINCT FROM OLD.notes)
    THEN
        NOTIFY video_monitor;
    END IF;
    PERFORM pg_notify('video_monitor$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$video_monitor_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.weather_sensor_notify() RETURNS TRIGGER AS
    $weather_sensor_notify$
BEGIN
    IF (NEW.site_id IS DISTINCT FROM OLD.site_id) OR
       (NEW.alt_id IS DISTINCT FROM OLD.alt_id) OR
       (NEW.notes IS DISTINCT FROM OLD.notes)
    THEN
        NOTIFY weather_sensor;
    END IF;
    PERFORM pg_notify('weather_sensor$1', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_notify$ LANGUAGE plpgsql;

COMMIT;
