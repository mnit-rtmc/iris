\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.21.0'
	WHERE name = 'database_version';

-- delete insecure system attributes (gate_arm_enable security)
DELETE FROM iris.system_attribute WHERE name = 'sample_archive_directory';
DELETE FROM iris.system_attribute WHERE name = 'kml_filename';
DELETE FROM iris.system_attribute WHERE name = 'uptime_log_filename';
DELETE FROM iris.system_attribute WHERE name = 'xml_output_directory';

-- delete obsolete station_xml_enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'station_xml_enable';

-- add condition look-up table
CREATE TABLE iris.condition (
	id INTEGER PRIMARY KEY,
	description VARCHAR(12) NOT NULL
);

COPY iris.condition (id, description) FROM stdin;
0	Planned
1	Active
2	Construction
3	Removed
\.

-- add condition column to controller table
ALTER TABLE iris.controller ADD COLUMN condition INTEGER REFERENCES
	iris.condition;
UPDATE iris.controller SET condition = 1 WHERE active = true;
UPDATE iris.controller SET condition = 2 WHERE active = false;
ALTER TABLE iris.controller ALTER COLUMN condition SET NOT NULL;

-- drop old views
DROP VIEW detector_view;
DROP VIEW gate_arm_view;
DROP VIEW gate_arm_array_view;
DROP VIEW tag_reader_view;
DROP VIEW weather_sensor_view;
DROP VIEW lane_marking_view;
DROP VIEW beacon_view;
DROP VIEW camera_view;
DROP VIEW controller_loc_view;
DROP VIEW controller_view;

-- recreate views
CREATE VIEW controller_view AS
	SELECT c.name, drop_id, comm_link, cabinet,
	       cnd.description AS condition, notes, cab.geo_loc
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
	SELECT c.name, drop_id, comm_link, cabinet, condition, c.notes,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller_view c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, c.encoder, c.encoder_channel,
	       et.description AS encoder_type, c.publish, c.geo_loc, l.roadway,
	       l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,l.lat,l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN iris.encoder_type et ON c.encoder_type = et.id
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW beacon_view AS
	SELECT b.name, b.notes, b.message, p.camera, p.preset_num, b.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon,
	       b.controller, b.pin, ctr.comm_link, ctr.drop_id, ctr.condition
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
	SELECT t.name, t.notes, t.geo_loc, l.roadway, l.road_dir, l.cross_mod,
	       l.cross_street, l.cross_dir, l.lat, l.lon,
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

-- drop active column from controller table
ALTER TABLE iris.controller DROP COLUMN active;

-- Replace active privilege with condition
UPDATE iris.privilege SET pattern = replace(pattern, 'active', 'condition')
	WHERE pattern LIKE 'controller%';
