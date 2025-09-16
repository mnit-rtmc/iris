\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add selectable to plan phase table
ALTER TABLE iris.plan_phase ADD COLUMN selectable BOOLEAN;
UPDATE iris.plan_phase SET selectable = true;
ALTER TABLE iris.plan_phase ALTER COLUMN selectable SET NOT NULL;

-- Allow phase hold time to be NULL
ALTER TABLE iris.plan_phase ALTER COLUMN hold_time DROP NOT NULL;
UPDATE iris.plan_phase SET hold_time = NULL WHERE hold_time < 1;
ALTER TABLE iris.plan_phase ADD CONSTRAINT plan_phase_hold_time_check
    CHECK (hold_time >= 1 AND hold_time <= 600);

INSERT INTO iris.plan_phase (name, selectable)
    VALUES ('ga_warn_cls', true);

-- Drop ga_array, idx from gate arms, rename downstream to downstream_hashtag
DROP VIEW gate_arm_view;
DROP VIEW iris.gate_arm;

ALTER TABLE iris._gate_arm DROP COLUMN ga_array;
ALTER TABLE iris._gate_arm DROP COLUMN idx;
ALTER TABLE iris._gate_arm ADD COLUMN downstream_hashtag VARCHAR(16);
UPDATE iris._gate_arm SET downstream_hashtag = downstream;
ALTER TABLE iris._gate_arm DROP COLUMN downstream;

ALTER TABLE iris._gate_arm ADD CONSTRAINT hashtag_ck
    CHECK (downstream_hashtag ~ '^#[A-Za-z0-9]+$');

ALTER TABLE iris._gate_arm
    ADD CONSTRAINT _gate_arm_geo_loc_fkey FOREIGN KEY (geo_loc)
    REFERENCES iris.geo_loc(name);

DROP TRIGGER gate_arm_notify_trig ON iris._gate_arm;

CREATE FUNCTION iris.gate_arm_notify() RETURNS TRIGGER AS
    $gate_arm_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.arm_state IS DISTINCT FROM OLD.arm_state) OR
       (NEW.interlock IS DISTINCT FROM OLD.interlock)
    THEN
        NOTIFY gate_arm;
    ELSE
        PERFORM pg_notify('gate_arm', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gate_arm_notify$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_notify_trig
    AFTER UPDATE ON iris._gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_notify();

CREATE TRIGGER gate_arm_table_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.gate_arm AS
    SELECT g.name, geo_loc, controller, pin, preset, notes, opposing,
           downstream_hashtag, arm_state, interlock, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    JOIN iris.device_preset p ON g.name = p.name;

CREATE OR REPLACE FUNCTION iris.gate_arm_insert() RETURNS TRIGGER AS
    $gate_arm_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'gate_arm', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
        VALUES (NEW.name, 'gate_arm', NEW.preset);
    INSERT INTO iris._gate_arm (
        name, geo_loc, notes, opposing, downstream_hashtag,
        arm_state, interlock, fault
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.opposing,
        NEW.downstream_hashtag, NEW.arm_state, NEW.interlock, NEW.fault
    );
    RETURN NEW;
END;
$gate_arm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_insert();

CREATE OR REPLACE FUNCTION iris.gate_arm_update() RETURNS TRIGGER AS
    $gate_arm_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller, pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._gate_arm
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           opposing = NEW.opposing,
           downstream_hashtag = NEW.downstream_hashtag,
           arm_state = NEW.arm_state,
           interlock = NEW.interlock,
           fault = NEW.fault
     WHERE name = OLD.name;
    RETURN NEW;
END;
$gate_arm_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.gate_arm_update();

CREATE TRIGGER gate_arm_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW gate_arm_view AS
    SELECT g.name, g.notes,
           g.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
           cp.camera, cp.preset_num, g.opposing, g.downstream_hashtag,
           gas.description AS arm_state, gai.description AS interlock, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    LEFT JOIN iris.device_preset p ON g.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON g.interlock = gai.id
    LEFT JOIN geo_loc_view l ON g.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

-- Remove gate arm arrays
UPDATE iris.gate_arm_array SET prereq = NULL;
DELETE FROM iris.gate_arm_array;
DELETE FROM iris.geo_loc WHERE resource_n = 'gate_arm_array';

DROP VIEW gate_arm_array_view;
DROP VIEW iris.gate_arm_array;
DROP FUNCTION iris.gate_arm_array_insert();
DROP FUNCTION iris.gate_arm_array_update();
DROP TABLE iris._gate_arm_array;
DROP FUNCTION iris.gate_arm_array_notify();

DELETE FROM iris.resource_type WHERE name = 'gate_arm_array';

-- Remove obsolete 'warn close' gate arm state
UPDATE iris.gate_arm SET arm_state = 0 WHERE arm_state = 4;
DELETE FROM iris.gate_arm_state WHERE id = 4;

COMMIT;
