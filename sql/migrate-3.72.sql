SET SESSION AUTHORIZATION 'tms';

CREATE TABLE geo_loc (
	name VARCHAR(20) PRIMARY KEY,
	freeway VARCHAR(20) REFERENCES road(name),
	free_dir smallint REFERENCES direction(id),
	cross_street VARCHAR(20) REFERENCES road(name),
	cross_dir smallint REFERENCES direction(id),
	cross_mod smallint REFERENCES road_modifier(id),
	easting integer,
	east_off integer,
	northing integer,
	north_off integer
);

INSERT INTO geo_loc (name, freeway, free_dir, cross_street, cross_dir,
	cross_mod, easting, east_off, northing, north_off)
	(SELECT 'ctl_' || ctl.vault_oid, freeway, free_dir, cross_street,
	cross_dir, cross_mod, easting, east_off, northing, north_off
	FROM controller ctl
	JOIN location loc ON ctl.location = loc.vault_oid);

INSERT INTO geo_loc (name, freeway, free_dir, cross_street, cross_dir,
	cross_mod, easting, east_off, northing, north_off)
	(SELECT 'dev_' || dev.vault_oid, freeway, free_dir, cross_street,
	cross_dir, cross_mod, easting, east_off, northing, north_off
	FROM device dev
	JOIN location loc ON dev.location = loc.vault_oid);

INSERT INTO geo_loc (name, freeway, free_dir, cross_street, cross_dir,
	cross_mod, easting, east_off, northing, north_off)
	(SELECT 'nod_' || nod.vault_oid, freeway, free_dir, cross_street,
	cross_dir, cross_mod, easting, east_off, northing, north_off
	FROM node nod
	JOIN location loc ON nod.location = loc.vault_oid);

INSERT INTO geo_loc (name, freeway, free_dir, cross_street, cross_dir,
	cross_mod, easting, east_off, northing, north_off)
	(SELECT 'rnd_' || rnd.vault_oid, freeway, free_dir, cross_street,
	cross_dir, cross_mod, easting, east_off, northing, north_off
	FROM r_node rnd
	JOIN location loc ON rnd.location = loc.vault_oid);

UPDATE geo_loc SET easting = null WHERE easting = 0;
UPDATE geo_loc SET east_off = null WHERE east_off = 0;
UPDATE geo_loc SET northing = null WHERE northing = 0;
UPDATE geo_loc SET north_off = null WHERE north_off = 0;

ALTER TABLE node ADD COLUMN geo_loc VARCHAR(20);
ALTER TABLE node ADD CONSTRAINT fk_node_loc
	FOREIGN KEY (geo_loc) REFERENCES geo_loc(name);
UPDATE node SET geo_loc = 'nod_' || vault_oid;

ALTER TABLE device ADD COLUMN geo_loc VARCHAR(20);
ALTER TABLE device ADD CONSTRAINT fk_device_loc
	FOREIGN KEY (geo_loc) REFERENCES geo_loc(name);
UPDATE device SET geo_loc = 'dev_' || vault_oid;

ALTER TABLE controller ADD COLUMN geo_loc VARCHAR(20);
ALTER TABLE controller ADD CONSTRAINT fk_controller_loc
	FOREIGN KEY (geo_loc) REFERENCES geo_loc(name);
UPDATE controller SET geo_loc = 'ctl_' || vault_oid;

ALTER TABLE r_node ADD COLUMN geo_loc VARCHAR(20);
ALTER TABLE r_node ADD CONSTRAINT fk_r_node_loc
	FOREIGN KEY (geo_loc) REFERENCES geo_loc(name);
UPDATE r_node SET geo_loc = 'rnd_' || vault_oid;

DROP VIEW ramp_meter_view;
DROP VIEW dms_view;
DROP VIEW detector_view;
DROP VIEW camera_view;
DROP VIEW r_node_view;
DROP VIEW freeway_station_view;
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW circuit_node_view;
DROP VIEW controller_location;
DROP VIEW device_location_view;
DROP VIEW location_view;
DROP TABLE location;

ALTER TABLE node DROP COLUMN location;
ALTER TABLE device DROP COLUMN location;
ALTER TABLE controller DROP COLUMN location;
ALTER TABLE r_node DROP COLUMN location;

CREATE VIEW geo_loc_view AS
	SELECT l.name, f.abbrev AS fwy, l.freeway,
	f_dir.direction AS free_dir, f_dir.dir AS fdir,
	m.modifier AS cross_mod, m.mod AS xmod, c.abbrev as xst,
	l.cross_street, c_dir.direction AS cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM geo_loc l
	LEFT JOIN road f ON l.freeway = f.name
	LEFT JOIN road_modifier m ON l.cross_mod = m.id
	LEFT JOIN road c ON l.cross_street = c.name
	LEFT JOIN direction f_dir ON l.free_dir = f_dir.id
	LEFT JOIN direction c_dir ON l.cross_dir = c_dir.id;
GRANT SELECT ON geo_loc_view TO PUBLIC;

CREATE VIEW ramp_meter_view AS
	SELECT m.vault_oid, m.id, m.notes,
	m."controlMode" AS control_mode, m."singleRelease" AS single_release,
	m."storage", m."maxWait" AS max_wait, c.id AS camera,
	l.fwy, l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM ramp_meter m
	JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN camera c ON m.camera = c.vault_oid;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.id, d.notes, c.id AS camera, d.mile, d.travel,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN camera c ON d.camera = c.vault_oid;
GRANT SELECT ON dms_view TO PUBLIC;

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
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN lane_type ln ON d."laneType" = ln.id
	LEFT JOIN controller c ON d.controller = c.vault_oid
	LEFT JOIN line_drop_view ld ON d.controller = ld.vault_oid;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.id, ld.line, ld."drop", ctr.active, c.notes,
	c.encoder, c.encoder_channel, c.nvr, c.publish,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM camera c
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN line_drop_view ld ON c.controller = ld.vault_oid
	LEFT JOIN controller ctr ON c.controller = ctr.vault_oid;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW r_node_view AS
	SELECT n.vault_oid, freeway, free_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, tr.name AS transition,
	n.lanes, n.attach_side, n.shift, n.station_id, n.speed_limit, n.notes
	FROM r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN r_node_type nt ON n.node_type = nt.n_type
	JOIN r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

CREATE VIEW freeway_station_view AS
	SELECT station_id, freeway, free_dir, cross_mod, cross_street,
	speed_limit
	FROM r_node r, geo_loc_view l
	WHERE r.geo_loc = l.name AND station_id != '';
GRANT SELECT ON freeway_station_view TO PUBLIC;

CREATE VIEW circuit_node_view AS
	SELECT c.vault_oid, c.id, cl."index" AS line, cl."bitRate",
	l.freeway, l.cross_street
	FROM circuit c
	JOIN communication_line cl ON c.line = cl.vault_oid
	JOIN node n ON c.node = n.vault_oid
	JOIN geo_loc_view l ON n.geo_loc = l.name;
GRANT SELECT ON circuit_node_view TO PUBLIC;

CREATE VIEW controller_device_view AS
	SELECT d.id, d.controller, d.pin,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM traffic_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

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
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN circuit_node_view cn ON c.circuit = cn.vault_oid
	LEFT JOIN controller_170 c1 ON c.vault_oid = c1.vault_oid
	LEFT JOIN cabinet_types ct ON c1.cabinet = ct."index"
	LEFT JOIN controller_device_view d1 ON
		d1.pin = 2 AND d1.controller = c.vault_oid
	LEFT JOIN controller_device_view d2 ON
		d2.pin = 3 AND d2.controller = c.vault_oid;
GRANT SELECT ON controller_report TO PUBLIC;

CREATE VIEW controller_loc_view AS
	SELECT c.vault_oid, c."drop", c.active, c.notes, c.mile, c.circuit,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller c
	JOIN geo_loc_view l ON c.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW device_loc_view AS
	SELECT d.vault_oid, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir
	FROM device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON device_loc_view TO PUBLIC;
