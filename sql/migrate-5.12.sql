\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.11.0', '5.12.0');

-- Re-add camera blank URL
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_blank_url', '')
	ON CONFLICT DO NOTHING;

DROP VIEW camera_view;
DROP VIEW iris.camera;
DROP FUNCTION iris.camera_insert;
DROP FUNCTION iris.camera_update;

ALTER TABLE iris._camera ADD COLUMN streamable BOOLEAN;
UPDATE iris._camera SET streamable = false;
ALTER TABLE iris._camera ALTER COLUMN streamable SET NOT NULL;

CREATE VIEW iris.camera AS
	SELECT c.name, geo_loc, controller, pin, notes, cam_num, encoder_type,
	       enc_address, enc_port, enc_mcast, enc_channel, publish,
	       streamable, video_loss
	FROM iris._camera c
	JOIN iris._device_io d ON c.name = d.name;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, cam_num, encoder_type,
	            enc_address, enc_port, enc_mcast, enc_channel, publish,
	            streamable, video_loss)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num,
	             NEW.encoder_type, NEW.enc_address, NEW.enc_port,
	             NEW.enc_mcast, NEW.enc_channel, NEW.publish,
	             NEW.streamable, NEW.video_loss);
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
	       enc_address = NEW.enc_address,
	       enc_port = NEW.enc_port,
	       enc_mcast = NEW.enc_mcast,
	       enc_channel = NEW.enc_channel,
	       publish = NEW.publish,
	       streamable = NEW.streamable,
	       video_loss = NEW.video_loss
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

CREATE VIEW camera_view AS
	SELECT c.name, cam_num, encoder_type, et.make, et.model, et.config,
	       c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel, c.publish,
	       c.streamable, c.video_loss, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
	FROM iris.camera c
	LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

COMMIT;
