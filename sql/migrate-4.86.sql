\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.85.0', '4.86.0');

-- Remove dash "-" from dir abbreviations
UPDATE iris.direction SET dir = 'NS' WHERE id = 5;
UPDATE iris.direction SET dir = 'EW' WHERE id = 6;

-- Replace parking_area_notify trigger
CREATE OR REPLACE FUNCTION iris.parking_area_notify() RETURNS TRIGGER AS
	$parking_area_notify$
BEGIN
	IF (NEW.time_stamp_static IS DISTINCT FROM OLD.time_stamp_static) THEN
		NOTIFY tms, 'parking_area';
	END IF;
	IF (NEW.time_stamp IS DISTINCT FROM OLD.time_stamp) THEN
		NOTIFY tms, 'parking_area_dynamic';
		NOTIFY tms, 'parking_area_archive';
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$parking_area_notify$ LANGUAGE plpgsql;

-- Add sign_detail table
CREATE TABLE iris.sign_detail (
	name VARCHAR(12) PRIMARY KEY,
	dms_type INTEGER NOT NULL REFERENCES iris.dms_type,
	portable BOOLEAN NOT NULL,
	technology VARCHAR(12) NOT NULL,
	sign_access VARCHAR(12) NOT NULL,
	legend VARCHAR(12) NOT NULL,
	beacon_type VARCHAR(32) NOT NULL,
	monochrome_foreground INTEGER NOT NULL,
	monochrome_background INTEGER NOT NULL,
	hardware_make VARCHAR(32) NOT NULL,
	hardware_model VARCHAR(32) NOT NULL,
	software_make VARCHAR(32) NOT NULL,
	software_model VARCHAR(32) NOT NULL
);

CREATE FUNCTION iris.sign_detail_notify() RETURNS TRIGGER AS
	$sign_detail_notify$
BEGIN
	NOTIFY tms, 'sign_detail';
	RETURN NULL; -- AFTER trigger return is ignored
END;
$sign_detail_notify$ LANGUAGE plpgsql;

CREATE TRIGGER sign_detail_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.sign_detail
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.sign_detail_notify();

CREATE VIEW sign_detail_view AS
	SELECT name, dt.description AS dms_type, portable, technology,
	       sign_access, legend, beacon_type, monochrome_foreground,
	       monochrome_background, hardware_make, hardware_model,
	       software_make, software_model
	FROM iris.sign_detail
	JOIN iris.dms_type dt ON sign_detail.dms_type = dt.id;
GRANT SELECT ON sign_detail_view TO PUBLIC;

-- Add sign_detail to _dms table
ALTER TABLE iris._dms ADD COLUMN sign_detail VARCHAR(12)
	REFERENCES iris.sign_detail;

-- Drop old DMS views
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

-- Add sign_detail to dms table / view
CREATE OR REPLACE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
	$dms_notify$
BEGIN
	IF (NEW.sign_config IS DISTINCT FROM OLD.sign_config OR
	    NEW.sign_detail IS DISTINCT FROM OLD.sign_detail) THEN
		NOTIFY tms, 'dms';
	END IF;
	IF (NEW.msg_current IS DISTINCT FROM OLD.msg_current) THEN
		NOTIFY tms, 'dms_message';
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       beacon, preset, sign_config, sign_detail, override_font,
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
	                       msg_sched, msg_current, expire_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.gps,
	             NEW.static_graphic, NEW.beacon, NEW.sign_config,
	             NEW.sign_detail, NEW.override_font, NEW.msg_sched,
	             NEW.msg_current, NEW.expire_time);
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

CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition, multi,
	       beacon_enabled, prefix_page, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, expire_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Drop columns from sign_config
DROP VIEW sign_config_view;

ALTER TABLE iris.sign_config DROP COLUMN dms_type;
ALTER TABLE iris.sign_config DROP COLUMN portable;
ALTER TABLE iris.sign_config DROP COLUMN technology;
ALTER TABLE iris.sign_config DROP COLUMN sign_access;
ALTER TABLE iris.sign_config DROP COLUMN legend;
ALTER TABLE iris.sign_config DROP COLUMN beacon_type;
ALTER TABLE iris.sign_config DROP COLUMN monochrome_foreground;
ALTER TABLE iris.sign_config DROP COLUMN monochrome_background;

CREATE VIEW sign_config_view AS
	SELECT name, face_width, face_height, border_horiz, border_vert,
	       pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
	       char_height, cs.description AS color_scheme, default_font
	FROM iris.sign_config
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, d.beacon, p.camera, p.preset_num,
	       d.sign_config, d.sign_detail, default_font, override_font,
	       msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

-- Find duplicate sign_config records
CREATE TEMP TABLE sign_config_dups AS
SELECT T1.name AS old_name, T2.name AS new_name
FROM iris.sign_config T1
JOIN iris.sign_config T2
  ON T1.ctid < T2.ctid
 AND T1.face_width = T2.face_width
 AND T1.face_height = T2.face_height
 AND T1.border_horiz = T2.border_horiz
 AND T1.border_vert = T2.border_vert
 AND T1.pitch_horiz = T2.pitch_horiz
 AND T1.pitch_vert = T2.pitch_vert
 AND T1.pixel_width = T2.pixel_width
 AND T1.pixel_height = T2.pixel_height
 AND T1.char_width = T2.char_width
 AND T1.char_height = T2.char_height
 AND T1.color_scheme = T2.color_scheme;

-- Replace duplicate sign_config on DMS records
UPDATE iris._dms d
   SET sign_config = n.new_name
  FROM sign_config_dups n
 WHERE n.old_name = d.sign_config;

-- Replace duplicate sign_config on quick_message recods
UPDATE iris.quick_message q
   SET sign_config = n.new_name
  FROM sign_config_dups n
 WHERE n.old_name = q.sign_config;

-- Replace duplicate sign_config on sign_message recods
UPDATE iris.sign_message s
   SET sign_config = n.new_name
  FROM sign_config_dups n
 WHERE n.old_name = s.sign_config;

-- Delete duplicate sign_config records
DELETE FROM iris.sign_config s
      USING sign_config_dups d
      WHERE s.name = d.old_name;

-- Add sign_detail sonar_type
INSERT INTO iris.sonar_type (name) VALUES ('sign_detail');

-- Add privileges for sign_detail
INSERT INTO iris.privilege (name, capability, type_n, write)
	VALUES ('PRV_sd0', 'dms_admin', 'sign_detail', true),
	       ('PRV_sd1', 'dms_tab', 'sign_detail', false);

-- Drop view temporarily
DROP VIEW i_user_view;

-- Increase i_user dn max length to 128
ALTER TABLE iris.i_user ALTER COLUMN dn TYPE VARCHAR(128);

CREATE VIEW i_user_view AS
	SELECT name, full_name, dn, role, enabled
	FROM iris.i_user;
GRANT SELECT ON i_user_view TO PUBLIC;

COMMIT;
