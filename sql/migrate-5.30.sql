\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.29.0', '5.30.0');

-- Handle NOTIFY for tables starting with underscore
CREATE OR REPLACE FUNCTION iris.table_notify() RETURNS TRIGGER AS
    $table_notify$
BEGIN
    PERFORM pg_notify(LTRIM(TG_TABLE_NAME, '_'), '');
    RETURN NULL; -- AFTER trigger return is ignored
END;
$table_notify$ LANGUAGE plpgsql;

-- Use table_notify for camera_table_notify_trig
DROP TRIGGER camera_table_notify_trig ON iris._camera;
DROP FUNCTION iris.camera_table_notify();
CREATE TRIGGER camera_table_notify_trig
    AFTER INSERT OR DELETE ON iris._camera
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Use table_notify for alarm_table_notify_trig
DROP TRIGGER alarm_notify_trig ON iris._alarm;
DROP FUNCTION iris.alarm_table_notify();
CREATE TRIGGER alarm_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._alarm
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Use table_notify for dms_table_notify_trig
DROP TRIGGER dms_table_notify_trig ON iris._dms;
DROP FUNCTION iris.dms_table_notify();
CREATE TRIGGER dms_table_notify_trig
    AFTER INSERT OR DELETE ON iris._dms
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Special-case 'settings' / 'sample' changes in weather_sensor_notify_trig
DROP TRIGGER weather_sensor_notify_trig ON iris._weather_sensor;

CREATE OR REPLACE FUNCTION iris.weather_sensor_notify() RETURNS TRIGGER AS
    $weather_sensor_notify$
BEGIN
    IF (NEW.settings IS DISTINCT FROM OLD.settings) THEN
        NOTIFY weather_sensor, 'settings';
    ELSIF (NEW.sample IS DISTINCT FROM OLD.sample) THEN
        NOTIFY weather_sensor, 'sample';
    ELSE
        NOTIFY weather_sensor;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_notify$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_notify_trig
    AFTER UPDATE ON iris._weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_notify();

CREATE TRIGGER weather_sensor_table_notify_trig
    AFTER INSERT OR DELETE ON iris._weather_sensor
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Rename geo_loc.notify_tag to resource_n
ALTER TABLE iris.geo_loc
    ADD COLUMN resource_n VARCHAR(16) REFERENCES iris.resource_type;
UPDATE iris.geo_loc SET resource_n = notify_tag::VARCHAR(16);

DROP TRIGGER geo_loc_notify_trig ON iris.geo_loc;
CREATE OR REPLACE FUNCTION iris.geo_loc_notify() RETURNS TRIGGER AS
    $geo_loc_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        IF (OLD.resource_n IS NOT NULL) THEN
            PERFORM pg_notify(OLD.resource_n, OLD.name);
        END IF;
    ELSIF (NEW.resource_n IS NOT NULL) THEN
        PERFORM pg_notify(NEW.resource_n, NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$geo_loc_notify$ LANGUAGE plpgsql;

CREATE TRIGGER geo_loc_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.geo_loc
    FOR EACH ROW EXECUTE PROCEDURE iris.geo_loc_notify();

ALTER TABLE iris.geo_loc DROP COLUMN notify_tag;

COPY iris.permission (role, resource_n, access_n) FROM stdin;
administrator	weather_sensor	4
\.

DROP VIEW beacon_view;
DROP VIEW iris.beacon;
DROP FUNCTION iris.beacon_insert();
DROP FUNCTION iris.beacon_update();

-- Change beacon.message to VARCHAR(128)
ALTER TABLE iris._beacon RENAME COLUMN message to old_message;
ALTER TABLE iris._beacon ADD COLUMN message VARCHAR(128);
UPDATE iris._beacon SET message = old_message::VARCHAR(128);
ALTER TABLE iris._beacon ALTER COLUMN message SET NOT NULL;
ALTER TABLE iris._beacon DROP COLUMN old_message;

-- Change beacon.notes to VARCHAR(128)
ALTER TABLE iris._beacon RENAME COLUMN notes to old_notes;
ALTER TABLE iris._beacon ADD COLUMN notes VARCHAR(128);
UPDATE iris._beacon SET notes = old_notes::VARCHAR(128);
ALTER TABLE iris._beacon ALTER COLUMN notes SET NOT NULL;
ALTER TABLE iris._beacon DROP COLUMN old_notes;

-- Add flashing column to beacon
ALTER TABLE iris._beacon ADD COLUMN flashing BOOLEAN;
UPDATE iris._beacon SET flashing = false;
ALTER TABLE iris._beacon ALTER COLUMN flashing SET NOT NULL;

CREATE VIEW iris.beacon AS
    SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin, preset,
           flashing
    FROM iris._beacon b
    JOIN iris.controller_io cio ON b.name = cio.name
    JOIN iris._device_preset p ON b.name = p.name;

CREATE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
    $beacon_insert$
BEGIN
    INSERT INTO iris.controller_io (name, controller, pin)
        VALUES (NEW.name, NEW.controller, NEW.pin);
    INSERT INTO iris._device_preset (name, preset)
        VALUES (NEW.name, NEW.preset);
    INSERT INTO iris._beacon (name, geo_loc, notes, message, verify_pin,
                              flashing)
        VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message,
                NEW.verify_pin, NEW.flashing);
    RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_insert();

CREATE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
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
           flashing = NEW.flashing
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
           ctr.condition, flashing
    FROM iris.beacon b
    LEFT JOIN iris.camera_preset p ON b.preset = p.name
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN controller_view ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

-- Special-case 'flashing' changes in beacon_notify_trig
DROP TRIGGER beacon_notify_trig ON iris._beacon;

CREATE OR REPLACE FUNCTION iris.beacon_notify() RETURNS TRIGGER AS
    $beacon_notify$
BEGIN
    IF (NEW.flashing IS DISTINCT FROM OLD.flashing) THEN
        NOTIFY beacon, 'flashing';
    ELSE
        NOTIFY beacon;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_notify$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_notify_trig
    AFTER UPDATE ON iris._beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_notify();

CREATE TRIGGER beacon_table_notify_trig
    AFTER INSERT OR DELETE ON iris._beacon
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

COMMIT;
