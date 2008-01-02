SET SESSION AUTHORIZATION 'tms';

ALTER TABLE "location" DROP CONSTRAINT fk_free_dir;
ALTER TABLE "location" DROP CONSTRAINT fk_cross_dir;

DROP VIEW controller_report;
DROP VIEW iris_det;
DROP VIEW iris_detector;
DROP VIEW green_detector;
DROP VIEW controller_location;
DROP VIEW controller_alarm;
DROP VIEW controller_device;
DROP VIEW device_location;
DROP VIEW r_node_view;
DROP VIEW segmentlist_view;
DROP VIEW freeway_station_view;
DROP VIEW ramp_meter_location;
DROP VIEW dms_view;
DROP VIEW location_view;
DROP VIEW line_drop;
DROP VIEW circuit_node;
DROP FUNCTION get_circuit(integer);
DROP FUNCTION get_det_no(integer);
DROP FUNCTION get_det_no(integer[]);
DROP FUNCTION get_location(integer, integer);
DROP FUNCTION get_location(integer, smallint, integer, smallint, smallint);
DROP FUNCTION get_location(integer, smallint);
DROP FUNCTION get_dir(smallint);
DROP FUNCTION get_roadway(integer);
DROP FUNCTION get_roadway_abbre(integer);
DROP FUNCTION get_line(integer);
DROP TABLE lane_type_description;
DROP SEQUENCE lane_type_id_seq;
DROP TABLE direction;

ALTER TABLE dms ADD COLUMN travel text;
UPDATE dms SET travel = '';
ALTER TABLE dms ALTER COLUMN travel SET NOT NULL;

UPDATE dms SET travel = 'FREEWAY TIME TO:[nl]' || replace(travel1, '%TIME', '[ttS' || TO_CHAR(dest1, 'FM9999') || ']') || '[nl]' || replace(travel2, '%TIME', '[ttS' || TO_CHAR(dest2, 'FM9999') || ']') WHERE dest1 > 0;

ALTER TABLE dms DROP COLUMN travel1;
ALTER TABLE dms DROP COLUMN dest1;
ALTER TABLE dms DROP COLUMN travel2;
ALTER TABLE dms DROP COLUMN dest2;

ALTER TABLE road_modifier ADD COLUMN mod varchar(2);
UPDATE road_modifier SET mod = '' WHERE id = 0;
UPDATE road_modifier SET mod = 'N' WHERE id = 1;
UPDATE road_modifier SET mod = 'S' WHERE id = 2;
UPDATE road_modifier SET mod = 'E' WHERE id = 3;
UPDATE road_modifier SET mod = 'W' WHERE id = 4;
UPDATE road_modifier SET mod = 'Nj' WHERE id = 5;
UPDATE road_modifier SET mod = 'Sj' WHERE id = 6;
UPDATE road_modifier SET mod = 'Ej' WHERE id = 7;
UPDATE road_modifier SET mod = 'Wj' WHERE id = 8;
ALTER TABLE road_modifier ALTER COLUMN mod SET NOT NULL;

CREATE TABLE direction (
    id smallint PRIMARY KEY,
    direction character varying(4) NOT NULL,
    dir character varying(4) NOT NULL
);

COPY direction (id, direction, dir) FROM stdin;
0		
1	NB	N
2	SB	S
3	EB	E
4	WB	W
5	N-S	N-S
6	E-W	E-W
\.

CREATE TABLE lane_type (
	id smallint PRIMARY KEY,
	description text NOT NULL,
	dcode varchar(2) NOT NULL
);

COPY lane_type (id, description, dcode) FROM stdin;
0		
1	Mainline	
2	Auxilliary	A
3	CD Lane	CD
4	Reversible	R
5	Merge	M
6	Queue	Q
7	Exit	X
8	Bypass	B
9	Passage	P
10	Velocity	V
11	Omnibus	O
12	Green	G
13	Wrong Way	Y
\.

CREATE VIEW location_view AS
	SELECT l.vault_oid, f.abbreviated AS fwy, f.name AS freeway,
	f_dir.direction AS free_dir, f_dir.dir AS fdir,
	m.modifier AS cross_mod, m.mod AS xmod, c.abbreviated as xst,
	c.name AS cross_street, c_dir.direction AS cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM "location" l LEFT JOIN roadway f ON l.freeway = f.vault_oid
	LEFT JOIN road_modifier m ON l.cross_mod = m.id
	LEFT JOIN roadway c ON l.cross_street = c.vault_oid
	LEFT JOIN direction f_dir ON l.free_dir = f_dir.id
	LEFT JOIN direction c_dir ON l.cross_dir = c_dir.id;

CREATE VIEW dms_view AS
	SELECT d.id, d.notes, c.id AS camera, d.mile, d.travel,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM dms d
	JOIN location_view l ON d."location" = l.vault_oid
	LEFT JOIN camera c ON d.camera = c.vault_oid;

CREATE VIEW ramp_meter_view AS
	SELECT m.vault_oid, m.id, m.notes, m.detector, m."controlMode",
	m."singleRelease", m."storage", m."maxWait", c.id AS camera,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM ramp_meter m
	JOIN location_view l ON m."location" = l.vault_oid
	LEFT JOIN camera c ON m.camera = c.vault_oid;

CREATE VIEW freeway_station_view AS
	SELECT station_id, freeway, free_dir, cross_mod, cross_street,
	speed_limit
	FROM r_node r, location_view l
	WHERE r.location = l.vault_oid AND station_id != '';

CREATE VIEW r_node_view AS
	SELECT n.vault_oid, freeway, free_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, tr.name AS transition,
	n.lanes, n.attach_side, n.shift, n.station_id, n.speed_limit, n.notes
	FROM r_node n, location_view l, r_node_type nt, r_node_transition tr
	WHERE n."location" = l.vault_oid AND nt.n_type = n.node_type AND
	tr.n_transition = n.transition;

CREATE VIEW controller_location AS
	SELECT c.vault_oid, c."drop", c.active, c.notes, c.mile, c.circuit,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller c, location_view l WHERE c."location" = l.vault_oid;

CREATE VIEW green_detector_view AS
	SELECT d."index" AS det_no, l.fwy AS freeway, l.free_dir,
	l.xst AS cross_street, l.cross_dir, ramp_meter.id AS ramp_id
	FROM detector d LEFT JOIN location_view l ON d."location" = l.vault_oid
	JOIN ramp_meter ON d.vault_oid = ramp_meter.detector;

CREATE VIEW line_drop_view AS
	SELECT c.vault_oid, l."index" AS line, c."drop"
	FROM circuit cir, controller c, communication_line l
	WHERE c.circuit = cir.vault_oid AND l.vault_oid = cir.line;

CREATE VIEW controller_alarm_view AS
	SELECT l.line, l."drop", a.pin, a.notes
	FROM line_drop_view l, alarm a
	WHERE l.vault_oid = a.controller;

CREATE FUNCTION detector_label(text, varchar, text, varchar, text, smallint,
	smallint, boolean, boolean) RETURNS text AS
'	DECLARE
		fwy ALIAS FOR $1;
		fdir ALIAS FOR $2;
		xst ALIAS FOR $3;
		cross_dir ALIAS FOR $4;
		xmod ALIAS FOR $5;
		l_type ALIAS FOR $6;
		lane_number ALIAS FOR $7;
		hov ALIAS FOR $8;
		abandoned ALIAS FOR $9;
		xmd varchar(2);
		ltyp varchar(2);
		lnum varchar(2);
		suffix varchar(5);
	BEGIN
		IF fwy IS NULL OR xst IS NULL THEN
			RETURN ''FUTURE'';
		END IF;
		SELECT INTO ltyp dcode FROM lane_type WHERE id = l_type;
		lnum = '''';
		IF lane_number > 0 THEN
			lnum = TO_CHAR(lane_number, ''FM9'');
		END IF;
		xmd = '''';
		IF xmod != ''@'' THEN
			xmd = xmod;
		END IF;
		suffix = '''';
		IF hov THEN
			suffix = ''H'';
		END IF;
		IF abandoned THEN
			suffix = ''-ABND'';
		END IF;
		RETURN fwy || ''/'' || cross_dir || xmd || xst || fdir ||
			ltyp || lnum || suffix;
	END;'
LANGUAGE plpgsql;

CREATE VIEW detector_view AS
	SELECT d."index" AS det_id, ld.line, c."drop", d.pin,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d."laneType", d."laneNumber", d.hov, d.abandoned) AS label,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	boolean_converter(d.hov) AS hov, d."laneNumber" AS lane_number,
	d."fieldLength" AS field_length, ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d."forceFail") AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM detector d
	LEFT JOIN location_view l ON d."location" = l.vault_oid
	LEFT JOIN lane_type ln ON d."laneType" = ln.id
	LEFT JOIN controller c ON d.controller = c.vault_oid
	LEFT JOIN line_drop_view ld ON d.controller = ld.vault_oid;

CREATE VIEW circuit_node_view AS
	SELECT c.vault_oid, c.id, cl."index" AS line, cl."bitRate",
	l.freeway, l.cross_street
	FROM circuit c, communication_line cl, node n, location_view l
	WHERE c.line = cl.vault_oid AND c.node = n.vault_oid AND
		n."location" = l.vault_oid;

GRANT SELECT ON TABLE circuit_node_view TO PUBLIC;

CREATE VIEW controller_device_view AS
	SELECT d.id, d.controller, d.pin,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM traffic_device d, location_view l WHERE d."location" = l.vault_oid;

GRANT SELECT ON TABLE controller_device_view TO PUBLIC;

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

GRANT SELECT ON TABLE controller_report TO PUBLIC;

ALTER TABLE "location"
	ADD CONSTRAINT fk_free_dir FOREIGN KEY (free_dir)
	REFERENCES direction(id);
ALTER TABLE "location"
	ADD CONSTRAINT fk_cross_dir FOREIGN KEY (cross_dir)
	REFERENCES direction(id);

GRANT SELECT ON direction TO PUBLIC;
GRANT SELECT ON lane_type TO PUBLIC;
GRANT SELECT ON location_view TO PUBLIC;
GRANT SELECT ON green_detector_view TO PUBLIC;
GRANT SELECT ON detector_view TO PUBLIC;
GRANT SELECT ON dms_view TO PUBLIC;
GRANT SELECT ON ramp_meter_view TO PUBLIC;
GRANT SELECT ON line_drop_view TO PUBLIC;
GRANT SELECT ON controller_alarm_view TO PUBLIC;
GRANT SELECT ON controller_location TO PUBLIC;
GRANT SELECT ON r_node_view TO PUBLIC;
GRANT SELECT ON freeway_station_view TO PUBLIC;
