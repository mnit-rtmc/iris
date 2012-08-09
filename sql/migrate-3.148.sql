\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.148.0'
	WHERE name = 'database_version';

CREATE OR REPLACE FUNCTION zone_meridian(zone INTEGER) RETURNS double precision
	AS $$
BEGIN
	RETURN radians((zone - 1) * 6 - 180 + 3);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION utm_to_lat(zone INTEGER, easting INTEGER,
	northing INTEGER) RETURNS double precision AS $$
DECLARE
	a double precision;	-- equatorial radius
	prad double precision;	-- polar radius
	ecc double precision;	-- eccentricity
	e2 double precision;	-- eccentricity squared
	k0 double precision;	-- scale at central meridian
	ep2 double precision;	-- eccentricity squared prime
	e1 double precision;
	x double precision;
	y double precision;
	m double precision;
	mu double precision;
	phi double precision;
	n1 double precision;
	t1 double precision;
	t2 double precision;
	c1 double precision;
	c2 double precision;
	r1 double precision;
	d double precision;
	lat_r double precision;	-- latitude radians
BEGIN
	a := 6378137.0;
	prad := 6356752.314245;
	ecc := sqrt(1 - prad ^ 2 / a ^ 2);
	e2 := ecc ^ 2;
	k0 := 0.9996;
	ep2 := e2 / (1 - e2);
	e1 := (1 - sqrt(1 - e2)) / (1 + sqrt(1 - e2));
	x := easting - 500000.0;
	y := northing;
	m := y / k0;
	mu := m / (a * (1 - e2 / 4 - 3 * e2 ^ 2 / 64 - 5 * e2 ^ 3 / 256));
	phi := (mu +
                (3 * e1 / 2 - 27 * e1 ^ 3 / 32) * sin(2 * mu) +
                (21 * e1 ^ 2 / 16 - 55 * e1 ^ 4 / 32) * sin(4 * mu) +
                (151 * e1 ^ 3 / 96) * sin(6 * mu)
        );
	n1 := a / sqrt(1 - e2 * sin(phi) ^ 2);
	t1 := tan(phi) ^ 2;
	t2 := t1 ^ 2;
	c1 := ep2 * cos(phi) ^ 2;
	c2 := c1 ^ 2;
	r1 := a * (1 - e2) / (1 - e2 * sin(phi) ^ 2) ^ 1.5;
	d := x / (n1 * k0);
	lat_r := phi - (n1 * tan(phi) / r1) * (
		d ^ 2 / 2 - (5 + 3 * t1 + 10 * c1 - 4 * c2 - 9 * ep2)
                * d ^ 4 / 24 +
                (61 + 90 * t1 + 298 * c1 + 45 * t2 - 252 * ep2 - 3 * c2)
                * d ^ 6 / 720
        );
	RETURN degrees(lat_r);
END
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION utm_to_lon(zone INTEGER, easting INTEGER,
	northing INTEGER) RETURNS double precision AS $$
DECLARE
	a double precision;	-- equatorial radius
	prad double precision;	-- polar radius
	ecc double precision;	-- eccentricity
	e2 double precision;	-- eccentricity squared
	k0 double precision;	-- scale at central meridian
	ep2 double precision;	-- eccentricity squared prime
	e1 double precision;
	x double precision;
	y double precision;
	m double precision;
	mu double precision;
	phi double precision;
	n1 double precision;
	t1 double precision;
	t2 double precision;
	c1 double precision;
	c2 double precision;
	d double precision;
	lon_r double precision; -- longitude radians
BEGIN
	a := 6378137.0;
	prad := 6356752.314245;
	ecc := sqrt(1 - prad ^ 2 / a ^ 2);
	e2 := ecc ^ 2;
	k0 := 0.9996;
	ep2 := e2 / (1 - e2);
	e1 := (1 - sqrt(1 - e2)) / (1 + sqrt(1 - e2));
	x := easting - 500000.0;
	y := northing;
	m := y / k0;
	mu := m / (a * (1 - e2 / 4 - 3 * e2 ^ 2 / 64 - 5 * e2 ^ 3 / 256));
	phi := (mu +
                (3 * e1 / 2 - 27 * e1 ^ 3 / 32) * sin(2 * mu) +
                (21 * e1 ^ 2 / 16 - 55 * e1 ^ 4 / 32) * sin(4 * mu) +
                (151 * e1 ^ 3 / 96) * sin(6 * mu)
        );
	n1 := a / sqrt(1 - e2 * sin(phi) ^ 2);
	t1 := tan(phi) ^ 2;
	t2 := t1 ^ 2;
	c1 := ep2 * cos(phi) ^ 2;
	c2 := c1 ^ 2;
	d := x / (n1 * k0);
	lon_r := (d - (1 + 2 * t1 + c1) * d ^ 3 / 6 +
                (5 - 2 * c1 + 28 * t1 - 3 * c2 + 8 * ep2 + 24 * t2)
                * d ^ 5 / 120) / cos(phi) + zone_meridian(zone);
	RETURN degrees(lon_r);
END
$$ LANGUAGE plpgsql;

DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW detector_view;
DROP VIEW detector_event_view;
DROP VIEW detector_label_view;
DROP VIEW weather_sensor_view;
DROP VIEW lane_marking_view;
DROP VIEW warning_sign_view;
DROP VIEW camera_view;
DROP VIEW ramp_meter_view;
DROP VIEW dms_view;
DROP VIEW controller_loc_view;
DROP VIEW roadway_station_view;
DROP VIEW r_node_view;
DROP VIEW geo_loc_view;

ALTER TABLE iris.geo_loc ADD COLUMN lat double precision;
ALTER TABLE iris.geo_loc ADD COLUMN lon double precision;
ALTER TABLE event.incident ADD COLUMN lat double precision;
ALTER TABLE event.incident ADD COLUMN lon double precision;

UPDATE iris.geo_loc SET lat = utm_to_lat(
(SELECT value::INTEGER FROM iris.system_attribute WHERE name = 'map_utm_zone'),
easting, northing);
UPDATE iris.geo_loc SET lon = utm_to_lon(
(SELECT value::INTEGER FROM iris.system_attribute WHERE name = 'map_utm_zone'),
easting, northing);

UPDATE event.incident SET lat = utm_to_lat(
(SELECT value::INTEGER FROM iris.system_attribute WHERE name = 'map_utm_zone'),
easting, northing);
UPDATE event.incident SET lon = utm_to_lon(
(SELECT value::INTEGER FROM iris.system_attribute WHERE name = 'map_utm_zone'),
easting, northing);

DROP FUNCTION utm_to_lat(INTEGER, INTEGER, INTEGER);
DROP FUNCTION utm_to_lon(INTEGER, INTEGER, INTEGER);
DROP FUNCTION zone_meridian(INTEGER);

ALTER TABLE iris.geo_loc DROP COLUMN easting;
ALTER TABLE iris.geo_loc DROP COLUMN northing;
ALTER TABLE event.incident DROP COLUMN easting;
ALTER TABLE event.incident DROP COLUMN northing;

CREATE VIEW geo_loc_view AS
	SELECT l.name, r.abbrev AS rd, l.roadway,
	r_dir.direction AS road_dir, r_dir.dir AS rdir,
	m.modifier AS cross_mod, m.mod AS xmod, c.abbrev as xst,
	l.cross_street, c_dir.direction AS cross_dir,
	l.lat, l.lon
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
	SELECT c.name, c.drop_id, c.comm_link, c.cabinet, c.active, c.notes,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.camera,
	d.aws_allowed, d.aws_controlled, d.default_font,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.lat, l.lon
	FROM iris.dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW ramp_meter_view AS
	SELECT m.name, geo_loc, controller, pin, notes,
	mt.description AS meter_type, storage, max_wait,
	alg.description AS algorithm, am_target, pm_target, camera,
	ml.description AS meter_lock,
	l.rd, l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.lat, l.lon
	FROM iris.ramp_meter m
	LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
	LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
	LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, c.encoder, c.encoder_channel,
	et.description AS encoder_type, c.publish, c.geo_loc, l.roadway,
	l.road_dir, l.cross_mod, l.cross_street,
	l.cross_dir, l.lat, l.lon,
	c.controller, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.camera c
	JOIN iris.encoder_type et ON c.encoder_type = et.id
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW warning_sign_view AS
	SELECT w.name, w.notes, w.message, w.camera, w.geo_loc,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.lat, l.lon,
	w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.warning_sign w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON w.controller = ctr.name;
GRANT SELECT ON warning_sign_view TO PUBLIC;

CREATE VIEW lane_marking_view AS
	SELECT m.name, m.notes, m.geo_loc,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.lat, l.lon,
	m.controller, m.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.lane_marking m
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
	SELECT w.name, w.notes, w.geo_loc,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.lat, l.lon,
	w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

CREATE VIEW detector_label_view AS
	SELECT d.name AS det_id,
	detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label
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
	SELECT d.name AS det_id, d.r_node, d.controller, c.comm_link, c.drop_id,
	d.pin, detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label,
	rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	l.cross_dir, d.lane_number, d.field_length, ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d.force_fail) AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN iris.controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW controller_device_view AS
	SELECT d.name, d.controller, d.pin, d.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) AS corridor,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_loc
	FROM iris.controller_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
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

DELETE FROM iris.system_attribute WHERE name = 'map_utm_zone';
