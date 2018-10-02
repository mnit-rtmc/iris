\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.81.0', '4.82.0');

-- Add auto_fail column to detector
ALTER TABLE iris._detector ADD COLUMN auto_fail BOOLEAN;
UPDATE iris._detector SET auto_fail = false;
ALTER TABLE iris._detector ALTER COLUMN auto_fail SET NOT NULL;

DROP VIEW detector_view;
DROP VIEW detector_fail_view;
DROP VIEW detector_event_view;
DROP VIEW detector_label_view;
DROP VIEW iris.detector;
DROP FUNCTION iris.detector_insert();
DROP FUNCTION iris.detector_update();
DROP FUNCTION iris.detector_delete();

CREATE VIEW iris.detector AS
	SELECT det.name, controller, pin, r_node, lane_type, lane_number,
	       abandoned, force_fail, auto_fail, field_length, fake, notes
	FROM iris._detector det
	JOIN iris._device_io d ON det.name = d.name;

CREATE FUNCTION iris.detector_insert() RETURNS TRIGGER AS
	$detector_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._detector
	            (name, r_node, lane_type, lane_number, abandoned,
	             force_fail, auto_fail, field_length, fake, notes)
	     VALUES (NEW.name, NEW.r_node, NEW.lane_type, NEW.lane_number,
	             NEW.abandoned, NEW.force_fail, NEW.auto_fail,
	             NEW.field_length, NEW.fake, NEW.notes);
	RETURN NEW;
END;
$detector_insert$ LANGUAGE plpgsql;

CREATE TRIGGER detector_insert_trig
    INSTEAD OF INSERT ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_insert();

CREATE FUNCTION iris.detector_update() RETURNS TRIGGER AS
	$detector_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._detector
	   SET r_node = NEW.r_node,
	       lane_type = NEW.lane_type,
	       lane_number = NEW.lane_number,
	       abandoned = NEW.abandoned,
	       force_fail = NEW.force_fail,
	       auto_fail = NEW.auto_fail,
	       field_length = NEW.field_length,
	       fake = NEW.fake,
	       notes = NEW.notes
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$detector_update$ LANGUAGE plpgsql;

CREATE TRIGGER detector_update_trig
    INSTEAD OF UPDATE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_update();

CREATE FUNCTION iris.detector_delete() RETURNS TRIGGER AS
	$detector_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$detector_delete$ LANGUAGE plpgsql;

CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_delete();

CREATE VIEW detector_label_view AS
	SELECT d.name AS det_id,
	       iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
	                           d.lane_type, d.lane_number, d.abandoned)
	       AS label
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
	       iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
	       d.lane_type, d.lane_number, d.abandoned) AS label,
	       rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, d.lane_number, d.field_length,
	       ln.description AS lane_type, d.abandoned, d.force_fail,
	       d.auto_fail, c.condition, d.fake, d.notes
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN controller_view c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE VIEW detector_auto_fail_view AS
	SELECT device_id, label, ed.description, count(*)
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id
	GROUP BY device_id, label, ed.description;

INSERT INTO iris.system_attribute (name, value)
	VALUES ('detector_event_purge_days', '90');

COMMIT;
