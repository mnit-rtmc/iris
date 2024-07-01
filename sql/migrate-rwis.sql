\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.53.0', '5.53.1');

-- Add rwis_sign table
CREATE TABLE iris.rwis_sign (
    name VARCHAR(32) PRIMARY KEY,
    rwis_conditions VARCHAR(256) NOT NULL DEFAULT '',
    msg_pattern VARCHAR(32)
);

-- Add weather_sensor_override column to dms table/views
DROP VIEW iris.dms CASCADE;
ALTER TABLE iris._dms ADD COLUMN weather_sensor_override VARCHAR(256) NOT NULL DEFAULT '';

CREATE OR REPLACE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
    $dms_notify$
BEGIN
    -- has_faults is derived from status (secondary attribute)
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.msg_current IS DISTINCT FROM OLD.msg_current) OR
       ((NEW.status->>'faults' IS NOT NULL) IS DISTINCT FROM
        (OLD.status->>'faults' IS NOT NULL))
    THEN
        NOTIFY dms;
    ELSE
        PERFORM pg_notify('dms', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE TRIGGER dms_notify_trig
    AFTER UPDATE ON iris._dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_notify();

CREATE TRIGGER dms_table_notify_trig
    AFTER INSERT OR DELETE ON iris._dms
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           beacon, preset, sign_config, sign_detail,
           msg_user, msg_sched, msg_current, expire_time, status, stuck_pixels,
           weather_sensor_override
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
        name, geo_loc, notes, gps, static_graphic, beacon,
        sign_config, sign_detail, msg_user, msg_sched, msg_current,
        expire_time, status, stuck_pixels, weather_sensor_override
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.gps, NEW.static_graphic,
        NEW.beacon, NEW.sign_config, NEW.sign_detail,
        NEW.msg_user, NEW.msg_sched, NEW.msg_current, NEW.expire_time,
        NEW.status, NEW.stuck_pixels, NEW.weather_sensor_override
    );
    RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

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
           stuck_pixels = NEW.stuck_pixels,
           weather_sensor_override = NEW.weather_sensor_override
     WHERE name = OLD.name;
    RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW dms_view AS
    SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
           d.sign_config, d.sign_detail, d.static_graphic, d.beacon,
           p.camera, p.preset_num, default_font,
           msg_user, msg_sched, msg_current, expire_time,
           status, stuck_pixels, weather_sensor_override,
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

-- Add basic PDMS RWIS message patterns
INSERT INTO iris.msg_pattern (name, multi, flash_beacon, compose_hashtag) VALUES
    ('RWIS_1_Slippery', 'SLIPPERY[nl]ROAD[nl]DETECTED[np]USE[nl]CAUTION', false, NULL),
    ('RWIS_2_ReducedVisib', 'REDUCED[nl]VISBLITY[nl]DETECTED[np]USE[nl]CAUTION', false, NULL),
    ('RWIS_3_Wind40mph', 'WIND GST[nl]>40 MPH[nl]DETECTED[np]USE[nl]CAUTION', false, NULL),
    ('RWIS_4_Wind60mph', 'WIND GST[nl]>60 MPH[nl]DETECTED[np]REDUCE[nl]SPEED', false, NULL),
    ('RWIS_5_VerySlippery', 'SLIPPERY[nl]ROAD[nl]DETECTED[np]REDUCE[nl]SPEED', false, NULL),
    ('RWIS_6_LowVisib', 'LOW[nl]VISBLITY[nl]DETECTED[np]REDUCE[nl]SPEED', false, NULL),
    ('RWIS_7_IceDetected', 'ICE[nl]DETECTED[np]REDUCE[nl]SPEED', false, NULL);

-- Add basic RWIS admin permissions
INSERT INTO iris.capability (name, enabled) VALUES ('rwis', true);
INSERT INTO iris.resource_type (name) VALUES ('rwis_sign');
INSERT INTO iris.role_capability (role, capability) VALUES ('administrator', 'rwis');
INSERT INTO iris.privilege (name, capability, type_n, obj_n, group_n, attr_n, write) VALUES
    ('PRV_RWIS', 'rwis', 'rwis_sign', '', '', '', false);

INSERT INTO iris.system_attribute (name, value) VALUES
    ('rwis_auto_max_m', '805'),
    ('rwis_cycle_sec', '-1'),
    ('rwis_msg_priority', '9');

COMMIT;
