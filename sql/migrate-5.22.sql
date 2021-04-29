\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.21.0', '5.22.0');

ALTER TABLE iris.sign_config
    ADD COLUMN exclude_font VARCHAR(16) REFERENCES iris.font;
ALTER TABLE iris.sign_config ADD COLUMN module_width INTEGER;
ALTER TABLE iris.sign_config ADD COLUMN module_height INTEGER;

DROP VIEW dms_view;
DROP VIEW sign_config_view;

CREATE VIEW sign_config_view AS
	SELECT name, face_width, face_height, border_horiz, border_vert,
	       pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
	       char_height, monochrome_foreground, monochrome_background,
	       cs.description AS color_scheme, default_font, exclude_font,
	       module_width, module_height
	FROM iris.sign_config
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, dp.description AS purpose, d.hidden, d.beacon,
	       p.camera, p.preset_num, d.sign_config, d.sign_detail,
	       default_font, exclude_font, override_font, override_foreground,
	       override_background, msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

INSERT INTO cap.event (code, description) VALUES
	('FWW', 'Red Flag Warning');

-- Add site_id and alt_id to weather sensor
DROP VIEW weather_sensor_view;
DROP VIEW iris.weather_sensor;
DROP FUNCTION iris.weather_sensor_insert;
DROP FUNCTION iris.weather_sensor_update;

ALTER TABLE iris._weather_sensor ADD COLUMN site_id VARCHAR(20);
ALTER TABLE iris._weather_sensor ADD COLUMN alt_id VARCHAR(20);

CREATE VIEW iris.weather_sensor AS SELECT
	m.name, site_id, alt_id, geo_loc, controller, pin, notes, settings,
	sample
	FROM iris._weather_sensor m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.weather_sensor_insert() RETURNS TRIGGER AS
	$weather_sensor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._weather_sensor (name, site_id, alt_id, geo_loc, notes,
	                                  settings, sample)
	     VALUES (NEW.name, NEW.site_id, NEW.alt_id, NEW.geo_loc, NEW.notes,
	             NEW.settings, NEW.sample);
	RETURN NEW;
END;
$weather_sensor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_insert_trig
    INSTEAD OF INSERT ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_insert();

CREATE FUNCTION iris.weather_sensor_update() RETURNS TRIGGER AS
	$weather_sensor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._weather_sensor
	   SET site_id = NEW.site_id,
	       alt_id = NEW.alt_id,
	       geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       settings = NEW.settings,
	       sample = NEW.sample
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$weather_sensor_update$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_update_trig
    INSTEAD OF UPDATE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_update();

CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

CREATE VIEW weather_sensor_view AS
	SELECT w.name, w.site_id, w.alt_id, w.notes, w.settings, w.sample,
	       w.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location,
	       w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

UPDATE iris.comm_protocol SET description = 'RTMS G4 vlog' WHERE id = 18;
UPDATE iris.comm_protocol SET description = 'SmartSensor 125 vlog'
	WHERE id = 19;

UPDATE iris.lane_type SET dcode = 'C' WHERE id = 3;
UPDATE iris.lane_type SET dcode = 'T' WHERE id = 15;
UPDATE iris.lane_type SET dcode = 'K' WHERE id = 17;

CREATE FUNCTION iris.detector_notify() RETURNS TRIGGER AS
	$detector_notify$
BEGIN
	IF (NEW.auto_fail IS DISTINCT FROM OLD.auto_fail) THEN
		NOTIFY detector, 'auto_fail';
	ELSE
		NOTIFY detector;
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$detector_notify$ LANGUAGE plpgsql;

CREATE TRIGGER detector_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.detector
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.detector_notify();

DROP VIEW detector_view;
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

COMMIT;
