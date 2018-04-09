\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.69.0', '4.70.0');

-- Add location to camera_view
DROP VIEW camera_view;
CREATE VIEW camera_view AS
	SELECT c.name, c.notes, cam_num, encoder_type, c.encoder, c.enc_mcast,
	       c.encoder_channel, c.publish, c.video_loss, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

-- Add group_n to video_monitor
DROP VIEW video_monitor_view;
DROP VIEW iris.video_monitor;
DROP FUNCTION iris.video_monitor_insert();
DROP FUNCTION iris.video_monitor_update();
DROP FUNCTION iris.video_monitor_delete();

ALTER TABLE iris._video_monitor ADD COLUMN group_n VARCHAR(16);

CREATE VIEW iris.video_monitor AS
	SELECT m.name, controller, group_n, pin, notes, mon_num, restricted,
	       monitor_style, camera
	FROM iris._video_monitor m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
	$video_monitor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._video_monitor (name, notes, group_n, mon_num,
	                                 restricted, monitor_style, camera)
	     VALUES (NEW.name, NEW.notes, NEW.group_n, NEW.mon_num,
	             NEW.restricted, NEW.monitor_style, NEW.camera);
	RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_insert();

CREATE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
	$video_monitor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._video_monitor
	   SET notes = NEW.notes,
	       group_n = NEW.group_n,
	       mon_num = NEW.mon_num,
	       restricted = NEW.restricted,
	       monitor_style = NEW.monitor_style,
	       camera = NEW.camera
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_update();

CREATE FUNCTION iris.video_monitor_delete() RETURNS TRIGGER AS
	$video_monitor_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$video_monitor_delete$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_delete();

CREATE VIEW video_monitor_view AS
	SELECT m.name, m.notes, group_n, mon_num, restricted, monitor_style,
	       m.controller, m.pin, ctr.condition, ctr.comm_link, camera
	FROM iris.video_monitor m
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

COMMIT;
