\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.99.0' WHERE name = 'database_version';

CREATE TABLE iris.holiday (
	name VARCHAR(32) PRIMARY KEY,
	month INTEGER NOT NULL,
	day INTEGER NOT NULL,
	week INTEGER NOT NULL,
	weekday INTEGER NOT NULL,
	shift INTEGER NOT NULL,
	period INTEGER NOT NULL
);

INSERT INTO iris.holiday (name, month, day, week, weekday, shift, period)
	(SELECT name, month, day, week, weekday, shift, period FROM holiday);

DROP TABLE holiday;

CREATE TABLE iris.lane_type (
	id smallint PRIMARY KEY,
	description VARCHAR(12) NOT NULL,
	dcode VARCHAR(2) NOT NULL
);

INSERT INTO iris.lane_type (id, description, dcode)
	(SELECT id, description, dcode FROM lane_type);

ALTER TABLE iris._detector DROP CONSTRAINT _detector_lane_type_fkey;
ALTER TABLE iris._detector ADD CONSTRAINT _detector_lane_type_fkey
	FOREIGN KEY (lane_type) REFERENCES iris.lane_type;

DROP VIEW detector_view;
DROP VIEW detector_event_view;
DROP VIEW detector_label_view;
DROP FUNCTION detector_label(text, varchar, text, varchar, text, smallint,
	smallint, boolean);

CREATE FUNCTION detector_label(text, varchar, text, varchar, text, smallint,
	smallint, boolean) RETURNS text AS
'	DECLARE
		fwy ALIAS FOR $1;
		fdir ALIAS FOR $2;
		xst ALIAS FOR $3;
		cross_dir ALIAS FOR $4;
		xmod ALIAS FOR $5;
		l_type ALIAS FOR $6;
		lane_number ALIAS FOR $7;
		abandoned ALIAS FOR $8;
		xmd varchar(2);
		ltyp varchar(2);
		lnum varchar(2);
		suffix varchar(5);
	BEGIN
		IF fwy IS NULL OR xst IS NULL THEN
			RETURN ''FUTURE'';
		END IF;
		SELECT INTO ltyp dcode FROM iris.lane_type WHERE id = l_type;
		lnum = '''';
		IF lane_number > 0 THEN
			lnum = TO_CHAR(lane_number, ''FM9'');
		END IF;
		xmd = '''';
		IF xmod != ''@'' THEN
			xmd = xmod;
		END IF;
		suffix = '''';
		IF abandoned THEN
			suffix = ''-ABND'';
		END IF;
		RETURN fwy || ''/'' || cross_dir || xmd || xst || fdir ||
			ltyp || lnum || suffix;
	END;'
LANGUAGE plpgsql;

CREATE VIEW detector_label_view AS
	SELECT d.name AS det_id,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

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
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN iris.controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

DROP TABLE lane_type;

CREATE TABLE iris.video_monitor (
	name VARCHAR(12) PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	restricted boolean NOT NULL
);

INSERT INTO iris.video_monitor (name, description, restricted)
	(SELECT name, description, restricted FROM video_monitor);

DROP TABLE video_monitor;

DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.controller_device;

CREATE VIEW iris.controller_lane_marking AS
	SELECT dio.name, dio.controller, dio.pin, m.geo_loc
	FROM iris._device_io dio
	JOIN iris.lane_marking m ON dio.name = m.name;

CREATE VIEW iris.controller_device AS
	SELECT * FROM iris.controller_dms UNION ALL
	SELECT * FROM iris.controller_lane_marking UNION ALL
	SELECT * FROM iris.controller_lcs UNION ALL
	SELECT * FROM iris.controller_meter UNION ALL
	SELECT * FROM iris.controller_warning_sign UNION ALL
	SELECT * FROM iris.controller_camera;

CREATE VIEW controller_device_view AS
	SELECT d.name, d.controller, d.pin, d.geo_loc,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM iris.controller_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.mile, cab.geo_loc,
	trim(l.freeway || ' ' || l.free_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_street AS cross_street, d.freeway AS freeway, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

INSERT INTO iris.lane_use_indication (id, description)
	VALUES (13, 'Low visibility');
