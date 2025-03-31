\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.64.0', '5.65.0');

-- Reset all controller status to blank
UPDATE iris.controller SET status = NULL;

-- Add ramp meter fault LUT
CREATE TABLE iris.meter_fault (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.meter_fault (id, description)
VALUES
    (0, 'police panel'),
    (1, 'manual mode'),
    (2, 'no entrance node'),
    (3, 'no green detector');

-- Add fault to ramp meter
DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;

ALTER TABLE iris._ramp_meter ADD COLUMN fault INTEGER
    REFERENCES iris.meter_fault;

CREATE OR REPLACE FUNCTION iris.ramp_meter_notify() RETURNS TRIGGER AS
    $ramp_meter_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.m_lock IS DISTINCT FROM OLD.m_lock) OR
       (NEW.fault IS DISTINCT FROM OLD.fault)
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
           m_lock, fault
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
        am_target, pm_target, beacon, m_lock, fault
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type, NEW.storage,
        NEW.max_wait, NEW.algorithm, NEW.am_target, NEW.pm_target, NEW.beacon,
        NEW.m_lock, NEW.fault
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
       SET notes = NEW.notes,
           meter_type = NEW.meter_type,
           storage = NEW.storage,
           max_wait = NEW.max_wait,
           algorithm = NEW.algorithm,
           am_target = NEW.am_target,
           pm_target = NEW.pm_target,
           beacon = NEW.beacon,
           m_lock = NEW.m_lock,
           fault = NEW.fault
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
           alg.description AS algorithm, am_target, pm_target, beacon, camera,
           preset_num, ml.description AS meter_lock, fl.description AS fault,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location, l.rd
    FROM iris._ramp_meter m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN iris.device_preset p ON m.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
    LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
    LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
    LEFT JOIN iris.meter_fault fl ON m.fault = fl.id
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

-- Replace "Police panel" / "Manual mode" meter locks with "Knocked down"
UPDATE iris._ramp_meter SET fault = 0 WHERE m_lock = 5;
UPDATE iris._ramp_meter SET fault = 1 WHERE m_lock = 6;
UPDATE iris._ramp_meter SET m_lock = NULL WHERE m_lock >= 5;
UPDATE iris.meter_lock SET description = 'Knocked down' WHERE id = 5;
DELETE FROM iris.meter_lock WHERE id = 6;

-- Add ordinal to r_node table
DROP VIEW r_node_view;

ALTER TABLE iris.r_node ADD COLUMN ordinal INTEGER UNIQUE;

CREATE VIEW r_node_view AS
    SELECT n.name, n.geo_loc, n.ordinal,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           nt.description AS node_type, n.pickable, n.above,
           tr.description AS transition, n.lanes, n.attach_side, n.shift,
           n.active, n.station_id, n.speed_limit, n.notes
    FROM iris.r_node n
    JOIN geo_loc_view l ON n.geo_loc = l.name
    JOIN iris.r_node_type nt ON n.node_type = nt.id
    JOIN iris.r_node_transition tr ON n.transition = tr.id;
GRANT SELECT ON r_node_view TO PUBLIC;

COMMIT;
