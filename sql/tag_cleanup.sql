\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

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

COMMIT;
