\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.80.0' WHERE name = 'database_version';

INSERT INTO system_attribute
	SELECT 'meter_green_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'meter_green_time';
INSERT INTO system_attribute
	SELECT 'meter_yellow_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'meter_yellow_time';
INSERT INTO system_attribute
	SELECT 'meter_min_red_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'meter_min_red_time';
INSERT INTO system_attribute
	SELECT 'dms_page_on_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'dms_page_on_time';
INSERT INTO system_attribute
	SELECT 'dms_page_off_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'dms_page_off_time';
INSERT INTO system_attribute
	SELECT 'incident_ring_1_miles', value::varchar
	FROM system_policy WHERE name = 'ring_radius_0';
INSERT INTO system_attribute
	SELECT 'incident_ring_2_miles', value::varchar
	FROM system_policy WHERE name = 'ring_radius_1';
INSERT INTO system_attribute
	SELECT 'incident_ring_3_miles', value::varchar
	FROM system_policy WHERE name = 'ring_radius_2';

DROP TABLE system_policy;

DROP VIEW detector_view;

CREATE VIEW detector_view AS
	SELECT d.name AS det_id, d.r_node, d.controller, c.comm_link, c.drop_id,
	d.pin, detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label,
	rnd.geo_loc, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir, d.lane_number, d.field_length, ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d.force_fail) AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN lane_type ln ON d.lane_type = ln.id
	LEFT JOIN controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;
