\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.71.0', '5.72.0');

ALTER TABLE iris.device_action ADD CONSTRAINT device_action_msg_priority_check
    CHECK (msg_priority >= 1 AND msg_priority <= 15);

-- DROP duration from sign_event
DROP VIEW recent_sign_event_view;
DROP VIEW sign_event_view;

ALTER TABLE event.sign_event DROP COLUMN duration;

CREATE VIEW sign_event_view AS
    SELECT event_id, event_date, description, device_id,
           event.multi_message(multi) as message, multi, msg_owner
    FROM event.sign_event JOIN event.event_description
    ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
    SELECT event_id, event_date, description, device_id, message, multi,
           msg_owner
    FROM sign_event_view
    WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

-- sign message: ADD sticky / DROP incident + duration
-- DMS: ADD lock / DROP expire_time
DROP VIEW sign_message_view;
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;

ALTER TABLE iris.sign_message ADD COLUMN sticky BOOLEAN;
UPDATE iris.sign_message SET sticky = false;
ALTER TABLE iris.sign_message ALTER COLUMN sticky SET NOT NULL;
ALTER TABLE iris.sign_message DROP COLUMN incident;
ALTER TABLE iris.sign_message DROP COLUMN duration;

ALTER TABLE iris._dms ADD COLUMN lock JSONB;
ALTER TABLE iris._dms DROP COLUMN expire_time;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, static_graphic,
           beacon, preset, sign_config, sign_detail,
           msg_user, msg_sched, msg_current, lock, status, pixel_failures
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name;

CREATE OR REPLACE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'dms', NEW.controller, NEW.pin);
    INSERT INTO iris.device_preset (name, resource_n, preset)
         VALUES (NEW.name, 'dms', NEW.preset);
    INSERT INTO iris._dms (
        name, geo_loc, notes, static_graphic, beacon,
        sign_config, sign_detail, msg_user, msg_sched, msg_current,
        lock, status, pixel_failures
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.static_graphic,
        NEW.beacon, NEW.sign_config, NEW.sign_detail,
        NEW.msg_user, NEW.msg_sched, NEW.msg_current,
        NEW.lock, NEW.status, NEW.pixel_failures
    );
    RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_insert();

CREATE OR REPLACE FUNCTION iris.dms_update() RETURNS TRIGGER AS
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
       SET notes = NEW.notes,
           static_graphic = NEW.static_graphic,
           beacon = NEW.beacon,
           sign_config = NEW.sign_config,
           sign_detail = NEW.sign_detail,
           msg_user = NEW.msg_user,
           msg_sched = NEW.msg_sched,
           msg_current = NEW.msg_current,
           lock = NEW.lock,
           status = NEW.status,
           pixel_failures = NEW.pixel_failures
     WHERE name = OLD.name;
    RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW dms_view AS
    SELECT d.name, d.geo_loc, cio.controller, cio.pin, d.notes,
           d.sign_config, d.sign_detail, d.static_graphic, d.beacon,
           cp.camera, cp.preset_num, default_font,
           msg_user, msg_sched, msg_current, lock, status, pixel_failures,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris.device_preset p ON d.name = p.name
    LEFT JOIN iris.camera_preset cp ON cp.name = p.preset
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, msg_owner, sticky,
           flash_beacon, pixel_service, msg_priority
    FROM iris._dms d
    LEFT JOIN iris.controller_io cio ON d.name = cio.name
    LEFT JOIN iris.controller c ON cio.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

CREATE VIEW sign_message_view AS
    SELECT name, sign_config, multi, msg_owner, sticky, flash_beacon,
           pixel_service, msg_priority
    FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

COMMIT;
