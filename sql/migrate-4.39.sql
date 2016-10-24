\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.39.0'
	WHERE name = 'database_version';

CREATE OR REPLACE VIEW iris.quick_message_priced AS
    SELECT name AS quick_message, 'priced'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzp,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzp%';
CREATE OR REPLACE VIEW iris.quick_message_open AS
    SELECT name AS quick_message, 'open'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzo,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzo%';
CREATE OR REPLACE VIEW iris.quick_message_closed AS
    SELECT name AS quick_message, 'closed'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzc,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzc%';
CREATE OR REPLACE VIEW iris.quick_message_toll_zone AS
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_priced UNION ALL
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_open UNION ALL
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_closed;

CREATE OR REPLACE VIEW dms_toll_zone_view AS
    SELECT dms, state, toll_zone, action_plan, dms_action_view.quick_message
    FROM dms_action_view
    JOIN iris.dms_sign_group
    ON dms_action_view.sign_group = dms_sign_group.sign_group
    JOIN iris.quick_message
    ON dms_action_view.quick_message = quick_message.name
    JOIN iris.quick_message_toll_zone
    ON dms_action_view.quick_message = quick_message_toll_zone.quick_message;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

-- Rename CBW protocol to match CommProtocol enum
UPDATE iris.comm_protocol SET description = 'CBW' WHERE id = 33;

-- Drop views using geo_loc milepoint
DROP VIEW detector_event_view;
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW detector_view;
DROP VIEW detector_label_view;
DROP VIEW gate_arm_view;
DROP VIEW gate_arm_array_view;
DROP VIEW tag_reader_view;
DROP VIEW weather_sensor_view;
DROP VIEW lane_marking_view;
DROP VIEW beacon_view;
DROP VIEW camera_view;
DROP VIEW ramp_meter_view;
DROP VIEW dms_view;
DROP VIEW controller_loc_view;
DROP VIEW roadway_station_view;
DROP VIEW r_node_view;
DROP VIEW geo_loc_view;

-- Rename geo_loc milepoint to landmark
ALTER TABLE iris.geo_loc RENAME COLUMN milepoint TO landmark;
ALTER TABLE iris.geo_loc ALTER COLUMN landmark TYPE VARCHAR(24);

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
	       d.default_font,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;

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

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, et.description AS encoder_type, c.encoder,
	       c.enc_mcast, c.encoder_channel, st.description AS stream_type,
	       c.publish, c.geo_loc, l.roadway,
	       l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,l.lat,l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN iris.encoder_type et ON c.encoder_type = et.id
	LEFT JOIN iris.stream_type st ON c.stream_type = st.id
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

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

CREATE VIEW lane_marking_view AS
	SELECT m.name, m.notes, m.geo_loc, l.roadway, l.road_dir, l.cross_mod,
	       l.cross_street, l.cross_dir, l.lat, l.lon,
	       m.controller, m.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.lane_marking m
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
	SELECT w.name, w.notes, w.geo_loc, l.roadway, l.road_dir, l.cross_mod,
	       l.cross_street, l.cross_dir, l.lat, l.lon,
	       w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

CREATE VIEW tag_reader_view AS
	SELECT t.name, t.notes, t.toll_zone, t.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       t.controller, t.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.tag_reader t
	LEFT JOIN geo_loc_view l ON t.geo_loc = l.name
	LEFT JOIN controller_view ctr ON t.controller = ctr.name;
GRANT SELECT ON tag_reader_view TO PUBLIC;

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

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

-- Drop video monitor views
DROP VIEW video_monitor_view;
DROP VIEW iris.video_monitor;

-- Add camera column to video_monitor table
ALTER TABLE iris._video_monitor
	ADD COLUMN camera VARCHAR(10) REFERENCES iris._camera;

CREATE VIEW iris.video_monitor AS SELECT
	m.name, controller, pin, notes, restricted, camera
	FROM iris._video_monitor m JOIN iris._device_io d ON m.name = d.name;

CREATE OR REPLACE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
	$video_monitor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._video_monitor (name, notes, restricted, camera)
	     VALUES (NEW.name, NEW.notes, NEW.restricted, NEW.camera);
	RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_insert();

CREATE OR REPLACE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
	$video_monitor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._video_monitor
	   SET notes = NEW.notes,
	       restricted = NEW.restricted,
	       camera = NEW.camera
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_update();

CREATE OR REPLACE FUNCTION iris.video_monitor_delete() RETURNS TRIGGER AS
	$video_monitor_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$video_monitor_delete$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_delete();

CREATE VIEW video_monitor_view AS
	SELECT m.name, m.notes, restricted, m.controller,
	       ctr.condition, ctr.comm_link, camera
	FROM iris.video_monitor m
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;
