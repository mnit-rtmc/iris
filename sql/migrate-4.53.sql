\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

UPDATE iris.system_attribute SET value = '4.53.0'
	WHERE name = 'database_version';

DROP VIEW public.geo_loc_view CASCADE;
DROP VIEW iris.dms;
DROP VIEW iris.device_geo_loc_view;
DROP VIEW iris.alarm CASCADE;
DROP VIEW iris.beacon;
DROP VIEW iris.camera;
DROP VIEW iris.detector;
DROP VIEW public.device_controller_view;
DROP VIEW iris.weather_sensor;
DROP VIEW iris.tag_reader CASCADE;
DROP VIEW iris.ramp_meter;
DROP VIEW iris.lcs_indication CASCADE;
DROP VIEW iris.lcs_array CASCADE;
DROP VIEW iris.lane_marking;
DROP VIEW iris.gate_arm_array;
DROP VIEW iris.gate_arm;
DROP VIEW public.camera_preset_view;
DROP VIEW public.sign_text_view;
DROP VIEW public.tag_reader_dms_view;
DROP VIEW public.meter_action_view;
DROP VIEW public.lcs_view;
DROP VIEW public.detector_fail_view;
DROP VIEW public.meter_event_view;
DROP VIEW iris.video_monitor CASCADE;
DROP VIEW public.dms_toll_zone_view;
DROP VIEW public.beacon_event_view;
DROP VIEW public.camera_switch_event_view;
DROP VIEW public.incident_view;
DROP VIEW public.travel_time_event_view;
DROP VIEW public.time_action_view;
DROP VIEW public.dms_action_view CASCADE;


ALTER TABLE iris._alarm ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._beacon ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._camera ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._detector ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._device_io ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._gate_arm ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._gate_arm ALTER COLUMN ga_array TYPE character varying(20);
ALTER TABLE iris._dms ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._dms ALTER COLUMN beacon TYPE character varying(20);
ALTER TABLE iris._device_preset ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._device_preset ALTER COLUMN preset TYPE character varying(20);
ALTER TABLE iris._gate_arm_array ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._gate_arm_array ALTER COLUMN prereq TYPE character varying(20);
ALTER TABLE iris._gate_arm_array ALTER COLUMN camera TYPE character varying(20);
ALTER TABLE iris._gate_arm_array ALTER COLUMN approach TYPE character varying(20);
ALTER TABLE iris._gate_arm_array ALTER COLUMN dms TYPE character varying(20);
ALTER TABLE iris._lane_marking ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._lcs_array ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._lcs_indication ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._lcs_indication ALTER COLUMN lcs TYPE character varying(20);
ALTER TABLE iris._ramp_meter ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._ramp_meter ALTER COLUMN beacon TYPE character varying(20);
ALTER TABLE iris._tag_reader ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris._video_monitor ALTER COLUMN camera TYPE character varying(20);
ALTER TABLE iris._weather_sensor ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris.beacon_action ALTER COLUMN beacon TYPE character varying(20);
ALTER TABLE iris.camera_preset ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris.camera_preset ALTER COLUMN camera TYPE character varying(20);
ALTER TABLE iris.dms_sign_group ALTER COLUMN dms TYPE character varying(20);
ALTER TABLE iris.tag_reader_dms ALTER COLUMN tag_reader TYPE character varying(20);
ALTER TABLE iris.tag_reader_dms ALTER COLUMN dms TYPE character varying(20);
ALTER TABLE iris.meter_action ALTER COLUMN ramp_meter TYPE character varying(20);
ALTER TABLE iris.lcs ALTER COLUMN name TYPE character varying(20);
ALTER TABLE iris.lcs ALTER COLUMN lcs_array TYPE character varying(20);
ALTER TABLE iris.lane_action ALTER COLUMN lane_marking TYPE character varying(20);
ALTER TABLE event.alarm_event ALTER COLUMN alarm TYPE character varying(20);
ALTER TABLE event.beacon_event ALTER COLUMN beacon TYPE character varying(20);
ALTER TABLE event.brightness_sample ALTER COLUMN dms TYPE character varying(20);
ALTER TABLE event.detector_event ALTER COLUMN device_id TYPE character varying(20);
ALTER TABLE event.meter_event ALTER COLUMN ramp_meter TYPE character varying(20);
ALTER TABLE event.tag_read_event ALTER COLUMN tag_reader TYPE character varying(20);
ALTER TABLE event.camera_switch_event ALTER COLUMN camera_id TYPE character varying(20);
ALTER TABLE event.incident ALTER COLUMN camera TYPE character varying(20);
ALTER TABLE event.travel_time_event ALTER COLUMN device_id TYPE character varying(20);
ALTER TABLE iris.time_action ALTER COLUMN name TYPE character varying(30);
ALTER TABLE iris.beacon_action ALTER COLUMN name TYPE character varying(30);
ALTER TABLE iris.lane_action ALTER COLUMN name TYPE character varying(30);
ALTER TABLE iris.meter_action ALTER COLUMN name TYPE character varying(30);
ALTER TABLE iris.dms_action ALTER COLUMN name TYPE character varying(30);



CREATE VIEW iris.device_geo_loc_view AS
	SELECT name, geo_loc FROM iris._lane_marking UNION ALL
	SELECT name, geo_loc FROM iris._beacon UNION ALL
	SELECT name, geo_loc FROM iris._weather_sensor UNION ALL
	SELECT name, geo_loc FROM iris._tag_reader UNION ALL
	SELECT name, geo_loc FROM iris._camera UNION ALL
	SELECT name, geo_loc FROM iris._dms UNION ALL
	SELECT name, geo_loc FROM iris._ramp_meter UNION ALL
	SELECT g.name, geo_loc FROM iris._gate_arm g
	JOIN iris._gate_arm_array ga ON g.ga_array = ga.name UNION ALL
	SELECT d.name, geo_loc FROM iris._detector d
	JOIN iris.r_node rn ON d.r_node = rn.name;

CREATE VIEW iris.alarm AS
	SELECT a.name, description, controller, pin, state, trigger_time
	FROM iris._alarm a JOIN iris._device_io d ON a.name = d.name;

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.trigger_time, a.controller,
		a.pin, c.comm_link, c.drop_id
	FROM iris.alarm a LEFT JOIN iris.controller c ON a.controller = c.name;
	GRANT SELECT ON alarm_view TO PUBLIC;

CREATE TRIGGER alarm_insert_trig
    INSTEAD OF INSERT ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_insert();

CREATE TRIGGER alarm_update_trig
    INSTEAD OF UPDATE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_update();

CREATE TRIGGER alarm_delete_trig
    INSTEAD OF DELETE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_delete();

CREATE VIEW alarm_event_view AS
	SELECT e.event_id, e.event_date, ed.description AS event_description,
		e.alarm, a.description
	FROM event.alarm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN iris.alarm a ON e.alarm = a.name;
GRANT SELECT ON alarm_event_view TO PUBLIC;

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, beacon, preset,
	       aws_allowed, aws_controlled, sign_config
	FROM iris._dms dms
	JOIN iris._device_io d ON dms.name = d.name
	JOIN iris._device_preset p ON dms.name = p.name;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_delete();

CREATE VIEW geo_loc_view AS
	SELECT l.name, r.abbrev AS rd, l.roadway,
	r_dir.direction AS road_dir, r_dir.dir AS rdir,
	m.modifier AS cross_mod, m.mod AS xmod, c.abbrev as xst,
	l.cross_street, c_dir.direction AS cross_dir,
	l.lat, l.lon, l.landmark
	FROM iris.geo_loc l
	LEFT JOIN iris.road r ON l.roadway = r.name
	LEFT JOIN iris.road_modifier m ON l.cross_mod = m.id
	LEFT JOIN iris.road c ON l.cross_street = c.name
	LEFT JOIN iris.direction r_dir ON l.road_dir = r_dir.id
	LEFT JOIN iris.direction c_dir ON l.cross_dir = c_dir.id;
	GRANT SELECT ON geo_loc_view TO PUBLIC;

CREATE VIEW r_node_view AS
	SELECT n.name, roadway, road_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, n.above,
	tr.name AS transition, n.lanes, n.attach_side, n.shift, n.active,
	n.abandoned, n.station_id, n.speed_limit, n.notes
	FROM iris.r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN iris.r_node_type nt ON n.node_type = nt.n_type
	JOIN iris.r_node_transition tr ON n.transition = tr.n_transition;
	GRANT SELECT ON r_node_view TO PUBLIC;

CREATE VIEW roadway_station_view AS
	SELECT station_id, roadway, road_dir, cross_mod, cross_street, active,
	speed_limit
	FROM iris.r_node r, geo_loc_view l
	WHERE r.geo_loc = l.name AND station_id IS NOT NULL;
	GRANT SELECT ON roadway_station_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
	SELECT c.name, drop_id, comm_link, cabinet, condition, c.notes,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller_view c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name;
	GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.beacon,
	       p.camera, p.preset_num, d.aws_allowed, d.aws_controlled,
	       d.sign_config, sc.default_font,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
	GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW iris.ramp_meter AS
	SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
	       max_wait, algorithm, am_target, pm_target, beacon, preset, m_lock
	FROM iris._ramp_meter m
	JOIN iris._device_io d ON m.name = d.name
	JOIN iris._device_preset p ON m.name = p.name;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_insert();

CREATE TRIGGER ramp_meter_update_trig
    INSTEAD OF UPDATE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_update();

CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_delete();

CREATE VIEW ramp_meter_view AS
	SELECT m.name, geo_loc, controller, pin, notes,
	       mt.description AS meter_type, storage, max_wait,
	       alg.description AS algorithm, am_target, pm_target, beacon,
	       camera, preset_num, ml.description AS meter_lock,
	       l.rd, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, l.lat, l.lon
	FROM iris.ramp_meter m
	LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
	LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
	LEFT JOIN iris.camera_preset p ON m.preset = p.name
	LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
	GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, cam_num, encoder_type, encoder,
		enc_mcast, encoder_channel, publish
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_insert();

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_delete();

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, cam_num, encoder_type, c.encoder, c.enc_mcast,
	       c.encoder_channel, c.publish, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
	GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW iris.beacon AS
	SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin,
	       preset
	FROM iris._beacon b
	JOIN iris._device_io d ON b.name = d.name
	JOIN iris._device_preset p ON b.name = p.name;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_insert();

CREATE TRIGGER beacon_update_trig
    INSTEAD OF UPDATE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_update();

CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_delete();

CREATE VIEW iris.detector AS SELECT
	det.name, controller, pin, r_node, lane_type, lane_number, abandoned,
	force_fail, field_length, fake, notes
	FROM iris._detector det JOIN iris._device_io d ON det.name = d.name;

CREATE TRIGGER detector_insert_trig
    INSTEAD OF INSERT ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_insert();

CREATE TRIGGER detector_update_trig
    INSTEAD OF UPDATE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_update();

CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_delete();

CREATE VIEW beacon_view AS
	SELECT b.name, b.notes, b.message, p.camera, p.preset_num, b.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon,
	       b.controller, b.pin, b.verify_pin, ctr.comm_link, ctr.drop_id,
	       ctr.condition
	FROM iris.beacon b
	LEFT JOIN iris.camera_preset p ON b.preset = p.name
	LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
	LEFT JOIN controller_view ctr ON b.controller = ctr.name;
	GRANT SELECT ON beacon_view TO PUBLIC;

CREATE VIEW iris.lane_marking AS SELECT
	m.name, geo_loc, controller, pin, notes
	FROM iris._lane_marking m JOIN iris._device_io d ON m.name = d.name;

CREATE TRIGGER lane_marking_insert_trig
    INSTEAD OF INSERT ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_insert();

CREATE TRIGGER lane_marking_update_trig
    INSTEAD OF UPDATE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_update();

CREATE TRIGGER lane_marking_delete_trig
    INSTEAD OF DELETE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_delete();

CREATE VIEW lane_marking_view AS
	SELECT m.name, m.notes, m.geo_loc, l.roadway, l.road_dir, l.cross_mod,
	       l.cross_street, l.cross_dir, l.lat, l.lon,
	       m.controller, m.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.lane_marking m
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
	GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW iris.weather_sensor AS SELECT
	m.name, geo_loc, controller, pin, notes
	FROM iris._weather_sensor m JOIN iris._device_io d ON m.name = d.name;

CREATE TRIGGER weather_sensor_insert_trig
    INSTEAD OF INSERT ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_insert();

CREATE TRIGGER weather_sensor_update_trig
    INSTEAD OF UPDATE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_update();

CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_delete();

CREATE VIEW weather_sensor_view AS
	SELECT w.name, w.notes, w.geo_loc, l.roadway, l.road_dir, l.cross_mod,
	       l.cross_street, l.cross_dir, l.lat, l.lon,
	       w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller_view ctr ON w.controller = ctr.name;
	GRANT SELECT ON weather_sensor_view TO PUBLIC;

CREATE VIEW iris.tag_reader AS SELECT
	t.name, geo_loc, controller, pin, notes, toll_zone
	FROM iris._tag_reader t JOIN iris._device_io d ON t.name = d.name;

CREATE TRIGGER tag_reader_insert_trig
    INSTEAD OF INSERT ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_insert();

CREATE TRIGGER tag_reader_update_trig
    INSTEAD OF UPDATE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_update();

CREATE TRIGGER tag_reader_delete_trig
    INSTEAD OF DELETE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_delete();

CREATE VIEW tag_reader_view AS
	SELECT t.name, t.notes, t.toll_zone, t.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       t.controller, t.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.tag_reader t
	LEFT JOIN geo_loc_view l ON t.geo_loc = l.name
	LEFT JOIN controller_view ctr ON t.controller = ctr.name;
	GRANT SELECT ON tag_reader_view TO PUBLIC;

CREATE VIEW iris.gate_arm_array AS SELECT
	_gate_arm_array.name, geo_loc, controller, pin, notes, prereq, camera,
	approach, dms, open_msg, closed_msg
	FROM iris._gate_arm_array JOIN iris._device_io
	ON _gate_arm_array.name = _device_io.name;

CREATE TRIGGER gate_arm_array_update_trig
    INSTEAD OF INSERT OR UPDATE OR DELETE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_update();

CREATE VIEW iris.gate_arm AS
	SELECT _gate_arm.name, ga_array, idx, controller, pin, notes
	FROM iris._gate_arm JOIN iris._device_io
	ON _gate_arm.name = _device_io.name;

CREATE TRIGGER gate_arm_update_trig
    INSTEAD OF INSERT OR UPDATE OR DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_update();

CREATE VIEW gate_arm_array_view AS
	SELECT ga.name, ga.notes, ga.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       ga.controller, ga.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach, ga.dms, ga.open_msg,
	       ga.closed_msg
	FROM iris.gate_arm_array ga
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON ga.controller = ctr.name;
	GRANT SELECT ON gate_arm_array_view TO PUBLIC;

CREATE VIEW gate_arm_view AS
	SELECT g.name, g.ga_array, g.notes, ga.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       g.controller, g.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach, ga.dms, ga.open_msg,
	       ga.closed_msg
	FROM iris.gate_arm g
	JOIN iris.gate_arm_array ga ON g.ga_array = ga.name
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON g.controller = ctr.name;
	GRANT SELECT ON gate_arm_view TO PUBLIC;

CREATE VIEW detector_label_view AS
	SELECT d.name AS det_id,
	iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
	GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_fail_view AS SELECT DISTINCT ON (device_id)
	device_id, description AS fail_reason
	FROM event.detector_event de
	JOIN event.event_description ed ON de.event_desc_id = ed.event_desc_id
	ORDER BY device_id, event_id DESC;
	GRANT SELECT ON detector_fail_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
	       iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
	       d.lane_type, d.lane_number, d.abandoned) AS label,
	       rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, d.lane_number, d.field_length,
	       ln.description AS lane_type, d.abandoned, d.force_fail,
	       df.fail_reason, c.condition, d.fake, d.notes
	FROM (iris.detector d
	LEFT OUTER JOIN detector_fail_view df
		ON d.name = df.device_id AND force_fail = 't')
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

CREATE VIEW controller_device_view AS
	SELECT d.name, d.controller, d.pin, g.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) AS corridor,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_loc
	FROM iris._device_io d
	JOIN iris.device_geo_loc_view g ON d.name = g.name
	JOIN geo_loc_view l ON g.geo_loc = l.name;
	GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, l.landmark, cab.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
	GRANT SELECT ON controller_report TO PUBLIC;

CREATE VIEW device_controller_view AS
	SELECT name, controller, pin
	FROM iris._device_io;
GRANT SELECT ON device_controller_view TO PUBLIC;

CREATE VIEW tag_read_event_view AS
	SELECT event_id, event_date, event_description.description,
	       tag_type.description AS tag_type, agency, tag_id, tag_reader,
	       toll_zone, tollway, hov, trip_id
	FROM event.tag_read_event
	JOIN event.event_description
	ON   tag_read_event.event_desc_id = event_description.event_desc_id
	JOIN event.tag_type
	ON   tag_read_event.tag_type = tag_type.id
	JOIN iris.tag_reader
	ON   tag_read_event.tag_reader = tag_reader.name
	LEFT JOIN iris.toll_zone
	ON        tag_reader.toll_zone = toll_zone.name;
	GRANT SELECT ON tag_read_event_view TO PUBLIC;

CREATE TRIGGER tag_read_event_view_update_trig
    INSTEAD OF UPDATE ON tag_read_event_view
    FOR EACH ROW EXECUTE PROCEDURE event.tag_read_event_view_update();

CREATE VIEW iris.lcs_indication AS SELECT
	d.name, controller, pin, lcs, indication
	FROM iris._lcs_indication li JOIN iris._device_io d ON li.name = d.name;

CREATE TRIGGER lcs_indication_insert_trig
    INSTEAD OF INSERT ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_insert();

CREATE TRIGGER lcs_indication_update_trig
    INSTEAD OF UPDATE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_update();

CREATE TRIGGER lcs_indication_delete_trig
    INSTEAD OF DELETE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_delete();

CREATE VIEW lcs_indication_view AS
	SELECT name, controller, pin, lcs, description AS indication
	FROM iris.lcs_indication
	JOIN iris.lane_use_indication ON indication = id;
GRANT SELECT ON lcs_indication_view TO PUBLIC;

CREATE VIEW iris.lcs_array AS SELECT
	d.name, controller, pin, notes, shift, lcs_lock
	FROM iris._lcs_array la JOIN iris._device_io d ON la.name = d.name;

CREATE TRIGGER lcs_array_insert_trig
    INSTEAD OF INSERT ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_insert();

CREATE TRIGGER lcs_array_update_trig
    INSTEAD OF UPDATE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_update();

CREATE TRIGGER lcs_array_delete_trig
    INSTEAD OF DELETE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_delete();

CREATE VIEW lcs_array_view AS
	SELECT name, shift, notes, lcs_lock
	FROM iris.lcs_array;
	GRANT SELECT ON lcs_array_view TO PUBLIC;

CREATE VIEW camera_preset_view AS
	SELECT cp.name, camera, preset_num, direction, dp.name AS device
	FROM iris.camera_preset cp
	JOIN iris._device_preset dp ON cp.name = dp.preset;
	GRANT SELECT ON camera_preset_view TO PUBLIC;

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, multi, rank
	FROM iris.dms_sign_group dsg
	JOIN iris.sign_group sg ON dsg.sign_group = sg.name
	JOIN iris.sign_text st ON sg.name = st.sign_group;
	GRANT SELECT ON sign_text_view TO PUBLIC;

CREATE VIEW tag_reader_dms_view AS
	SELECT tag_reader, dms
	FROM iris.tag_reader_dms;
	GRANT SELECT ON tag_reader_dms_view TO PUBLIC;

CREATE VIEW lcs_view AS
	SELECT name, lcs_array, lane
	FROM iris.lcs;
GRANT SELECT ON lcs_view TO PUBLIC;

CREATE VIEW meter_event_view AS
	SELECT event_id, event_date, event_description.description,
	       ramp_meter, meter_phase.description AS phase,
	       meter_queue_state.description AS q_state, q_len, dem_adj,
	       wait_secs, meter_limit_control.description AS limit_ctrl,
	       min_rate, rel_rate, max_rate, d_node, seg_density
	FROM event.meter_event
	JOIN event.event_description
	ON meter_event.event_desc_id = event_description.event_desc_id
	JOIN event.meter_phase ON phase = meter_phase.id
	JOIN event.meter_queue_state ON q_state = meter_queue_state.id
	JOIN event.meter_limit_control ON limit_ctrl = meter_limit_control.id;
	GRANT SELECT ON meter_event_view TO PUBLIC;

CREATE VIEW iris.video_monitor AS SELECT
	m.name, controller, pin, notes, mon_num, direct, restricted,
	monitor_style, camera
	FROM iris._video_monitor m JOIN iris._device_io d ON m.name = d.name;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_insert();

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_update();

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_delete();

CREATE VIEW video_monitor_view AS
	SELECT m.name, m.notes, mon_num, direct, restricted, monitor_style,
	       m.controller, ctr.condition, ctr.comm_link, camera
	FROM iris.video_monitor m
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
	GRANT SELECT ON video_monitor_view TO PUBLIC;


CREATE VIEW beacon_event_view AS
	SELECT event_id, event_date, event_description.description, beacon
	FROM event.beacon_event
	JOIN event.event_description
	ON beacon_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON beacon_event_view TO PUBLIC;

CREATE VIEW camera_switch_event_view AS
	SELECT event_id, event_date, event_description.description, monitor_id,
	       camera_id, source
	FROM event.camera_switch_event
	JOIN event.event_description
	ON camera_switch_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON camera_switch_event_view TO PUBLIC;

CREATE VIEW incident_view AS
    SELECT iu.event_id, name, iu.event_date, ed.description, road,
           d.direction, iu.impact, iu.cleared, iu.confirmed, camera,
           ln.description AS lane_type, detail, replaces, lat, lon
    FROM event.incident i
    JOIN event.incident_update iu ON i.name = iu.incident
    LEFT JOIN event.event_description ed ON i.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_type ln ON i.lane_type = ln.id;
GRANT SELECT ON incident_view TO PUBLIC;

CREATE VIEW travel_time_event_view AS
	SELECT event_id, event_date, event_description.description, device_id
	FROM event.travel_time_event
	JOIN event.event_description
	ON travel_time_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON travel_time_event_view TO PUBLIC;

CREATE VIEW time_action_view AS
	SELECT name, action_plan, day_plan, sched_date, time_of_day, phase
	FROM iris.time_action;
GRANT SELECT ON time_action_view TO PUBLIC;

CREATE VIEW meter_action_view AS
	SELECT ramp_meter, ta.phase, time_of_day, day_plan, sched_date
	FROM iris.meter_action ma, iris.action_plan ap, iris.time_action ta
	WHERE ma.action_plan = ap.name
	AND ap.name = ta.action_plan
	AND active = true
	ORDER BY ramp_meter, time_of_day;
GRANT SELECT ON meter_action_view TO PUBLIC;

CREATE VIEW dms_action_view AS
	SELECT name, action_plan, sign_group, phase, quick_message,
	       beacon_enabled, a_priority, r_priority
	FROM iris.dms_action;
GRANT SELECT ON dms_action_view TO PUBLIC;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, state, toll_zone, action_plan, dms_action_view.quick_message
    FROM dms_action_view
    JOIN iris.dms_sign_group
    ON dms_action_view.sign_group = dms_sign_group.sign_group
    JOIN iris.quick_message
    ON dms_action_view.quick_message = quick_message.name
    JOIN iris.quick_message_toll_zone
    ON dms_action_view.quick_message = quick_message_toll_zone.quick_message;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

COMMIT;
