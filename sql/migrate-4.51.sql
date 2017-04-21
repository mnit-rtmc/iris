\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.51.0'
	WHERE name = 'database_version';

-- Add comm_idle_disconnect system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_idle_disconnect_dms_sec', '-1');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_idle_disconnect_modem_sec', '20');

-- Add blank camera URLs
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_blank_url', '');

-- Add latency to encoder_type and encoder_type_view
ALTER TABLE encoder_type ADD COLUMN latency INTEGER;
UPDATE encoder_type SET latency = 50;
ALTER TABLE encoder_type ALTER COLUMN latency SET NOT NULL;
DROP VIEW encoder_type_view;
CREATE VIEW encoder_type_view AS
	SELECT name, http_path, rtsp_path, latency FROM iris.encoder_type;
GRANT SELECT ON encoder_type_view TO PUBLIC;

-- Remove video monitor stuff (temporarily)
DROP VIEW video_monitor_view;
DROP VIEW iris.video_monitor;
DROP FUNCTION iris.video_monitor_insert;
DROP FUNCTION iris.video_monitor_update;
DROP FUNCTION iris.video_monitor_delete;

-- Add force_aspect to video_monitor
ALTER TABLE iris._video_monitor ADD COLUMN force_aspect BOOLEAN;
UPDATE iris._video_monitor SET force_aspect = false;
ALTER TABLE iris._video_monitor ALTER COLUMN force_aspect SET NOT NULL;

-- Create iris.video_monitor view
CREATE VIEW iris.video_monitor AS SELECT
	m.name, controller, pin, notes, mon_num, direct, restricted,
	force_aspect, camera
	FROM iris._video_monitor m JOIN iris._device_io d ON m.name = d.name;

-- Create video_monitor_insert TRIGGER
CREATE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
	$video_monitor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._video_monitor (name, notes, mon_num, direct,
	                                 restricted, force_aspect, camera)
	     VALUES (NEW.name, NEW.notes, NEW.mon_num, NEW.direct,
	             NEW.restricted, NEW.force_aspect, NEW.camera);
	RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_insert();

-- Create video_monitor_update TRIGGER
CREATE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
	$video_monitor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._video_monitor
	   SET notes = NEW.notes,
	       mon_num = NEW.mon_num,
	       direct = NEW.direct,
	       restricted = NEW.restricted,
	       force_aspect = NEW.force_aspect,
	       camera = NEW.camera
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_update();

-- Create video_monitor_delete TRIGGER
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

-- Create video_monitor_view
CREATE VIEW video_monitor_view AS
	SELECT m.name, m.notes, mon_num, direct, restricted, force_aspect,
	       m.controller, ctr.condition, ctr.comm_link, camera
	FROM iris.video_monitor m
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;
