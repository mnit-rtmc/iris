\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.27.0', '5.28.0');

-- Add "SECURITY DEFINER" to fix permission problems
CREATE OR REPLACE FUNCTION iris.multi_tags_str(INTEGER)
    RETURNS text AS $multi_tags_str$
DECLARE
    bits ALIAS FOR $1;
BEGIN
    RETURN (
        SELECT string_agg(mt.tag, ', ') FROM (
            SELECT bit, tag FROM iris.multi_tags(bits) ORDER BY bit
        ) AS mt
    );
END;
$multi_tags_str$ LANGUAGE plpgsql SECURITY DEFINER;

-- Add NOTIFY triggers for more tables
CREATE TRIGGER alarm_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._alarm
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER comm_config_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.comm_config
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE FUNCTION iris.comm_link_notify() RETURNS TRIGGER AS
    $comm_link_notify$
BEGIN
    IF (NEW.connected IS DISTINCT FROM OLD.connected) THEN
        PERFORM pg_notify('comm_link', 'connected');
    ELSE
        NOTIFY comm_link;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$comm_link_notify$ LANGUAGE plpgsql;

CREATE TRIGGER comm_link_notify_trig
    AFTER UPDATE ON iris.comm_link
    FOR EACH ROW EXECUTE PROCEDURE iris.comm_link_notify();

CREATE TRIGGER comm_link_table_notify_trig
    AFTER INSERT OR DELETE ON iris.comm_link
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER modem_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.modem
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER cabinet_style_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.cabinet_style
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE FUNCTION iris.controller_notify() RETURNS TRIGGER AS
    $controller_notify$
BEGIN
    IF (NEW.fail_time IS DISTINCT FROM OLD.fail_time) THEN
        PERFORM pg_notify('controller', 'fail_time');
    ELSE
        NOTIFY controller;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$controller_notify$ LANGUAGE plpgsql;

CREATE TRIGGER controller_notify_trig
    AFTER UPDATE ON iris.controller
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_notify();

CREATE TRIGGER controller_table_notify_trig
    AFTER INSERT OR DELETE ON iris.controller
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Rename modem.timeout to timeout_ms
DROP VIEW modem_view;

ALTER TABLE iris.modem ADD COLUMN timeout_ms INTEGER;
UPDATE iris.modem SET timeout_ms = timeout;
ALTER TABLE iris.modem ALTER COLUMN timeout_ms SET NOT NULL;
ALTER TABLE iris.modem DROP COLUMN timeout;

CREATE VIEW modem_view AS
    SELECT name, uri, config, timeout_ms, enabled
    FROM iris.modem;
GRANT SELECT ON modem_view TO PUBLIC;

-- Drop views depending on cabinet table
DROP VIEW camera_view;
DROP VIEW beacon_view;
DROP VIEW detector_view;
DROP VIEW gate_arm_array_view;
DROP VIEW gate_arm_view;
DROP VIEW lane_marking_view;
DROP VIEW video_monitor_view;
DROP VIEW flow_stream_view;
DROP VIEW weather_sensor_view;

DROP VIEW cabinet_view;
DROP VIEW controller_loc_view;
DROP VIEW controller_view;
DROP VIEW controller_report;

DELETE FROM iris.privilege WHERE type_n = 'cabinet';
DELETE FROM iris.sonar_type WHERE name = 'cabinet';

-- Copy cabinet columns to controller table
ALTER TABLE iris.controller
    ADD COLUMN cabinet_style VARCHAR(20) REFERENCES iris.cabinet_style;
ALTER TABLE iris.controller
    ADD COLUMN geo_loc VARCHAR(20) REFERENCES iris.geo_loc;
UPDATE iris.controller c
    SET cabinet_style = cab.style, geo_loc = cab.geo_loc
    FROM iris.cabinet cab
    WHERE c.cabinet = cab.name;
ALTER TABLE iris.controller
    ALTER COLUMN geo_loc SET NOT NULL;

-- Drop cabinet table
ALTER TABLE iris.controller DROP COLUMN cabinet;
DROP TABLE iris.cabinet;

UPDATE iris.geo_loc SET notify_tag = 'controller' WHERE notify_tag = 'cabinet';

-- Recreate views
CREATE VIEW controller_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, geo_loc,
           cnd.description AS condition, notes, version, fail_time
    FROM iris.controller c
    LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
    SELECT c.name, drop_id, comm_link, cabinet_style, condition, c.notes,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
    FROM controller_view c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW controller_report AS
    SELECT c.name, c.comm_link, c.drop_id, l.landmark, c.geo_loc, l.location,
           cabinet_style, d.name AS device, d.pin, d.cross_loc, d.corridor,
           c.notes
    FROM iris.controller c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

CREATE VIEW camera_view AS
    SELECT c.name, cam_num, c.cam_template, encoder_type, et.make, et.model,
           et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
           c.publish, c.streamable, c.video_loss, c.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           c.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
    FROM iris.camera c
    LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW beacon_view AS
    SELECT b.name, b.notes, b.message, p.camera, p.preset_num, b.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           b.controller, b.pin, b.verify_pin, ctr.comm_link, ctr.drop_id,
           ctr.condition
    FROM iris.beacon b
    LEFT JOIN iris.camera_preset p ON b.preset = p.name
    LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
    LEFT JOIN controller_view ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

CREATE VIEW detector_view AS
    SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
           iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
           d.lane_type, d.lane_number, d.abandoned) AS label,
           rnd.geo_loc, l.rd || '_' || l.road_dir AS cor_id,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           d.lane_number, d.field_length, ln.description AS lane_type,
           ln.dcode AS lane_code, d.abandoned, d.force_fail, d.auto_fail,
           c.condition, d.fake, d.notes
    FROM iris.detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
    LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
    LEFT JOIN controller_view c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW gate_arm_array_view AS
    SELECT ga.name, ga.notes, ga.geo_loc, l.roadway, l.road_dir, l.cross_mod,
           l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon, l.corridor,
           l.location, ga.controller, ga.pin, ctr.comm_link, ctr.drop_id,
           ctr.condition, ga.opposing, ga.prereq, ga.camera, ga.approach,
           ga.action_plan, gas.description AS arm_state,
           gai.description AS interlock
    FROM iris.gate_arm_array ga
    JOIN iris.gate_arm_state gas ON ga.arm_state = gas.id
    JOIN iris.gate_arm_interlock gai ON ga.interlock = gai.id
    LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
    LEFT JOIN controller_view ctr ON ga.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

CREATE VIEW gate_arm_view AS
    SELECT g.name, g.ga_array, g.notes, ga.geo_loc, l.roadway, l.road_dir,
           l.cross_mod, l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon,
           l.corridor, l.location, g.controller, g.pin, ctr.comm_link,
           ctr.drop_id, ctr.condition, ga.opposing, ga.prereq, ga.camera,
           ga.approach, gas.description AS arm_state, fault
    FROM iris.gate_arm g
    JOIN iris.gate_arm_state gas ON g.arm_state = gas.id
    JOIN iris._gate_arm_array ga ON g.ga_array = ga.name
    LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
    LEFT JOIN controller_view ctr ON g.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

CREATE VIEW lane_marking_view AS
    SELECT m.name, m.notes, m.geo_loc, l.roadway, l.road_dir, l.cross_mod,
           l.cross_street, l.cross_dir, l.landmark, l.lat, l.lon, l.corridor,
           l.location, m.controller, m.pin, ctr.comm_link, ctr.drop_id,
           ctr.condition
    FROM iris.lane_marking m
    LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
    LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW video_monitor_view AS
    SELECT m.name, m.notes, group_n, mon_num, restricted, monitor_style,
           m.controller, m.pin, ctr.condition, ctr.comm_link, camera
    FROM iris.video_monitor m
    LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

CREATE VIEW flow_stream_view AS
    SELECT f.name, f.controller, pin, condition, comm_link, restricted,
           loc_overlay, eq.description AS quality, camera, mon_num, address,
           port, s.description AS status
    FROM iris.flow_stream f
    JOIN iris.flow_stream_status s ON f.status = s.id
    LEFT JOIN controller_view ctr ON controller = ctr.name
    LEFT JOIN iris.encoding_quality eq ON f.quality = eq.id;
GRANT SELECT ON flow_stream_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
    SELECT w.name, w.site_id, w.alt_id, w.notes, w.settings, w.sample,
           w.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
           w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
    FROM iris.weather_sensor w
    LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
    LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

-- Add `connected` column to `comm_link`
ALTER TABLE iris.comm_link ADD COLUMN connected BOOLEAN;
UPDATE iris.comm_link SET connected = false;
ALTER TABLE iris.comm_link ALTER COLUMN connected SET NOT NULL;

DROP VIEW comm_link_view;
CREATE VIEW comm_link_view AS
    SELECT cl.name, cl.description, uri, poll_enabled,
           cp.description AS protocol, cc.description AS comm_config,
           modem, timeout_ms, poll_period_sec, connected
    FROM iris.comm_link cl
    JOIN iris.comm_config cc ON cl.comm_config = cc.name
    JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;

COMMIT;
