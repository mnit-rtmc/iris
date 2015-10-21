\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.27.0'
	WHERE name = 'database_version';

-- add tollway column to toll_zone
ALTER TABLE iris.toll_zone ADD COLUMN tollway VARCHAR(16);

-- add tollway to toll_zone_view
CREATE OR REPLACE VIEW toll_zone_view AS
	SELECT name, start_id, end_id, tollway
	FROM iris.toll_zone;
GRANT SELECT ON toll_zone_view TO PUBLIC;

-- rename dms_op_status_enable sys attr to device_op_status_enable
UPDATE iris.system_attribute SET name = 'device_op_status_enable'
	WHERE name = 'dms_op_status_enable';

-- add toll_zone column to tag_reader
DROP VIEW tag_reader_view;
DROP VIEW iris.tag_reader;
DROP FUNCTION iris.tag_reader_insert();
DROP FUNCTION iris.tag_reader_update();
DROP FUNCTION iris.tag_reader_delete();

ALTER TABLE iris._tag_reader
    ADD COLUMN toll_zone VARCHAR(20) REFERENCES iris.toll_zone(name);

CREATE VIEW iris.tag_reader AS SELECT
	t.name, geo_loc, controller, pin, notes, toll_zone
	FROM iris._tag_reader t JOIN iris._device_io d ON t.name = d.name;

CREATE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
	$tag_reader_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._tag_reader (name, geo_loc, notes, toll_zone)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone);
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
	       toll_zone = NEW.toll_zone
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
	SELECT t.name, t.notes, t.toll_zone, t.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       t.controller, t.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.tag_reader t
	LEFT JOIN geo_loc_view l ON t.geo_loc = l.name
	LEFT JOIN controller_view ctr ON t.controller = ctr.name;
GRANT SELECT ON tag_reader_view TO PUBLIC;

-- drop old tag_read_event_view
DROP VIEW tag_read_event_view;

-- drop toll_zone and tollway from event.tag_read_event table
ALTER TABLE event.tag_read_event DROP COLUMN toll_zone;
ALTER TABLE event.tag_read_event DROP COLUMN tollway;

-- change tag_read_event_view to use tollway from toll_zone
CREATE VIEW tag_read_event_view AS
	SELECT event_id, event_date, event_description.description,
	       tag_type.description AS tag_type, tag_id, tag_reader,
	       toll_zone, tollway, hov, trip_id
	FROM event.tag_read_event
	JOIN event.event_description
	ON   tag_read_event.event_desc_id = event_description.event_desc_id
	JOIN event.tag_type
	ON   tag_read_event.tag_type = tag_type.id
	JOIN iris.tag_reader
	ON   tag_read_event.tag_reader = tag_reader.name
	LEFT JOIN iris.toll_zone
	ON        tag_reader.toll_zone = toll_zone.name;
GRANT SELECT ON tag_read_event_view TO PUBLIC;
