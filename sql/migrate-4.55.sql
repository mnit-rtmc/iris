\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

UPDATE iris.system_attribute SET value = '4.55.0'
	WHERE name = 'database_version';

-- Remove camera stuff (temporarily)
DROP VIEW camera_view;
DROP VIEW iris.camera;
DROP FUNCTION iris.camera_insert();
DROP FUNCTION iris.camera_update();
DROP FUNCTION iris.camera_delete();

-- Add video_loss column to camera table
ALTER TABLE iris._camera ADD COLUMN video_loss BOOLEAN;
UPDATE iris._camera SET video_loss = false;
ALTER TABLE iris._camera ALTER COLUMN video_loss SET NOT NULL;

CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, cam_num, encoder_type, encoder,
		enc_mcast, encoder_channel, publish, video_loss
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, cam_num, encoder_type,
	            encoder, enc_mcast, encoder_channel, publish, video_loss)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num,
	             NEW.encoder_type, NEW.encoder, NEW.enc_mcast,
	             NEW.encoder_channel, NEW.publish, NEW.video_loss);
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
	       publish = NEW.publish,
	       video_loss = NEW.video_loss
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

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, cam_num, encoder_type, c.encoder, c.enc_mcast,
	       c.encoder_channel, c.publish, c.video_loss, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

COMMIT;
