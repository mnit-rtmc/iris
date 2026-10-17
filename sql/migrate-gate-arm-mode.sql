\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add gate arm control mode
DROP VIEW gate_arm_view;
DROP VIEW iris.gate_arm;

CREATE TABLE iris.gate_arm_control_mode (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.gate_arm_control_mode (id, description)
VALUES
    (0, 'normal'),
    (1, 'override'),
    (2, 'close delay');

ALTER TABLE iris._gate_arm
    ADD COLUMN control_mode INTEGER REFERENCES iris.gate_arm_control_mode;
UPDATE iris._gate_arm SET control_mode = 0;
ALTER TABLE iris._gate_arm ALTER COLUMN control_mode SET NOT NULL;

CREATE OR REPLACE FUNCTION iris.gate_arm_notify() RETURNS TRIGGER AS
    $gate_arm_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.control_mode IS DISTINCT FROM OLD.control_mode) OR
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

CREATE VIEW iris.gate_arm AS
    SELECT g.name, geo_loc, controller, pin, preset, notes, opposing,
           downstream_hashtag, control_mode, arm_state, interlock, fault
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
        control_mode, arm_state, interlock, fault
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.opposing,
        NEW.downstream_hashtag, NEW.control_mode, NEW.arm_state,
        NEW.interlock, NEW.fault
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
           control_mode = NEW.control_mode,
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
           cio.controller, cio.pin, c.comm_link, c.drop_id,
           cnd.description AS condition, cp.camera, cp.preset_num, g.opposing,
           g.downstream_hashtag, gcm.description AS control_mode,
           gas.description AS arm_state, gai.description AS interlock, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    LEFT JOIN iris.device_preset p ON g.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    JOIN iris.gate_arm_control_mode gcm ON g.control_mode = gcm.id
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON g.interlock = gai.id
    LEFT JOIN geo_loc_view l ON g.geo_loc = l.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON gate_arm_view TO PUBLIC;

COMMIT;
