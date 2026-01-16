\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add device to beacon
DROP VIEW beacon_view;
DROP VIEW iris.beacon;
DROP FUNCTION iris.beacon_insert();
DROP FUNCTION iris.beacon_update();

ALTER TABLE iris._beacon
    ADD COLUMN device VARCHAR(20) REFERENCES iris.controller_io;

CREATE OR REPLACE FUNCTION iris.beacon_notify() RETURNS TRIGGER AS
    $beacon_notify$
BEGIN
    IF (NEW.message IS DISTINCT FROM OLD.message) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.device IS DISTINCT FROM OLD.device) OR
       (NEW.state IS DISTINCT FROM OLD.state)
    THEN
        NOTIFY beacon;
    ELSE
        PERFORM pg_notify('beacon', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_notify$ LANGUAGE plpgsql;

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, device,
           verify_pin, ext_mode, preset, state
    FROM iris._beacon b
    JOIN iris.controller_io cio ON b.name = cio.name
    JOIN iris.device_preset p ON b.name = p.name;

CREATE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
    $beacon_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
        VALUES (NEW.name, 'beacon', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
        VALUES (NEW.name, 'beacon', NEW.preset);
    INSERT INTO iris._beacon (name, geo_loc, notes, message, device,
                              verify_pin, ext_mode, state)
        VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message, NEW.device,
                NEW.verify_pin, NEW.ext_mode, NEW.state);
    RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE FUNCTION iris.beacon_insert();

CREATE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
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
       SET notes = NEW.notes,
           message = NEW.message,
           device = NEW.device,
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
    SELECT b.name, b.notes, b.message, b.device, cp.camera, cp.preset_num,
           b.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, b.verify_pin, b.ext_mode,
           c.comm_link, c.drop_id, cnd.description AS condition,
           bs.description AS state
    FROM iris._beacon b
    JOIN iris.beacon_state bs ON b.state = bs.id
    JOIN iris.controller_io cio ON b.name = cio.name
    LEFT JOIN iris.device_preset p ON b.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON beacon_view TO PUBLIC;

-- Set DMS beacon devices
WITH d AS (
    SELECT name, beacon
      FROM iris.dms
     WHERE beacon IS NOT NULL
)
UPDATE iris.beacon b
   SET device = d.name
  FROM d
 WHERE d.beacon = b.name;

-- DROP beacon from DMS table
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
ALTER TABLE iris._dms DROP COLUMN beacon;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, static_graphic,
           preset, sign_config, sign_detail,
           msg_sched, msg_current, lock, status, pixel_failures
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'dms', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
         VALUES (NEW.name, 'dms', NEW.preset);
    INSERT INTO iris._dms (
        name, geo_loc, notes, static_graphic, sign_config, sign_detail,
        msg_sched, msg_current, lock, status, pixel_failures
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.static_graphic,
        NEW.sign_config, NEW.sign_detail, NEW.msg_sched,
        NEW.msg_current, NEW.lock, NEW.status, NEW.pixel_failures
    );
    RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_insert();

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
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
       SET notes = NEW.notes,
           static_graphic = NEW.static_graphic,
           sign_config = NEW.sign_config,
           sign_detail = NEW.sign_detail,
           msg_sched = NEW.msg_sched,
           msg_current = NEW.msg_current,
           lock = NEW.lock,
           status = NEW.status,
           pixel_failures = NEW.pixel_failures
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
    SELECT d.name, d.geo_loc, cio.controller, cio.pin, d.notes,
           d.sign_config, d.sign_detail, d.static_graphic,
           cp.camera, cp.preset_num, default_font,
           msg_sched, msg_current, lock, status, pixel_failures,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

-- Set ramp meter beacon devices
WITH m AS (
    SELECT name, beacon
      FROM iris.ramp_meter
     WHERE beacon IS NOT NULL
)
UPDATE iris.beacon b
   SET device = m.name
  FROM m
 WHERE m.beacon = b.name;

-- DROP beacon from ramp_meter table
DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;
DROP FUNCTION iris.ramp_meter_insert();
DROP FUNCTION iris.ramp_meter_update();
ALTER TABLE iris._ramp_meter DROP COLUMN beacon;

CREATE VIEW iris.ramp_meter AS
    SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
           max_wait, algorithm, am_target, pm_target, preset, lock, status
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    JOIN iris.device_preset p ON m.name = p.name;

CREATE FUNCTION iris.ramp_meter_insert() RETURNS TRIGGER AS
    $ramp_meter_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'ramp_meter', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
         VALUES (NEW.name, 'ramp_meter', NEW.preset);
    INSERT INTO iris._ramp_meter (
        name, geo_loc, notes, meter_type, storage, max_wait, algorithm,
        am_target, pm_target, lock, status
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type, NEW.storage,
        NEW.max_wait, NEW.algorithm, NEW.am_target, NEW.pm_target,
        NEW.lock, NEW.status
    );
    RETURN NEW;
END;
$ramp_meter_insert$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.ramp_meter_insert();

CREATE FUNCTION iris.ramp_meter_update() RETURNS TRIGGER AS
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
       SET notes = NEW.notes,
           meter_type = NEW.meter_type,
           storage = NEW.storage,
           max_wait = NEW.max_wait,
           algorithm = NEW.algorithm,
           am_target = NEW.am_target,
           pm_target = NEW.pm_target,
           lock = NEW.lock,
           status = NEW.status
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
    SELECT m.name, geo_loc, cio.controller, cio.pin, notes,
           mt.description AS meter_type, storage, max_wait,
           alg.description AS algorithm, am_target, pm_target,
           camera, preset_num, lock, status,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location, l.rd
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN iris.device_preset p ON m.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
    LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

COMMIT;
