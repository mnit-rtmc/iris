\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.52.0'
	WHERE name = 'database_version';

-- Add encoding to replace stream_type
CREATE TABLE iris.encoding (
	id integer PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

COPY iris.encoding (id, description) FROM stdin;
0	UNKNOWN
1	MJPEG
2	MPEG2
3	MPEG4
4	H264
5	H265
\.

-- Drop old views
DROP VIEW encoder_type_view;

-- Add columns to encoder_type
ALTER TABLE iris.encoder_type ADD COLUMN encoding INTEGER;
UPDATE iris.encoder_type SET encoding = 0;
ALTER TABLE iris.encoder_type ADD CONSTRAINT encoder_type_encoding_fkey
	FOREIGN KEY (encoding) REFERENCES iris.encoding;
ALTER TABLE iris.encoder_type ALTER COLUMN encoding SET NOT NULL;

ALTER TABLE iris.encoder_type ADD COLUMN uri_scheme VARCHAR(8);
UPDATE iris.encoder_type SET uri_scheme = '';
ALTER TABLE iris.encoder_type ALTER COLUMN uri_scheme SET NOT NULL;

ALTER TABLE iris.encoder_type ADD COLUMN uri_path VARCHAR(64);
UPDATE iris.encoder_type SET uri_path = rtsp_path;
ALTER TABLE iris.encoder_type ALTER COLUMN uri_path SET NOT NULL;

-- Drop columns from encoder_type
ALTER TABLE iris.encoder_type DROP COLUMN http_path;
ALTER TABLE iris.encoder_type DROP COLUMN rtsp_path;

-- Add encoder_type_view
CREATE VIEW encoder_type_view AS
	SELECT name, enc.description AS encoding, uri_scheme, uri_path, latency
	FROM iris.encoder_type et
	LEFT JOIN iris.encoding enc ON et.encoding = enc.id;
GRANT SELECT ON encoder_type_view TO PUBLIC;

-- Remove camera stuff (temporarily)
DROP VIEW camera_view;
DROP VIEW iris.camera;
DROP FUNCTION iris.camera_insert();
DROP FUNCTION iris.camera_update();
DROP FUNCTION iris.camera_delete();

-- Drop column from iris._camera
ALTER TABLE iris._camera DROP COLUMN stream_type;

-- Create iris.camera view
CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, cam_num, encoder_type, encoder,
		enc_mcast, encoder_channel, publish
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, cam_num, encoder_type,
	            encoder, enc_mcast, encoder_channel, publish)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num,
	             NEW.encoder_type, NEW.encoder, NEW.enc_mcast,
	             NEW.encoder_channel, NEW.publish);
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
	       cam_num = NEW.cam_num,
	       encoder_type = NEW.encoder_type,
	       encoder = NEW.encoder,
	       enc_mcast = NEW.enc_mcast,
	       encoder_channel = NEW.encoder_channel,
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

-- Create camera_view
CREATE VIEW camera_view AS
	SELECT c.name, c.notes, cam_num, encoder_type, c.encoder, c.enc_mcast,
	       c.encoder_channel, c.publish, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

-- Drop stream_type table
DROP TABLE iris.stream_type;
