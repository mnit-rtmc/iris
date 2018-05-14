\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.73.0', '4.74.0');

-- Remove dms_aws_enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_aws_enable';

-- Drop old views and functions
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

-- Drop aws columns from iris._dms table
ALTER TABLE iris._dms DROP COLUMN aws_allowed;
ALTER TABLE iris._dms DROP COLUMN aws_controlled;

-- Add static_graphic column to iris._dms table
ALTER TABLE iris._dms ADD COLUMN static_graphic VARCHAR(20)
	REFERENCES iris.graphic;

-- Create iris.dms view
CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, static_graphic, beacon,
	       preset, sign_config, default_font, msg_sched, msg_current,
	       deploy_time
	FROM iris._dms dms
	JOIN iris._device_io d ON dms.name = d.name
	JOIN iris._device_preset p ON dms.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
	$dms_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._device_preset (name, preset)
	     VALUES (NEW.name, NEW.preset);
	INSERT INTO iris._dms (name, geo_loc, notes, static_graphic, beacon,
	                       sign_config, default_font, msg_sched, msg_current,
	                       deploy_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.static_graphic,
	             NEW.beacon, NEW.sign_config, NEW.default_font,
	             NEW.msg_sched, NEW.msg_current, NEW.deploy_time);
	RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
	$dms_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._device_preset
	   SET preset = NEW.preset
	 WHERE name = OLD.name;
	UPDATE iris._dms
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       static_graphic = NEW.static_graphic,
	       beacon = NEW.beacon,
	       sign_config = NEW.sign_config,
	       default_font = NEW.default_font,
	       msg_sched = NEW.msg_sched,
	       msg_current = NEW.msg_current,
	       deploy_time = NEW.deploy_time
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE FUNCTION iris.dms_delete() RETURNS TRIGGER AS
	$dms_delete$
BEGIN
	DELETE FROM iris._device_preset WHERE name = OLD.name;
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$dms_delete$ LANGUAGE plpgsql;

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_delete();

-- Create dms_view
CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.static_graphic,
	       d.beacon, p.camera, p.preset_num, d.sign_config,
	       COALESCE(d.default_font, sc.default_font) AS default_font,
	       msg_sched, msg_current, deploy_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

-- Create dms_message_view
CREATE VIEW dms_message_view AS
	SELECT d.name, cc.description AS condition, multi, beacon_enabled,
	       prefix_page, msg_priority, iris.sign_msg_sources(source)
	       AS sources, duration, deploy_time, owner
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Drop old play_list stuff
DROP VIEW play_list_view;
DROP VIEW iris.play_list;
DROP FUNCTION iris.play_list_insert();
DROP FUNCTION iris.play_list_update();
DROP FUNCTION iris.play_list_delete();

-- Add description column to play list
ALTER TABLE iris._play_list ADD COLUMN description VARCHAR(32);

-- Create iris.play_list view
CREATE VIEW iris.play_list AS
	SELECT name, seq_num, description
	FROM iris._play_list;

CREATE FUNCTION iris.play_list_insert() RETURNS TRIGGER AS
	$play_list_insert$
BEGIN
	IF NEW.seq_num IS NOT NULL THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	INSERT INTO iris._play_list (name, seq_num, description)
	     VALUES (NEW.name, NEW.seq_num, NEW.description);
	RETURN NEW;
END;
$play_list_insert$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_insert_trig
    INSTEAD OF INSERT ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_insert();

CREATE FUNCTION iris.play_list_update() RETURNS TRIGGER AS
	$play_list_update$
BEGIN
	IF NEW.seq_num IS NOT NULL AND (OLD.seq_num IS NULL OR
	                                NEW.seq_num != OLD.seq_num)
	THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	UPDATE iris._play_list
	   SET seq_num = NEW.seq_num,
	       description = NEW.description
	 WHERE name = OLD.name;
	IF OLD.seq_num IS NOT NULL AND (NEW.seq_num IS NULL OR
	                                NEW.seq_num != OLD.seq_num)
	THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
	END IF;
	RETURN NEW;
END;
$play_list_update$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_update_trig
    INSTEAD OF UPDATE ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_update();

CREATE FUNCTION iris.play_list_delete() RETURNS TRIGGER AS
	$play_list_delete$
BEGIN
	DELETE FROM iris._play_list WHERE name = OLD.name;
	IF FOUND THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$play_list_delete$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_delete_trig
    INSTEAD OF DELETE ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_delete();

-- Create play_list_view
CREATE VIEW play_list_view AS
	SELECT play_list, ordinal, seq_num, camera
	FROM iris.play_list_camera
	JOIN iris.play_list ON play_list_camera.play_list = play_list.name;
GRANT SELECT ON play_list_view TO PUBLIC;

-- Drop old catalog stuff
DROP VIEW iris.catalog;
DROP FUNCTION iris.catalog_insert();
DROP FUNCTION iris.catalog_update();
DROP FUNCTION iris.catalog_delete();

-- Add description column to catalog
ALTER TABLE iris._catalog ADD COLUMN description VARCHAR(32);

-- Create new catalog view
CREATE VIEW iris.catalog AS
	SELECT name, seq_num, description
	FROM iris._catalog;

CREATE FUNCTION iris.catalog_insert() RETURNS TRIGGER AS
	$catalog_insert$
BEGIN
	INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	INSERT INTO iris._catalog (name, seq_num, description)
	     VALUES (NEW.name,NEW.seq_num, NEW.catalog);
	RETURN NEW;
END;
$catalog_insert$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_insert_trig
    INSTEAD OF INSERT ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_insert();

CREATE FUNCTION iris.catalog_update() RETURNS TRIGGER AS
	$catalog_update$
BEGIN
	IF NEW.seq_num != OLD.seq_num THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	UPDATE iris._catalog
	   SET seq_num = NEW.seq_num,
	       description = NEW.description
	 WHERE name = OLD.name;
	IF NEW.seq_num != OLD.seq_num THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
	END IF;
	RETURN NEW;
END;
$catalog_update$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_update_trig
    INSTEAD OF UPDATE ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_update();

CREATE FUNCTION iris.catalog_delete() RETURNS TRIGGER AS
	$catalog_delete$
BEGIN
	DELETE FROM iris._catalog WHERE name = OLD.name;
	IF FOUND THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$catalog_delete$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_delete_trig
    INSTEAD OF DELETE ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_delete();

COMMIT;
