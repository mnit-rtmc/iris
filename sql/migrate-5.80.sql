\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.79.0', '5.80.0');

-- Add comm_state to comm_event
DROP VIEW comm_event_view;
DROP TABLE event.comm_event;
DROP VIEW beacon_view;
DROP VIEW camera_view;
DROP VIEW detector_view;
DROP VIEW flow_stream_view;
DROP VIEW gate_arm_view;
DROP VIEW video_monitor_view;
DROP VIEW weather_sensor_view;
DROP VIEW controller_loc_view;
DROP VIEW controller_view;
DELETE FROM event.event_description WHERE event_desc_id IN (
    8, 9, 10, 11, 12, 13, 14, 15, 65
);

CREATE TABLE iris.comm_state (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.comm_state (id, description)
VALUES
    (0, 'Unknown'),
    (1, 'OK'),
    (2, 'FAILED'),
    (3, 'Connection Error'),
    (4, 'Controller Error'),
    (5, 'Checksum Error'),
    (6, 'Parsing Error'),
    (7, 'Timeout Error'),
    (8, 'Error');

ALTER TABLE iris.controller
    ADD COLUMN comm_state INTEGER DEFAULT 0 REFERENCES iris.comm_state;
UPDATE iris.controller SET comm_state = 0;
ALTER TABLE iris.controller ALTER COLUMN comm_state SET NOT NULL;

CREATE TABLE event.comm_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    controller VARCHAR(20) NOT NULL REFERENCES iris.controller(name)
        ON DELETE CASCADE,
    comm_state INTEGER NOT NULL REFERENCES iris.comm_state
);

-- DELETE of iris.controller *very* slow without this index
CREATE INDEX ON event.comm_event (controller);

CREATE FUNCTION event.controller_comm_state_trig() RETURNS TRIGGER AS
    $controller_comm_state_trig$
BEGIN
    IF NEW.comm_state != OLD.comm_state THEN
        INSERT INTO event.comm_event (controller, comm_state)
            VALUES (NEW.name, NEW.comm_state);
    END IF;
    RETURN NEW;
END;
$controller_comm_state_trig$ LANGUAGE plpgsql;

CREATE TRIGGER controller_comm_state_trig
    AFTER UPDATE ON iris.controller
    FOR EACH ROW EXECUTE FUNCTION event.controller_comm_state_trig();

CREATE VIEW controller_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, geo_loc,
           cnd.description AS condition, notes, setup, status,
           comm_state, fail_time
    FROM iris.controller c
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style,
           cnd.description AS condition, c.notes,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
    FROM iris.controller c
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW beacon_view AS
    SELECT b.name, b.notes, b.message, cp.camera, cp.preset_num, b.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, b.verify_pin, b.ext_mode,
           c.comm_link, c.drop_id, cnd.description AS condition,
           bs.description AS state
    FROM iris._beacon b
    JOIN iris.beacon_state bs ON b.state = bs.id
    JOIN iris.controller_io cio ON b.name = cio.name
    LEFT JOIN iris.device_preset p ON b.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON beacon_view TO PUBLIC;

CREATE VIEW camera_view AS
    SELECT c.name, cam_num, c.cam_template, encoder_type, et.make, et.model,
           et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
           c.publish, c.video_loss, c.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, ctr.comm_link, ctr.drop_id,
           cnd.description AS condition, c.notes
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name
    LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN iris.controller ctr ON cio.controller = ctr.name
    LEFT JOIN iris.condition cnd ON ctr.condition = cnd.id;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW detector_view AS
    SELECT d.name, d.r_node, c.comm_link, c.drop_id, cio.controller, cio.pin,
           dl.label, dl.geo_loc, l.rd || '_' || l.road_dir AS cor_id,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           d.lane_number, d.field_length, lc.description AS lane_type,
           d.lane_code, d.abandoned, d.force_fail, d.auto_fail,
           cnd.description AS condition, d.fake, d.notes
    FROM iris.detector d
    JOIN iris.controller_io cio ON d.name = cio.name
    LEFT JOIN detector_label_view dl ON d.name = dl.det_id
    LEFT JOIN geo_loc_view l ON dl.geo_loc = l.name
    LEFT JOIN iris.lane_code lc ON d.lane_code = lc.lcode
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW flow_stream_view AS
    SELECT f.name, cio.controller, cio.pin, cnd.description AS condition,
           comm_link, restricted, loc_overlay, eq.description AS quality,
           camera, mon_num, address, port, s.description AS status
    FROM iris._flow_stream f
    JOIN iris.controller_io cio ON f.name = cio.name
    JOIN iris.flow_stream_status s ON f.status = s.id
    LEFT JOIN iris.controller c ON controller = c.name
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id
    LEFT JOIN iris.encoding_quality eq ON f.quality = eq.id;
GRANT SELECT ON flow_stream_view TO PUBLIC;

CREATE VIEW gate_arm_view AS
    SELECT g.name, g.notes,
           g.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, c.comm_link, c.drop_id,
           cnd.description AS condition, cp.camera, cp.preset_num, g.opposing,
           g.downstream_hashtag, gas.description AS arm_state,
           gai.description AS interlock, fault
    FROM iris._gate_arm g
    JOIN iris.controller_io cio ON g.name = cio.name
    LEFT JOIN iris.device_preset p ON g.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON g.interlock = gai.id
    LEFT JOIN geo_loc_view l ON g.geo_loc = l.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON gate_arm_view TO PUBLIC;

CREATE VIEW video_monitor_view AS
    SELECT m.name, m.notes, mon_num, restricted, monitor_style,
           cio.controller, cio.pin, cnd.description AS condition,
           c.comm_link, camera
    FROM iris._video_monitor m
    JOIN iris.controller_io cio ON m.name = cio.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON video_monitor_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
    SELECT w.name, site_id, alt_id, w.notes, settings, sample, sample_time,
           w.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           cio.controller, cio.pin, c.comm_link, c.drop_id,
           cnd.description AS condition
    FROM iris._weather_sensor w
    JOIN iris.controller_io cio ON w.name = cio.name
    LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

CREATE VIEW comm_event_view AS
    SELECT e.id, e.event_date, cs.description, e.controller,
           c.comm_link, c.drop_id
    FROM event.comm_event e
    JOIN iris.comm_state cs ON e.comm_state = cs.id
    LEFT JOIN iris.controller c ON e.controller = c.name;
GRANT SELECT ON comm_event_view TO PUBLIC;

-- Update triggers for "comm" channel
CREATE OR REPLACE FUNCTION iris.comm_link_notify() RETURNS TRIGGER AS
    $comm_link_notify$
BEGIN
    IF (NEW.description IS DISTINCT FROM OLD.description) OR
       (NEW.uri IS DISTINCT FROM OLD.uri) OR
       (NEW.poll_enabled IS DISTINCT FROM OLD.poll_enabled) OR
       (NEW.comm_config IS DISTINCT FROM OLD.comm_config)
    THEN
        NOTIFY comm_link;
    ELSE
        PERFORM pg_notify('comm_link', NEW.name);
    END IF;
    IF (NEW.connected IS DISTINCT FROM OLD.connected) THEN
        -- notify "comm" channel on connected change
        PERFORM pg_notify('comm', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$comm_link_notify$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.controller_notify() RETURNS TRIGGER AS
    $controller_notify$
BEGIN
    IF (NEW.drop_id IS DISTINCT FROM OLD.drop_id) OR
       (NEW.comm_link IS DISTINCT FROM OLD.comm_link) OR
       (NEW.cabinet_style IS DISTINCT FROM OLD.cabinet_style) OR
       (NEW.condition IS DISTINCT FROM OLD.condition) OR
       (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.setup IS DISTINCT FROM OLD.setup)
    THEN
        NOTIFY controller;
    ELSE
        PERFORM pg_notify('controller', NEW.name);
    END IF;
    IF (NEW.comm_state IS DISTINCT FROM OLD.comm_state) THEN
        -- notify "comm" channel on comm_state change
        PERFORM pg_notify('comm', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_notify$ LANGUAGE plpgsql;

-- DROP user_id from gate_arm_event
DROP VIEW gate_arm_event_view;

ALTER TABLE event.gate_arm_event DROP COLUMN user_id;

CREATE VIEW gate_arm_event_view AS
    SELECT ev.id, event_date, ed.description, device_id, fault
    FROM event.gate_arm_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON gate_arm_event_view TO PUBLIC;

COMMIT;
