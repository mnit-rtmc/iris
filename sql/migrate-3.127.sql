\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.127.0'
	WHERE name = 'database_version';

DROP VIEW camera_view;
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.controller_device;
DROP VIEW iris.controller_camera;
DROP VIEW iris.camera;

CREATE TABLE iris.encoder_type (
	id integer PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

COPY iris.encoder_type (id, description) FROM stdin;
0	
1	Axis MJPEG
2	Axis MPEG4
3	Infinova MPEG4
\.

CREATE VIEW encoder_type_view AS
	SELECT id, description FROM iris.encoder_type;
GRANT SELECT ON encoder_type_view TO PUBLIC;

ALTER TABLE iris._camera DROP COLUMN nvr;
ALTER TABLE iris._camera ADD COLUMN encoder_type integer REFERENCES iris.encoder_type;
UPDATE iris._camera SET encoder_type = 0;
ALTER TABLE iris._camera ALTER COLUMN encoder_type SET NOT NULL;

CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, encoder, encoder_channel,
		encoder_type, publish
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE RULE camera_insert AS ON INSERT TO iris.camera DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera(name, geo_loc, notes, encoder,
		encoder_channel, encoder_type, publish)
	VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.encoder,
		NEW.encoder_channel, NEW.encoder_type, NEW.publish);
);

CREATE RULE camera_update AS ON UPDATE TO iris.camera DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._camera SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes,
		encoder = NEW.encoder,
		encoder_channel = NEW.encoder_channel,
		encoder_type = NEW.encoder_type,
		publish = NEW.publish
	WHERE name = OLD.name;
);

CREATE RULE camera_delete AS ON DELETE TO iris.camera DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE VIEW iris.controller_camera AS
	SELECT dio.name, dio.controller, dio.pin, c.geo_loc
	FROM iris._device_io dio
	JOIN iris.camera c ON dio.name = c.name;

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

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, c.encoder, c.encoder_channel,
	et.description AS encoder_type, c.publish, c.geo_loc, l.roadway,
	l.road_dir, l.cross_mod, l.cross_street,
	l.cross_dir, l.easting, l.northing,
	c.controller, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.camera c
	JOIN iris.encoder_type et ON c.encoder_type = et.id
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;
