\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.123.0'
	WHERE name = 'database_version';

ALTER TABLE iris._dms ADD COLUMN default_font VARCHAR(16) REFERENCES iris.font;

DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.controller_device;
DROP VIEW iris.controller_lcs;
DROP VIEW iris.controller_dms;
DROP VIEW dms_view;
DROP VIEW iris.dms;

CREATE VIEW iris.dms AS SELECT
	d.name, geo_loc, controller, pin, notes, camera, aws_allowed,
	aws_controlled, default_font
	FROM iris._dms dms JOIN iris._device_io d ON dms.name = d.name;

CREATE RULE dms_insert AS ON INSERT TO iris.dms DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._dms VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.camera, NEW.aws_allowed, NEW.aws_controlled,
		NEW.default_font);
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
		aws_controlled = NEW.aws_controlled,
		default_font = NEW.default_font
	WHERE name = OLD.name;
);

CREATE RULE dms_delete AS ON DELETE TO iris.dms DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.camera,
	d.aws_allowed, d.aws_controlled, d.default_font,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing
	FROM iris.dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;

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
	SELECT * FROM iris.controller_lane_marking UNION ALL
	SELECT * FROM iris.controller_weather_sensor UNION ALL
	SELECT * FROM iris.controller_lcs UNION ALL
	SELECT * FROM iris.controller_meter UNION ALL
	SELECT * FROM iris.controller_warning_sign UNION ALL
	SELECT * FROM iris.controller_camera;

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
