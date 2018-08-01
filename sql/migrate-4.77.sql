\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.76.0', '4.77.0');

CREATE TABLE iris.color_scheme (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

COPY iris.color_scheme (id, description) FROM stdin;
1	monochrome1Bit
2	monochrome8Bit
3	colorClassic
4	color24Bit
\.

ALTER TABLE iris.sign_config
	ADD COLUMN color_scheme INTEGER REFERENCES iris.color_scheme;
ALTER TABLE iris.sign_config ADD COLUMN monochrome_foreground INTEGER;
ALTER TABLE iris.sign_config ADD COLUMN monochrome_background INTEGER;

UPDATE iris.sign_config SET color_scheme = 1;
UPDATE iris.sign_config SET monochrome_foreground = 16764928; -- amber
UPDATE iris.sign_config SET monochrome_background = 0;

ALTER TABLE iris.sign_config ALTER COLUMN color_scheme SET NOT NULL;
ALTER TABLE iris.sign_config ALTER COLUMN monochrome_foreground SET NOT NULL;
ALTER TABLE iris.sign_config ALTER COLUMN monochrome_background SET NOT NULL;

DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();
DROP VIEW sign_config_view;
CREATE VIEW sign_config_view AS
	SELECT name, dt.description AS dms_type, portable, technology,
	       sign_access, legend, beacon_type, face_width, face_height,
	       border_horiz, border_vert, pitch_horiz, pitch_vert,
	       pixel_width, pixel_height, char_width, char_height,
	       cs.description AS color_scheme,
	       monochrome_foreground, monochrome_background, default_font
	FROM iris.sign_config
	JOIN iris.dms_type dt ON sign_config.dms_type = dt.id
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

ALTER TABLE iris.sign_message
	ADD COLUMN sign_config VARCHAR(12) REFERENCES iris.sign_config;

-- Blank scheduled messages on DMS with no sign config
UPDATE iris._dms SET msg_sched = NULL WHERE sign_config IS NULL;

-- Set sign_config for sign_message ref'd from msg_sched
WITH sq AS (
	SELECT sign_config, msg_sched
	FROM iris._dms
)
UPDATE iris.sign_message sm
   SET sign_config = sq.sign_config
  FROM sq
 WHERE sm.name = sq.msg_sched;

-- Set sign_config for sign_message red'd from msg_current
WITH sq AS (
	SELECT sign_config, msg_current
	FROM iris._dms
)
UPDATE iris.sign_message sm
SET sign_config = sq.sign_config
FROM sq
WHERE sm.name = sq.msg_current;

-- Delete sign_message rows with no sign_config (not referenced)
DELETE FROM iris.sign_message WHERE sign_config IS NULL;

ALTER TABLE iris.sign_message
	ALTER COLUMN sign_config SET NOT NULL;

CREATE VIEW sign_message_view AS
	SELECT name, sign_config, incident, multi, beacon_enabled, prefix_page,
	       msg_priority, iris.sign_msg_sources(source) AS sources, owner,
	       duration
	FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

-- Rename DMS default_font to override_font
ALTER TABLE iris._dms ADD COLUMN override_font VARCHAR(16) REFERENCES iris.font;
UPDATE iris._dms SET override_font = default_font;
ALTER TABLE iris._dms DROP COLUMN default_font;

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       beacon, preset, sign_config, override_font, msg_sched,
	       msg_current, deploy_time
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
	                       beacon, sign_config, override_font, msg_sched,
	                       msg_current, deploy_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.gps,
	             NEW.static_graphic, NEW.beacon, NEW.sign_config,
	             NEW.override_font, NEW.msg_sched, NEW.msg_current,
	             NEW.deploy_time);
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
	       override_font = NEW.override_font,
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

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, d.beacon, p.camera, p.preset_num, d.sign_config,
	       default_font, override_font, msg_sched, msg_current, deploy_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
	SELECT d.name, cc.description AS condition, multi, beacon_enabled,
	       prefix_page, msg_priority, iris.sign_msg_sources(source)
	       AS sources, duration, deploy_time, owner
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

COMMIT;
