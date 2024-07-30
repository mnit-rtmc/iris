\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.57.0', '5.58.0');

-- Add exent IDs for DMS MSG RESET and Client UPDATE PASSWORD
INSERT INTO event.event_description (event_desc_id, description)
    VALUES (83, 'DMS MSG RESET'), (209, 'Client UPDATE PASSWORD');

-- Change notes columns to VARCHAR(256)
DROP VIEW beacon_view;
DROP VIEW iris.beacon;
DROP VIEW gps_view;
DROP VIEW iris.gps;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP VIEW camera_view;
DROP VIEW detector_auto_fail_view;
DROP VIEW detector_event_view;
DROP VIEW detector_view;
DROP VIEW detector_label_view;
DROP VIEW iris.detector;
DROP VIEW flow_stream_view;
DROP VIEW gate_arm_array_view;
DROP VIEW iris.gate_arm_array;
DROP VIEW gate_arm_view;
DROP VIEW iris.gate_arm;
DROP VIEW lane_marking_view;
DROP VIEW iris.lane_marking;
DROP VIEW lcs_array_view;
DROP VIEW iris.lcs_array;
DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;
DROP VIEW tag_reader_view;
DROP VIEW iris.tag_reader;
DROP VIEW video_monitor_view;
DROP VIEW iris.video_monitor;
DROP VIEW weather_sensor_view;
DROP VIEW iris.weather_sensor;
DROP VIEW controller_report;
DROP VIEW controller_loc_view;
DROP VIEW controller_view;

ALTER TABLE iris.controller ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris.controller ADD CONSTRAINT controller_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._beacon ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._beacon ADD CONSTRAINT _beacon_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._gps ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._gps ADD CONSTRAINT _gps_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._dms ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._dms ADD CONSTRAINT _dms_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._gate_arm_array ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._gate_arm_array ADD CONSTRAINT _gate_arm_array_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._gate_arm ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._gate_arm ADD CONSTRAINT _gate_arm_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._lane_marking ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._lane_marking ADD CONSTRAINT _lane_marking_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._lcs_array ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._lcs_array ADD CONSTRAINT _lcs_array_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._ramp_meter ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._tag_reader ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._tag_reader ADD CONSTRAINT _tag_reader_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._video_monitor ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._video_monitor ADD CONSTRAINT _video_monitor_notes_check
    CHECK (LENGTH(notes) < 256);

ALTER TABLE iris._weather_sensor ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._weather_sensor ADD CONSTRAINT _weather_sensor_notes_check
    CHECK (LENGTH(notes) < 256);

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

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin,
           ext_mode, preset, state
    FROM iris._beacon b
    JOIN iris.controller_io cio ON b.name = cio.name
    JOIN iris.device_preset p ON b.name = p.name;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_insert();

CREATE TRIGGER beacon_update_trig
    INSTEAD OF UPDATE ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_update();

CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

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

CREATE VIEW iris.gps AS
    SELECT g.name, controller, pin, notes, latest_poll, latest_sample, lat, lon
    FROM iris._gps g
    JOIN iris.controller_io cio ON g.name = cio.name;

CREATE TRIGGER gps_insert_trig
    INSTEAD OF INSERT ON iris.gps
    FOR EACH ROW EXECUTE FUNCTION iris.gps_insert();

CREATE TRIGGER gps_update_trig
    INSTEAD OF UPDATE ON iris.gps
    FOR EACH ROW EXECUTE FUNCTION iris.gps_update();

CREATE TRIGGER gps_delete_trig
    INSTEAD OF DELETE ON iris.gps
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW gps_view AS
    SELECT g.name, controller, pin, notes, latest_poll, latest_sample, lat, lon
    FROM iris._gps g
    JOIN iris.controller_io cio ON g.name = cio.name;
GRANT SELECT ON gps_view TO PUBLIC;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           beacon, preset, sign_config, sign_detail,
           msg_user, msg_sched, msg_current, expire_time, status, stuck_pixels
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_insert();

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
           status, stuck_pixels,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    LEFT JOIN iris.device_preset p ON d.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

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

CREATE VIEW iris.detector AS
    SELECT d.name, controller, pin, r_node, lane_code, lane_number,
           abandoned, force_fail, auto_fail, field_length, fake, notes
    FROM iris._detector d
    JOIN iris.controller_io cio ON d.name = cio.name;

CREATE TRIGGER detector_insert_trig
    INSTEAD OF INSERT ON iris.detector
    FOR EACH ROW EXECUTE FUNCTION iris.detector_insert();

CREATE TRIGGER detector_update_trig
    INSTEAD OF UPDATE ON iris.detector
    FOR EACH ROW EXECUTE FUNCTION iris.detector_update();

CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW detector_label_view AS
    SELECT d.name AS det_id,
           iris.detector_label(
               iris.root_lbl(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod, l.landmark),
               d.lane_code, d.lane_number, d.abandoned
           ) AS label, rnd.geo_loc
    FROM iris._detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

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

CREATE VIEW iris.gate_arm_array AS
    SELECT ga.name, geo_loc, controller, pin, notes, opposing, prereq, camera,
           approach, action_plan, arm_state, interlock
    FROM iris._gate_arm_array ga
    JOIN iris.controller_io cio ON ga.name = cio.name;

CREATE TRIGGER gate_arm_array_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_array_insert();

CREATE TRIGGER gate_arm_array_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_array_update();

CREATE TRIGGER gate_arm_array_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

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

CREATE VIEW iris.gate_arm AS
    SELECT g.name, ga_array, idx, controller, pin, notes, arm_state, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name;

CREATE TRIGGER gate_arm_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_insert();

CREATE TRIGGER gate_arm_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_update();

CREATE TRIGGER gate_arm_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

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

CREATE VIEW iris.lane_marking AS
    SELECT m.name, geo_loc, controller, pin, notes, deployed
    FROM iris._lane_marking m
    JOIN iris.controller_io cio ON m.name = cio.name;

CREATE TRIGGER lane_marking_insert_trig
    INSTEAD OF INSERT ON iris.lane_marking
    FOR EACH ROW EXECUTE FUNCTION iris.lane_marking_insert();

CREATE TRIGGER lane_marking_update_trig
    INSTEAD OF UPDATE ON iris.lane_marking
    FOR EACH ROW EXECUTE FUNCTION iris.lane_marking_update();

CREATE TRIGGER lane_marking_delete_trig
    INSTEAD OF DELETE ON iris.lane_marking
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

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

CREATE VIEW iris.lcs_array AS
    SELECT la.name, controller, pin, notes, shift, lcs_lock
    FROM iris._lcs_array la
    JOIN iris.controller_io cio ON la.name = cio.name;

CREATE TRIGGER lcs_array_insert_trig
    INSTEAD OF INSERT ON iris.lcs_array
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_array_insert();

CREATE TRIGGER lcs_array_update_trig
    INSTEAD OF UPDATE ON iris.lcs_array
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_array_update();

CREATE TRIGGER lcs_array_delete_trig
    INSTEAD OF DELETE ON iris.lcs_array
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW lcs_array_view AS
    SELECT name, shift, notes, lcs_lock
    FROM iris._lcs_array;
GRANT SELECT ON lcs_array_view TO PUBLIC;

CREATE VIEW iris.ramp_meter AS
    SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
           max_wait, algorithm, am_target, pm_target, beacon, preset, m_lock
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    JOIN iris.device_preset p ON m.name = p.name;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_insert();

CREATE TRIGGER ramp_meter_update_trig
    INSTEAD OF UPDATE ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_update();

CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW ramp_meter_view AS
    SELECT m.name, geo_loc, cio.controller, cio.pin, notes,
           mt.description AS meter_type, storage, max_wait,
           alg.description AS algorithm, am_target, pm_target, beacon, camera,
           preset_num, ml.description AS meter_lock, l.roadway, l.road_dir,
           l.cross_mod, l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon,
           l.corridor, l.location, l.rd
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN iris.device_preset p ON m.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
    LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
    LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW iris.tag_reader AS
    SELECT t.name, geo_loc, controller, pin, notes, toll_zone, settings
    FROM iris._tag_reader t
    JOIN iris.controller_io cio ON t.name = cio.name;

CREATE TRIGGER tag_reader_insert_trig
    INSTEAD OF INSERT ON iris.tag_reader
    FOR EACH ROW EXECUTE FUNCTION iris.tag_reader_insert();

CREATE TRIGGER tag_reader_update_trig
    INSTEAD OF UPDATE ON iris.tag_reader
    FOR EACH ROW EXECUTE FUNCTION iris.tag_reader_update();

CREATE TRIGGER tag_reader_delete_trig
    INSTEAD OF DELETE ON iris.tag_reader
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW tag_reader_view AS
    SELECT t.name, t.geo_loc, location, controller, pin, notes, toll_zone,
           settings
    FROM iris._tag_reader t
    JOIN iris.controller_io cio ON t.name = cio.name
    LEFT JOIN geo_loc_view l ON t.geo_loc = l.name;
GRANT SELECT ON tag_reader_view TO PUBLIC;

CREATE VIEW iris.video_monitor AS
    SELECT m.name, controller, pin, notes, group_n, mon_num, restricted,
           monitor_style, camera
    FROM iris._video_monitor m
    JOIN iris.controller_io cio ON m.name = cio.name;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_insert();

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_update();

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW video_monitor_view AS
    SELECT m.name, m.notes, group_n, mon_num, restricted, monitor_style,
           cio.controller, cio.pin, ctr.condition, ctr.comm_link, camera
    FROM iris._video_monitor m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

CREATE VIEW iris.weather_sensor AS
    SELECT w.name, site_id, alt_id, geo_loc, controller, pin, notes, settings,
           sample, sample_time
    FROM iris._weather_sensor w
    JOIN iris.controller_io cio ON w.name = cio.name;

CREATE TRIGGER weather_sensor_insert_trig
    INSTEAD OF INSERT ON iris.weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.weather_sensor_insert();

CREATE TRIGGER weather_sensor_update_trig
    INSTEAD OF UPDATE ON iris.weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.weather_sensor_update();

CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

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

CREATE VIEW controller_report AS
    SELECT c.name, c.comm_link, c.drop_id, l.landmark, c.geo_loc, l.location,
           cabinet_style, d.name AS device, d.pin, d.cross_loc, d.corridor,
           c.notes
    FROM iris.controller c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

-- Concatenate DMS hashtags to notes fields
WITH dms_hashtags AS (
    SELECT name, string_agg(hashtag, ' ' ORDER BY hashtag) hashtags
    FROM hashtag_view
    WHERE resource_n = 'dms'
    GROUP BY name
)
UPDATE iris.dms d
SET notes = concat_ws(e'\n\n', notes, h.hashtags)
FROM dms_hashtags h
WHERE h.name = d.name;

-- Concatenate camera hashtags to notes fields
WITH cam_hashtags AS (
    SELECT name, string_agg(hashtag, ' ' ORDER BY hashtag) hashtags
    FROM hashtag_view
    WHERE resource_n = 'camera'
    GROUP BY name
)
UPDATE iris.camera c
SET notes = concat_ws(e'\n\n', notes, h.hashtags)
FROM cam_hashtags h
WHERE h.name = c.name;

-- Drop hashtag notification trigger
DROP TRIGGER hashtag_notify_trig ON iris.hashtag;
DROP FUNCTION iris.hashtag_notify();

COMMIT;
