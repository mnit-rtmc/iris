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
ALTER TABLE iris.geo_loc ALTER COLUMN resource_n SET NOT NULL;

DROP TRIGGER geo_loc_notify_trig ON iris.geo_loc;
DROP FUNCTION iris.geo_loc_notify();

CREATE FUNCTION iris.resource_notify() RETURNS TRIGGER AS
    $resource_notify$
BEGIN
    PERFORM pg_notify(NEW.resource_n, NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$resource_notify$ LANGUAGE plpgsql;

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.geo_loc
    FOR EACH ROW EXECUTE PROCEDURE iris.resource_notify();

ALTER TABLE iris.geo_loc DROP COLUMN notify_tag;

COPY iris.permission (role, resource_n, access_n) FROM stdin;
administrator	beacon	4
administrator	lane_marking	4
administrator	weather_sensor	4
\.

DROP VIEW beacon_view;
DROP VIEW iris.beacon;
DROP FUNCTION iris.beacon_insert();
DROP FUNCTION iris.beacon_update();

-- Change beacon.message to VARCHAR(128)
ALTER TABLE iris._beacon RENAME COLUMN message to old_message;
ALTER TABLE iris._beacon ADD COLUMN message VARCHAR(128);
UPDATE iris._beacon SET message = old_message::VARCHAR(128);
ALTER TABLE iris._beacon ALTER COLUMN message SET NOT NULL;
ALTER TABLE iris._beacon DROP COLUMN old_message;

-- Change beacon.notes to VARCHAR(128)
ALTER TABLE iris._beacon RENAME COLUMN notes to old_notes;
ALTER TABLE iris._beacon ADD COLUMN notes VARCHAR(128);
UPDATE iris._beacon SET notes = old_notes::VARCHAR(128);
ALTER TABLE iris._beacon ALTER COLUMN notes SET NOT NULL;
ALTER TABLE iris._beacon DROP COLUMN old_notes;

-- Add flashing column to beacon
ALTER TABLE iris._beacon ADD COLUMN flashing BOOLEAN;
UPDATE iris._beacon SET flashing = false;
ALTER TABLE iris._beacon ALTER COLUMN flashing SET NOT NULL;

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin, preset,
           flashing
    FROM iris._beacon b
    JOIN iris.controller_io cio ON b.name = cio.name
    JOIN iris._device_preset p ON b.name = p.name;

CREATE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
    $beacon_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._beacon
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           message = NEW.message,
           verify_pin = NEW.verify_pin,
           flashing = NEW.flashing
     WHERE name = OLD.name;
    RETURN NEW;
END;
$beacon_update$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_update_trig
    INSTEAD OF UPDATE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_update();

CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW beacon_view AS
    SELECT b.name, b.notes, b.message, p.camera, p.preset_num, b.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           b.controller, b.pin, b.verify_pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, flashing
    FROM iris.beacon b
    LEFT JOIN iris.camera_preset p ON b.preset = p.name
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN controller_view ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

-- Special-case 'flashing' changes in beacon_notify_trig
DROP TRIGGER beacon_notify_trig ON iris._beacon;

CREATE OR REPLACE FUNCTION iris.beacon_notify() RETURNS TRIGGER AS
    $beacon_notify$
BEGIN
    IF (NEW.flashing IS DISTINCT FROM OLD.flashing) THEN
        NOTIFY beacon, 'flashing';
    ELSE
        NOTIFY beacon;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_notify$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_notify_trig
    AFTER UPDATE ON iris._beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_notify();

CREATE TRIGGER beacon_table_notify_trig
    AFTER INSERT OR DELETE ON iris._beacon
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Add deployed column to lane_marking
DROP VIEW lane_marking_view;
DROP VIEW iris.lane_marking;
DROP FUNCTION iris.lane_marking_update();

CREATE TRIGGER lane_marking_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lane_marking
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

ALTER TABLE iris._lane_marking ADD COLUMN deployed BOOLEAN;
UPDATE iris._lane_marking SET deployed = false;
ALTER TABLE iris._lane_marking ALTER COLUMN deployed SET NOT NULL;

CREATE VIEW iris.lane_marking AS
    SELECT m.name, geo_loc, controller, pin, notes, deployed
    FROM iris._lane_marking m
    JOIN iris.controller_io cio ON m.name = cio.name;

CREATE FUNCTION iris.lane_marking_update() RETURNS TRIGGER AS
    $lane_marking_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._lane_marking
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           deployed = NEW.deployed
     WHERE name = OLD.name;
    RETURN NEW;
END;
$lane_marking_update$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_update_trig
    INSTEAD OF UPDATE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_update();

CREATE TRIGGER lane_marking_delete_trig
    INSTEAD OF DELETE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW lane_marking_view AS
    SELECT m.name, m.notes, m.geo_loc, l.roadway, l.road_dir, l.cross_mod,
           l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon, l.corridor,
           l.location, m.controller, m.pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, m.deployed
    FROM iris.lane_marking m
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
    LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

-- Add resource_n to controller_io
ALTER TABLE iris.controller_io
    ADD COLUMN resource_n VARCHAR(16) REFERENCES iris.resource_type;
UPDATE iris.controller_io SET resource_n = 'alarm'
    WHERE name IN (SELECT name FROM iris.alarm);
UPDATE iris.controller_io SET resource_n = 'beacon'
    WHERE name IN (SELECT name FROM iris.beacon);
UPDATE iris.controller_io SET resource_n = 'camera'
    WHERE name IN (SELECT name FROM iris.camera);
UPDATE iris.controller_io SET resource_n = 'detector'
    WHERE name IN (SELECT name FROM iris.detector);
UPDATE iris.controller_io SET resource_n = 'dms'
    WHERE name IN (SELECT name FROM iris.dms);
UPDATE iris.controller_io SET resource_n = 'flow_stream'
    WHERE name IN (SELECT name FROM iris.flow_stream);
UPDATE iris.controller_io SET resource_n = 'gate_arm'
    WHERE name IN (SELECT name FROM iris.gate_arm);
UPDATE iris.controller_io SET resource_n = 'gate_arm_array'
    WHERE name IN (SELECT name FROM iris.gate_arm_array);
UPDATE iris.controller_io SET resource_n = 'gps'
    WHERE name IN (SELECT name FROM iris.gps);
UPDATE iris.controller_io SET resource_n = 'lane_marking'
    WHERE name IN (SELECT name FROM iris.lane_marking);
UPDATE iris.controller_io SET resource_n = 'lcs_array'
    WHERE name IN (SELECT name FROM iris.lcs_array);
UPDATE iris.controller_io SET resource_n = 'lcs_indication'
    WHERE name IN (SELECT name FROM iris.lcs_indication);
UPDATE iris.controller_io SET resource_n = 'ramp_meter'
    WHERE name IN (SELECT name FROM iris.ramp_meter);
UPDATE iris.controller_io SET resource_n = 'tag_reader'
    WHERE name IN (SELECT name FROM iris.tag_reader);
UPDATE iris.controller_io SET resource_n = 'video_monitor'
    WHERE name IN (SELECT name FROM iris.video_monitor);
UPDATE iris.controller_io SET resource_n = 'weather_sensor'
    WHERE name IN (SELECT name FROM iris.weather_sensor);
ALTER TABLE iris.controller_io ALTER COLUMN resource_n SET NOT NULL;

CREATE TRIGGER controller_io_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.controller_io
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.controller_io
    FOR EACH ROW EXECUTE PROCEDURE iris.resource_notify();

DROP VIEW controller_io_view;
CREATE VIEW controller_io_view AS
    SELECT name, resource_n, controller, pin
    FROM iris.controller_io;
GRANT SELECT ON controller_io_view TO PUBLIC;

DROP TRIGGER alarm_insert_trig ON iris.alarm;
CREATE OR REPLACE FUNCTION iris.alarm_insert() RETURNS TRIGGER AS
    $alarm_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'alarm', NEW.controller, NEW.pin);
    INSERT INTO iris._alarm (name, description, state, trigger_time)
         VALUES (NEW.name, NEW.description, NEW.state, NEW.trigger_time);
    RETURN NEW;
END;
$alarm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_insert_trig
    INSTEAD OF INSERT ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_insert();

CREATE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
    $beacon_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
        VALUES (NEW.name, 'beacon', NEW.controller, NEW.pin);
    INSERT INTO iris._device_preset (name, preset)
        VALUES (NEW.name, NEW.preset);
    INSERT INTO iris._beacon (name, geo_loc, notes, message, verify_pin,
                              flashing)
        VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message,
                NEW.verify_pin, NEW.flashing);
    RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_insert();

DROP TRIGGER camera_insert_trig ON iris.camera;
CREATE OR REPLACE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
    $camera_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'camera', NEW.controller, NEW.pin);
    INSERT INTO iris._camera (
        name, geo_loc, notes, cam_num, cam_template, encoder_type, enc_address,
        enc_port, enc_mcast, enc_channel, publish, streamable, video_loss
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num, NEW.cam_template,
        NEW.encoder_type, NEW.enc_address, NEW.enc_port, NEW.enc_mcast,
        NEW.enc_channel, NEW.publish, NEW.streamable, NEW.video_loss
    );
    RETURN NEW;
END;
$camera_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_insert();

DROP TRIGGER detector_insert_trig ON iris.detector;
CREATE OR REPLACE FUNCTION iris.detector_insert() RETURNS TRIGGER AS
    $detector_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'detector', NEW.controller, NEW.pin);
    INSERT INTO iris._detector (
        name, r_node, lane_type, lane_number, abandoned, force_fail, auto_fail,
        field_length, fake, notes
    ) VALUES (
        NEW.name, NEW.r_node, NEW.lane_type, NEW.lane_number, NEW.abandoned,
        NEW.force_fail, NEW.auto_fail, NEW.field_length, NEW.fake, NEW.notes
    );
    RETURN NEW;
END;
$detector_insert$ LANGUAGE plpgsql;

CREATE TRIGGER detector_insert_trig
    INSTEAD OF INSERT ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_insert();

DROP TRIGGER gps_insert_trig ON iris.gps;
CREATE OR REPLACE FUNCTION iris.gps_insert() RETURNS TRIGGER AS
    $gps_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'gps', NEW.controller, NEW.pin);
    INSERT INTO iris._gps (name, notes, latest_poll, latest_sample, lat,lon)
         VALUES (NEW.name, NEW.notes, NEW.latest_poll, NEW.latest_sample,
                 NEW.lat, NEW.lon);
    RETURN NEW;
END;
$gps_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gps_insert_trig
    INSTEAD OF INSERT ON iris.gps
    FOR EACH ROW EXECUTE PROCEDURE iris.gps_insert();

DROP TRIGGER dms_insert_trig ON iris.dms;
CREATE OR REPLACE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'dms', NEW.controller, NEW.pin);
    INSERT INTO iris._device_preset (name, preset)
         VALUES (NEW.name, NEW.preset);
    INSERT INTO iris._dms (
        name, geo_loc, notes, gps, static_graphic, purpose, hidden, beacon,
        sign_config, sign_detail, override_font, override_foreground,
        override_background, msg_user, msg_sched, msg_current, expire_time
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.gps, NEW.static_graphic,
        NEW.purpose, NEW.hidden, NEW.beacon, NEW.sign_config, NEW.sign_detail,
        NEW.override_font, NEW.override_foreground, NEW.override_background,
        NEW.msg_user, NEW.msg_sched, NEW.msg_current, NEW.expire_time
    );
    RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

DROP TRIGGER gate_arm_array_insert_trig ON iris.gate_arm_array;
CREATE OR REPLACE FUNCTION iris.gate_arm_array_insert() RETURNS TRIGGER AS
    $gate_arm_array_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'gate_arm_array', NEW.controller, NEW.pin);
    INSERT INTO iris._gate_arm_array (
        name, geo_loc, notes, opposing, prereq, camera, approach, action_plan,
        arm_state, interlock
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.opposing, NEW.prereq, NEW.camera,
        NEW.approach, NEW.action_plan, NEW.arm_state, NEW.interlock
    );
    RETURN NEW;
END;
$gate_arm_array_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_insert();

DROP TRIGGER gate_arm_insert_trig ON iris.gate_arm;
CREATE OR REPLACE FUNCTION iris.gate_arm_insert() RETURNS TRIGGER AS
    $gate_arm_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'gate_arm', NEW.controller, NEW.pin);
    INSERT INTO iris._gate_arm (
        name, ga_array, idx, notes, arm_state, fault
    ) VALUES (
        NEW.name, NEW.ga_array, NEW.idx, NEW.notes, NEW.arm_state, NEW.fault
    );
    RETURN NEW;
END;
$gate_arm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_insert();

CREATE OR REPLACE FUNCTION iris.lane_marking_insert() RETURNS TRIGGER AS
    $lane_marking_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'lane_marking', NEW.controller, NEW.pin);
    INSERT INTO iris._lane_marking (name, geo_loc, notes, deployed)
         VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.deployed);
    RETURN NEW;
END;
$lane_marking_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_insert_trig
    INSTEAD OF INSERT ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_insert();

DROP TRIGGER lcs_array_insert_trig ON iris.lcs_array;
CREATE OR REPLACE FUNCTION iris.lcs_array_insert() RETURNS TRIGGER AS
    $lcs_array_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'lcs_array', NEW.controller, NEW.pin);
    INSERT INTO iris._lcs_array(name, notes, shift, lcs_lock)
         VALUES (NEW.name, NEW.notes, NEW.shift, NEW.lcs_lock);
    RETURN NEW;
END;
$lcs_array_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_insert_trig
    INSTEAD OF INSERT ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_insert();

DROP TRIGGER lcs_indication_insert_trig ON iris.lcs_indication;
CREATE OR REPLACE FUNCTION iris.lcs_indication_insert() RETURNS TRIGGER AS
    $lcs_indication_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'lcs_indication', NEW.controller, NEW.pin);
    INSERT INTO iris._lcs_indication(name, lcs, indication)
         VALUES (NEW.name, NEW.lcs, NEW.indication);
    RETURN NEW;
END;
$lcs_indication_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_insert_trig
    INSTEAD OF INSERT ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_insert();

DROP TRIGGER ramp_meter_insert_trig ON iris.ramp_meter;
CREATE OR REPLACE FUNCTION iris.ramp_meter_insert() RETURNS TRIGGER AS
    $ramp_meter_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'ramp_meter', NEW.controller, NEW.pin);
    INSERT INTO iris._device_preset (name, preset)
         VALUES (NEW.name, NEW.preset);
    INSERT INTO iris._ramp_meter (
        name, geo_loc, notes, meter_type, storage, max_wait, algorithm,
        am_target, pm_target, beacon, m_lock
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type, NEW.storage,
        NEW.max_wait, NEW.algorithm, NEW.am_target, NEW.pm_target, NEW.beacon,
        NEW.m_lock
    );
    RETURN NEW;
END;
$ramp_meter_insert$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_insert();

DROP TRIGGER tag_reader_insert_trig ON iris.tag_reader;
CREATE OR REPLACE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
    $tag_reader_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'tag_reader', NEW.controller, NEW.pin);
    INSERT INTO iris._tag_reader (
        name, geo_loc, notes, toll_zone, downlink_freq_khz, uplink_freq_khz,
        sego_atten_downlink_db, sego_atten_uplink_db, sego_data_detect_db,
        sego_seen_count, sego_unique_count, iag_atten_downlink_db,
        iag_atten_uplink_db, iag_data_detect_db, iag_seen_count,
        iag_unique_count, line_loss_db, sync_mode, slave_select_count
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone, NEW.downlink_freq_khz,
        NEW.uplink_freq_khz, NEW.sego_atten_downlink_db,
        NEW.sego_atten_uplink_db, NEW.sego_data_detect_db, NEW.sego_seen_count,
        NEW.sego_unique_count, NEW.iag_atten_downlink_db,
        NEW.iag_atten_uplink_db, NEW.iag_data_detect_db, NEW.iag_seen_count,
        NEW.iag_unique_count, NEW.line_loss_db, NEW.sync_mode,
        NEW.slave_select_count
    );
    RETURN NEW;
END;
$tag_reader_insert$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_insert_trig
    INSTEAD OF INSERT ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_insert();

DROP TRIGGER video_monitor_insert_trig ON iris.video_monitor;
CREATE OR REPLACE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
    $video_monitor_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'video_monitor', NEW.controller, NEW.pin);
    INSERT INTO iris._video_monitor (
        name, notes, group_n, mon_num, restricted, monitor_style, camera
    ) VALUES (
        NEW.name, NEW.notes, NEW.group_n, NEW.mon_num, NEW.restricted,
        NEW.monitor_style, NEW.camera
    );
    RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_insert();

DROP TRIGGER flow_stream_insert_trig ON iris.flow_stream;
CREATE OR REPLACE FUNCTION iris.flow_stream_insert() RETURNS TRIGGER AS
    $flow_stream_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'flow_stream', NEW.controller, NEW.pin);
    INSERT INTO iris._flow_stream (
        name, restricted, loc_overlay, quality, camera, mon_num, address, port,
        status
    ) VALUES (
        NEW.name, NEW.restricted, NEW.loc_overlay, NEW.quality, NEW.camera,
        NEW.mon_num, NEW.address, NEW.port, NEW.status
    );
    RETURN NEW;
END;
$flow_stream_insert$ LANGUAGE plpgsql;

CREATE TRIGGER flow_stream_insert_trig
    INSTEAD OF INSERT ON iris.flow_stream
    FOR EACH ROW EXECUTE PROCEDURE iris.flow_stream_insert();

-- Add `sample_time` to `weather_sensor`
DROP VIEW weather_sensor_view;
DROP VIEW iris.weather_sensor;
DROP FUNCTION iris.weather_sensor_insert();
DROP FUNCTION iris.weather_sensor_update();

ALTER TABLE iris._weather_sensor
    ADD COLUMN sample_time TIMESTAMP WITH TIME ZONE;

CREATE VIEW iris.weather_sensor AS
    SELECT w.name, site_id, alt_id, geo_loc, controller, pin, notes, settings,
           sample, sample_time
      FROM iris._weather_sensor w
      JOIN iris.controller_io cio ON w.name = cio.name;

CREATE FUNCTION iris.weather_sensor_insert() RETURNS TRIGGER AS
    $weather_sensor_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'weather_sensor', NEW.controller, NEW.pin);
    INSERT INTO iris._weather_sensor (
        name, site_id, alt_id, geo_loc, notes, settings, sample, sample_time
    ) VALUES (
        NEW.name, NEW.site_id, NEW.alt_id, NEW.geo_loc, NEW.notes, NEW.settings,
        NEW.sample, NEW.sample_time
    );
    RETURN NEW;
END;
$weather_sensor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_insert_trig
    INSTEAD OF INSERT ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_insert();

CREATE FUNCTION iris.weather_sensor_update() RETURNS TRIGGER AS
    $weather_sensor_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._weather_sensor
       SET site_id = NEW.site_id,
           alt_id = NEW.alt_id,
           geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           settings = NEW.settings,
           sample = NEW.sample,
           sample_time = NEW.sample_time
     WHERE name = OLD.name;
    RETURN NEW;
END;
$weather_sensor_update$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_update_trig
    INSTEAD OF UPDATE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_update();

CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW weather_sensor_view AS
    SELECT w.name, site_id, alt_id, w.notes, settings, sample, sample_time,
           w.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
    FROM iris.weather_sensor w
    LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
    LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

COMMIT;
