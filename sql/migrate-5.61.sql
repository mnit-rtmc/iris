\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.60.0', '5.61.0');

-- Add shared hashtag_trig function
CREATE FUNCTION iris.hashtag_trig() RETURNS TRIGGER AS
    $hashtag_trig$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        IF (TG_OP != 'INSERT') THEN
            DELETE FROM iris.hashtag
            WHERE resource_n = TG_ARGV[0] AND name = OLD.name;
        END IF;
        IF (TG_OP != 'DELETE') THEN
            INSERT INTO iris.hashtag (resource_n, name, hashtag)
            SELECT TG_ARGV[0], NEW.name, iris.parse_tags(NEW.notes);
        END IF;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$hashtag_trig$ LANGUAGE plpgsql;

DROP TRIGGER camera_hashtag_trig ON iris._camera;
DROP TRIGGER beacon_hashtag_trig ON iris._beacon;
DROP TRIGGER gps_hashtag_trig ON iris._gps;
DROP TRIGGER dms_hashtag_trig ON iris._dms;
DROP TRIGGER gate_arm_array_hashtag_trig ON iris._gate_arm_array;
DROP TRIGGER lane_marking_hashtag_trig ON iris._lane_marking;
DROP TRIGGER ramp_meter_hashtag_trig ON iris._ramp_meter;
DROP TRIGGER weather_sensor_hashtag_trig ON iris._weather_sensor;

DROP FUNCTION iris.camera_hashtag();
DROP FUNCTION iris.beacon_hashtag();
DROP FUNCTION iris.gps_hashtag();
DROP FUNCTION iris.dms_hashtag();
DROP FUNCTION iris.gate_arm_array_hashtag();
DROP FUNCTION iris.lane_marking_hashtag();
DROP FUNCTION iris.ramp_meter_hashtag();
DROP FUNCTION iris.weather_sensor_hashtag();

CREATE TRIGGER camera_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._camera
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('camera');

CREATE TRIGGER beacon_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._beacon
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('beacon');

CREATE TRIGGER gps_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gps
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('gps');

CREATE TRIGGER dms_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._dms
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('dms');

CREATE TRIGGER gate_arm_array_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm_array
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('gate_arm_array');

CREATE TRIGGER lane_marking_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._lane_marking
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('lane_marking');

CREATE TRIGGER ramp_meter_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._ramp_meter
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('ramp_meter');

CREATE TRIGGER weather_sensor_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._weather_sensor
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('weather_sensor');

-- NOTE: this is a new trigger
CREATE TRIGGER video_monitor_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('video_monitor');

-- NOTE: since trigger is new, force hashtags to be created
UPDATE iris.video_monitor SET notes = notes || ' ';
UPDATE iris.video_monitor SET notes = trim(notes);

-- Change type of camera notes column
DROP VIEW camera_view;
DROP VIEW iris.camera;

ALTER TABLE iris._camera ALTER COLUMN notes TYPE VARCHAR;
ALTER TABLE iris._camera ADD CONSTRAINT _camera_notes_check
    CHECK (LENGTH(notes) < 256);

CREATE VIEW iris.camera AS
    SELECT c.name, geo_loc, controller, pin, notes, cam_num, cam_template,
           encoder_type, enc_address, enc_port, enc_mcast, enc_channel,
           publish, video_loss
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_insert();

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_update();

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW camera_view AS
    SELECT c.name, cam_num, c.cam_template, encoder_type, et.make, et.model,
           et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
           c.publish, c.video_loss, c.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name
    LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

-- Drop group_n from video_monitor
DROP VIEW video_monitor_view;
DROP VIEW iris.video_monitor;
DROP FUNCTION iris.video_monitor_insert();
DROP FUNCTION iris.video_monitor_update();

ALTER TABLE iris._video_monitor DROP COLUMN group_n;

CREATE VIEW iris.video_monitor AS
    SELECT m.name, controller, pin, notes, mon_num, restricted, monitor_style,
           camera
    FROM iris._video_monitor m
    JOIN iris.controller_io cio ON m.name = cio.name;

CREATE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
    $video_monitor_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'video_monitor', NEW.controller, NEW.pin);
    INSERT INTO iris._video_monitor (
        name, notes, mon_num, restricted, monitor_style, camera
    ) VALUES (
        NEW.name, NEW.notes, NEW.mon_num, NEW.restricted, NEW.monitor_style,
        NEW.camera
    );
    RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_insert();

CREATE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
    $video_monitor_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._video_monitor
       SET notes = NEW.notes,
           mon_num = NEW.mon_num,
           restricted = NEW.restricted,
           monitor_style = NEW.monitor_style,
           camera = NEW.camera
     WHERE name = OLD.name;
    RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_update();

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW video_monitor_view AS
    SELECT m.name, m.notes, mon_num, restricted, monitor_style,
           cio.controller, cio.pin, ctr.condition, ctr.comm_link, camera
    FROM iris._video_monitor m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN controller_view ctr ON cio.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

-- Replace action plan description with notes (+ hashtag_trig)
DROP VIEW action_plan_view;

ALTER TABLE iris.action_plan ADD COLUMN notes VARCHAR;
ALTER TABLE iris.action_plan ADD CONSTRAINT action_plan_notes_check
    CHECK (LENGTH(notes) < 256);

CREATE TRIGGER action_plan_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.action_plan
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('action_plan');

UPDATE iris.action_plan SET notes = description;

ALTER TABLE iris.action_plan DROP COLUMN description;
ALTER TABLE iris.action_plan DROP COLUMN group_n;

CREATE VIEW action_plan_view AS
    SELECT name, notes, sync_actions, sticky, ignore_auto_fail, active,
           default_phase, phase
    FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

-- Make video monitor a base resource
UPDATE iris.resource_type SET base = true WHERE name = 'video_monitor';

-- Drop privileges and capabilities
DROP VIEW role_privilege_view;
DROP TABLE iris.privilege;
DROP TABLE iris.role_capability;
DROP TABLE iris.capability;

CREATE TABLE perm (
    name VARCHAR(8) PRIMARY KEY,
    role VARCHAR(15) NOT NULL REFERENCES iris.role ON DELETE CASCADE,
    base_resource VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    hashtag VARCHAR(16),
    access_level INTEGER NOT NULL
);

INSERT INTO perm (name, role, base_resource, hashtag, access_level) (
    SELECT 'prm_' || id, role, resource_n, hashtag, access_n
    FROM iris.permission
);

DROP TABLE iris.permission;

CREATE TABLE iris.permission (
    name VARCHAR(8) PRIMARY KEY,
    role VARCHAR(15) NOT NULL REFERENCES iris.role ON DELETE CASCADE,
    base_resource VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    hashtag VARCHAR(16),
    access_level INTEGER NOT NULL,

    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$'),
    CONSTRAINT permission_access
        CHECK (access_level >= 1 AND access_level <= 4),
    -- hashtag cannot be applied to "View" access level
    CONSTRAINT hashtag_access_ck CHECK (hashtag IS NULL OR access_level != 1)
);

ALTER TABLE iris.permission
    ADD CONSTRAINT base_resource_ck
        CHECK (iris.resource_is_base(base_resource)) NOT VALID;

INSERT INTO iris.permission (name, role, base_resource, hashtag, access_level) (
    SELECT name, role, base_resource, hashtag, access_level
    FROM perm
);

DROP TABLE perm;

CREATE UNIQUE INDEX permission_role_base_resource_hashtag_idx
    ON iris.permission (role, base_resource, COALESCE(hashtag, ''));

CREATE TRIGGER permission_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.permission
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW permission_view AS
    SELECT name, role, base_resource, hashtag, access_level
    FROM iris.permission;
GRANT SELECT ON permission_view TO PUBLIC;

COMMIT;
