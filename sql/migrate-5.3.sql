\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.2.0', '5.3.0');

-- Blocked lanes function
CREATE OR REPLACE FUNCTION event.incident_blocked_lanes(TEXT)
	RETURNS INTEGER AS $incident_blocked_lanes$
DECLARE
	impact ALIAS FOR $1;
	imp TEXT;
	lanes INTEGER;
BEGIN
	lanes = length(impact) - 2;
	IF lanes > 0 THEN
		imp = substring(impact FROM 2 FOR lanes);
		RETURN lanes - length(replace(imp, '!', ''));
	ELSE
		RETURN 0;
	END IF;
END;
$incident_blocked_lanes$ LANGUAGE plpgsql;

-- Blocked shoulders function
CREATE OR REPLACE FUNCTION event.incident_blocked_shoulders(TEXT)
	RETURNS INTEGER AS $incident_blocked_shoulders$
DECLARE
	impact ALIAS FOR $1;
	len INTEGER;
	imp TEXT;
BEGIN
	len = length(impact);
	IF len > 2 THEN
		imp = substring(impact FROM 1 FOR 1) ||
		      substring(impact FROM len FOR 1);
		RETURN 2 - length(replace(imp, '!', ''));
	ELSE
		RETURN 0;
	END IF;
END;
$incident_blocked_shoulders$ LANGUAGE plpgsql;

-- Add blocked_lanes / blocked_shoulders to incident_view
DROP VIEW incident_view;
CREATE VIEW incident_view AS
    SELECT event_id, name, event_date, ed.description, road, d.direction,
           impact, event.incident_blocked_lanes(impact) AS blocked_lanes,
           event.incident_blocked_shoulders(impact) AS blocked_shoulders,
           cleared, confirmed, camera, ln.description AS lane_type, detail,
           replaces, lat, lon
    FROM event.incident i
    LEFT JOIN event.event_description ed ON i.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_type ln ON i.lane_type = ln.id;
GRANT SELECT ON incident_view TO PUBLIC;

-- Add allow_retain column to road_affix
ALTER TABLE iris.road_affix ADD COLUMN allow_retain BOOLEAN;
UPDATE iris.road_affix SET allow_retain = 'f';
UPDATE iris.road_affix SET allow_retain = 't'
	WHERE name IN ('AVE', 'DR', 'RD', 'ST');
ALTER TABLE iris.road_affix ALTER COLUMN allow_retain SET NOT NULL;

-- Add sample to weather_sensor
DROP VIEW weather_sensor_view;
DROP VIEW iris.weather_sensor;
DROP FUNCTION iris.weather_sensor_insert();
DROP FUNCTION iris.weather_sensor_update();
DROP FUNCTION iris.weather_sensor_delete();

ALTER TABLE iris._weather_sensor ADD COLUMN sample JSONB;

CREATE VIEW iris.weather_sensor AS SELECT
	m.name, geo_loc, controller, pin, notes, sample
	FROM iris._weather_sensor m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.weather_sensor_insert() RETURNS TRIGGER AS
	$weather_sensor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._weather_sensor (name, geo_loc, notes, sample)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.sample);
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
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       sample = NEW.sample
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$weather_sensor_update$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_update_trig
    INSTEAD OF UPDATE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_update();

CREATE FUNCTION iris.weather_sensor_delete() RETURNS TRIGGER AS
	$weather_sensor_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$weather_sensor_delete$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_delete();

CREATE VIEW weather_sensor_view AS
	SELECT w.name, w.notes, w.sample, w.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

COMMIT;
