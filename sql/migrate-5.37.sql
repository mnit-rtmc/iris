\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.36.0', '5.37.0');

ALTER TABLE iris.graphic DROP CONSTRAINT graphic_height_ck;
ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_height_ck
    CHECK (height >= 1 AND height <= 144);

ALTER TABLE iris.graphic DROP CONSTRAINT graphic_width_ck;
ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_width_ck
    CHECK (width >= 1 AND width <= 240);

-- Remove exclude_font from sign_config
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW sign_config_view;

ALTER TABLE iris.sign_config DROP COLUMN exclude_font;

CREATE VIEW sign_config_view AS
	SELECT name, face_width, face_height, border_horiz, border_vert,
	       pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
	       char_height, monochrome_foreground, monochrome_background,
	       cs.description AS color_scheme, default_font,
	       module_width, module_height
	FROM iris.sign_config
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

-- Remove override_font, override_foreground and override_background from dms
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();

ALTER TABLE iris._dms DROP COLUMN override_font;
ALTER TABLE iris._dms DROP COLUMN override_foreground;
ALTER TABLE iris._dms DROP COLUMN override_background;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           purpose, hidden, beacon, preset, sign_config, sign_detail,
           msg_user, msg_sched, msg_current, expire_time, status, stuck_pixels
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris._device_preset p ON d.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'dms', NEW.controller, NEW.pin);
    INSERT INTO iris._device_preset (name, preset)
         VALUES (NEW.name, NEW.preset);
    INSERT INTO iris._dms (
        name, geo_loc, notes, gps, static_graphic, purpose, hidden, beacon,
        sign_config, sign_detail, msg_user, msg_sched, msg_current,
        expire_time, status, stuck_pixels
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.gps, NEW.static_graphic,
        NEW.purpose, NEW.hidden, NEW.beacon, NEW.sign_config, NEW.sign_detail,
        NEW.msg_user, NEW.msg_sched, NEW.msg_current, NEW.expire_time,
        NEW.status, NEW.stuck_pixels
    );
    RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
    $dms_update$
BEGIN
    UPDATE iris.controller_io
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
           msg_user = NEW.msg_user,
           msg_sched = NEW.msg_sched,
           msg_current = NEW.msg_current,
           expire_time = NEW.expire_time,
           status = NEW.status,
           stuck_pixels = NEW.stuck_pixels
     WHERE name = OLD.name;
    RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW dms_view AS
    SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
           d.static_graphic, dp.description AS purpose, d.hidden, d.beacon,
           p.camera, p.preset_num, d.sign_config, d.sign_detail,
           default_font, msg_user, msg_sched, msg_current, expire_time,
           status, stuck_pixels,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris.dms d
    LEFT JOIN iris.camera_preset p ON d.preset = p.name
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, beacon_enabled,
           mc.description AS msg_combining, msg_priority,
           iris.sign_msg_sources(source) AS sources, duration, expire_time
    FROM iris.dms d
    LEFT JOIN iris.controller c ON d.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name
    LEFT JOIN iris.msg_combining mc ON sm.msg_combining = mc.id;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Remove DMS font selection system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_font_selection_enable';

-- Remove DMS page-on selection system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_page_on_selection_enable';

-- Remove DMS default line/page justification system attributes
DELETE FROM iris.system_attribute WHERE name = 'dms_default_justification_line';
DELETE FROM iris.system_attribute WHERE name = 'dms_default_justification_page';

-- Remove DMS default page time system attributes
DELETE FROM iris.system_attribute WHERE name = 'dms_page_on_default_secs';
DELETE FROM iris.system_attribute WHERE name = 'dms_page_off_default_secs';

COMMIT;
