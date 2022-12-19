\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.34.0', '5.35.0');

INSERT INTO iris.parking_area_amenities (bit, amenity)
    VALUES (15, 'Family restroom');

DROP VIEW beacon_event_view;
DROP VIEW beacon_view;

ALTER TABLE iris.beacon_state ALTER COLUMN description TYPE VARCHAR(18);
INSERT INTO iris.beacon_state (id, description)
    VALUES (7, 'Flashing: External');

CREATE VIEW beacon_event_view AS
    SELECT event_id, event_date, beacon, bs.description AS state
    FROM event.beacon_event be
    JOIN iris.beacon_state bs ON be.state = bs.id;
GRANT SELECT ON beacon_event_view TO PUBLIC;

-- Add ext_mode to beacon
DROP VIEW iris.beacon;

ALTER TABLE iris._beacon ADD COLUMN ext_mode BOOLEAN;
UPDATE iris._beacon SET ext_mode = FALSE;
ALTER TABLE iris._beacon ALTER COLUMN ext_mode SET NOT NULL;

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin,
           ext_mode, preset, state
    FROM iris._beacon b
    JOIN iris.controller_io cio ON b.name = cio.name
    JOIN iris._device_preset p ON b.name = p.name;

CREATE OR REPLACE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
    $beacon_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
        VALUES (NEW.name, 'beacon', NEW.controller, NEW.pin);
    INSERT INTO iris._device_preset (name, preset)
        VALUES (NEW.name, NEW.preset);
    INSERT INTO iris._beacon (name, geo_loc, notes, message, verify_pin,
                              ext_mode, state)
        VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message,
                NEW.verify_pin, NEW.ext_mode, NEW.state);
    RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_insert();

CREATE OR REPLACE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
    $beacon_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._device_preset
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
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_update();

CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

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

-- Change ramp meter notes to VARCHAR(128)
DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;

ALTER TABLE iris._ramp_meter ALTER COLUMN notes TYPE VARCHAR(128);

CREATE VIEW iris.ramp_meter AS
    SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
           max_wait, algorithm, am_target, pm_target, beacon, preset, m_lock
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    JOIN iris._device_preset p ON m.name = p.name;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_insert();

CREATE TRIGGER ramp_meter_update_trig
    INSTEAD OF UPDATE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_update();

CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

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

COMMIT;
