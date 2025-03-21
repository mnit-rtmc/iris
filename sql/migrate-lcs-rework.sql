\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Rework LCS tables
CREATE TEMP TABLE lcs (
    name VARCHAR(20) PRIMARY KEY,
    controller VARCHAR(20),
    pin INTEGER NOT NULL,
    geo_loc VARCHAR(20),
    notes VARCHAR,
    preset VARCHAR(20),
    lcs_type INTEGER NOT NULL,
    shift INTEGER NOT NULL,
    lock JSONB,
    status JSONB
);

INSERT INTO iris.geo_loc (
    name, resource_n, roadway, road_dir, cross_street, cross_dir, cross_mod,
    lat, lon, landmark
) (
    SELECT la.name, 'lcs', roadway, road_dir, cross_street, cross_dir,
           cross_mod, lat, lon, landmark
    FROM iris._lcs_array la
    JOIN iris.lcs l ON l.lcs_array = la.name
    JOIN iris.geo_loc loc ON loc.name = l.name
    WHERE l.lane = 1
);

INSERT INTO lcs (
    name, controller, pin, geo_loc, notes, preset, lcs_type, shift
) (
    SELECT la.name, d.controller, d.pin, la.name, la.notes, d.preset, 0, shift
    FROM iris._lcs_array la
    JOIN iris.lcs l ON l.lcs_array = la.name
    JOIN iris.dms d ON d.name = l.name
    WHERE l.lane = 1
);

CREATE TEMP TABLE lcs_state (
    name VARCHAR(20) PRIMARY KEY,
    controller VARCHAR(20),
    pin INTEGER NOT NULL,
    lcs VARCHAR(20),
    lane INTEGER NOT NULL,
    indication INTEGER NOT NULL,
    msg_pattern VARCHAR(20),
    msg_num INTEGER
);

INSERT INTO lcs_state (
    name, controller, pin, lcs, lane, indication
) (
    SELECT li.name, controller, pin, lcs_array, lane, indication
    FROM iris.lcs l
    JOIN iris.lcs_indication li ON li.lcs = l.name
);

ALTER TABLE iris.lcs DROP CONSTRAINT lcs_name_fkey;
DELETE FROM iris.dms WHERE name IN (
    SELECT d.name
    FROM iris._lcs_array la
    JOIN iris.lcs l ON l.lcs_array = la.name
    JOIN iris.dms d ON d.name = l.name
);
DELETE FROM iris.geo_loc
    WHERE resource_n = 'dms' AND name NOT IN (SELECT name FROM iris.dms);
DROP VIEW lane_use_multi_view;
DROP TABLE iris.lane_use_multi;
DELETE FROM iris.lcs_indication;
DROP VIEW lcs_indication_view;
DROP VIEW iris.lcs_indication;
DROP TABLE iris._lcs_indication;
DELETE FROM iris.lcs;
DROP VIEW lcs_view;
DROP TABLE iris.lcs;
DELETE FROM iris.lcs_array;
DROP VIEW lcs_array_view;
DROP VIEW iris.lcs_array;
DROP TABLE iris._lcs_array;
DROP TABLE iris.lane_use_indication;
DROP TABLE iris.lcs_lock;

DROP FUNCTION iris.lcs_array_insert();
DROP FUNCTION iris.lcs_array_update();
DROP FUNCTION iris.lcs_indication_insert();
DROP FUNCTION iris.lcs_indication_update();

CREATE TABLE iris.lcs_type (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.lcs_type (id, description)
VALUES
    (0, 'Over lane dedicated'),
    (1, 'Over lane DMS'),
    (2, 'Pavement LED');

CREATE TABLE iris._lcs (
    name VARCHAR(20) PRIMARY KEY,
    geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc,
    notes VARCHAR CHECK (LENGTH(notes) < 256),
    lcs_type INTEGER NOT NULL REFERENCES iris.lcs_type,
    shift INTEGER NOT NULL,
    lock JSONB,
    status JSONB
);

ALTER TABLE iris._lcs ADD CONSTRAINT _lcs_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER lcs_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lcs
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.lcs AS
    SELECT l.name, geo_loc, controller, pin, notes, preset, lcs_type, shift,
           lock, status
    FROM iris._lcs l
    JOIN iris.controller_io cio ON l.name = cio.name
    JOIN iris.device_preset p ON l.name = p.name;

CREATE FUNCTION iris.lcs_insert() RETURNS TRIGGER AS
    $lcs_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'lcs', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
        VALUES (NEW.name, 'lcs', NEW.preset);
    INSERT INTO iris._lcs (
        name, geo_loc, notes, lcs_type, shift, lock, status
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.lcs_type, NEW.shift,
        NEW.lock, NEW.status
     );
    RETURN NEW;
END;
$lcs_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_insert_trig
    INSTEAD OF INSERT ON iris.lcs
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_insert();

CREATE FUNCTION iris.lcs_update() RETURNS TRIGGER AS
    $lcs_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris.device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._lcs
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           lcs_type = NEW.lcs_type,
           shift = NEW.shift,
           lock = NEW.lock,
           status = NEW.status
     WHERE name = OLD.name;
    RETURN NEW;
END;
$lcs_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_update_trig
    INSTEAD OF UPDATE ON iris.lcs
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_update();

CREATE TRIGGER lcs_delete_trig
    INSTEAD OF DELETE ON iris.lcs
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW lcs_view AS
    SELECT l.name, geo_loc, controller, pin, notes, camera, preset_num,
           lt.description AS lcs_type, shift, lock, status
    FROM iris._lcs l
    JOIN iris.controller_io cio ON l.name = cio.name
    LEFT JOIN iris.device_preset p ON l.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN iris.lcs_type lt ON l.lcs_type = lt.id;
GRANT SELECT ON lcs_view TO PUBLIC;

CREATE TABLE iris.lcs_indication (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.lcs_indication (id, description)
VALUES
    (0, 'Unknown'),
    (1, 'Dark'),
    (2, 'Lane open'),
    (3, 'Use caution'),
    (4, 'Lane closed ahead'),
    (5, 'Lane closed'),
    (6, 'Merge right'),
    (7, 'Merge left'),
    (8, 'Merge left or right'),
    (9, 'Must exit right'),
    (10, 'Must exit left'),
    (11, 'HOV / HOT'),
    (12, 'HOV / HOT begins'),
    (13, 'Variable speed advisory'),
    (14, 'Variable speed limit'),
    (15, 'Low visibility');

CREATE TABLE iris._lcs_state (
    name VARCHAR(20) PRIMARY KEY,
    lcs VARCHAR(20) NOT NULL REFERENCES iris._lcs,
    lane INTEGER NOT NULL,
    indication INTEGER NOT NULL REFERENCES iris.lcs_indication,
    msg_pattern VARCHAR(20) REFERENCES iris.msg_pattern,
    msg_num INTEGER
);
CREATE UNIQUE INDEX lcs_lane_indication
    ON iris._lcs_state USING btree (lcs, lane, indication);

ALTER TABLE iris._lcs_state ADD CONSTRAINT _lcs_state_fkey
    FOREIGN KEY (name) REFERENCES iris.controller_io ON DELETE CASCADE;

CREATE TRIGGER lcs_state_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lcs_state
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.lcs_state AS
    SELECT ls.name, controller, pin, lcs, lane, indication, msg_pattern,
           msg_num
    FROM iris._lcs_state ls
    JOIN iris.controller_io cio ON ls.name = cio.name;

CREATE FUNCTION iris.lcs_state_insert() RETURNS TRIGGER AS
    $lcs_state_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'lcs_state', NEW.controller, NEW.pin);
    INSERT INTO iris._lcs_state
                (name, lcs, lane, indication, msg_pattern, msg_num)
         VALUES (NEW.name, NEW.lcs, NEW.lane, NEW.indication,
                 NEW.msg_pattern, NEW.msg_num);
    RETURN NEW;
END;
$lcs_state_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_state_insert_trig
    INSTEAD OF INSERT ON iris.lcs_state
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_state_insert();

CREATE FUNCTION iris.lcs_state_update() RETURNS TRIGGER AS
    $lcs_state_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._lcs_state
       SET lcs = NEW.lcs,
           lane = NEW.lane,
           indication = NEW.indication,
           msg_pattern = NEW.msg_pattern,
           msg_num = NEW.msg_num
     WHERE name = OLD.name;
    RETURN NEW;
END;
$lcs_state_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_state_update_trig
    INSTEAD OF UPDATE ON iris.lcs_state
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_state_update();

CREATE TRIGGER lcs_state_delete_trig
    INSTEAD OF DELETE ON iris.lcs_state
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW lcs_state_view AS
    SELECT name, controller, pin, lcs, lane, description AS indication,
           msg_pattern, msg_num
    FROM iris.lcs_state
    JOIN iris.lcs_indication ON indication = id;
GRANT SELECT ON lcs_state_view TO PUBLIC;

INSERT INTO iris.resource_type (name, base)
    VALUES ('lcs_state', 'lcs');

INSERT INTO iris.lcs (
    name, controller, pin, geo_loc, notes, preset, lcs_type, shift
) (
    SELECT name, controller, pin, geo_loc, notes, preset, lcs_type, shift
    FROM lcs
);
INSERT INTO iris.lcs_state (
    name, controller, pin, lcs, lane, indication, msg_pattern, msg_num
) (
    SELECT name, controller, pin, lcs, lane, indication, msg_pattern, msg_num
    FROM lcs_state
);

DELETE FROM iris.resource_type
    WHERE name IN ('lane_use_multi', 'lcs_array', 'lcs_indication');

COMMIT;
