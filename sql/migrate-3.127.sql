\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.127.0'
	WHERE name = 'database_version';

DROP VIEW camera_view;
DROP VIEW iris.camera;

CREATE TABLE iris.encoder_type (
	id integer PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

COPY iris.encoder_type (id, description) FROM stdin;
0	
1	Axis MJPEG
2	Axis MPEG4
3	Infinova MJPEG
4	Infinova MPEG4
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
	INSERT INTO iris._camera VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.encoder, NEW.encoder_channel, NEW.encoder_type,NEW.publish);
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
