\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.8.0', '5.9.0');

-- Change Junction to Jct in road_modifier table
UPDATE iris.road_modifier SET modifier = 'N Jct' WHERE id = 5;
UPDATE iris.road_modifier SET modifier = 'S Jct' WHERE id = 6;
UPDATE iris.road_modifier SET modifier = 'E Jct' WHERE id = 7;
UPDATE iris.road_modifier SET modifier = 'W Jct' WHERE id = 8;

CREATE OR REPLACE FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT)
	RETURNS TEXT AS $geo_location$
DECLARE
	roadway ALIAS FOR $1;
	road_dir ALIAS FOR $2;
	cross_mod ALIAS FOR $3;
	cross_street ALIAS FOR $4;
	cross_dir ALIAS FOR $5;
	landmark ALIAS FOR $6;
	corridor TEXT;
	xloc TEXT;
	lmrk TEXT;
BEGIN
	corridor = trim(roadway || concat(' ', road_dir));
	xloc = trim(concat(cross_mod, ' ') || cross_street
	    || concat(' ', cross_dir));
	lmrk = replace('(' || landmark || ')', '()', '');
	RETURN NULLIF(trim(concat(corridor, ' ' || xloc, ' ' || lmrk)), '');
END;
$geo_location$ LANGUAGE plpgsql;

-- Add hidden column to dms table / views
ALTER TABLE iris._dms ADD COLUMN hidden BOOLEAN;
UPDATE iris._dms SET hidden = false;
ALTER TABLE iris._dms ALTER COLUMN hidden SET NOT NULL;

DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       purpose, hidden, beacon, preset, sign_config, sign_detail,
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
	                       purpose, hidden, beacon, sign_config, sign_detail,
	                       override_font, override_foreground,
	                       override_background, msg_sched, msg_current,
	                       expire_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.gps,
	             NEW.static_graphic, NEW.purpose, NEW.hidden, NEW.beacon,
	             NEW.sign_config, NEW.sign_detail, NEW.override_font,
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
	       purpose = NEW.purpose,
	       hidden = NEW.hidden,
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

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, dp.description AS purpose, d.hidden, d.beacon,
	       p.camera, p.preset_num, d.sign_config, d.sign_detail,
	       default_font, override_font, override_foreground,
	       override_background, msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition, multi,
	       beacon_enabled, prefix_page, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, expire_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Remove hidden from sign_group
DROP VIEW dms_sign_group_view;
DROP VIEW sign_group_view;

ALTER TABLE iris.sign_group DROP COLUMN hidden;

CREATE VIEW sign_group_view AS
	SELECT name, local
	FROM iris.sign_group;
GRANT SELECT ON sign_group_view TO PUBLIC;

CREATE VIEW dms_sign_group_view AS
	SELECT d.name, dms, sign_group, local
	FROM iris.dms_sign_group d
	JOIN iris.sign_group sg ON d.sign_group = sg.name;
GRANT SELECT ON dms_sign_group_view TO PUBLIC;

COMMIT;
