\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.86.0', '4.87.0');

-- Temp drop DMS stuff
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

-- Add DMS override colors
ALTER TABLE iris._dms ADD COLUMN override_foreground INTEGER;
ALTER TABLE iris._dms ADD COLUMN override_background INTEGER;

-- Find duplicate sign_detail records
CREATE TEMP TABLE sign_detail_dups AS
SELECT T1.name AS old_name, T2.name AS new_name
FROM iris.sign_detail T1
JOIN iris.sign_detail T2
  ON T1.ctid < T2.ctid
 AND T1.dms_type = T2.dms_type
 AND T1.portable = T2.portable
 AND T1.technology = T2.technology
 AND T1.sign_access = T2.sign_access
 AND T1.legend = T2.legend
 AND T1.beacon_type = T2.beacon_type
 AND T1.hardware_make = T2.hardware_make
 AND T1.hardware_model = T2.hardware_model
 AND T1.software_make = T2.software_make
 AND T1.software_model = T2.software_model;

-- Remove double duplicate records
DELETE FROM sign_detail_dups T1
      USING sign_detail_dups T2
      WHERE T1.new_name = T2.old_name;

-- Replace duplicate sign_detail on DMS records
UPDATE iris._dms d
   SET sign_detail = n.new_name
  FROM sign_detail_dups n
 WHERE d.sign_detail = n.old_name;

-- Delete duplicate sign_config records
DELETE FROM iris.sign_detail sd
      USING sign_detail_dups d
      WHERE sd.name = d.old_name;

-- Update sign_config_view
DROP VIEW sign_config_view;
CREATE VIEW sign_config_view AS
	SELECT name, face_width, face_height, border_horiz, border_vert,
	       pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
	       char_height, monochrome_foreground, monochrome_background,
	       cs.description AS color_scheme, default_font
	FROM iris.sign_config
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

-- Add supported tags to sign_detail
ALTER TABLE iris.sign_detail ADD COLUMN supported_tags INTEGER;
UPDATE iris.sign_detail SET supported_tags = 0;
ALTER TABLE iris.sign_detail ALTER COLUMN supported_tags SET NOT NULL;

-- Add max_pages to sign_detail
ALTER TABLE iris.sign_detail ADD COLUMN max_pages INTEGER;
UPDATE iris.sign_detail SET max_pages = 0;
ALTER TABLE iris.sign_detail ALTER COLUMN max_pages SET NOT NULL;

-- Add max_multi_len to sign_detail
ALTER TABLE iris.sign_detail ADD COLUMN max_multi_len INTEGER;
UPDATE iris.sign_detail SET max_multi_len = 0;
ALTER TABLE iris.sign_detail ALTER COLUMN max_multi_len SET NOT NULL;

-- Update sign_detail_view
DROP VIEW sign_detail_view;
CREATE VIEW sign_detail_view AS
	SELECT name, dt.description AS dms_type, portable, technology,
	       sign_access, legend, beacon_type, hardware_make, hardware_model,
	       software_make, software_model, supported_tags, max_pages,
	       max_multi_len
	FROM iris.sign_detail
	JOIN iris.dms_type dt ON sign_detail.dms_type = dt.id;
GRANT SELECT ON sign_detail_view TO PUBLIC;

-- Add iris.dms view
CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       beacon, preset, sign_config, sign_detail,
	       override_font, override_foreground, override_background,
	       msg_sched, msg_current, expire_time
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
	INSERT INTO iris._dms (name, geo_loc, notes, gps, static_graphic,
	                       beacon, sign_config, sign_detail, override_font,
	                       override_foreground, override_background,
	                       msg_sched, msg_current, expire_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.gps,
	             NEW.static_graphic, NEW.beacon, NEW.sign_config,
	             NEW.sign_detail, NEW.override_font,
	             NEW.override_foreground, NEW.override_background,
	             NEW.msg_sched, NEW.msg_current, NEW.expire_time);
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
	       gps = NEW.gps,
	       static_graphic = NEW.static_graphic,
	       beacon = NEW.beacon,
	       sign_config = NEW.sign_config,
	       sign_detail = NEW.sign_detail,
	       override_font = NEW.override_font,
	       override_foreground = NEW.override_foreground,
	       override_background = NEW.override_background,
	       msg_sched = NEW.msg_sched,
	       msg_current = NEW.msg_current,
	       expire_time = NEW.expire_time
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

-- Update dms_view
CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, d.beacon, p.camera, p.preset_num,
	       d.sign_config, d.sign_detail, default_font, override_font,
	       override_foreground, override_background,
	       msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

-- Add dms_message_view
CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition, multi,
	       beacon_enabled, prefix_page, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, expire_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Add 'unknown' color scheme
INSERT INTO iris.color_scheme (id, description) VALUES (0, 'unknown');

COMMIT;
