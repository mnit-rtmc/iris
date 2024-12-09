\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.62.0', '5.63.0');

-- Add controller status
ALTER TABLE iris.controller ADD COLUMN status JSONB;

DROP VIEW weather_sensor_view;
DROP VIEW flow_stream_view;
DROP VIEW video_monitor_view;
DROP VIEW lane_marking_view;
DROP VIEW gate_arm_view;
DROP VIEW gate_arm_array_view;
DROP VIEW beacon_view;
DROP VIEW camera_view;
DROP VIEW detector_view;
DROP VIEW controller_loc_view;
DROP VIEW controller_view;

CREATE VIEW controller_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, geo_loc,
           cnd.description AS condition, notes, setup, status, fail_time
    FROM iris.controller c
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, condition, c.notes,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
    FROM controller_view c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW detector_view AS
    SELECT d.name, d.r_node, c.comm_link, c.drop_id, cio.controller, cio.pin,
           dl.label, dl.geo_loc, l.rd || '_' || l.road_dir AS cor_id,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           d.lane_number, d.field_length, lc.description AS lane_type,
           d.lane_code, d.abandoned, d.force_fail, d.auto_fail, c.condition,
           d.fake, d.notes
    FROM iris.detector d
    JOIN iris.controller_io cio ON d.name = cio.name
    LEFT JOIN detector_label_view dl ON d.name = dl.det_id
    LEFT JOIN geo_loc_view l ON dl.geo_loc = l.name
    LEFT JOIN iris.lane_code lc ON d.lane_code = lc.lcode
    LEFT JOIN controller_view c ON cio.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

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

CREATE VIEW beacon_view AS
    SELECT b.name, b.notes, b.message, cp.camera, cp.preset_num, b.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, b.verify_pin, b.ext_mode,
           ctr.comm_link, ctr.drop_id, ctr.condition, bs.description AS state
    FROM iris._beacon b
    JOIN iris.beacon_state bs ON b.state = bs.id
    JOIN iris.controller_io cio ON b.name = cio.name
    LEFT JOIN iris.device_preset p ON b.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

CREATE VIEW gate_arm_array_view AS
    SELECT ga.name, ga.notes, ga.geo_loc, l.roadway, l.road_dir, l.cross_mod,
           l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon, l.corridor,
           l.location, cio.controller, cio.pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, ga.opposing, ga.prereq, ga.camera, ga.approach,
           ga.action_plan, gas.description AS arm_state,
           gai.description AS interlock
    FROM iris._gate_arm_array ga
    JOIN iris.controller_io cio ON ga.name = cio.name
    JOIN iris.gate_arm_state gas ON ga.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON ga.interlock = gai.id
    LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

CREATE VIEW gate_arm_view AS
    SELECT g.name, g.ga_array, g.notes, ga.geo_loc, l.roadway, l.road_dir,
           l.cross_mod, l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon,
           l.corridor, l.location, cio.controller, cio.pin, ctr.comm_link,
           ctr.drop_id, ctr.condition, ga.opposing, ga.prereq, ga.camera,
           ga.approach, gas.description AS arm_state, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris._gate_arm_array ga ON g.ga_array = ga.name
    LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

CREATE VIEW lane_marking_view AS
    SELECT m.name, m.notes, m.geo_loc, l.roadway, l.road_dir, l.cross_mod,
           l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon, l.corridor,
           l.location, cio.controller, cio.pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, m.deployed
    FROM iris._lane_marking m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW video_monitor_view AS
    SELECT m.name, m.notes, mon_num, restricted, monitor_style,
           cio.controller, cio.pin, ctr.condition, ctr.comm_link, camera
    FROM iris._video_monitor m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

CREATE VIEW flow_stream_view AS
    SELECT f.name, cio.controller, cio.pin, condition, comm_link, restricted,
           loc_overlay, eq.description AS quality, camera, mon_num, address,
           port, s.description AS status
    FROM iris._flow_stream f
    JOIN iris.controller_io cio ON f.name = cio.name
    JOIN iris.flow_stream_status s ON f.status = s.id
    LEFT JOIN controller_view ctr ON controller = ctr.name
    LEFT JOIN iris.encoding_quality eq ON f.quality = eq.id;
GRANT SELECT ON flow_stream_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
    SELECT w.name, site_id, alt_id, w.notes, settings, sample, sample_time,
           w.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, ctr.comm_link, ctr.drop_id, ctr.condition
    FROM iris._weather_sensor w
    JOIN iris.controller_io cio ON w.name = cio.name
    LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

-- Delete system attribute
DELETE FROM iris.system_attribute
    WHERE name = 'dms_pixel_maint_threshold';

-- Add action plan notify triggers
CREATE TRIGGER plan_phase_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.plan_phase
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE FUNCTION iris.action_plan_notify() RETURNS TRIGGER AS
    $action_plan_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.active IS DISTINCT FROM OLD.active)
    THEN
        NOTIFY action_plan;
    ELSE
        PERFORM pg_notify('action_plan', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$action_plan_notify$ LANGUAGE plpgsql;

CREATE TRIGGER action_plan_notify_trig
    AFTER UPDATE ON iris.action_plan
    FOR EACH STATEMENT EXECUTE FUNCTION iris.action_plan_notify();

CREATE TRIGGER action_plan_table_notify_trig
    AFTER INSERT OR DELETE ON iris.action_plan
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE FUNCTION iris.time_action_notify() RETURNS TRIGGER AS
    $time_action_notify$
BEGIN
    PERFORM pg_notify('time_action', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$time_action_notify$ LANGUAGE plpgsql;

CREATE TRIGGER time_action_notify_trig
    AFTER UPDATE ON iris.time_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.time_action_notify();

CREATE TRIGGER time_action_table_notify_trig
    AFTER INSERT OR DELETE ON iris.time_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE FUNCTION iris.device_action_notify() RETURNS TRIGGER AS
    $device_action_notify$
BEGIN
    IF (NEW.hashtag IS DISTINCT FROM OLD.hashtag) THEN
        NOTIFY device_action;
    ELSE
        PERFORM pg_notify('device_action', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$device_action_notify$ LANGUAGE plpgsql;

CREATE TRIGGER device_action_notify_trig
    AFTER UPDATE ON iris.device_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.device_action_notify();

CREATE TRIGGER device_action_table_notify_trig
    AFTER INSERT OR DELETE ON iris.device_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

COMMIT;
