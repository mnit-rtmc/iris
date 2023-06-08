\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.41.0', '5.42.0');

-- Drop columns from tag reader which are now in JSON settings
DROP VIEW tag_reader_view;
DROP VIEW iris.tag_reader;

ALTER TABLE iris._tag_reader DROP COLUMN downlink_freq_khz;
ALTER TABLE iris._tag_reader DROP COLUMN uplink_freq_khz;
ALTER TABLE iris._tag_reader DROP COLUMN sego_atten_downlink_db;
ALTER TABLE iris._tag_reader DROP COLUMN sego_atten_uplink_db;
ALTER TABLE iris._tag_reader DROP COLUMN sego_data_detect_db;
ALTER TABLE iris._tag_reader DROP COLUMN sego_seen_count;
ALTER TABLE iris._tag_reader DROP COLUMN sego_unique_count;
ALTER TABLE iris._tag_reader DROP COLUMN iag_atten_downlink_db;
ALTER TABLE iris._tag_reader DROP COLUMN iag_atten_uplink_db;
ALTER TABLE iris._tag_reader DROP COLUMN iag_data_detect_db;
ALTER TABLE iris._tag_reader DROP COLUMN iag_seen_count;
ALTER TABLE iris._tag_reader DROP COLUMN iag_unique_count;
ALTER TABLE iris._tag_reader DROP COLUMN line_loss_db;
ALTER TABLE iris._tag_reader DROP COLUMN sync_mode;
ALTER TABLE iris._tag_reader DROP COLUMN slave_select_count;

DROP TABLE iris.tag_reader_sync_mode;

CREATE VIEW iris.tag_reader AS
    SELECT t.name, geo_loc, controller, pin, notes, toll_zone, settings
    FROM iris._tag_reader t JOIN iris.controller_io cio ON t.name = cio.name;

CREATE OR REPLACE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
    $tag_reader_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'tag_reader', NEW.controller, NEW.pin);
    INSERT INTO iris._tag_reader (
        name, geo_loc, notes, toll_zone, settings
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone, NEW.settings
    );
    RETURN NEW;
END;
$tag_reader_insert$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_insert_trig
    INSTEAD OF INSERT ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_insert();

CREATE OR REPLACE FUNCTION iris.tag_reader_update() RETURNS TRIGGER AS
    $tag_reader_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._tag_reader
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           toll_zone = NEW.toll_zone,
           settings = NEW.settings
     WHERE name = OLD.name;
    RETURN NEW;
END;
$tag_reader_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_update_trig
    INSTEAD OF UPDATE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_update();

CREATE TRIGGER tag_reader_delete_trig
    INSTEAD OF DELETE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW tag_reader_view AS
    SELECT t.name, t.geo_loc, location, controller, pin, notes, toll_zone,
           settings
    FROM iris.tag_reader t
    LEFT JOIN geo_loc_view l ON t.geo_loc = l.name;
GRANT SELECT ON tag_reader_view TO PUBLIC;

-- Replace sign_message owner with msg_owner
DROP VIEW dms_message_view;
DROP VIEW sign_message_view;

-- Change spaces to underscores in msg sources
UPDATE iris.sign_msg_source SET source = 'gate_arm' WHERE bit = 4;
UPDATE iris.sign_msg_source SET source = 'travel_time' WHERE bit = 8;
UPDATE iris.sign_msg_source SET source = 'slow_warning' WHERE bit = 10;
UPDATE iris.sign_msg_source SET source = 'speed_advisory' WHERE bit = 11;
UPDATE iris.sign_msg_source SET source = 'exit_warning' WHERE bit = 14;

CREATE OR REPLACE FUNCTION iris.sign_msg_sources(INTEGER) RETURNS TEXT
    AS $sign_msg_sources$
DECLARE
    src ALIAS FOR $1;
    res TEXT;
    ms RECORD;
    b INTEGER;
BEGIN
    res = '';
    FOR ms IN SELECT bit, source FROM iris.sign_msg_source ORDER BY bit LOOP
        b = 1 << ms.bit;
        IF (src & b) = b THEN
            IF char_length(res) > 0 THEN
                res = res || '+' || ms.source;
            ELSE
                res = ms.source;
            END IF;
        END IF;
    END LOOP;
    RETURN res;
END;
$sign_msg_sources$ LANGUAGE plpgsql;

ALTER TABLE iris.sign_message ADD COLUMN msg_owner VARCHAR(127);
UPDATE iris.sign_message
    SET msg_owner = 'IRIS; ' || iris.sign_msg_sources(source) || '; ' || COALESCE(owner, '');
ALTER TABLE iris.sign_message ALTER COLUMN msg_owner SET NOT NULL;
ALTER TABLE iris.sign_message DROP COLUMN owner;
ALTER TABLE iris.sign_message DROP COLUMN source;

DROP FUNCTION iris.sign_msg_sources(INTEGER);
DROP TABLE iris.sign_msg_source;

CREATE VIEW sign_message_view AS
    SELECT name, sign_config, incident, multi, msg_owner, flash_beacon,
           msg_priority, duration
    FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, msg_owner, flash_beacon,
           msg_priority, duration, expire_time
    FROM iris.dms d
    LEFT JOIN iris.controller c ON d.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Replace sign_event owner with msg_owner
DROP VIEW recent_sign_event_view;
DROP VIEW sign_event_view;

ALTER TABLE event.sign_event ADD COLUMN msg_owner VARCHAR(127);
UPDATE event.sign_event SET msg_owner = 'IRIS; unknown; ' || owner;
ALTER TABLE event.sign_event DROP COLUMN owner;

CREATE VIEW sign_event_view AS
    SELECT event_id, event_date, description, device_id,
           event.multi_message(multi) as message, multi, msg_owner, duration
    FROM event.sign_event JOIN event.event_description
    ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
    SELECT event_id, event_date, description, device_id, message, multi,
           msg_owner, duration
    FROM sign_event_view
    WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

-- Replace DmsMsgPriority with SignMsgPriority values
-- ALERT_LOW => low_4
UPDATE iris.sign_message SET msg_priority = 4 WHERE msg_priority = 5;
UPDATE iris.dms_action SET msg_priority = 4 WHERE msg_priority = 5;
-- LCS => high_1
UPDATE iris.sign_message SET msg_priority = 11 WHERE msg_priority = 15;
UPDATE iris.dms_action SET msg_priority = 11 WHERE msg_priority = 15;
-- OPERATOR => high_1
UPDATE iris.sign_message SET msg_priority = 11 WHERE msg_priority = 12;
UPDATE iris.dms_action SET msg_priority = 11 WHERE msg_priority = 12;
-- INCIDENT_LOW => high_2
UPDATE iris.sign_message SET msg_priority = 12 WHERE msg_priority = 16;
UPDATE iris.dms_action SET msg_priority = 12 WHERE msg_priority = 16;
-- INCIDENT_MED => high_3
UPDATE iris.sign_message SET msg_priority = 13 WHERE msg_priority = 17;
UPDATE iris.dms_action SET msg_priority = 13 WHERE msg_priority = 17;
-- SCHED_HIGH => high_3
UPDATE iris.sign_message SET msg_priority = 13 WHERE msg_priority = 18;
UPDATE iris.dms_action SET msg_priority = 13 WHERE msg_priority = 18;
-- INCIDENT_HIGH => high_4
UPDATE iris.sign_message SET msg_priority = 14 WHERE msg_priority = 19;
UPDATE iris.dms_action SET msg_priority = 14 WHERE msg_priority = 19;
-- ALERT_HIGH => high_4
UPDATE iris.sign_message SET msg_priority = 14 WHERE msg_priority = 20;
UPDATE iris.dms_action SET msg_priority = 14 WHERE msg_priority = 20;

COMMIT;
