\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.18.0'
	WHERE name = 'database_version';

CREATE OR REPLACE VIEW iris.device_geo_loc_view AS
	SELECT name, geo_loc FROM iris._lane_marking UNION ALL
	SELECT name, geo_loc FROM iris._beacon UNION ALL
	SELECT name, geo_loc FROM iris._weather_sensor UNION ALL
	SELECT name, geo_loc FROM iris._camera UNION ALL
	SELECT name, geo_loc FROM iris._dms UNION ALL
	SELECT name, geo_loc FROM iris._ramp_meter UNION ALL
	SELECT g.name, geo_loc FROM iris._gate_arm g
	JOIN iris._gate_arm_array ga ON g.ga_array = ga.name UNION ALL
	SELECT d.name, geo_loc FROM iris._detector d
	JOIN iris.r_node rn ON d.r_node = rn.name;

CREATE OR REPLACE VIEW controller_device_view AS
	SELECT d.name, d.controller, d.pin, g.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) AS corridor,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_loc
	FROM iris._device_io d
	JOIN iris.device_geo_loc_view g ON d.name = g.name
	JOIN geo_loc_view l ON g.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE OR REPLACE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.mile, cab.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
                            priv_d)
       VALUES ('prv_cc1', 'camera_control', 'camera/.*/deviceRequest', false,
               true, false, false);
