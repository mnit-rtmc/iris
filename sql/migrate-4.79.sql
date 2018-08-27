\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.78.0', '4.79.0');

INSERT INTO event.tag_type (id, description) VALUES (2, 'IAG');
INSERT INTO event.tag_type (id, description) VALUES (3, 'ASTMv6');
UPDATE event.tag_read_event SET tag_type = 3 WHERE tag_type = 0;
UPDATE event.tag_type SET description = 'Unknown' WHERE id = 0;

DROP VIEW tag_read_event_view;
DROP FUNCTION event.tag_read_event_view_update();
DROP VIEW tag_reader_view;
DROP VIEW iris.tag_reader;
DROP FUNCTION iris.tag_reader_insert();
DROP FUNCTION iris.tag_reader_update();
DROP FUNCTION iris.tag_reader_delete();

-- Add tag reader configuration stuff
ALTER TABLE iris._tag_reader ADD COLUMN downlink_freq_khz INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN uplink_freq_khz INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN sego_atten_downlink_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN sego_atten_uplink_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN iag_atten_downlink_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN iag_atten_uplink_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN line_loss_db INTEGER;

CREATE VIEW iris.tag_reader AS SELECT
	t.name, geo_loc, controller, pin, notes, toll_zone, downlink_freq_khz,
	uplink_freq_khz, sego_atten_downlink_db, sego_atten_uplink_db,
	iag_atten_downlink_db, iag_atten_uplink_db, line_loss_db
	FROM iris._tag_reader t JOIN iris._device_io d ON t.name = d.name;

CREATE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
	$tag_reader_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._tag_reader (name, geo_loc, notes, toll_zone,
	                              downlink_freq_khz, uplink_freq_khz,
	                              sego_atten_downlink_db,
	                              sego_atten_uplink_db,
	                              iag_atten_downlink_db,
	                              iag_atten_uplink_db, line_loss_db)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone,
	             NEW.downlink_freq_khz, NEW.uplink_freq_khz,
	             NEW.sego_atten_downlink_db, NEW.sego_atten_uplink_db,
	             NEW.iag_atten_downlink_db, NEW.iag_atten_uplink_db,
	             NEW.line_loss_db);
	RETURN NEW;
END;
$tag_reader_insert$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_insert_trig
    INSTEAD OF INSERT ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_insert();

CREATE FUNCTION iris.tag_reader_update() RETURNS TRIGGER AS
	$tag_reader_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._tag_reader
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       toll_zone = NEW.toll_zone,
	       downlink_freq_khz = NEW.downlink_freq_khz,
	       uplink_freq_khz = NEW.uplink_freq_khz,
	       sego_atten_downlink_db = NEW.sego_atten_downlink_db,
	       sego_atten_uplink_db = NEW.sego_atten_uplink_db,
	       iag_atten_downlink_db = NEW.iag_atten_downlink_db,
	       iag_atten_uplink_db = NEW.iag_atten_uplink_db,
	       line_loss_db = NEW.line_loss_db
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$tag_reader_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_update_trig
    INSTEAD OF UPDATE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_update();

CREATE FUNCTION iris.tag_reader_delete() RETURNS TRIGGER AS
	$tag_reader_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$tag_reader_delete$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_delete_trig
    INSTEAD OF DELETE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_delete();

CREATE VIEW tag_reader_view AS
	SELECT t.name, t.geo_loc, l.location, t.controller, t.pin, t.notes,
	       t.toll_zone, t.downlink_freq_khz, t.uplink_freq_khz,
	       t.sego_atten_downlink_db, t.sego_atten_uplink_db,
	       t.iag_atten_downlink_db, t.iag_atten_uplink_db, t.line_loss_db
	FROM iris.tag_reader t
	LEFT JOIN geo_loc_view l ON t.geo_loc = l.name;
GRANT SELECT ON tag_reader_view TO PUBLIC;

CREATE VIEW tag_read_event_view AS
	SELECT event_id, event_date, event_description.description,
	       tag_type.description AS tag_type, agency, tag_id, tag_reader,
	       toll_zone, tollway, hov, trip_id
	FROM event.tag_read_event
	JOIN event.event_description
	ON   tag_read_event.event_desc_id = event_description.event_desc_id
	JOIN event.tag_type
	ON   tag_read_event.tag_type = tag_type.id
	JOIN iris._tag_reader
	ON   tag_read_event.tag_reader = _tag_reader.name
	LEFT JOIN iris.toll_zone
	ON        _tag_reader.toll_zone = toll_zone.name;
GRANT SELECT ON tag_read_event_view TO PUBLIC;

CREATE FUNCTION event.tag_read_event_view_update() RETURNS TRIGGER AS
	$tag_read_event_view_update$
BEGIN
	UPDATE event.tag_read_event
	   SET trip_id = NEW.trip_id
	 WHERE event_id = OLD.event_id;
	RETURN NEW;
END;
$tag_read_event_view_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_read_event_view_update_trig
    INSTEAD OF UPDATE ON tag_read_event_view
    FOR EACH ROW EXECUTE PROCEDURE event.tag_read_event_view_update();

COMMIT;
