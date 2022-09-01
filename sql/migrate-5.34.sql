\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.33.0', '5.34.0');

-- Add beacon state LUT
CREATE TABLE iris.beacon_state (
    id INTEGER PRIMARY KEY,
    description VARCHAR(16) NOT NULL
);

COPY iris.beacon_state (id, description) FROM stdin;
0	Unknown
1	Dark Req
2	Dark
3	Flashing Req
4	Flashing
5	Fault: No Verify
6	Fault: Stuck On
\.

-- Replace beacon flashing column with state
ALTER TABLE iris._beacon ADD COLUMN state INTEGER REFERENCES iris.beacon_state;
UPDATE iris._beacon SET state = 0; -- Unknown
ALTER TABLE iris._beacon ALTER COLUMN state SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.beacon_notify() RETURNS TRIGGER AS
    $beacon_notify$
BEGIN
    IF (NEW.state IS DISTINCT FROM OLD.state) THEN
        NOTIFY beacon, 'state';
    ELSE
        NOTIFY beacon;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_notify$ LANGUAGE plpgsql;

DROP VIEW beacon_view;
DROP VIEW iris.beacon;

ALTER TABLE iris._beacon DROP COLUMN flashing;

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin, preset,
           state
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
    INSERT INTO iris._beacon (name, geo_loc, notes, message, verify_pin, state)
        VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message,
                NEW.verify_pin, NEW.state);
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
           b.controller, b.pin, b.verify_pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, bs.description AS state
    FROM iris.beacon b
    JOIN iris.beacon_state bs ON b.state = bs.id
    LEFT JOIN iris.camera_preset p ON b.preset = p.name
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN controller_view ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

-- Replace beacon_event event_desc_id column with state
DROP VIEW beacon_event_view;

ALTER TABLE event.beacon_event
    ADD COLUMN state INTEGER REFERENCES iris.beacon_state;
UPDATE event.beacon_event SET state = 0;
UPDATE event.beacon_event SET state = 4 WHERE event_desc_id = 501; -- ON
UPDATE event.beacon_event SET state = 2 WHERE event_desc_id = 502; -- OFF
ALTER TABLE event.beacon_event ALTER COLUMN state SET NOT NULL;

ALTER TABLE event.beacon_event DROP COLUMN event_desc_id;

CREATE VIEW beacon_event_view AS
    SELECT event_id, event_date, beacon, bs.description AS state
    FROM event.beacon_event be
    JOIN iris.beacon_state bs ON be.state = bs.id;
GRANT SELECT ON beacon_event_view TO PUBLIC;

-- Update privileges from beacon.flashing to .state
UPDATE iris.privilege SET attr_n = 'state'
    WHERE type_n = 'beacon' AND attr_n = 'flashing';

COMMIT;
