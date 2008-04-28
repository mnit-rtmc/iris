SET SESSION AUTHORIZATION 'tms';

DELETE FROM vault_types WHERE "table" = 'roadway';

CREATE TABLE road_class (
	id integer PRIMARY KEY,
	description VARCHAR(12) NOT NULL,
	grade CHAR NOT NULL
);

COPY road_class (id, description, grade) FROM stdin;
1	residential	A
2	business	B
3	collector	C
4	arterial	D
5	expressway	E
6	freeway	F
7	CD road	
\.

CREATE TABLE road (
	name VARCHAR(20) PRIMARY KEY,
	abbrev VARCHAR(6) NOT NULL,
	r_class smallint,
	direction smallint,
	alt_dir smallint
);

INSERT INTO road (name, abbrev, r_class, direction)
	(SELECT substring(name FOR 20), abbreviated, "type", direction
	FROM roadway);

UPDATE road SET r_class = NULL WHERE r_class = 0;
UPDATE road SET direction = NULL WHERE direction = 0;

ALTER TABLE road
	ADD CONSTRAINT fk_r_class FOREIGN KEY (r_class)
	REFERENCES road_class(id);
ALTER TABLE road
	ADD CONSTRAINT fk_direction FOREIGN KEY (direction)
	REFERENCES direction(id);
ALTER TABLE road
	ADD CONSTRAINT fk_alt_dir FOREIGN KEY (alt_dir)
	REFERENCES direction(id);

CREATE TEMP TABLE loc_road (
	loc integer NOT NULL,
	freeway VARCHAR(20),
	cross_street VARCHAR(20)
);

INSERT INTO loc_road (loc, freeway, cross_street)
	(SELECT vault_oid, substring(freeway FOR 20),
		substring(cross_street FOR 20)
	FROM location_view);

DROP VIEW controller_location;
DROP VIEW freeway_station_view;
DROP VIEW dms_view;
DROP VIEW r_node_view;
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW circuit_node_view;
DROP VIEW ramp_meter_view;
DROP VIEW detector_view;
DROP VIEW camera_view;
DROP VIEW location_view;

ALTER TABLE "location" DROP COLUMN freeway;
ALTER TABLE "location" ADD COLUMN freeway VARCHAR(20);
ALTER TABLE "location"
	ADD CONSTRAINT fk_freeway FOREIGN KEY (freeway)
	REFERENCES road(name);

ALTER TABLE "location" DROP COLUMN cross_street;
ALTER TABLE "location" ADD COLUMN cross_street VARCHAR(20);
ALTER TABLE "location"
	ADD CONSTRAINT fk_cross_street FOREIGN KEY (cross_street)
	REFERENCES road(name);

UPDATE location SET freeway = loc_road.freeway,
	cross_street = loc_road.cross_street
	FROM loc_road WHERE loc = vault_oid;

CREATE VIEW location_view AS
	SELECT l.vault_oid, f.abbrev AS fwy, l.freeway,
	f_dir.direction AS free_dir, f_dir.dir AS fdir,
	m.modifier AS cross_mod, m.mod AS xmod, c.abbrev as xst,
	l.cross_street, c_dir.direction AS cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM "location" l
	LEFT JOIN road f ON l.freeway = f.name
	LEFT JOIN road_modifier m ON l.cross_mod = m.id
	LEFT JOIN road c ON l.cross_street = c.name
	LEFT JOIN direction f_dir ON l.free_dir = f_dir.id
	LEFT JOIN direction c_dir ON l.cross_dir = c_dir.id;

CREATE VIEW camera_view AS
	SELECT c.id, ld.line, ld."drop", ctr.active, c.notes,
	c.encoder, c.encoder_channel, c.nvr, c.publish,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM camera c
	JOIN location_view l ON c."location" = l.vault_oid
	LEFT JOIN line_drop_view ld ON c.controller = ld.vault_oid
	LEFT JOIN controller ctr ON c.controller = ctr.vault_oid;

CREATE VIEW detector_view AS
	SELECT d."index" AS det_id, ld.line, c."drop", d.pin,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d."laneType", d."laneNumber", d.abandoned) AS label,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	d."laneNumber" AS lane_number, d."fieldLength" AS field_length,
	ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d."forceFail") AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM detector d
	LEFT JOIN location_view l ON d."location" = l.vault_oid
	LEFT JOIN lane_type ln ON d."laneType" = ln.id
	LEFT JOIN controller c ON d.controller = c.vault_oid
	LEFT JOIN line_drop_view ld ON d.controller = ld.vault_oid;

CREATE VIEW ramp_meter_view AS
	SELECT m.vault_oid, m.id, m.notes,
	m."controlMode" AS control_mode, m."singleRelease" AS single_release,
	m."storage", m."maxWait" AS max_wait, c.id AS camera,
	l.fwy, l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM ramp_meter m
	JOIN location_view l ON m."location" = l.vault_oid
	LEFT JOIN camera c ON m.camera = c.vault_oid;

CREATE VIEW circuit_node_view AS
	SELECT c.vault_oid, c.id, cl."index" AS line, cl."bitRate",
	l.freeway, l.cross_street
	FROM circuit c, communication_line cl, node n, location_view l
	WHERE c.line = cl.vault_oid AND c.node = n.vault_oid AND
		n."location" = l.vault_oid;

CREATE VIEW controller_device_view AS
	SELECT d.id, d.controller, d.pin,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM traffic_device d, location_view l WHERE d."location" = l.vault_oid;

CREATE VIEW controller_report AS
	SELECT cn.id AS circuit, cn.line, c."drop", c.mile,
	trim(l.freeway || ' ' || l.free_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	ct.name AS "type", d1.id AS "id (meter1)",
	d1.cross_street AS "from (meter1)", d1.freeway AS "to (meter1)",
	d2.id AS meter2, d2.cross_street AS "from (meter2)",
	d2.freeway AS "to (meter2)", c.notes, cn."bitRate",
	cn.freeway || ' & ' || cn.cross_street AS node_location
	FROM controller c
	LEFT JOIN location_view l ON c."location" = l.vault_oid
	LEFT JOIN circuit_node_view cn ON c.circuit = cn.vault_oid
	LEFT JOIN controller_170 c1 ON c.vault_oid = c1.vault_oid
	LEFT JOIN cabinet_types ct ON c1.cabinet = ct."index"
	LEFT JOIN controller_device_view d1 ON
		d1.pin = 2 AND d1.controller = c.vault_oid
	LEFT JOIN controller_device_view d2 ON
		d2.pin = 3 AND d2.controller = c.vault_oid;

CREATE VIEW r_node_view AS
	SELECT n.vault_oid, freeway, free_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, tr.name AS transition,
	n.lanes, n.attach_side, n.shift, n.station_id, n.speed_limit, n.notes
	FROM r_node n, location_view l, r_node_type nt, r_node_transition tr
	WHERE n."location" = l.vault_oid AND nt.n_type = n.node_type AND
	tr.n_transition = n.transition;

CREATE VIEW dms_view AS
	SELECT d.id, d.notes, c.id AS camera, d.mile, d.travel,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM dms d
	JOIN location_view l ON d."location" = l.vault_oid
	LEFT JOIN camera c ON d.camera = c.vault_oid;

CREATE VIEW freeway_station_view AS
	SELECT station_id, freeway, free_dir, cross_mod, cross_street,
	speed_limit
	FROM r_node r, location_view l
	WHERE r.location = l.vault_oid AND station_id != '';

CREATE VIEW controller_location AS
	SELECT c.vault_oid, c."drop", c.active, c.notes, c.mile, c.circuit,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller c, location_view l WHERE c."location" = l.vault_oid;

CREATE VIEW road_view AS
	SELECT name, abbrev, rcl.description AS r_class, dir.direction,
	adir.direction AS alt_dir
	FROM road
	LEFT JOIN road_class rcl ON road.r_class = rcl.id
	LEFT JOIN direction dir ON road.direction = dir.id
	LEFT JOIN direction adir ON road.alt_dir = adir.id;

CREATE VIEW device_location_view AS
	SELECT d.vault_oid, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir
	FROM device d JOIN location_view l ON d."location" = l.vault_oid;

REVOKE ALL ON TABLE road FROM PUBLIC;
REVOKE ALL ON TABLE location FROM PUBLIC;

GRANT SELECT ON controller_location TO PUBLIC;
GRANT SELECT ON freeway_station_view TO PUBLIC;
GRANT SELECT ON dms_view TO PUBLIC;
GRANT SELECT ON r_node_view TO PUBLIC;
GRANT SELECT ON circuit_node_view TO PUBLIC;
GRANT SELECT ON controller_device_view TO PUBLIC;
GRANT SELECT ON controller_report TO PUBLIC;
GRANT SELECT ON ramp_meter_view TO PUBLIC;
GRANT SELECT ON detector_view TO PUBLIC;
GRANT SELECT ON camera_view TO PUBLIC;
GRANT SELECT ON location_view TO PUBLIC;
GRANT SELECT ON road_view TO PUBLIC;
GRANT SELECT ON device_location_view TO PUBLIC;

DROP TABLE roadway;
