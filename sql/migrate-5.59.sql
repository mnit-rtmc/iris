\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.58.0', '5.59.0');

-- Move parse_tags to iris schema
CREATE FUNCTION iris.parse_tags(notes TEXT) RETURNS SETOF TEXT AS
    $parse_tags$
BEGIN
    RETURN QUERY SELECT tag[1] FROM (
        SELECT regexp_matches(notes, '#([A-Za-z0-9]+)', 'g') AS tag
    ) AS tags;
END;
$parse_tags$ LANGUAGE plpgsql STABLE;

CREATE OR REPLACE FUNCTION iris.camera_hashtag() RETURNS TRIGGER AS
    $camera_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'camera' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'camera', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.beacon_hashtag() RETURNS TRIGGER AS
    $beacon_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'beacon' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'beacon', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.gps_hashtag() RETURNS TRIGGER AS
    $gps_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'gps' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'gps', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gps_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.dms_hashtag() RETURNS TRIGGER AS
    $dms_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'dms' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'dms', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.gate_arm_array_hashtag() RETURNS TRIGGER AS
    $gate_arm_array_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'gate_arm_array' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'gate_arm_array', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gate_arm_array_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.ramp_meter_hashtag() RETURNS TRIGGER AS
    $ramp_meter_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'ramp_meter' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'ramp_meter', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$ramp_meter_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.weather_sensor_hashtag() RETURNS TRIGGER AS
    $weather_sensor_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'weather_sensor' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'weather_sensor', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_hashtag$ LANGUAGE plpgsql;

DROP FUNCTION parse_tags(text);

-- Replace DMS stuck_pixels with pixel_failures
DROP VIEW dms_view;
DROP VIEW iris.dms;

ALTER TABLE iris._dms DROP COLUMN stuck_pixels;
ALTER TABLE iris._dms ADD COLUMN pixel_failures VARCHAR;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           beacon, preset, sign_config, sign_detail,
           msg_user, msg_sched, msg_current, expire_time, status,
           pixel_failures
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name;

CREATE OR REPLACE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'dms', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
         VALUES (NEW.name, 'dms', NEW.preset);
    INSERT INTO iris._dms (
        name, geo_loc, notes, gps, static_graphic, beacon,
        sign_config, sign_detail, msg_user, msg_sched, msg_current,
        expire_time, status, pixel_failures
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.gps, NEW.static_graphic,
        NEW.beacon, NEW.sign_config, NEW.sign_detail,
        NEW.msg_user, NEW.msg_sched, NEW.msg_current, NEW.expire_time,
        NEW.status, NEW.pixel_failures
    );
    RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_insert();

CREATE OR REPLACE FUNCTION iris.dms_update() RETURNS TRIGGER AS
    $dms_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._dms
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           gps = NEW.gps,
           static_graphic = NEW.static_graphic,
           beacon = NEW.beacon,
           sign_config = NEW.sign_config,
           sign_detail = NEW.sign_detail,
           msg_user = NEW.msg_user,
           msg_sched = NEW.msg_sched,
           msg_current = NEW.msg_current,
           expire_time = NEW.expire_time,
           status = NEW.status,
           pixel_failures = NEW.pixel_failures
     WHERE name = OLD.name;
    RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW dms_view AS
    SELECT d.name, d.geo_loc, cio.controller, cio.pin, d.notes, d.gps,
           d.sign_config, d.sign_detail, d.static_graphic, d.beacon,
           cp.camera, cp.preset_num, default_font,
           msg_user, msg_sched, msg_current, expire_time,
           status, pixel_failures,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

-- Set sign module sizes to NULL if zero
UPDATE iris.sign_config SET module_width = NULL WHERE module_width = 0;
ALTER TABLE iris.sign_config ADD CONSTRAINT sign_config_module_width_check
    CHECK (module_width > 0);

UPDATE iris.sign_config SET module_height = NULL WHERE module_height = 0;
ALTER TABLE iris.sign_config ADD CONSTRAINT sign_config_module_height_check
    CHECK (module_height > 0);

-- Make geo_loc for devices NOT NULL
ALTER TABLE iris._camera ALTER COLUMN geo_loc SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.camera_update() RETURNS TRIGGER AS
    $camera_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._camera
       SET notes = NEW.notes,
           cam_num = NEW.cam_num,
           cam_template = NEW.cam_template,
           encoder_type = NEW.encoder_type,
           enc_address = NEW.enc_address,
           enc_port = NEW.enc_port,
           enc_mcast = NEW.enc_mcast,
           enc_channel = NEW.enc_channel,
           publish = NEW.publish,
           video_loss = NEW.video_loss
     WHERE name = OLD.name;
    RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

ALTER TABLE iris._beacon ALTER COLUMN geo_loc SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
    $beacon_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._beacon
       SET notes = NEW.notes,
           message = NEW.message,
           verify_pin = NEW.verify_pin,
           ext_mode = NEW.ext_mode,
           state = NEW.state
     WHERE name = OLD.name;
    RETURN NEW;
END;
$beacon_update$ LANGUAGE plpgsql;

ALTER TABLE iris._dms ALTER COLUMN geo_loc SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.dms_update() RETURNS TRIGGER AS
    $dms_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._dms
       SET notes = NEW.notes,
           gps = NEW.gps,
           static_graphic = NEW.static_graphic,
           beacon = NEW.beacon,
           sign_config = NEW.sign_config,
           sign_detail = NEW.sign_detail,
           msg_user = NEW.msg_user,
           msg_sched = NEW.msg_sched,
           msg_current = NEW.msg_current,
           expire_time = NEW.expire_time,
           status = NEW.status,
           pixel_failures = NEW.pixel_failures
     WHERE name = OLD.name;
    RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

ALTER TABLE iris._gate_arm_array ALTER COLUMN geo_loc SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.gate_arm_array_update() RETURNS TRIGGER AS
    $gate_arm_array_update$
BEGIN
    UPDATE iris.controller_io SET controller = NEW.controller, pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._gate_arm_array
       SET notes = NEW.notes,
           opposing = NEW.opposing,
           prereq = NEW.prereq,
           camera = NEW.camera,
           approach = NEW.approach,
           action_plan = NEW.action_plan,
           arm_state = NEW.arm_state,
           interlock = NEW.interlock
     WHERE name = OLD.name;
    RETURN NEW;
END;
$gate_arm_array_update$ LANGUAGE plpgsql;

ALTER TABLE iris._lane_marking ALTER COLUMN geo_loc SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.lane_marking_update() RETURNS TRIGGER AS
    $lane_marking_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._lane_marking
       SET notes = NEW.notes,
           deployed = NEW.deployed
     WHERE name = OLD.name;
    RETURN NEW;
END;
$lane_marking_update$ LANGUAGE plpgsql;

ALTER TABLE iris._ramp_meter ALTER COLUMN geo_loc SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.ramp_meter_update() RETURNS TRIGGER AS
    $ramp_meter_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._ramp_meter
       SET notes = NEW.notes,
           meter_type = NEW.meter_type,
           storage = NEW.storage,
           max_wait = NEW.max_wait,
           algorithm = NEW.algorithm,
           am_target = NEW.am_target,
           pm_target = NEW.pm_target,
           beacon = NEW.beacon,
           m_lock = NEW.m_lock
     WHERE name = OLD.name;
    RETURN NEW;
END;
$ramp_meter_update$ LANGUAGE plpgsql;

ALTER TABLE iris._tag_reader ALTER COLUMN geo_loc SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.tag_reader_update() RETURNS TRIGGER AS
    $tag_reader_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._tag_reader
       SET notes = NEW.notes,
           toll_zone = NEW.toll_zone,
           settings = NEW.settings
     WHERE name = OLD.name;
    RETURN NEW;
END;
$tag_reader_update$ LANGUAGE plpgsql;

ALTER TABLE iris._weather_sensor ALTER COLUMN geo_loc SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.weather_sensor_update() RETURNS TRIGGER AS
    $weather_sensor_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._weather_sensor
       SET site_id = NEW.site_id,
           alt_id = NEW.alt_id,
           notes = NEW.notes,
           settings = NEW.settings,
           sample = NEW.sample,
           sample_time = NEW.sample_time
     WHERE name = OLD.name;
    RETURN NEW;
END;
$weather_sensor_update$ LANGUAGE plpgsql;

COMMIT;
