\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

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

DROP FUNCTION iris.sign_message_sources(INTEGER);
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

COMMIT;
