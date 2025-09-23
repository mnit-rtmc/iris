\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Change drop_id to INTEGER type
DROP VIEW controller_report;
DROP VIEW flow_stream_view;
DROP VIEW video_monitor_view;
DROP VIEW weather_sensor_view;
DROP VIEW gate_arm_view;
DROP VIEW beacon_view;
DROP VIEW alarm_view;
DROP VIEW camera_view;
DROP VIEW detector_view;
DROP VIEW comm_event_view;
DROP VIEW controller_loc_view;
DROP VIEW controller_view;
DROP INDEX iris.ctrl_link_drop_idx;

ALTER TABLE iris.controller ALTER COLUMN drop_id TYPE INTEGER;

CREATE UNIQUE INDEX ctrl_link_drop_idx ON iris.controller
    USING btree (comm_link, drop_id);

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

CREATE VIEW comm_event_view AS
    SELECT e.event_id, e.event_date, ed.description, e.controller,
           c.comm_link, c.drop_id
    FROM event.comm_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.controller c ON e.controller = c.name;
GRANT SELECT ON comm_event_view TO PUBLIC;

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

CREATE VIEW alarm_view AS
    SELECT a.name, a.description, a.state, a.trigger_time, a.controller, a.pin,
           c.comm_link, c.drop_id
    FROM iris.alarm a
    LEFT JOIN iris.controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

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

CREATE VIEW gate_arm_view AS
    SELECT g.name, g.notes,
           g.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
           cp.camera, cp.preset_num, g.opposing, g.downstream_hashtag,
           gas.description AS arm_state, gai.description AS interlock, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    LEFT JOIN iris.device_preset p ON g.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON g.interlock = gai.id
    LEFT JOIN geo_loc_view l ON g.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

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

CREATE VIEW controller_report AS
    SELECT c.name, c.comm_link, c.drop_id, l.landmark, c.geo_loc, l.location,
           cabinet_style, d.name AS device, d.pin, d.cross_loc, d.corridor,
           c.notes
    FROM iris.controller c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

COMMIT;
