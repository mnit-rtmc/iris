\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.37.0'
	WHERE name = 'database_version';

-- Add camera_auth_ system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_auth_password', '');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_auth_username', '');

-- Add stream_type look-up table
CREATE TABLE iris.stream_type (
	id integer PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);
COPY iris.stream_type (id, description) FROM stdin;
0	UNKNOWN
1	MJPEG
2	MPEG4
3	H264
4	H265
\.

-- Add enc_mcast column to camera table
ALTER TABLE iris._camera ADD COLUMN enc_mcast VARCHAR(64);
UPDATE iris._camera SET enc_mcast = '';
ALTER TABLE iris._camera ALTER COLUMN enc_mcast SET NOT NULL;

-- Add stream_type column to camera table
ALTER TABLE iris._camera ADD COLUMN stream_type INTEGER REFERENCES
	iris.stream_type;
UPDATE iris._camera SET stream_type = 0;
ALTER TABLE iris._camera ALTER COLUMN stream_type SET NOT NULL;

-- Set stream type from old encoder types
UPDATE iris._camera SET stream_type = 1 WHERE encoder_type = 1;
UPDATE iris._camera SET stream_type = 2 WHERE encoder_type >= 2
                                          AND encoder_type <= 5;

-- Add uri scheme for unusual encoder types
UPDATE iris.camera SET encoder = 'axrtsp://' || encoder WHERE encoder_type = 4;
UPDATE iris.camera SET encoder = 'axrtsphttp://' || encoder
	WHERE encoder_type = 5;
UPDATE iris.camera SET encoder = 'mms://' || encoder WHERE encoder_type = 6;

-- Add temp encoder type column
ALTER TABLE iris._camera ADD COLUMN enc INTEGER;
UPDATE iris._camera SET enc = 0;
UPDATE iris._camera SET enc = 1 WHERE encoder_type = 1
                                   OR encoder_type = 2
                                   OR encoder_type = 4
                                   OR encoder_type = 5
                                   OR encoder_type = 7;
UPDATE iris._camera SET enc = 2 WHERE encoder_type = 3;

-- Update encoder types
UPDATE iris.camera SET encoder_type = 0;
DELETE FROM iris.encoder_type WHERE id > 2;
UPDATE iris.encoder_type SET description = 'Generic' WHERE id = 0;
UPDATE iris.encoder_type SET description = 'Axis' WHERE id = 1;
UPDATE iris.encoder_type SET description = 'Infinova' WHERE id = 2;

-- Remove temp encoder type column
UPDATE iris._camera SET encoder_type = enc;
ALTER TABLE iris._camera DROP COLUMN enc;

-- Add stream_type to iris.camera view
DROP VIEW camera_view;
DROP VIEW iris.camera;

CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, encoder_type, encoder,
		enc_mcast, encoder_channel, stream_type, publish
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE OR REPLACE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, encoder_type, encoder,
	            enc_mcast, encoder_channel, stream_type, publish)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.encoder_type,
	             NEW.encoder, NEW.enc_mcast, NEW.encoder_channel,
	             NEW.stream_type, NEW.publish);
	RETURN NEW;
END;
$camera_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_insert();

CREATE OR REPLACE FUNCTION iris.camera_update() RETURNS TRIGGER AS
	$camera_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._camera
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       encoder_type = NEW.encoder_type,
	       encoder = NEW.encoder,
	       enc_mcast = NEW.enc_mcast,
	       encoder_channel = NEW.encoder_channel,
	       stream_type = NEW.stream_type,
	       publish = NEW.publish
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE OR REPLACE FUNCTION iris.camera_delete() RETURNS TRIGGER AS
	$camera_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$camera_delete$ LANGUAGE plpgsql;

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_delete();

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, et.description AS encoder_type, c.encoder,
	       c.enc_mcast, c.encoder_channel, st.description AS stream_type,
	       c.publish, c.geo_loc, l.roadway,
	       l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,l.lat,l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN iris.encoder_type et ON c.encoder_type = et.id
	LEFT JOIN iris.stream_type st ON c.stream_type = st.id
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;
