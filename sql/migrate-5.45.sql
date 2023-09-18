\set ON_ERROR_STOP

BEGIN;

ALTER SCHEMA public OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.44.0', '5.45.0');

-- Change resource_notify trigger to use column name as payload
DROP TRIGGER resource_notify_trig ON iris.geo_loc;
DROP TRIGGER resource_notify_trig ON iris.controller_io;
DROP FUNCTION iris.resource_notify();

CREATE FUNCTION iris.resource_notify() RETURNS TRIGGER AS
    $resource_notify$
DECLARE
    arg TEXT;
BEGIN
    FOREACH arg IN ARRAY TG_ARGV LOOP
        IF (TG_OP = 'DELETE') THEN
            PERFORM pg_notify(OLD.resource_n, arg);
        ELSE
            PERFORM pg_notify(NEW.resource_n, arg);
        END IF;
    END LOOP;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$resource_notify$ LANGUAGE plpgsql;

CREATE TRIGGER resource_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.hashtag
    FOR EACH ROW EXECUTE PROCEDURE iris.resource_notify('hashtags');

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.geo_loc
    FOR EACH ROW EXECUTE PROCEDURE iris.resource_notify('geo_loc');

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.controller_io
    FOR EACH ROW EXECUTE PROCEDURE iris.resource_notify('controller_io');

-- Replace device_purpose table with hashtags
INSERT INTO iris.hashtag (resource_n, name, hashtag)
    SELECT 'dms', name, '#Wayfinding' FROM iris.dms WHERE purpose = 1;
INSERT INTO iris.hashtag (resource_n, name, hashtag)
    SELECT 'dms', name, '#Tolling' FROM iris.dms WHERE purpose = 2;
INSERT INTO iris.hashtag (resource_n, name, hashtag)
    SELECT 'dms', name, '#Parking' FROM iris.dms WHERE purpose = 3;
INSERT INTO iris.hashtag (resource_n, name, hashtag)
    SELECT 'dms', name, '#TravelTime' FROM iris.dms WHERE purpose = 4;
INSERT INTO iris.hashtag (resource_n, name, hashtag)
    SELECT 'dms', name, '#Safety' FROM iris.dms WHERE purpose = 5;
INSERT INTO iris.hashtag (resource_n, name, hashtag)
    SELECT 'dms', name, '#LaneUse' FROM iris.dms WHERE purpose = 6;
INSERT INTO iris.hashtag (resource_n, name, hashtag)
    SELECT 'dms', name, '#VSL' FROM iris.dms WHERE purpose = 7;

INSERT INTO iris.hashtag (resource_n, name, hashtag)
    SELECT 'dms', name, '#Hidden' FROM iris.dms WHERE hidden = true;

DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();

ALTER TABLE iris._dms DROP COLUMN purpose;
ALTER TABLE iris._dms DROP COLUMN hidden;
DROP TABLE iris.device_purpose;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           beacon, preset, sign_config, sign_detail,
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
        name, geo_loc, notes, gps, static_graphic, beacon,
        sign_config, sign_detail, msg_user, msg_sched, msg_current,
        expire_time, status, stuck_pixels
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.gps, NEW.static_graphic,
        NEW.beacon, NEW.sign_config, NEW.sign_detail,
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
           d.sign_config, d.sign_detail, d.static_graphic, d.beacon,
           p.camera, p.preset_num, default_font,
           msg_user, msg_sched, msg_current, expire_time,
           status, stuck_pixels,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris.dms d
    LEFT JOIN iris.camera_preset p ON d.preset = p.name
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, msg_owner, flash_beacon,
           msg_priority, duration, expire_time
    FROM iris.dms d
    LEFT JOIN iris.controller c ON d.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

COMMIT;
