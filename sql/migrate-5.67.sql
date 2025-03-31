\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.66.0', '5.67.0');

-- Replace meter `fault` with `status` and `m_lock` with `lock`
ALTER TABLE iris._ramp_meter ADD COLUMN lock JSONB;
ALTER TABLE iris._ramp_meter ADD COLUMN status JSONB;

UPDATE iris._ramp_meter m
    SET lock = json_object('reason': LOWER(ml.description))
    FROM iris.meter_lock ml
    WHERE m.m_lock = ml.id;

UPDATE iris._ramp_meter m
    SET status = json_object('fault': f.description)
    FROM iris.meter_fault f
    WHERE m.fault = f.id;

DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;
DROP FUNCTION iris.ramp_meter_insert();
DROP FUNCTION iris.ramp_meter_update();

CREATE OR REPLACE FUNCTION iris.ramp_meter_notify() RETURNS TRIGGER AS
    $ramp_meter_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.lock IS DISTINCT FROM OLD.lock) OR
       (NEW.status IS DISTINCT FROM OLD.status)
    THEN
        NOTIFY ramp_meter;
    ELSE
        PERFORM pg_notify('ramp_meter', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$ramp_meter_notify$ LANGUAGE plpgsql;

CREATE VIEW iris.ramp_meter AS
    SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
           max_wait, algorithm, am_target, pm_target, beacon, preset,
           lock, status
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
        am_target, pm_target, beacon, lock, status
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type, NEW.storage,
        NEW.max_wait, NEW.algorithm, NEW.am_target, NEW.pm_target, NEW.beacon,
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
           beacon = NEW.beacon,
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
           alg.description AS algorithm, am_target, pm_target, beacon,
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

ALTER TABLE iris._ramp_meter DROP COLUMN fault;
ALTER TABLE iris._ramp_meter DROP COLUMN m_lock;

DROP TABLE iris.meter_fault;

DROP VIEW meter_lock_event_view;

ALTER TABLE event.meter_lock_event ADD COLUMN lock JSONB;

UPDATE event.meter_lock_event ev
    SET lock = json_object('reason': ml.description, 'user_id': user_id)
    FROM iris.meter_lock ml
    WHERE ev.m_lock = ml.id;

ALTER TABLE event.meter_lock_event DROP COLUMN m_lock;
ALTER TABLE event.meter_lock_event DROP COLUMN user_id;

DROP TABLE iris.meter_lock;

CREATE VIEW meter_lock_event_view AS
    SELECT ev.id, event_date, ed.description, ramp_meter, lock
    FROM event.meter_lock_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON meter_lock_event_view TO PUBLIC;

-- Add Maintenance controller condition
INSERT INTO iris.condition (id, description) VALUES (5, 'Maintenance');

-- Add check contraints for ramp meter
ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_storage_check
    CHECK (storage >= 0);
ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_max_wait_check
    CHECK (max_wait > 0);
ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_am_target_check
    CHECK (am_target >= 0);
ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_pm_target_check
    CHECK (pm_target >= 0);

-- Change comm protocol 22 to ADEC TDC
UPDATE iris.comm_protocol
    SET description = 'ADEC TDC'
    WHERE id = 22;

COMMIT;
