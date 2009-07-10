\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.93.0' WHERE name = 'database_version';

DROP TABLE iris.sign_message;

CREATE TABLE iris.sign_message (
	name VARCHAR(20) PRIMARY KEY,
	multi VARCHAR(256) NOT NULL,
	bitmaps text NOT NULL,
	a_priority INTEGER NOT NULL,
	r_priority INTEGER NOT NULL,
	duration INTEGER
);

INSERT INTO iris.quick_message (name, multi)
	(SELECT 'TT_' || name, travel FROM iris.dms WHERE travel != '');

CREATE TEMP TABLE temp_ap (
	name VARCHAR(20) PRIMARY KEY,
	start_min INTEGER,
	stop_min INTEGER,
	active BOOLEAN
);

CREATE FUNCTION am_pm(INTEGER) RETURNS VARCHAR(2) AS
'	DECLARE m ALIAS FOR $1;
	BEGIN
		IF m < 720 THEN
			RETURN ''AM'';
		END IF;
		RETURN ''PM'';
	END;'
LANGUAGE PLPGSQL;

INSERT INTO temp_ap (name, start_min, stop_min, active)
	(SELECT DISTINCT ON (start_min, stop_min, active)
	'TRAVEL_TIME_' || am_pm(start_min), start_min, stop_min,
	active FROM iris.timing_plan WHERE plan_type = 0 ORDER BY start_min);

DROP FUNCTION am_pm(INTEGER);

INSERT INTO iris.action_plan (name, description, active, deployed)
	(SELECT name, '', active, 'f' FROM temp_ap);

CREATE TEMP SEQUENCE temp_tt_seq;

INSERT INTO iris.time_action (name, action_plan, minute, deploy)
	(SELECT name || '_' || nextval('temp_tt_seq'), name, start_min, 't'
	FROM temp_ap);
INSERT INTO iris.time_action (name, action_plan, minute, deploy)
	(SELECT name || '_' || nextval('temp_tt_seq'), name, stop_min, 'f'
	FROM temp_ap);

CREATE TEMP SEQUENCE temp_da_seq;

INSERT INTO iris.dms_action (name, action_plan, sign_group, on_deploy,
	quick_message, priority) (SELECT ap.name || '_'||nextval('temp_da_seq'),
	ap.name, tp.device, 't', 'TT_' || tp.device, 3
	FROM temp_ap ap, iris.timing_plan tp, iris._dms d
	WHERE ap.start_min = tp.start_min
	AND ap.stop_min = tp.stop_min
	AND ap.active = tp.active
	AND tp.device = d.name);

DELETE FROM iris.timing_plan WHERE plan_type = 0;

DROP VIEW dms_view;
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.controller_device;
DROP VIEW iris.controller_dms;
DROP VIEW iris.controller_lcs;
DROP VIEW iris.dms;

ALTER TABLE iris._dms DROP COLUMN travel;

CREATE VIEW iris.dms AS SELECT
	d.name, geo_loc, controller, pin, notes, camera, aws_allowed,
	aws_controlled
	FROM iris._dms dms JOIN iris._device_io d ON dms.name = d.name;

CREATE RULE dms_insert AS ON INSERT TO iris.dms DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._dms VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.camera, NEW.aws_allowed, NEW.aws_controlled);
);

CREATE RULE dms_update AS ON UPDATE TO iris.dms DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._dms SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes,
		camera = NEW.camera,
		aws_allowed = NEW.aws_allowed,
		aws_controlled = NEW.aws_controlled
	WHERE name = OLD.name;
);

CREATE RULE dms_delete AS ON DELETE TO iris.dms DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE VIEW iris.controller_dms AS
	SELECT dio.name, dio.controller, dio.pin, d.geo_loc
	FROM iris._device_io dio
	JOIN iris.dms d ON dio.name = d.name;

CREATE VIEW iris.controller_lcs AS
	SELECT dio.name, dio.controller, dio.pin, d.geo_loc
	FROM iris._device_io dio
	JOIN iris.dms d ON dio.name = d.name;

CREATE VIEW iris.controller_device AS
	SELECT * FROM iris.controller_dms UNION ALL
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
	FROM controller c
	LEFT JOIN cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.camera,
	d.aws_allowed, d.aws_controlled,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM iris.dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;
