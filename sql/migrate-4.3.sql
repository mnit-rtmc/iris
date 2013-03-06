\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.3.0'
	WHERE name = 'database_version';

CREATE OR REPLACE VIEW detector_fail_view AS SELECT DISTINCT ON (device_id)
	device_id, description AS fail_reason
	FROM event.detector_event de
	JOIN event.event_description ed ON de.event_desc_id = ed.event_desc_id
	ORDER BY device_id, event_id DESC;
GRANT SELECT ON detector_fail_view TO PUBLIC;

DROP VIEW detector_view;
CREATE VIEW detector_view AS
	SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id,
	d.pin, detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label,
	rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	l.cross_dir, d.lane_number, d.field_length, ln.description AS lane_type,
	d.abandoned, d.force_fail, df.fail_reason, c.active, d.fake, d.notes
	FROM (iris.detector d
	LEFT OUTER JOIN detector_fail_view df
		ON d.name = df.device_id AND force_fail = 't')
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN iris.controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

DROP FUNCTION boolean_converter(boolean);

INSERT INTO iris.comm_protocol (id, description) VALUES (27, 'Axis PTZ');
