\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.63.0', '5.64.0');

-- Add notify triggers
CREATE FUNCTION iris.monitor_style_notify() RETURNS TRIGGER AS
    $monitor_style_notify$
BEGIN
    PERFORM pg_notify('monitor_style', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$monitor_style_notify$ LANGUAGE plpgsql;

CREATE TRIGGER monitor_style_notify_trig
    AFTER UPDATE ON iris.monitor_style
    FOR EACH ROW EXECUTE FUNCTION iris.monitor_style_notify();

CREATE TRIGGER monitor_style_table_notify_trig
    AFTER INSERT OR DELETE ON iris.monitor_style
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE FUNCTION iris.play_list_notify() RETURNS TRIGGER AS
    $play_list_notify$
BEGIN
    IF (NEW.seq_num IS DISTINCT FROM OLD.seq_num) OR
       (NEW.notes IS DISTINCT FROM OLD.notes)
    THEN
        NOTIFY play_list;
    ELSE
        PERFORM pg_notify('play_list', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$play_list_notify$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_notify_trig
    AFTER UPDATE ON iris.play_list
    FOR EACH ROW EXECUTE FUNCTION iris.play_list_notify();

CREATE TRIGGER play_list_table_notify_trig
    AFTER INSERT OR DELETE ON iris.play_list
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE FUNCTION iris.play_list_entry_notify() RETURNS TRIGGER AS
    $play_list_entry_notify$
BEGIN
    -- Entries are secondary, but it's not worth notifying
    -- for each row in a large batch update
    NOTIFY play_list;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$play_list_entry_notify$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_entry_notify_trig
    AFTER INSERT OR DELETE ON iris.play_list_entry
    FOR EACH STATEMENT EXECUTE FUNCTION iris.play_list_entry_notify();

CREATE FUNCTION iris.toll_zone_notify() RETURNS TRIGGER AS
    $toll_zone_notify$
BEGIN
    IF (NEW.tollway IS DISTINCT FROM OLD.tollway) THEN
        NOTIFY toll_zone;
    ELSE
        PERFORM pg_notify('toll_zone', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$toll_zone_notify$ LANGUAGE plpgsql;

CREATE TRIGGER toll_zone_notify_trig
    AFTER UPDATE ON iris.toll_zone
    FOR EACH ROW EXECUTE FUNCTION iris.toll_zone_notify();

CREATE TRIGGER toll_zone_table_notify_trig
    AFTER INSERT OR DELETE ON iris.toll_zone
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

-- Delete obsolete system attributes
DELETE FROM iris.system_attribute WHERE name IN (
    'camera_wiper_precip_mm_hr',
    'dmsxml_reinit_detect',
    'email_recipient_action_plan',
    'email_recipient_aws',
    'email_recipient_dmsxml_reinit',
    'email_recipient_gate_arm'
);

-- Reset all controller status to blank
UPDATE iris.controller SET status = NULL;

-- Add email events
INSERT INTO iris.event_config (name, enable_store, enable_purge, purge_days)
VALUES ('email_event', true, true, 30);

INSERT INTO event.event_description (event_desc_id, description)
VALUES
    (308, 'Gate Arm SYSTEM'),
    (903, 'Action Plan SYSTEM');

CREATE TABLE event.email_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    subject VARCHAR(32) NOT NULL,
    message VARCHAR NOT NULL
);

CREATE VIEW email_event_view AS
    SELECT ev.id, event_date, ed.description, subject, message
    FROM event.email_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON email_event_view TO PUBLIC;

COMMIT;
