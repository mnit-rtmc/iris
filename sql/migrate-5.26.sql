\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.25.0', '5.26.0');

INSERT INTO iris.sign_msg_source (bit, source) VALUES (14, 'exit warning');

-- Drop old views and functions referring to iris._device_io
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.device_geo_loc_view;
DROP VIEW device_controller_view;
DROP VIEW camera_view;
DROP VIEW iris.camera;
DROP FUNCTION iris.camera_insert();
DROP FUNCTION iris.camera_update();
DROP VIEW alarm_event_view;
DROP VIEW alarm_view;
DROP VIEW iris.alarm;
DROP FUNCTION iris.alarm_insert();
DROP FUNCTION iris.alarm_update();
DROP VIEW beacon_view;
DROP VIEW iris.beacon;
DROP FUNCTION iris.beacon_insert();
DROP FUNCTION iris.beacon_update();
DROP VIEW detector_auto_fail_view;
DROP VIEW detector_event_view;
DROP VIEW detector_view;
DROP VIEW detector_label_view;
DROP VIEW iris.detector;
DROP FUNCTION iris.detector_insert();
DROP FUNCTION iris.detector_update();
DROP VIEW gps_view;
DROP VIEW iris.gps;
DROP FUNCTION iris.gps_insert();
DROP FUNCTION iris.gps_update();
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP VIEW gate_arm_view;
DROP VIEW iris.gate_arm;
DROP FUNCTION iris.gate_arm_insert();
DROP FUNCTION iris.gate_arm_update();
DROP VIEW gate_arm_array_view;
DROP VIEW iris.gate_arm_array;
DROP FUNCTION iris.gate_arm_array_insert();
DROP FUNCTION iris.gate_arm_array_update();
DROP VIEW lane_marking_view;
DROP VIEW iris.lane_marking;
DROP FUNCTION iris.lane_marking_insert();
DROP FUNCTION iris.lane_marking_update();
DROP VIEW lcs_array_view;
DROP VIEW iris.lcs_array;
DROP FUNCTION iris.lcs_array_insert();
DROP FUNCTION iris.lcs_array_update();
DROP VIEW lcs_indication_view;
DROP VIEW iris.lcs_indication;
DROP FUNCTION iris.lcs_indication_insert();
DROP FUNCTION iris.lcs_indication_update();
DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;
DROP FUNCTION iris.ramp_meter_insert();
DROP FUNCTION iris.ramp_meter_update();
DROP VIEW tag_reader_view;
DROP VIEW iris.tag_reader;
DROP FUNCTION iris.tag_reader_insert();
DROP FUNCTION iris.tag_reader_update();
DROP VIEW video_monitor_view;
DROP VIEW iris.video_monitor;
DROP FUNCTION iris.video_monitor_insert();
DROP FUNCTION iris.video_monitor_update();
DROP VIEW flow_stream_view;
DROP VIEW iris.flow_stream;
DROP FUNCTION iris.flow_stream_insert();
DROP FUNCTION iris.flow_stream_update();
DROP VIEW weather_sensor_view;
DROP VIEW iris.weather_sensor;
DROP FUNCTION iris.weather_sensor_insert();
DROP FUNCTION iris.weather_sensor_update();

-- Replace iris._device_io with iris.controller_io
CREATE TABLE iris.controller_io (
    name VARCHAR(20) PRIMARY KEY,
    controller VARCHAR(20) REFERENCES iris.controller,
    pin INTEGER NOT NULL,
    UNIQUE (controller, pin)
);

INSERT INTO iris.controller_io (name, controller, pin)
    SELECT name, controller, pin
    FROM iris._device_io;

CREATE FUNCTION iris.controller_io_delete() RETURNS TRIGGER AS
    $controller_io_delete$
BEGIN
    DELETE FROM iris._device_preset WHERE name = OLD.name;
    DELETE FROM iris.controller_io WHERE name = OLD.name;
    IF FOUND THEN
        RETURN OLD;
    ELSE
        RETURN NULL;
    END IF;
END;
$controller_io_delete$ LANGUAGE plpgsql;

CREATE VIEW controller_io_view AS
    SELECT name, controller, pin
    FROM iris.controller_io;
GRANT SELECT ON controller_io_view TO PUBLIC;

-- Update camera stuff
ALTER TABLE iris._camera DROP CONSTRAINT _camera_fkey;
ALTER TABLE iris._camera ADD CONSTRAINT _camera_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.camera AS
    SELECT c.name, geo_loc, controller, pin, notes, cam_num, cam_template,
           encoder_type, enc_address, enc_port, enc_mcast, enc_channel,
           publish, streamable, video_loss
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
    $camera_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.camera_update() RETURNS TRIGGER AS
    $camera_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._camera
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           cam_num = NEW.cam_num,
           cam_template = NEW.cam_template,
           encoder_type = NEW.encoder_type,
           enc_address = NEW.enc_address,
           enc_port = NEW.enc_port,
           enc_mcast = NEW.enc_mcast,
           enc_channel = NEW.enc_channel,
           publish = NEW.publish,
           streamable = NEW.streamable,
           video_loss = NEW.video_loss
     WHERE name = OLD.name;
    RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW camera_view AS
    SELECT c.name, cam_num, c.cam_template, encoder_type, et.make, et.model,
           et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
           c.publish, c.streamable, c.video_loss, c.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           c.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
    FROM iris.camera c
    LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

-- Update alarm stuff
ALTER TABLE iris._alarm DROP CONSTRAINT _alarm_fkey;
ALTER TABLE iris._alarm ADD CONSTRAINT _alarm_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.alarm AS
    SELECT a.name, description, controller, pin, state, trigger_time
    FROM iris._alarm a JOIN iris.controller_io cio ON a.name = cio.name;

CREATE FUNCTION iris.alarm_insert() RETURNS TRIGGER AS
    $alarm_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
    INSERT INTO iris._alarm (name, description, state, trigger_time)
         VALUES (NEW.name, NEW.description, NEW.state, NEW.trigger_time);
    RETURN NEW;
END;
$alarm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_insert_trig
    INSTEAD OF INSERT ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_insert();

CREATE FUNCTION iris.alarm_update() RETURNS TRIGGER AS
    $alarm_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._alarm
       SET description = NEW.description,
           state = NEW.state,
           trigger_time = NEW.trigger_time
     WHERE name = OLD.name;
    RETURN NEW;
END;
$alarm_update$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_update_trig
    INSTEAD OF UPDATE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_update();

CREATE TRIGGER alarm_delete_trig
    INSTEAD OF DELETE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW alarm_view AS
    SELECT a.name, a.description, a.state, a.trigger_time, a.controller, a.pin,
           c.comm_link, c.drop_id
    FROM iris.alarm a
    LEFT JOIN iris.controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

CREATE VIEW alarm_event_view AS
    SELECT e.event_id, e.event_date, ed.description AS event_description,
           e.alarm, a.description
    FROM event.alarm_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
    JOIN iris._alarm a ON e.alarm = a.name;
GRANT SELECT ON alarm_event_view TO PUBLIC;

-- Update beacon stuff
ALTER TABLE iris._beacon DROP CONSTRAINT _beacon_fkey;
ALTER TABLE iris._beacon ADD CONSTRAINT _beacon_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin, preset
    FROM iris._beacon b
    JOIN iris.controller_io cio ON b.name = cio.name
    JOIN iris._device_preset p ON b.name = p.name;

CREATE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
    $beacon_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
        VALUES (NEW.name, NEW.controller, NEW.pin);
    INSERT INTO iris._device_preset (name, preset)
        VALUES (NEW.name, NEW.preset);
    INSERT INTO iris._beacon (name, geo_loc, notes, message, verify_pin)
        VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message,
                NEW.verify_pin);
    RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_insert();

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
           verify_pin = NEW.verify_pin
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
           ctr.condition
    FROM iris.beacon b
    LEFT JOIN iris.camera_preset p ON b.preset = p.name
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN controller_view ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

-- Update detector stuff
ALTER TABLE iris._detector DROP CONSTRAINT _detector_fkey;
ALTER TABLE iris._detector ADD CONSTRAINT _detector_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.detector AS
    SELECT det.name, controller, pin, r_node, lane_type, lane_number,
           abandoned, force_fail, auto_fail, field_length, fake, notes
    FROM iris._detector det
    JOIN iris.controller_io cio ON det.name = cio.name;

CREATE FUNCTION iris.detector_insert() RETURNS TRIGGER AS
    $detector_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.detector_update() RETURNS TRIGGER AS
    $detector_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._detector
       SET r_node = NEW.r_node,
           lane_type = NEW.lane_type,
           lane_number = NEW.lane_number,
           abandoned = NEW.abandoned,
           force_fail = NEW.force_fail,
           auto_fail = NEW.auto_fail,
           field_length = NEW.field_length,
           fake = NEW.fake,
           notes = NEW.notes
     WHERE name = OLD.name;
    RETURN NEW;
END;
$detector_update$ LANGUAGE plpgsql;

CREATE TRIGGER detector_update_trig
    INSTEAD OF UPDATE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_update();

CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW detector_label_view AS
    SELECT d.name AS det_id,
           iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
                               d.lane_type, d.lane_number, d.abandoned)
           AS label
    FROM iris.detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_view AS
    SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
           iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
           d.lane_type, d.lane_number, d.abandoned) AS label,
           rnd.geo_loc, l.rd || '_' || l.road_dir AS cor_id,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           d.lane_number, d.field_length, ln.description AS lane_type,
           ln.dcode AS lane_code, d.abandoned, d.force_fail, d.auto_fail,
           c.condition, d.fake, d.notes
    FROM iris.detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
    LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
    LEFT JOIN controller_view c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW detector_event_view AS
    SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
    FROM event.detector_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
    JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE VIEW detector_auto_fail_view AS
    WITH af AS (SELECT device_id, event_desc_id, count(*) AS event_count,
                max(event_date) AS last_fail
                FROM event.detector_event
                GROUP BY device_id, event_desc_id)
    SELECT device_id, label, ed.description, event_count, last_fail
    FROM af
    JOIN event.event_description ed ON af.event_desc_id = ed.event_desc_id
    JOIN detector_label_view dl ON af.device_id = dl.det_id;
GRANT SELECT ON detector_auto_fail_view TO PUBLIC;

-- Update GPS stuff
ALTER TABLE iris._gps DROP CONSTRAINT _gps_fkey;
ALTER TABLE iris._gps ADD CONSTRAINT _gps_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.gps AS
    SELECT g.name, controller, pin, notes, latest_poll, latest_sample, lat, lon
    FROM iris._gps g
    JOIN iris.controller_io cio ON g.name = cio.name;

CREATE FUNCTION iris.gps_insert() RETURNS TRIGGER AS
    $gps_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
    INSERT INTO iris._gps (name, notes, latest_poll, latest_sample, lat,lon)
         VALUES (NEW.name, NEW.notes, NEW.latest_poll, NEW.latest_sample,
                 NEW.lat, NEW.lon);
    RETURN NEW;
END;
$gps_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gps_insert_trig
	INSTEAD OF INSERT ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_insert();

CREATE FUNCTION iris.gps_update() RETURNS TRIGGER AS
    $gps_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._gps
       SET notes = NEW.notes,
           latest_poll = NEW.latest_poll,
           latest_sample = NEW.latest_sample,
           lat = NEW.lat,
           lon = NEW.lon
     WHERE name = OLD.name;
    RETURN NEW;
END;
$gps_update$ LANGUAGE plpgsql;

CREATE TRIGGER gps_update_trig
	INSTEAD OF UPDATE ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_update();

CREATE TRIGGER gps_delete_trig
    INSTEAD OF DELETE ON iris.gps
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW gps_view AS
    SELECT name, controller, pin, notes, latest_poll, latest_sample, lat, lon
    FROM iris.gps;
GRANT SELECT ON gps_view TO PUBLIC;

-- Update DMS stuff
ALTER TABLE iris._dms DROP CONSTRAINT _dms_fkey;
ALTER TABLE iris._dms ADD CONSTRAINT _dms_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           purpose, hidden, beacon, preset, sign_config, sign_detail,
           override_font, override_foreground, override_background, msg_user,
           msg_sched, msg_current, expire_time
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris._device_preset p ON d.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
    $dms_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._dms
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           gps = NEW.gps,
           static_graphic = NEW.static_graphic,
           purpose = NEW.purpose,
           hidden = NEW.hidden,
           beacon = NEW.beacon,
           sign_config = NEW.sign_config,
           sign_detail = NEW.sign_detail,
           override_font = NEW.override_font,
           override_foreground = NEW.override_foreground,
           override_background = NEW.override_background,
           msg_user = NEW.msg_user,
           msg_sched = NEW.msg_sched,
           msg_current = NEW.msg_current,
           expire_time = NEW.expire_time
     WHERE name = OLD.name;
    RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW dms_view AS
    SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
           d.static_graphic, dp.description AS purpose, d.hidden, d.beacon,
           p.camera, p.preset_num, d.sign_config, d.sign_detail,
           default_font, override_font, override_foreground,
           override_background, msg_user, msg_sched, msg_current,
           expire_time, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris.dms d
    LEFT JOIN iris.camera_preset p ON d.preset = p.name
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
    LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, beacon_enabled,
           mc.description AS msg_combining, msg_priority,
           iris.sign_msg_sources(source) AS sources, duration, expire_time
    FROM iris.dms d
    LEFT JOIN iris.controller c ON d.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name
    LEFT JOIN iris.msg_combining mc ON sm.msg_combining = mc.id;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Update gate arm array stuff
ALTER TABLE iris._gate_arm_array DROP CONSTRAINT _gate_arm_array_fkey;
ALTER TABLE iris._gate_arm_array ADD CONSTRAINT _gate_arm_array_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.gate_arm_array AS
    SELECT g.name, geo_loc, controller, pin, notes, opposing, prereq, camera,
           approach, action_plan, arm_state, interlock
    FROM iris._gate_arm_array g JOIN iris.controller_io cio
    ON g.name = cio.name;

CREATE FUNCTION iris.gate_arm_array_insert() RETURNS TRIGGER AS
    $gate_arm_array_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.gate_arm_array_update() RETURNS TRIGGER AS
    $gate_arm_array_update$
BEGIN
    UPDATE iris.controller_io SET controller = NEW.controller, pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._gate_arm_array
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
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

CREATE TRIGGER gate_arm_array_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_update();

CREATE TRIGGER gate_arm_array_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW gate_arm_array_view AS
    SELECT ga.name, ga.notes, ga.geo_loc, l.roadway, l.road_dir, l.cross_mod,
           l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon, l.corridor,
           l.location, ga.controller, ga.pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, ga.opposing, ga.prereq, ga.camera, ga.approach,
           ga.action_plan, gas.description AS arm_state,
           gai.description AS interlock
    FROM iris.gate_arm_array ga
    JOIN iris.gate_arm_state gas ON ga.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON ga.interlock = gai.id
    LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
    LEFT JOIN controller_view ctr ON ga.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

-- Update gate arm stuff
ALTER TABLE iris._gate_arm DROP CONSTRAINT _gate_arm_fkey;
ALTER TABLE iris._gate_arm ADD CONSTRAINT _gate_arm_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.gate_arm AS
    SELECT g.name, ga_array, idx, controller, pin, notes, arm_state, fault
    FROM iris._gate_arm g JOIN iris.controller_io cio ON g.name = cio.name;

CREATE FUNCTION iris.gate_arm_insert() RETURNS TRIGGER AS
    $gate_arm_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.gate_arm_update() RETURNS TRIGGER AS
    $gate_arm_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller, pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._gate_arm
       SET ga_array = NEW.ga_array,
           idx = NEW.idx,
           notes = NEW.notes,
           arm_state = NEW.arm_state,
           fault = NEW.fault
     WHERE name = OLD.name;
    RETURN NEW;
END;
$gate_arm_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_update();

CREATE TRIGGER gate_arm_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW gate_arm_view AS
    SELECT g.name, g.ga_array, g.notes, ga.geo_loc, l.roadway, l.road_dir,
           l.cross_mod, l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon,
           l.corridor, l.location, g.controller, g.pin, ctr.comm_link,
           ctr.drop_id, ctr.condition, ga.opposing, ga.prereq, ga.camera,
           ga.approach, gas.description AS arm_state, fault
    FROM iris.gate_arm g
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris._gate_arm_array ga ON g.ga_array = ga.name
    LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
    LEFT JOIN controller_view ctr ON g.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

-- Update lane marking stuff
ALTER TABLE iris._lane_marking DROP CONSTRAINT _lane_marking_fkey;
ALTER TABLE iris._lane_marking ADD CONSTRAINT _lane_marking_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.lane_marking AS
    SELECT m.name, geo_loc, controller, pin, notes
    FROM iris._lane_marking m
    JOIN iris.controller_io cio ON m.name = cio.name;

CREATE FUNCTION iris.lane_marking_insert() RETURNS TRIGGER AS
    $lane_marking_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
    INSERT INTO iris._lane_marking (name, geo_loc, notes)
         VALUES (NEW.name, NEW.geo_loc, NEW.notes);
    RETURN NEW;
END;
$lane_marking_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_insert_trig
    INSTEAD OF INSERT ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_insert();

CREATE FUNCTION iris.lane_marking_update() RETURNS TRIGGER AS
    $lane_marking_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._lane_marking
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes
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
           ctr.condition
    FROM iris.lane_marking m
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
    LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

-- Update LCS array stuff
ALTER TABLE iris._lcs_array DROP CONSTRAINT _lcs_array_fkey;
ALTER TABLE iris._lcs_array ADD CONSTRAINT _lcs_array_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.lcs_array AS
    SELECT la.name, controller, pin, notes, shift, lcs_lock
    FROM iris._lcs_array la JOIN iris.controller_io cio ON la.name = cio.name;

CREATE FUNCTION iris.lcs_array_insert() RETURNS TRIGGER AS
    $lcs_array_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
    INSERT INTO iris._lcs_array(name, notes, shift, lcs_lock)
         VALUES (NEW.name, NEW.notes, NEW.shift, NEW.lcs_lock);
    RETURN NEW;
END;
$lcs_array_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_insert_trig
    INSTEAD OF INSERT ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_insert();

CREATE FUNCTION iris.lcs_array_update() RETURNS TRIGGER AS
    $lcs_array_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._lcs_array
       SET notes = NEW.notes,
           shift = NEW.shift,
           lcs_lock = NEW.lcs_lock
     WHERE name = OLD.name;
    RETURN NEW;
END;
$lcs_array_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_update_trig
    INSTEAD OF UPDATE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_update();

CREATE TRIGGER lcs_array_delete_trig
    INSTEAD OF DELETE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW lcs_array_view AS
    SELECT name, shift, notes, lcs_lock
    FROM iris.lcs_array;
GRANT SELECT ON lcs_array_view TO PUBLIC;

-- Update LCS indication stuff
ALTER TABLE iris._lcs_indication DROP CONSTRAINT _lcs_indication_fkey;
ALTER TABLE iris._lcs_indication ADD CONSTRAINT _lcs_indication_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.lcs_indication AS
    SELECT li.name, controller, pin, lcs, indication
    FROM iris._lcs_indication li
    JOIN iris.controller_io cio ON li.name = cio.name;

CREATE FUNCTION iris.lcs_indication_insert() RETURNS TRIGGER AS
    $lcs_indication_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
    INSERT INTO iris._lcs_indication(name, lcs, indication)
         VALUES (NEW.name, NEW.lcs, NEW.indication);
    RETURN NEW;
END;
$lcs_indication_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_insert_trig
    INSTEAD OF INSERT ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_insert();

CREATE FUNCTION iris.lcs_indication_update() RETURNS TRIGGER AS
    $lcs_indication_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._lcs_indication
       SET lcs = NEW.lcs,
           indication = NEW.indication
     WHERE name = OLD.name;
    RETURN NEW;
END;
$lcs_indication_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_update_trig
    INSTEAD OF UPDATE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_update();

CREATE TRIGGER lcs_indication_delete_trig
    INSTEAD OF DELETE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW lcs_indication_view AS
    SELECT name, controller, pin, lcs, description AS indication
    FROM iris.lcs_indication
    JOIN iris.lane_use_indication ON indication = id;
GRANT SELECT ON lcs_indication_view TO PUBLIC;

-- Update ramp meter stuff
ALTER TABLE iris._ramp_meter DROP CONSTRAINT _ramp_meter_fkey;
ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.ramp_meter AS
    SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
           max_wait, algorithm, am_target, pm_target, beacon, preset, m_lock
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    JOIN iris._device_preset p ON m.name = p.name;

CREATE FUNCTION iris.ramp_meter_insert() RETURNS TRIGGER AS
    $ramp_meter_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.ramp_meter_update() RETURNS TRIGGER AS
    $ramp_meter_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._ramp_meter
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
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

CREATE TRIGGER ramp_meter_update_trig
    INSTEAD OF UPDATE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_update();

CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW ramp_meter_view AS
    SELECT m.name, geo_loc, controller, pin, notes,
           mt.description AS meter_type, storage, max_wait,
           alg.description AS algorithm, am_target, pm_target, beacon, camera,
           preset_num, ml.description AS meter_lock, l.roadway, l.road_dir,
           l.cross_mod, l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon,
           l.corridor, l.location, l.rd
    FROM iris.ramp_meter m
    LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
    LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
    LEFT JOIN iris.camera_preset p ON m.preset = p.name
    LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

-- Update tag reader stuff
ALTER TABLE iris._tag_reader DROP CONSTRAINT _tag_reader_fkey;
ALTER TABLE iris._tag_reader ADD CONSTRAINT _tag_reader_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.tag_reader AS
    SELECT t.name, geo_loc, controller, pin, notes, toll_zone,
           downlink_freq_khz, uplink_freq_khz, sego_atten_downlink_db,
           sego_atten_uplink_db, sego_data_detect_db, sego_seen_count,
           sego_unique_count, iag_atten_downlink_db, iag_atten_uplink_db,
           iag_data_detect_db, iag_seen_count, iag_unique_count, line_loss_db,
           sync_mode, slave_select_count
    FROM iris._tag_reader t JOIN iris.controller_io cio ON t.name = cio.name;

CREATE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
    $tag_reader_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.tag_reader_update() RETURNS TRIGGER AS
    $tag_reader_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._tag_reader
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           toll_zone = NEW.toll_zone,
           downlink_freq_khz = NEW.downlink_freq_khz,
           uplink_freq_khz = NEW.uplink_freq_khz,
           sego_atten_downlink_db = NEW.sego_atten_downlink_db,
           sego_atten_uplink_db = NEW.sego_atten_uplink_db,
           sego_data_detect_db = NEW.sego_data_detect_db,
           sego_seen_count = NEW.sego_seen_count,
           sego_unique_count = NEW.sego_unique_count,
           iag_atten_downlink_db = NEW.iag_atten_downlink_db,
           iag_atten_uplink_db = NEW.iag_atten_uplink_db,
           iag_data_detect_db = NEW.iag_data_detect_db,
           iag_seen_count = NEW.iag_seen_count,
           iag_unique_count = NEW.iag_unique_count,
           line_loss_db = NEW.line_loss_db,
           sync_mode = NEW.sync_mode,
           slave_select_count = NEW.slave_select_count
     WHERE name = OLD.name;
    RETURN NEW;
END;
$tag_reader_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_update_trig
    INSTEAD OF UPDATE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_update();

CREATE TRIGGER tag_reader_delete_trig
    INSTEAD OF DELETE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW tag_reader_view AS
    SELECT t.name, t.geo_loc, location, controller, pin, notes, toll_zone,
           downlink_freq_khz, uplink_freq_khz, sego_atten_downlink_db,
           sego_atten_uplink_db, sego_data_detect_db, sego_seen_count,
           sego_unique_count, iag_atten_downlink_db, iag_atten_uplink_db,
           iag_data_detect_db, iag_seen_count, iag_unique_count, line_loss_db,
           m.description AS sync_mode, slave_select_count
    FROM iris.tag_reader t
    LEFT JOIN geo_loc_view l ON t.geo_loc = l.name
    LEFT JOIN iris.tag_reader_sync_mode m ON t.sync_mode = m.id;
GRANT SELECT ON tag_reader_view TO PUBLIC;

-- Update video monitor stuff
ALTER TABLE iris._video_monitor DROP CONSTRAINT _video_monitor_fkey;
ALTER TABLE iris._video_monitor ADD CONSTRAINT _video_monitor_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.video_monitor AS
    SELECT m.name, controller, pin, notes, group_n, mon_num, restricted,
           monitor_style, camera
    FROM iris._video_monitor m JOIN iris.controller_io cio ON m.name = cio.name;

CREATE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
    $video_monitor_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
    $video_monitor_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._video_monitor
       SET notes = NEW.notes,
           group_n = NEW.group_n,
           mon_num = NEW.mon_num,
           restricted = NEW.restricted,
           monitor_style = NEW.monitor_style,
           camera = NEW.camera
     WHERE name = OLD.name;
    RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_update();

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW video_monitor_view AS
    SELECT m.name, m.notes, group_n, mon_num, restricted, monitor_style,
           m.controller, m.pin, ctr.condition, ctr.comm_link, camera
    FROM iris.video_monitor m
    LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

-- Update flow stream stuff
ALTER TABLE iris._flow_stream DROP CONSTRAINT _flow_stream_fkey;
ALTER TABLE iris._flow_stream ADD CONSTRAINT _flow_stream_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.flow_stream AS
    SELECT f.name, controller, pin, restricted, loc_overlay, quality, camera,
           mon_num, address, port, status
    FROM iris._flow_stream f JOIN iris.controller_io cio ON f.name = cio.name;

CREATE FUNCTION iris.flow_stream_insert() RETURNS TRIGGER AS
    $flow_stream_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
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

CREATE FUNCTION iris.flow_stream_update() RETURNS TRIGGER AS
    $flow_stream_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._flow_stream
       SET restricted = NEW.restricted,
           loc_overlay = NEW.loc_overlay,
           quality = NEW.quality,
           camera = NEW.camera,
           mon_num = NEW.mon_num,
           address = NEW.address,
           port = NEW.port,
           status = NEW.status
     WHERE name = OLD.name;
    RETURN NEW;
END;
$flow_stream_update$ LANGUAGE plpgsql;

CREATE TRIGGER flow_stream_update_trig
    INSTEAD OF UPDATE ON iris.flow_stream
    FOR EACH ROW EXECUTE PROCEDURE iris.flow_stream_update();

CREATE TRIGGER flow_stream_delete_trig
    INSTEAD OF DELETE ON iris.flow_stream
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW flow_stream_view AS
    SELECT f.name, f.controller, pin, condition, comm_link, restricted,
           loc_overlay, eq.description AS quality, camera, mon_num, address,
           port, s.description AS status
    FROM iris.flow_stream f
    JOIN iris.flow_stream_status s ON f.status = s.id
    LEFT JOIN controller_view ctr ON controller = ctr.name
    LEFT JOIN iris.encoding_quality eq ON f.quality = eq.id;
GRANT SELECT ON flow_stream_view TO PUBLIC;

-- Update weather sensor stuff
ALTER TABLE iris._weather_sensor DROP CONSTRAINT _weather_sensor_fkey;
ALTER TABLE iris._weather_sensor ADD CONSTRAINT _weather_sensor_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE VIEW iris.weather_sensor AS
    SELECT w.name, site_id, alt_id, geo_loc, controller, pin, notes, settings,
           sample
      FROM iris._weather_sensor w
      JOIN iris.controller_io cio ON w.name = cio.name;

CREATE FUNCTION iris.weather_sensor_insert() RETURNS TRIGGER AS
    $weather_sensor_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
         VALUES (NEW.name, NEW.controller, NEW.pin);
    INSERT INTO iris._weather_sensor (
        name, site_id, alt_id, geo_loc, notes, settings, sample
    ) VALUES (
        NEW.name, NEW.site_id, NEW.alt_id, NEW.geo_loc, NEW.notes, NEW.settings,
        NEW.sample
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
           sample = NEW.sample
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
    SELECT w.name, w.site_id, w.alt_id, w.notes, w.settings, w.sample,
           w.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
    FROM iris.weather_sensor w
    LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
    LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

-- Replace old views
CREATE VIEW iris.device_geo_loc_view AS
    SELECT name, geo_loc FROM iris._beacon UNION ALL
    SELECT name, geo_loc FROM iris._camera UNION ALL
    SELECT name, geo_loc FROM iris._dms UNION ALL
    SELECT name, geo_loc FROM iris._lane_marking UNION ALL
    SELECT name, geo_loc FROM iris._ramp_meter UNION ALL
    SELECT name, geo_loc FROM iris._tag_reader UNION ALL
    SELECT name, geo_loc FROM iris._weather_sensor UNION ALL
    SELECT d.name, geo_loc FROM iris._detector d
    JOIN iris.r_node rn ON d.r_node = rn.name UNION ALL
    SELECT g.name, geo_loc FROM iris._gate_arm g
    JOIN iris._gate_arm_array ga ON g.ga_array = ga.name;

CREATE VIEW controller_device_view AS
    SELECT cio.name, cio.controller, cio.pin, g.geo_loc,
           trim(l.roadway || ' ' || l.road_dir) AS corridor,
           trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
           || ' ' || l.cross_dir) AS cross_loc
      FROM iris.controller_io cio
      JOIN iris.device_geo_loc_view g ON cio.name = g.name
      JOIN geo_loc_view l ON g.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
    SELECT c.name, c.comm_link, c.drop_id, l.landmark, cab.geo_loc, l.location,
           cab.style AS "type", d.name AS device, d.pin, d.cross_loc,
           d.corridor, c.notes
    FROM iris.controller c
    LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
    LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
    LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

DROP FUNCTION iris.device_delete;
DROP TABLE iris._device_io;

COMMIT;
