\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.32.0', '5.33.0');

-- Update comm_link_notify trigger
CREATE OR REPLACE FUNCTION iris.comm_link_notify() RETURNS TRIGGER AS
    $comm_link_notify$
BEGIN
    IF (NEW.connected IS DISTINCT FROM OLD.connected) THEN
        NOTIFY comm_link, 'connected';
    ELSE
        NOTIFY comm_link;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$comm_link_notify$ LANGUAGE plpgsql;

-- Drop views depending on controller_view
DROP VIEW camera_view;
DROP VIEW beacon_view;
DROP VIEW detector_view;
DROP VIEW gate_arm_array_view;
DROP VIEW gate_arm_view;
DROP VIEW lane_marking_view;
DROP VIEW video_monitor_view;
DROP VIEW flow_stream_view;
DROP VIEW weather_sensor_view;
DROP VIEW controller_loc_view;
DROP VIEW controller_view;

-- Replace controller version column with setup
ALTER TABLE iris.controller ADD COLUMN setup JSONB;
UPDATE iris.controller
 SET setup = ('{"version":"' || version || '"}')::jsonb
 WHERE version IS NOT NULL AND version != '';
ALTER TABLE iris.controller DROP COLUMN version;

CREATE VIEW controller_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, geo_loc,
           cnd.description AS condition, notes, setup, fail_time
    FROM iris.controller c
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, condition, c.notes,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
    FROM controller_view c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

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

CREATE VIEW detector_view AS
    SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
           iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
           d.lane_code, d.lane_number, d.abandoned) AS label,
           rnd.geo_loc, l.rd || '_' || l.road_dir AS cor_id,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           d.lane_number, d.field_length, lc.description AS lane_type,
           d.lane_code, d.abandoned, d.force_fail, d.auto_fail, c.condition,
           d.fake, d.notes
    FROM iris.detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
    LEFT JOIN iris.lane_code lc ON d.lane_code = lc.lcode
    LEFT JOIN controller_view c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

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

CREATE VIEW lane_marking_view AS
    SELECT m.name, m.notes, m.geo_loc, l.roadway, l.road_dir, l.cross_mod,
           l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon, l.corridor,
           l.location, m.controller, m.pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, m.deployed
    FROM iris.lane_marking m
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
    LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW video_monitor_view AS
    SELECT m.name, m.notes, group_n, mon_num, restricted, monitor_style,
           m.controller, m.pin, ctr.condition, ctr.comm_link, camera
    FROM iris.video_monitor m
    LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

CREATE VIEW flow_stream_view AS
    SELECT f.name, f.controller, pin, condition, comm_link, restricted,
           loc_overlay, eq.description AS quality, camera, mon_num, address,
           port, s.description AS status
    FROM iris.flow_stream f
    JOIN iris.flow_stream_status s ON f.status = s.id
    LEFT JOIN controller_view ctr ON controller = ctr.name
    LEFT JOIN iris.encoding_quality eq ON f.quality = eq.id;
GRANT SELECT ON flow_stream_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
    SELECT w.name, site_id, alt_id, w.notes, settings, sample, sample_time,
           w.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
    FROM iris.weather_sensor w
    LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
    LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

-- Update controller_notify trigger
CREATE OR REPLACE FUNCTION iris.controller_notify() RETURNS TRIGGER AS
    $controller_notify$
BEGIN
    IF (NEW.fail_time IS DISTINCT FROM OLD.fail_time) THEN
        NOTIFY controller, 'fail_time';
    ELSIF (NEW.setup IS DISTINCT FROM OLD.setup) THEN
        NOTIFY controller, 'setup';
    ELSE
        NOTIFY controller;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_notify$ LANGUAGE plpgsql;

-- Purge alert records more than 2 weeks old
SELECT action_plan
  INTO TEMP purge_alert_action_plan
  FROM iris.time_action
  WHERE action_plan LIKE 'ALERT_%'
    AND sched_date + make_interval(weeks => 2) < CURRENT_DATE
  GROUP BY action_plan;

-- Find alert "ALL" sign groups
SELECT sign_group
  INTO TEMP purge_alert_sign_group_all
  FROM cap.alert_info
  WHERE end_date + make_interval(weeks => 2) < CURRENT_DATE
  GROUP by sign_group;

-- Find alert "ACT" sign groups
SELECT sign_group
  INTO TEMP purge_alert_sign_group_act
  FROM iris.dms_action
  WHERE action_plan IN (SELECT action_plan FROM purge_alert_action_plan)
  GROUP by sign_group;

DELETE FROM iris.dms_sign_group
  WHERE sign_group IN (SELECT sign_group FROM purge_alert_sign_group_all);
DELETE FROM iris.dms_sign_group
  WHERE sign_group IN (SELECT sign_group FROM purge_alert_sign_group_act);

DELETE FROM iris.dms_action
  WHERE action_plan IN (SELECT action_plan FROM purge_alert_action_plan);

DELETE FROM cap.alert_info
  WHERE action_plan IN (SELECT action_plan FROM purge_alert_action_plan);

DELETE FROM iris.sign_group
  WHERE name IN (SELECT sign_group FROM purge_alert_sign_group_all);
DELETE FROM iris.sign_group
  WHERE name IN (SELECT sign_group FROM purge_alert_sign_group_act);

DELETE FROM iris.time_action
  WHERE action_plan IN (SELECT action_plan FROM purge_alert_action_plan);

DELETE FROM iris.action_plan
  WHERE name IN (SELECT action_plan FROM purge_alert_action_plan);

-- This shouldn't be necessary, but some bogus records exist
DELETE FROM iris.action_plan
  WHERE group_n = 'alert';
DELETE FROM iris.dms_sign_group
  WHERE name LIKE 'ALERT%';
DELETE FROM iris.sign_group
  WHERE name LIKE 'ALERT%';

COMMIT;
