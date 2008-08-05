\set ON_ERROR_STOP

CREATE SCHEMA event;
ALTER SCHEMA event OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

CREATE TABLE comm_link (
	name VARCHAR(20) PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	url VARCHAR(64) NOT NULL,
	protocol smallint NOT NULL,
	timeout integer NOT NULL
);

INSERT INTO comm_link (name, description, url, protocol, timeout)
	(SELECT 'L' || trim(both FROM to_char(l."index", '009')),
	description::VARCHAR(32), port, protocol, timeout
	FROM communication_line l);

CREATE TABLE cabinet_style (
	name VARCHAR(20) PRIMARY KEY,
	dip integer
);

UPDATE cabinet_types SET name = '334Z-05' WHERE "index" = 13;

COPY cabinet_style (name, dip) FROM stdin;
336	0
334Z	1
334D	2
334Z-94	3
Drum	4
334DZ	5
334	6
334Z-99	7
S334Z	9
Prehistoric	10
334Z-00	11
334Z-05	13
334ZP	15
\.

CREATE TABLE cabinet (
	name VARCHAR(20) PRIMARY KEY,
	style VARCHAR(20) REFERENCES cabinet_style(name),
	geo_loc VARCHAR(20) REFERENCES geo_loc(name),
	mile real
);

INSERT INTO cabinet (name, geo_loc, mile)
	(SELECT 'cab_' || vault_oid, geo_loc, mile FROM ONLY controller);
INSERT INTO cabinet (name, style, geo_loc, mile)
	(SELECT 'cab_' || vault_oid, t.name, geo_loc, mile
	FROM controller_170 JOIN cabinet_types t ON cabinet = t."index");
UPDATE cabinet SET mile = NULL WHERE mile = 0;

CREATE TEMP TABLE ctl_temp (
	name VARCHAR(20),
	drop_id smallint NOT NULL,
	comm_link VARCHAR(20) NOT NULL,
	cabinet VARCHAR(20),
	active boolean NOT NULL,
	notes text NOT NULL
);

INSERT INTO ctl_temp (name, drop_id, comm_link, cabinet, active, notes)
	(SELECT 'ctl_' || ctl.vault_oid, "drop",
	'L' || trim(both FROM to_char(l."index", '009')),
	'cab_' || ctl.vault_oid, active, notes FROM controller ctl
	JOIN circuit ON ctl.circuit = circuit.vault_oid
	JOIN communication_line l ON circuit.line = l.vault_oid);

DROP VIEW controller_alarm_view;
DROP VIEW camera_view;
DROP VIEW detector_view;
DROP VIEW line_drop_view;
DROP VIEW controller_loc_view;
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW circuit_node_view;

ALTER TABLE controller DROP COLUMN circuit;

DROP TABLE controller_170;
DROP TABLE controller;
DROP TABLE circuit;
DROP TABLE node;
DROP TABLE node_group;
DROP TABLE cabinet_types;
DROP TABLE communication_line;

CREATE TABLE controller (
	name VARCHAR(20) PRIMARY KEY,
	drop_id smallint NOT NULL,
	comm_link VARCHAR(20) NOT NULL REFERENCES comm_link(name),
	cabinet VARCHAR(20) REFERENCES cabinet(name),
	active boolean NOT NULL,
	notes text NOT NULL
);

INSERT INTO controller (name, drop_id, comm_link, cabinet, active, notes)
	(SELECT name, drop_id, comm_link, cabinet, active, notes
	FROM ctl_temp);

ALTER TABLE alarm ADD COLUMN ctl VARCHAR(20);
UPDATE alarm SET ctl = 'ctl_' || controller;
ALTER TABLE alarm DROP COLUMN controller;
ALTER TABLE alarm ADD COLUMN controller VARCHAR(20);
ALTER TABLE alarm ADD CONSTRAINT fk_alarm_controller
	FOREIGN KEY (controller) REFERENCES controller(name);
UPDATE alarm SET controller = ctl;
ALTER TABLE alarm DROP COLUMN ctl;

ALTER TABLE device ADD COLUMN ctl VARCHAR(20);
UPDATE device SET ctl = 'ctl_' || controller;
UPDATE device SET ctl = NULL WHERE controller = 0;
ALTER TABLE device DROP COLUMN controller;
ALTER TABLE device ADD COLUMN controller VARCHAR(20);
ALTER TABLE device ADD CONSTRAINT fk_device_controller
	FOREIGN KEY (controller) REFERENCES controller(name);
ALTER TABLE detector ADD CONSTRAINT fk_detector_controller
	FOREIGN KEY (controller) REFERENCES controller(name);
ALTER TABLE dms ADD CONSTRAINT fk_dms_controller
	FOREIGN KEY (controller) REFERENCES controller(name);
ALTER TABLE ramp_meter ADD CONSTRAINT fk_meter_controller
	FOREIGN KEY (controller) REFERENCES controller(name);
ALTER TABLE camera ADD CONSTRAINT fk_camera_controller
	FOREIGN KEY (controller) REFERENCES controller(name);
ALTER TABLE lcs ADD CONSTRAINT fk_lcs_controller
	FOREIGN KEY (controller) REFERENCES controller(name);
ALTER TABLE warning_sign ADD CONSTRAINT fk_warning_sign_controller
	FOREIGN KEY (controller) REFERENCES controller(name);
UPDATE device SET controller = ctl;
ALTER TABLE device DROP COLUMN ctl;

CREATE VIEW controller_loc_view AS
	SELECT c.name, c.drop_id, c.comm_link, c.cabinet, c.active, c.notes,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller c
	LEFT JOIN cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW controller_device_view AS
	SELECT d.id, d.controller, d.pin,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM traffic_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.id, ctr.comm_link, ctr.drop_id, ctr.active, c.notes,
	c.encoder, c.encoder_channel, c.nvr, c.publish,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM camera c
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d."index" AS det_id, c.comm_link, c.drop_id, d.pin,
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
	LEFT JOIN controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.comm_link, c.drop_id, cab.mile,
	trim(l.freeway || ' ' || l.free_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d1.id AS "id (meter1)",
	d1.cross_street AS "from (meter1)", d1.freeway AS "to (meter1)",
	d2.id AS meter2, d2.cross_street AS "from (meter2)",
	d2.freeway AS "to (meter2)", c.notes
	FROM controller c
	LEFT JOIN cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d1 ON
		d1.pin = 2 AND d1.controller = c.name
	LEFT JOIN controller_device_view d2 ON
		d2.pin = 3 AND d2.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

DELETE FROM vault_types WHERE "table" = 'station';
DELETE FROM vault_types WHERE "table" = 'controller';
DELETE FROM vault_types WHERE "table" = 'controller_170';
DELETE FROM vault_types WHERE "table" = 'circuit';
DELETE FROM vault_types WHERE "table" = 'node';
DELETE FROM vault_types WHERE "table" = 'node_group';

SET search_path = event, public, pg_catalog;

CREATE SEQUENCE event.event_id_seq;

CREATE TABLE event.event_description (
	event_desc_id integer PRIMARY KEY,
	description text NOT NULL
);

CREATE TABLE event.comm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	controller VARCHAR(20) NOT NULL REFERENCES controller(name)
		ON DELETE CASCADE,
	device_id VARCHAR(20)
);

CREATE TABLE event.detector_event (
	event_id integer DEFAULT nextval('event_id_seq') NOT NULL,
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id integer REFERENCES detector("index")
);

CREATE TABLE event.sign_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20),
	message text,
	iris_user VARCHAR(15) REFERENCES iris_user(name)
);

SET search_path = public, event, pg_catalog;

CREATE VIEW comm_event_view AS
	SELECT e.event_id, e.event_date, ed.description,
		e.controller, c.comm_link, c.drop_id
	FROM comm_event e
	JOIN event_description ed ON e.event_desc_id = ed.event_desc_id
	LEFT JOIN controller c ON e.controller = c.name;
GRANT SELECT ON comm_event_view TO PUBLIC;

CREATE VIEW detector_label_view AS
	SELECT d."index" AS det_id,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d."laneType", d."laneNumber", d.abandoned) AS label
	FROM detector d
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM detector_event e
	JOIN event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE FUNCTION event.message_line(text, integer) RETURNS text AS
'DECLARE
	message ALIAS FOR $1;
	line ALIAS FOR $2;
	word text;
	wstop int2;
BEGIN
	word := message;

	FOR w in 1..(line-1) LOOP
		wstop := strpos(word, ''[nl]'');
		IF wstop > 0 THEN
			word := SUBSTR(word, wstop + 4);
		ELSE
			word := '''';
		END IF;
	END LOOP;
	wstop := strpos(word, ''[nl]'');
	IF wstop > 0 THEN
		word := SUBSTR(word, 0, wstop);
	END IF;
	RETURN word;
END;' LANGUAGE plpgsql;

CREATE VIEW sign_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id,
		message_line(e.message, 1) AS line1,
		message_line(e.message, 2) AS line2,
		message_line(e.message, 3) AS line3,
		e.iris_user
	FROM sign_event e
	JOIN event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
	SELECT * FROM sign_event_view
	WHERE (CURRENT_TIMESTAMP - event_date) < interval '90 days';
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

COPY event.event_description (event_desc_id, description) FROM stdin;
8	Comm ERROR
9	Comm RESTORED
65	Comm FAILED
89	LCS DEPLOYED
90	LCS CLEARED
91	Sign DEPLOYED
92	Sign CLEARED
94	NO HITS
95	LOCKED ON
96	CHATTER
\.
