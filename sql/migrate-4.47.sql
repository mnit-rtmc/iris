\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.47.0'
	WHERE name = 'database_version';

-- delete camera_auth_ system attributes
DELETE FROM iris.system_attribute WHERE name = 'camera_auth_password';
DELETE FROM iris.system_attribute WHERE name = 'camera_auth_username';

-- drop old views
DROP VIEW camera_view;
DROP VIEW encoder_type_view;
DROP VIEW iris.camera;

-- drop camera trigger functions
DROP FUNCTION iris.camera_insert();
DROP FUNCTION iris.camera_update();
DROP FUNCTION iris.camera_delete();

-- Save off camera encoder_type
ALTER TABLE iris._camera ADD COLUMN et VARCHAR(24);
UPDATE iris._camera SET et = 'Generic' WHERE encoder_type = 0;
UPDATE iris._camera SET et = 'Axis 243Q' WHERE encoder_type = 1;
UPDATE iris._camera SET et = 'Infinova 1492N' WHERE encoder_type = 2;

-- drop old camera encoder_type
ALTER TABLE iris._camera DROP COLUMN encoder_type;

-- drop old encoder type table
DROP TABLE iris.encoder_type;

-- Add encoder_type to sonar_type table
INSERT INTO iris.sonar_type (name) VALUES ('encoder_type');

-- Add privileges for encoder_type
INSERT INTO iris.privilege (name, capability, type_n, obj_n, attr_n, write)
	VALUES ('PRV_002A', 'camera_admin', 'encoder_type', '', '', true),
	       ('PRV_003A', 'camera_tab', 'encoder_type', '', '', false);

-- Create new encoder type table
CREATE TABLE iris.encoder_type (
	name VARCHAR(24) PRIMARY KEY,
	http_path VARCHAR(64) NOT NULL,
	rtsp_path VARCHAR(64) NOT NULL
);

INSERT INTO iris.encoder_type (name, http_path, rtsp_path) VALUES
	('Generic', '', ''),
	('Axis 243Q', '/axis-cgi/mjpg/video.cgi', '/mpeg4/media.amp'),
	('Axis Q1602', '', '/axis-media/media.amp'),
	('Axis Q7436', '', '/axis-media/media.amp'),
	('Infinova 1492N', '', '/1AMP');

-- Add new camera encoder type
ALTER TABLE iris._camera ADD COLUMN encoder_type VARCHAR(24);
UPDATE iris._camera SET encoder_type = et;
ALTER TABLE iris._camera ADD CONSTRAINT _camera_encoder_type_fkey
	FOREIGN KEY (encoder_type) REFERENCES iris.encoder_type;
ALTER TABLE iris._camera DROP COLUMN et;

-- change camera encoder to VARCHAR(64)
ALTER TABLE iris._camera ADD COLUMN enc VARCHAR(64);
UPDATE iris._camera SET enc = encoder::VARCHAR(64);
ALTER TABLE iris._camera DROP COLUMN encoder;
ALTER TABLE iris._camera ADD COLUMN encoder VARCHAR(64);
UPDATE iris._camera SET encoder = enc;
ALTER TABLE iris._camera DROP COLUMN enc;
ALTER TABLE iris._camera ALTER COLUMN encoder SET NOT NULL;

-- Create iris.camera view
CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, encoder_type, encoder,
		enc_mcast, encoder_channel, stream_type, publish
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
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

CREATE FUNCTION iris.camera_update() RETURNS TRIGGER AS
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

CREATE FUNCTION iris.camera_delete() RETURNS TRIGGER AS
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

-- Create new encoder type view
CREATE VIEW encoder_type_view AS
	SELECT name, http_path, rtsp_path FROM iris.encoder_type;
GRANT SELECT ON encoder_type_view TO PUBLIC;

-- Create new camera_view
CREATE VIEW camera_view AS
	SELECT c.name, c.notes, encoder_type, c.encoder, c.enc_mcast,
	       c.encoder_channel, st.description AS stream_type,
	       c.publish, c.geo_loc, l.roadway,
	       l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,l.lat,l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN iris.stream_type st ON c.stream_type = st.id
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;
