\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.35.0', '5.36.0');

DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert;
DROP FUNCTION iris.dms_update;

ALTER TABLE iris._dms ALTER COLUMN notes TYPE VARCHAR(128);
ALTER TABLE iris._dms ADD COLUMN status JSONB;
ALTER TABLE iris._dms ADD COLUMN stuck_pixels JSONB;

CREATE OR REPLACE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
    $dms_notify$
BEGIN
    IF (NEW.msg_user IS DISTINCT FROM OLD.msg_user) THEN
        NOTIFY dms, 'msg_user';
    ELSIF (NEW.msg_sched IS DISTINCT FROM OLD.msg_sched) THEN
        NOTIFY dms, 'msg_sched';
    ELSIF (NEW.msg_current IS DISTINCT FROM OLD.msg_current) THEN
        NOTIFY dms, 'msg_current';
    ELSIF (NEW.expire_time IS DISTINCT FROM OLD.expire_time) THEN
        NOTIFY dms, 'expire_time';
    ELSIF (NEW.status IS DISTINCT FROM OLD.status) THEN
        NOTIFY dms, 'status';
    ELSIF (NEW.stuck_pixels IS DISTINCT FROM OLD.stuck_pixels) THEN
        NOTIFY DMS, 'stuck_pixels';
    ELSE
        NOTIFY dms;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           purpose, hidden, beacon, preset, sign_config, sign_detail,
           override_font, override_foreground, override_background, msg_user,
           msg_sched, msg_current, expire_time, status, stuck_pixels
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
        sign_config, sign_detail, override_font, override_foreground,
        override_background, msg_user, msg_sched, msg_current, expire_time,
        status, stuck_pixels
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.gps, NEW.static_graphic,
        NEW.purpose, NEW.hidden, NEW.beacon, NEW.sign_config, NEW.sign_detail,
        NEW.override_font, NEW.override_foreground, NEW.override_background,
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
           override_font = NEW.override_font,
           override_foreground = NEW.override_foreground,
           override_background = NEW.override_background,
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
           default_font, override_font, override_foreground,
           override_background, msg_user, msg_sched, msg_current,
           expire_time, status, stuck_pixels,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris.dms d
    LEFT JOIN iris.camera_preset p ON d.preset = p.name
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
    LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
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

COMMIT;
