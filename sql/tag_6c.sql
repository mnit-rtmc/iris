\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Delete any old ASTMv6 events (just in case)
DELETE FROM event.tag_read_event WHERE tag_type = 3;

-- Reuse ASTMv6 tag type for 6C protocol
UPDATE event.tag_type SET description = '6C' WHERE id = 3;

-- Add settings column to tag_reader
DROP VIEW tag_reader_view;
DROP VIEW iris.tag_reader;
DROP TRIGGER tag_reader_notify_trig ON iris._tag_reader;

ALTER TABLE iris._tag_reader ADD COLUMN settings JSONB;

CREATE OR REPLACE FUNCTION iris.tag_reader_notify() RETURNS TRIGGER AS
    $tag_reader_notify$
BEGIN
    IF (NEW.settings IS DISTINCT FROM OLD.settings) THEN
        NOTIFY tag_reader, 'settings';
    ELSE
        NOTIFY tag_reader;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$tag_reader_notify$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_notify_trig
    AFTER UPDATE ON iris._tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_notify();

CREATE TRIGGER tag_reader_table_notify_trig
    AFTER INSERT OR DELETE ON iris._tag_reader
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE VIEW iris.tag_reader AS
    SELECT t.name, geo_loc, controller, pin, notes, toll_zone, settings,
           downlink_freq_khz, uplink_freq_khz, sego_atten_downlink_db,
           sego_atten_uplink_db, sego_data_detect_db, sego_seen_count,
           sego_unique_count, iag_atten_downlink_db, iag_atten_uplink_db,
           iag_data_detect_db, iag_seen_count, iag_unique_count, line_loss_db,
           sync_mode, slave_select_count
    FROM iris._tag_reader t JOIN iris.controller_io cio ON t.name = cio.name;

CREATE OR REPLACE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
    $tag_reader_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'tag_reader', NEW.controller, NEW.pin);
    INSERT INTO iris._tag_reader (
        name, geo_loc, notes, toll_zone, settings,
        downlink_freq_khz, uplink_freq_khz,
        sego_atten_downlink_db, sego_atten_uplink_db, sego_data_detect_db,
        sego_seen_count, sego_unique_count, iag_atten_downlink_db,
        iag_atten_uplink_db, iag_data_detect_db, iag_seen_count,
        iag_unique_count, line_loss_db, sync_mode, slave_select_count
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone, NEW.settings,
        NEW.downlink_freq_khz,
        NEW.uplink_freq_khz, NEW.sego_atten_downlink_db,
        NEW.sego_atten_uplink_db, NEW.sego_data_detect_db, NEW.sego_seen_count,
        NEW.sego_unique_count, NEW.iag_atten_downlink_db,
        NEW.iag_atten_uplink_db, NEW.iag_data_detect_db, NEW.iag_seen_count,
        NEW.iag_unique_count, NEW.line_loss_db, NEW.sync_mode,
        NEW.slave_select_count
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
           settings = NEW.settings,
           downlink_freq_khz = NEW.downlink_freq_khz,
           uplink_freq_khz = NEW.uplink_freq_khz,
           sego_atten_downlink_db = NEW.sego_atten_downlink_db,
           sego_atten_uplink_db = NEW.sego_atten_uplink_db,
           sego_data_detect_db = NEW.sego_data_detect_db,
           sego_seen_count = NEW.sego_seen_count,
           sego_unique_count = NEW.sego_unique_count,
           iag_atten_downlink_db = NEW.iag_atten_downlink_db,
           iag_atten_uplink_db = NEW.iag_atten_uplink_db,
           iag_data_detect_db = NEW.iag_data_detect_db,
           iag_seen_count = NEW.iag_seen_count,
           iag_unique_count = NEW.iag_unique_count,
           line_loss_db = NEW.line_loss_db,
           sync_mode = NEW.sync_mode,
           slave_select_count = NEW.slave_select_count
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
           settings,
           downlink_freq_khz, uplink_freq_khz, sego_atten_downlink_db,
           sego_atten_uplink_db, sego_data_detect_db, sego_seen_count,
           sego_unique_count, iag_atten_downlink_db, iag_atten_uplink_db,
           iag_data_detect_db, iag_seen_count, iag_unique_count, line_loss_db,
           m.description AS sync_mode, slave_select_count
    FROM iris.tag_reader t
    LEFT JOIN geo_loc_view l ON t.geo_loc = l.name
    LEFT JOIN iris.tag_reader_sync_mode m ON t.sync_mode = m.id;
GRANT SELECT ON tag_reader_view TO PUBLIC;

COMMIT;
