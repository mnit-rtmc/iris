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

-- Replace privileges and capabilities with permissions
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
DROP FUNCTION iris.resource_is_base(VARCHAR(16));
ALTER TABLE iris.privilege DROP CONSTRAINT privilege_type_n_fkey;

ALTER TABLE iris.resource_type DROP COLUMN base;
ALTER TABLE iris.resource_type ADD COLUMN base VARCHAR(16)
    REFERENCES iris.resource_type;
DELETE FROM iris.resource_type WHERE name IN (
    'capability', 'catalog', 'privilege'
);
UPDATE iris.resource_type SET base = 'action_plan' WHERE name IN (
    'day_matcher', 'day_plan', 'device_action', 'plan_phase', 'time_action'
);
UPDATE iris.resource_type SET base = 'alert_config' WHERE name IN (
    'alert_info', 'alert_message'
);
UPDATE iris.resource_type SET base = 'camera' WHERE name IN (
    'camera_preset', 'camera_template', 'cam_vid_src_ord', 'encoder_stream',
    'encoder_type', 'vid_src_template'
);
UPDATE iris.resource_type SET base = 'controller' WHERE name IN (
    'alarm', 'comm_link', 'geo_loc', 'gps', 'modem'
);
UPDATE iris.resource_type SET base = 'detector' WHERE name IN (
    'r_node', 'road', 'road_affix', 'station'
);
UPDATE iris.resource_type SET base = 'dms' WHERE name IN (
    'font', 'glyph', 'graphic', 'msg_line', 'msg_pattern', 'sign_config',
    'sign_detail', 'sign_message', 'word'
);
UPDATE iris.resource_type SET base = 'gate_arm' WHERE name = 'gate_arm_array';
UPDATE iris.resource_type SET base = 'incident' WHERE name IN (
    'inc_advice', 'inc_descriptor', 'inc_locator', 'incident_detail'
);
UPDATE iris.resource_type SET base = 'lcs' WHERE name IN (
    'lane_marking', 'lane_use_multi', 'lcs_array', 'lcs_indication'
);
UPDATE iris.resource_type SET base = 'permission' WHERE name IN (
    'connection', 'domain', 'role', 'user_id'
);
UPDATE iris.resource_type SET base = 'system_attribute' WHERE name IN (
    'cabinet_style', 'comm_config', 'map_extent', 'rpt_conduit'
);
INSERT INTO iris.resource_type (name, base)
    VALUES ('rpt_conduit', 'system_attribute')
    ON CONFLICT DO NOTHING;
UPDATE iris.resource_type SET base = 'toll_zone' WHERE name = 'tag_reader';
UPDATE iris.resource_type SET base = 'video_monitor' WHERE name IN (
    'flow_stream', 'monitor_style', 'play_list'
);

CREATE FUNCTION iris.resource_is_base(VARCHAR(16)) RETURNS BOOLEAN AS
    $resource_is_base$
SELECT EXISTS (
    SELECT 1
    FROM iris.resource_type
    WHERE name = $1 AND base IS NULL
);
$resource_is_base$ LANGUAGE sql;

CREATE TABLE iris.access_level (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

COPY iris.access_level (id, description) FROM stdin;
1	View
2	Operate
3	Manage
4	Configure
\.

CREATE TABLE iris.permission (
    name VARCHAR(8) PRIMARY KEY,
    role VARCHAR(15) NOT NULL REFERENCES iris.role ON DELETE CASCADE,
    base_resource VARCHAR(16) NOT NULL REFERENCES iris.resource_type,
    hashtag VARCHAR(16),
    access_level INTEGER NOT NULL REFERENCES iris.access_level,

    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$'),
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

CREATE FUNCTION perm_num(TEXT) RETURNS INTEGER AS $$
DECLARE
    nm ALIAS FOR $1;
    parts TEXT[];
BEGIN
    parts = regexp_split_to_array(nm, '_');
    IF array_length(parts, 1) = 2 AND parts[1] = 'prm' THEN
        RETURN parts[2]::INTEGER;
    ELSE
        RETURN 0;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION perm_next() RETURNS TEXT AS $$
    SELECT 'prm_' || (1 + MAX(perm_num(name))) FROM iris.permission;
$$ LANGUAGE sql;

CREATE FUNCTION perm_cap(cap TEXT, res TEXT, lvl INTEGER) RETURNS VOID AS
$$
    INSERT INTO iris.permission (name, role, base_resource, access_level) (
        SELECT perm_next(), role, res, lvl
        FROM iris.role_capability
        WHERE capability = cap
    ) ON CONFLICT DO NOTHING;
$$ LANGUAGE sql;

-- WARNING: fragile conversion from capabilities to permissions
SELECT perm_cap('plan_admin', 'action_plan', 4);
SELECT perm_cap('plan_control', 'action_plan', 2);
SELECT perm_cap('plan_tab', 'action_plan', 1);
SELECT perm_cap('alert_admin', 'alert_config', 4);
SELECT perm_cap('alert_deploy', 'alert_config', 2);
SELECT perm_cap('alert_tab', 'alert_config', 1);
SELECT perm_cap('device_admin', 'beacon', 4);
SELECT perm_cap('beacon_admin', 'beacon', 4);
SELECT perm_cap('beacon_control', 'beacon', 2);
SELECT perm_cap('beacon_tab', 'beacon', 1);
SELECT perm_cap('camera_admin', 'camera', 4);
SELECT perm_cap('camera_publish', 'camera', 3);
SELECT perm_cap('camera_policy', 'camera', 3);
SELECT perm_cap('camera_control', 'camera', 2);
SELECT perm_cap('camera_tab', 'camera', 1);
SELECT perm_cap('ctrl_admin', 'controller', 4);
SELECT perm_cap('device_admin', 'controller', 4);
SELECT perm_cap('comm_admin', 'controller', 4);
SELECT perm_cap('integrator', 'controller', 4);
SELECT perm_cap('maintenance', 'controller', 2);
SELECT perm_cap('comm_control', 'controller', 2);
SELECT perm_cap('controllers', 'controller', 1);
SELECT perm_cap('rwis', 'controller', 1);
SELECT perm_cap('comm_tab', 'controller', 1);
SELECT perm_cap('device_admin', 'detector', 4);
SELECT perm_cap('sensor_admin', 'detector', 4);
SELECT perm_cap('integrator', 'detector', 4);
SELECT perm_cap('det_control', 'detector', 2);
SELECT perm_cap('sensor_control', 'detector', 2);
SELECT perm_cap('login', 'detector', 1);
SELECT perm_cap('sensor_tab', 'detector', 1);
SELECT perm_cap('dms_admin', 'dms', 4);
SELECT perm_cap('msg_admin_all', 'dms', 3);
SELECT perm_cap('dms_policy', 'dms', 3);
SELECT perm_cap('dms_control', 'dms', 2);
SELECT perm_cap('dms_tab', 'dms', 1);
SELECT perm_cap('device_admin', 'gate_arm', 4);
SELECT perm_cap('gate_arm_admin', 'gate_arm', 4);
SELECT perm_cap('gate_arm_control', 'gate_arm', 2);
SELECT perm_cap('gate_arm_tab', 'gate_arm', 1);
SELECT perm_cap('policy_admin', 'incident', 4);
SELECT perm_cap('dms_admin', 'incident', 4);
SELECT perm_cap('incident_admin', 'incident', 4);
SELECT perm_cap('incident_control', 'incident', 2);
SELECT perm_cap('incident_tab', 'incident', 1);
SELECT perm_cap('device_admin', 'lcs', 4);
SELECT perm_cap('lcs_admin', 'lcs', 4);
SELECT perm_cap('maintenance', 'lcs', 3);
SELECT perm_cap('lcs_control', 'lcs', 2);
SELECT perm_cap('lcs_tab', 'lcs', 1);
SELECT perm_cap('parking_admin', 'parking_area', 4);
SELECT perm_cap('parking_tab', 'parking_area', 1);
SELECT perm_cap('user_admin', 'permission', 4);
SELECT perm_cap('base_admin', 'permission', 4);
SELECT perm_cap('login', 'permission', 1);
SELECT perm_cap('base', 'permission', 1);
SELECT perm_cap('device_admin', 'ramp_meter', 4);
SELECT perm_cap('meter_admin', 'ramp_meter', 4);
SELECT perm_cap('maintenance', 'ramp_meter', 3);
SELECT perm_cap('meter_control', 'ramp_meter', 2);
SELECT perm_cap('meter_tab', 'ramp_meter', 1);
SELECT perm_cap('system_admin', 'system_attribute', 4);
SELECT perm_cap('login', 'system_attribute', 1);
SELECT perm_cap('base_policy', 'system_attribute', 3);
SELECT perm_cap('base', 'system_attribute', 1);
SELECT perm_cap('device_admin', 'toll_zone', 4);
SELECT perm_cap('toll_admin', 'toll_zone', 4);
SELECT perm_cap('tag_reader_tab', 'toll_zone', 1);
SELECT perm_cap('toll_tab', 'toll_zone', 1);
SELECT perm_cap('camera_admin', 'video_monitor', 4);
SELECT perm_cap('camera_publish', 'video_monitor', 3);
SELECT perm_cap('camera_policy', 'video_monitor', 3);
SELECT perm_cap('camera_control', 'video_monitor', 2);
SELECT perm_cap('camera_tab', 'video_monitor', 1);
SELECT perm_cap('device_admin', 'weather_sensor', 4);
SELECT perm_cap('rwis_admin', 'weather_sensor', 4);
SELECT perm_cap('rwis', 'weather_sensor', 1);

DROP FUNCTION perm_cap(TEXT, TEXT, INTEGER);
DROP FUNCTION perm_next();
DROP FUNCTION perm_num(TEXT);

-- Drop privileges and capabilities
DROP VIEW role_privilege_view;
DROP TABLE iris.privilege;
DROP TABLE iris.role_capability;
DROP TABLE iris.capability;

CREATE TRIGGER permission_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.permission
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW permission_view AS
    SELECT name, role, base_resource, hashtag, description AS access_level
    FROM iris.permission p
    JOIN iris.access_level a ON a.id = p.access_level;
GRANT SELECT ON permission_view TO PUBLIC;

-- Improve flow_stream constraint
ALTER TABLE iris._flow_stream DROP CONSTRAINT camera_or_monitor;
ALTER TABLE iris._flow_stream ADD CONSTRAINT camera_or_monitor CHECK (
    (camera IS NULL) != (mon_num IS NULL)
);

-- Combine catalog with play list
DROP VIEW play_list_view;
DROP VIEW iris.play_list;
DROP FUNCTION iris.play_list_insert();
DROP FUNCTION iris.play_list_update();
DROP FUNCTION iris.play_list_delete();
DROP VIEW iris.catalog;
DROP FUNCTION iris.catalog_insert();
DROP FUNCTION iris.catalog_update();
DROP FUNCTION iris.catalog_delete();

CREATE TABLE iris.play_list (
    name VARCHAR(20) PRIMARY KEY,
    meta BOOLEAN NOT NULL,
    seq_num INTEGER UNIQUE,
    notes VARCHAR CHECK (LENGTH(notes) < 64)
);

CREATE TRIGGER play_list_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.play_list
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('play_list');

CREATE FUNCTION iris.play_list_is_meta(VARCHAR(20)) RETURNS BOOLEAN AS
$play_list_is_meta$
    SELECT meta FROM iris.play_list WHERE name = $1;
$play_list_is_meta$ LANGUAGE sql;

CREATE TABLE iris.play_list_entry (
    play_list VARCHAR(20) NOT NULL REFERENCES iris.play_list,
    ordinal INTEGER NOT NULL,
    camera VARCHAR(20) REFERENCES iris._camera,
    sub_list VARCHAR(20) REFERENCES iris.play_list,

    CONSTRAINT camera_ck CHECK (
        iris.play_list_is_meta(play_list) = (camera IS NULL)
    ) NOT VALID,
    CONSTRAINT sub_list_ck CHECK (
        iris.play_list_is_meta(play_list) = (sub_list IS NOT NULL)
        AND NOT iris.play_list_is_meta(sub_list)
    ) NOT VALID
);
ALTER TABLE iris.play_list_entry ADD PRIMARY KEY (play_list, ordinal);

CREATE VIEW play_list_view AS
    SELECT pl.name AS play_list, pl.seq_num, notes, pe.ordinal,
           se.ordinal AS sub_ordinal, COALESCE(pe.camera, se.camera) AS camera
    FROM iris.play_list pl
    JOIN iris.play_list_entry pe ON pe.play_list = pl.name
    LEFT JOIN iris.play_list_entry se ON se.play_list = pe.sub_list
    ORDER BY pl.name, pe.ordinal, se.ordinal;
GRANT SELECT ON play_list_view TO PUBLIC;

INSERT INTO iris.play_list (name, meta, seq_num, notes)
SELECT p.name, false, p.seq_num, p.description
FROM iris._play_list p
JOIN iris.play_list_camera c ON c.play_list = p.name
GROUP BY p.name, p.seq_num, p.description;

INSERT INTO iris.play_list_entry (play_list, ordinal, camera)
SELECT play_list, ordinal, camera
FROM iris.play_list_camera;

INSERT INTO iris.play_list (name, meta, seq_num, notes)
SELECT name, true, seq_num, description
FROM iris._catalog;

INSERT INTO iris.play_list_entry (play_list, ordinal, sub_list)
SELECT catalog, ordinal, play_list
FROM iris.catalog_play_list;

DROP TABLE iris.play_list_camera;
DROP TABLE iris.catalog_play_list;
DROP TABLE iris._catalog;
DROP TABLE iris._play_list;
DROP TABLE iris._cam_sequence;

-- Add pixel_service to sign_message
ALTER TABLE iris.sign_message ADD COLUMN pixel_service BOOLEAN;
UPDATE iris.sign_message SET pixel_service = false;
ALTER TABLE iris.sign_message ALTER COLUMN pixel_service SET NOT NULL;

DROP VIEW sign_message_view;
CREATE VIEW sign_message_view AS
    SELECT name, sign_config, incident, multi, msg_owner, flash_beacon,
           pixel_service, msg_priority, duration
    FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

DROP VIEW dms_message_view;
CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, msg_owner, flash_beacon,
           pixel_service, msg_priority, duration, expire_time
    FROM iris._dms d
    LEFT JOIN iris.controller_io cio ON d.name = cio.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Add attributes to disable legacy XML output
INSERT INTO iris.system_attribute (name, value) VALUES
    ('legacy_xml_config_enable', 'true'),
    ('legacy_xml_detector_enable', 'true'),
    ('legacy_xml_incident_enable', 'true'),
    ('legacy_xml_sign_message_enable', 'true'),
    ('legacy_xml_weather_sensor_enable', 'true');

-- Add event config table
CREATE TABLE iris.event_config (
    name VARCHAR(32) PRIMARY KEY,
    enable_store BOOLEAN NOT NULL,
    enable_purge BOOLEAN NOT NULL,
    purge_days INTEGER NOT NULL
);

INSERT INTO iris.resource_type (name, base)
    VALUES ('event_config', 'system_attribute');

INSERT INTO iris.event_config (name, enable_store, enable_purge, purge_days)
VALUES
    ('action_plan_event', true, true, 90),
    ('alarm_event', true, false, 0),
    ('beacon_event', true, false, 0),
    ('brightness_sample', true, false, 0),
    ('camera_switch_event', true, true, 30),
    ('camera_video_event', true, true, 14),
    ('cap_alert', true, true, 7),
    ('client_event', true, false, 0),
    ('comm_event', true, true, 14),
    ('detector_event', true, true, 90),
    ('gate_arm_event', true, false, 0),
    ('incident', true, false, 0),
    ('incident_update', true, false, 0),
    ('meter_event', true, true, 14),
    ('price_message_event', true, false, 0),
    ('sign_event', true, false, 0),
    ('tag_read_event', true, false, 0),
    ('travel_time_event', true, true, 1),
    ('weather_sensor_sample', true, true, 90),
    ('weather_sensor_settings', true, false, 0);

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'action_plan_event'
    AND a.name = 'action_plan_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'alarm_event'
    AND a.name = 'alarm_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'beacon_event'
    AND a.name = 'beacon_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'camera_switch_event'
    AND a.name = 'camera_switch_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'camera_video_event'
    AND a.name = 'camera_video_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'cap_alert'
    AND a.name = 'cap_alert_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'client_event'
    AND a.name = 'client_event_purge_days';

UPDATE iris.event_config c
    SET enable_store = CAST(a.value AS BOOLEAN)
    FROM iris.system_attribute a
    WHERE c.name = 'comm_event'
    AND a.name = 'comm_event_enable';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'comm_event'
    AND a.name = 'comm_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'detector_event'
    AND a.name = 'detector_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'gate_arm_event'
    AND a.name = 'gate_arm_event_purge_days';

UPDATE iris.event_config c
    SET enable_store = CAST(a.value AS BOOLEAN)
    FROM iris.system_attribute a
    WHERE c.name = 'meter_event'
    AND a.name = 'meter_event_enable';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'meter_event'
    AND a.name = 'meter_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'price_message_event'
    AND a.name = 'price_message_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'sign_event'
    AND a.name = 'sign_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'tag_read_event'
    AND a.name = 'tag_read_event_purge_days';

UPDATE iris.event_config c
    SET enable_purge = (a.value <> '0'), purge_days = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE c.name = 'weather_sensor_sample'
    AND a.name = 'weather_sensor_event_purge_days';

CREATE TRIGGER event_config_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.event_config
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

-- Rename system attributes
UPDATE iris.system_attribute SET name = 'detector_data_archive_enable'
    WHERE name = 'sample_archive_enable';
UPDATE iris.system_attribute SET name = 'camera_playlist_dwell_sec'
    WHERE name = 'camera_sequence_dwell_sec';

-- Replace operation_retry_threshold system attribute with comm_config column
DROP VIEW comm_config_view;
ALTER TABLE iris.comm_config ADD COLUMN retry_threshold INTEGER;
UPDATE iris.comm_config SET retry_threshold = CAST(a.value AS INTEGER)
    FROM iris.system_attribute a
    WHERE a.name = 'operation_retry_threshold';
ALTER TABLE iris.comm_config ALTER COLUMN retry_threshold SET NOT NULL;

ALTER TABLE iris.comm_config
    ADD CONSTRAINT retry_threshold_ck
    CHECK (retry_threshold >= 0 AND retry_threshold <= 8);

CREATE VIEW comm_config_view AS
    SELECT cc.name, cc.description, cp.description AS protocol,
           timeout_ms, retry_threshold, poll_period_sec, long_poll_period_sec,
           idle_disconnect_sec, no_response_disconnect_sec
    FROM iris.comm_config cc
    JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_config_view TO PUBLIC;

-- Delete unused system attributes
DELETE FROM iris.system_attribute WHERE name IN (
    'dms_lamp_test_timeout_secs',
    'action_plan_event_purge_days',
    'alarm_event_purge_days',
    'beacon_event_purge_days',
    'camera_switch_event_purge_days',
    'camera_video_event_purge_days',
    'cap_alert_purge_days',
    'client_event_purge_days',
    'comm_event_enable',
    'comm_event_purge_days',
    'detector_event_purge_days',
    'gate_arm_event_purge_days',
    'meter_event_enable',
    'meter_event_purge_days',
    'operation_retry_threshold',
    'price_message_event_purge_days',
    'sign_event_purge_days',
    'tag_read_event_purge_days',
    'weather_sensor_event_purge_days'
);

COMMIT;
