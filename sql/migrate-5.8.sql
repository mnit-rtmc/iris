\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.7.0', '5.8.0');

-- Drop abandoned column from r_node
DROP VIEW r_node_view;
ALTER TABLE iris.r_node DROP CONSTRAINT active_ck;
ALTER TABLE iris.r_node DROP COLUMN abandoned;

-- Drop old views and function
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW weather_sensor_view;
DROP VIEW tag_reader_view;
DROP VIEW ramp_meter_view;
DROP VIEW parking_area_view;
DROP VIEW lane_marking_view;
DROP VIEW gate_arm_view;
DROP VIEW gate_arm_array_view;
DROP VIEW dms_view;
DROP VIEW detector_auto_fail_view;
DROP VIEW detector_view;
DROP VIEW detector_event_view;
DROP VIEW detector_label_view;
DROP VIEW beacon_view;
DROP VIEW camera_view;
DROP VIEW controller_loc_view;
DROP VIEW roadway_station_view;
DROP VIEW geo_loc_view;
DROP FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT);

-- Create new function and views
CREATE FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT)
	RETURNS TEXT AS $geo_location$
DECLARE
	roadway ALIAS FOR $1;
	road_dir ALIAS FOR $2;
	cross_mod ALIAS FOR $3;
	cross_street ALIAS FOR $4;
	cross_dir ALIAS FOR $5;
	landmark ALIAS FOR $6;
	corridor TEXT;
	xloc TEXT;
	lmrk TEXT;
BEGIN
	corridor = trim(roadway || concat(' ', road_dir));
	xloc = trim(concat(cross_mod, ' ') || cross_street
	    || concat(' ', cross_dir));
	lmrk = replace('(' || landmark || ')', '()', '');
	RETURN trim(concat(corridor, ' ' || xloc, ' ' || lmrk));
END;
$geo_location$ LANGUAGE plpgsql;

CREATE VIEW geo_loc_view AS
	SELECT l.name, r.abbrev AS rd, l.roadway, r_dir.direction AS road_dir,
	       r_dir.dir AS rdir, m.modifier AS cross_mod, m.mod AS xmod,
	       c.abbrev as xst, l.cross_street, c_dir.direction AS cross_dir,
	       l.landmark, l.lat, l.lon,
	       trim(l.roadway || concat(' ', r_dir.direction)) AS corridor,
	       iris.geo_location(l.roadway, r_dir.direction, m.modifier,
	       l.cross_street, c_dir.direction, l.landmark) AS location
	FROM iris.geo_loc l
	LEFT JOIN iris.road r ON l.roadway = r.name
	LEFT JOIN iris.road_modifier m ON l.cross_mod = m.id
	LEFT JOIN iris.road c ON l.cross_street = c.name
	LEFT JOIN iris.direction r_dir ON l.road_dir = r_dir.id
	LEFT JOIN iris.direction c_dir ON l.cross_dir = c_dir.id;
GRANT SELECT ON geo_loc_view TO PUBLIC;

CREATE VIEW r_node_view AS
	SELECT n.name, n.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       nt.name AS node_type, n.pickable, n.above, tr.name AS transition,
	       n.lanes, n.attach_side, n.shift, n.active,
	       n.station_id, n.speed_limit, n.notes
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

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, cam_num, encoder_type, c.encoder, c.enc_mcast,
	       c.encoder_channel, c.publish, c.video_loss, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

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

CREATE VIEW detector_label_view AS
	SELECT d.name AS det_id,
	       iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
	                           d.lane_type, d.lane_number, d.abandoned)
	       AS label
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
	       iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
	       d.lane_type, d.lane_number, d.abandoned) AS label,
	       rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, d.lane_number, d.field_length,
	       ln.description AS lane_type, d.abandoned, d.force_fail,
	       d.auto_fail, c.condition, d.fake, d.notes
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN controller_view c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

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

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, dp.description AS purpose, d.beacon, p.camera,
	       p.preset_num, d.sign_config, d.sign_detail, default_font,
	       override_font, override_foreground, override_background,
	       msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW gate_arm_array_view AS
	SELECT ga.name, ga.notes, ga.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       ga.controller, ga.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach, ga.action_plan, ga.open_phase,
	       ga.closed_phase
	FROM iris.gate_arm_array ga
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON ga.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

CREATE VIEW gate_arm_view AS
	SELECT g.name, g.ga_array, g.notes, ga.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       g.controller, g.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach
	FROM iris.gate_arm g
	JOIN iris.gate_arm_array ga ON g.ga_array = ga.name
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON g.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

CREATE VIEW lane_marking_view AS
	SELECT m.name, m.notes, m.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       m.controller, m.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.lane_marking m
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW parking_area_view AS
	SELECT pa.name, site_id, time_stamp_static, relevant_highway,
	       reference_post, exit_id, facility_name, street_adr, city, state,
	       zip, time_zone, ownership, capacity, low_threshold,
	       (SELECT string_agg(a.amenity, ', ') FROM
	        (SELECT bit, amenity FROM iris.parking_area_amenities(amenities)
	         ORDER BY bit) AS a) AS amenities,
	       time_stamp, reported_available, true_available, trend, open,
	       trust_data, last_verification_check, verification_check_amplitude,
	       p1.camera AS camera_1, p2.camera AS camera_2,
	       p3.camera AS camera_3,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       sa.value AS camera_image_base_url
	FROM iris.parking_area pa
	LEFT JOIN iris.camera_preset p1 ON preset_1 = p1.name
	LEFT JOIN iris.camera_preset p2 ON preset_2 = p2.name
	LEFT JOIN iris.camera_preset p3 ON preset_3 = p3.name
	LEFT JOIN geo_loc_view l ON pa.geo_loc = l.name
	LEFT JOIN iris.system_attribute sa ON sa.name = 'camera_image_base_url';
GRANT SELECT ON parking_area_view TO PUBLIC;

CREATE VIEW ramp_meter_view AS
	SELECT m.name, geo_loc, controller, pin, notes,
	       mt.description AS meter_type, storage, max_wait,
	       alg.description AS algorithm, am_target, pm_target, beacon,
	       camera, preset_num, ml.description AS meter_lock,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location, l.rd
	FROM iris.ramp_meter m
	LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
	LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
	LEFT JOIN iris.camera_preset p ON m.preset = p.name
	LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW tag_reader_view AS
	SELECT t.name, t.geo_loc, location, controller, pin, notes, toll_zone,
	       downlink_freq_khz, uplink_freq_khz,
	       sego_atten_downlink_db, sego_atten_uplink_db, sego_data_detect_db,
	       sego_seen_count, sego_unique_count,
	       iag_atten_downlink_db, iag_atten_uplink_db, iag_data_detect_db,
	       iag_seen_count, iag_unique_count, line_loss_db,
	       m.description AS sync_mode, slave_select_count
	FROM iris.tag_reader t
	LEFT JOIN geo_loc_view l ON t.geo_loc = l.name
	LEFT JOIN iris.tag_reader_sync_mode m ON t.sync_mode = m.id;
GRANT SELECT ON tag_reader_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
	SELECT w.name, w.notes, w.settings, w.sample, w.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

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
	       l.location, cab.style AS "type", d.name AS device, d.pin,
	       d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

-- Delete unused IN/OUT directions
DELETE FROM iris.direction WHERE id > 6;

-- Drop alt_dir from road / road_view
DROP VIEW road_view;

ALTER TABLE iris.road DROP COLUMN alt_dir;

CREATE VIEW road_view AS
	SELECT name, abbrev, rcl.description AS r_class, dir.direction
	FROM iris.road r
	LEFT JOIN iris.road_class rcl ON r.r_class = rcl.id
	LEFT JOIN iris.direction dir ON r.direction = dir.id;
GRANT SELECT ON road_view TO PUBLIC;

COMMIT;
