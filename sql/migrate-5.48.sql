\set ON_ERROR_STOP

BEGIN;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.47.0', '5.48.0');

-- DROP unused trigger
DROP TRIGGER controller_io_notify_trig ON iris.controller_io;

-- Replace _device_preset with device_preset
CREATE TABLE iris.device_preset (
    name VARCHAR(20) PRIMARY KEY,
    resource_n VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    preset VARCHAR(20) UNIQUE REFERENCES iris.camera_preset(name)
);

-- Copy records from _device_preset to device_preset
INSERT INTO iris.device_preset (name, resource_n, preset)
  SELECT name, 'beacon', preset
  FROM iris.beacon;

INSERT INTO iris.device_preset (name, resource_n, preset)
  SELECT name, 'dms', preset
  FROM iris.dms;

INSERT INTO iris.device_preset (name, resource_n, preset)
  SELECT name, 'ramp_meter', preset
  FROM iris.ramp_meter;

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.device_preset
    FOR EACH ROW EXECUTE FUNCTION iris.resource_notify();

-- DROP old _device_preset stuff
DROP VIEW camera_preset_view;
DROP VIEW beacon_view;
DROP VIEW iris.beacon;
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;

CREATE VIEW camera_preset_view AS
    SELECT cp.name, camera, preset_num, direction, p.name AS device
    FROM iris.camera_preset cp
    JOIN iris.device_preset p ON cp.name = p.preset;
GRANT SELECT ON camera_preset_view TO PUBLIC;

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin,
           ext_mode, preset, state
    FROM iris._beacon b
    JOIN iris.controller_io cio ON b.name = cio.name
    JOIN iris.device_preset p ON b.name = p.name;

CREATE OR REPLACE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
    $beacon_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
        VALUES (NEW.name, 'beacon', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
        VALUES (NEW.name, 'beacon', NEW.preset);
    INSERT INTO iris._beacon (name, geo_loc, notes, message, verify_pin,
                              ext_mode, state)
        VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message,
                NEW.verify_pin, NEW.ext_mode, NEW.state);
    RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_insert();

CREATE OR REPLACE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
    $beacon_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._beacon
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           message = NEW.message,
           verify_pin = NEW.verify_pin,
           ext_mode = NEW.ext_mode,
           state = NEW.state
     WHERE name = OLD.name;
    RETURN NEW;
END;
$beacon_update$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_update_trig
    INSTEAD OF UPDATE ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_update();

CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW beacon_view AS
    SELECT b.name, b.notes, b.message, p.camera, p.preset_num, b.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           b.controller, b.pin, b.verify_pin, b.ext_mode,
           ctr.comm_link, ctr.drop_id, ctr.condition, bs.description AS state
    FROM iris.beacon b
    JOIN iris.beacon_state bs ON b.state = bs.id
    LEFT JOIN iris.camera_preset p ON b.preset = p.name
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN controller_view ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

-- Make DMS views with new device_preset
CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           beacon, preset, sign_config, sign_detail,
           msg_user, msg_sched, msg_current, expire_time, status, stuck_pixels
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name;

CREATE OR REPLACE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'dms', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
         VALUES (NEW.name, 'dms', NEW.preset);
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
    FOR EACH ROW EXECUTE FUNCTION iris.dms_insert();

CREATE OR REPLACE FUNCTION iris.dms_update() RETURNS TRIGGER AS
    $dms_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
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
    FOR EACH ROW EXECUTE FUNCTION iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

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
    FROM iris._dms d
    LEFT JOIN iris.controller_io cio ON d.name = cio.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Make ramp meter views with new device_preset
CREATE VIEW iris.ramp_meter AS
    SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
           max_wait, algorithm, am_target, pm_target, beacon, preset, m_lock
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    JOIN iris.device_preset p ON m.name = p.name;

CREATE OR REPLACE FUNCTION iris.ramp_meter_insert() RETURNS TRIGGER AS
    $ramp_meter_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'ramp_meter', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
         VALUES (NEW.name, 'ramp_meter', NEW.preset);
    INSERT INTO iris._ramp_meter (
        name, geo_loc, notes, meter_type, storage, max_wait, algorithm,
        am_target, pm_target, beacon, m_lock
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type, NEW.storage,
        NEW.max_wait, NEW.algorithm, NEW.am_target, NEW.pm_target, NEW.beacon,
        NEW.m_lock
    );
    RETURN NEW;
END;
$ramp_meter_insert$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_insert();

CREATE OR REPLACE FUNCTION iris.ramp_meter_update() RETURNS TRIGGER AS
    $ramp_meter_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._ramp_meter
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           meter_type = NEW.meter_type,
           storage = NEW.storage,
           max_wait = NEW.max_wait,
           algorithm = NEW.algorithm,
           am_target = NEW.am_target,
           pm_target = NEW.pm_target,
           beacon = NEW.beacon,
           m_lock = NEW.m_lock
     WHERE name = OLD.name;
    RETURN NEW;
END;
$ramp_meter_update$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_update_trig
    INSTEAD OF UPDATE ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_update();

CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW ramp_meter_view AS
    SELECT m.name, geo_loc, controller, pin, notes,
           mt.description AS meter_type, storage, max_wait,
           alg.description AS algorithm, am_target, pm_target, beacon, camera,
           preset_num, ml.description AS meter_lock, l.roadway, l.road_dir,
           l.cross_mod, l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon,
           l.corridor, l.location, l.rd
    FROM iris.ramp_meter m
    LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
    LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
    LEFT JOIN iris.camera_preset p ON m.preset = p.name
    LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

-- Update controller io delete trigger
CREATE OR REPLACE FUNCTION iris.controller_io_delete() RETURNS TRIGGER AS
    $controller_io_delete$
BEGIN
    DELETE FROM iris.device_preset WHERE name = OLD.name;
    DELETE FROM iris.controller_io WHERE name = OLD.name;
    IF FOUND THEN
        RETURN OLD;
    ELSE
        RETURN NULL;
    END IF;
END;
$controller_io_delete$ LANGUAGE plpgsql;

DROP TABLE iris._device_preset;

COMMIT;
