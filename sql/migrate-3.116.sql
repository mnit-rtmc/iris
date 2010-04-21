\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.116.0'
	WHERE name = 'database_version';

CREATE VIEW lane_marking_view AS
	SELECT m.name, m.notes, m.geo_loc,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing,
	m.controller, m.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.lane_marking m
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

