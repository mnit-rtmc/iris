\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.3.0', '5.4.0');

-- Add settings to weather_sensor
DROP VIEW weather_sensor_view;
DROP VIEW iris.weather_sensor;
DROP FUNCTION iris.weather_sensor_insert();
DROP FUNCTION iris.weather_sensor_update();
DROP FUNCTION iris.weather_sensor_delete();

ALTER TABLE iris._weather_sensor ADD COLUMN settings JSONB;

CREATE VIEW iris.weather_sensor AS SELECT
	m.name, geo_loc, controller, pin, notes, settings, sample
	FROM iris._weather_sensor m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.weather_sensor_insert() RETURNS TRIGGER AS
	$weather_sensor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._weather_sensor (name, geo_loc, notes, settings, sample)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.settings, NEW.sample);
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
	       settings = NEW.settings,
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
	SELECT w.name, w.notes, w.settings, w.sample, w.geo_loc, l.roadway,
	       l.road_dir, l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

-- Add weather sensor sample events
CREATE TABLE event.weather_sensor_settings (
	event_id SERIAL PRIMARY KEY,
	event_date TIMESTAMP WITH time zone NOT NULL,
	weather_sensor VARCHAR(20) NOT NULL,
	settings JSONB
);

CREATE TABLE event.weather_sensor_sample (
	event_id SERIAL PRIMARY KEY,
	event_date TIMESTAMP WITH time zone NOT NULL,
	weather_sensor VARCHAR(20) NOT NULL,
	sample JSONB
);

CREATE FUNCTION event.weather_sensor_sample_trig() RETURNS TRIGGER AS
$weather_sensor_sample_trig$
BEGIN
    IF NEW.settings != OLD.settings THEN
        INSERT INTO event.weather_sensor_settings
                   (event_date, weather_sensor, settings)
            VALUES (now(), NEW.name, NEW.settings);
    END IF;
    IF NEW.sample != OLD.sample THEN
        INSERT INTO event.weather_sensor_sample
                   (event_date, weather_sensor, sample)
            VALUES (now(), NEW.name, NEW.sample);
    END IF;
    RETURN NEW;
END;
$weather_sensor_sample_trig$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_sample_trigger
	AFTER UPDATE ON iris._weather_sensor
	FOR EACH ROW EXECUTE PROCEDURE event.weather_sensor_sample_trig();

CREATE VIEW weather_sensor_settings_view AS
	SELECT event_id, event_date, weather_sensor, settings
	FROM event.weather_sensor_settings;
GRANT SELECT ON weather_sensor_settings_view TO PUBLIC;

CREATE VIEW weather_sensor_sample_view AS
	SELECT event_id, event_date, weather_sensor, sample
	FROM event.weather_sensor_sample;
GRANT SELECT ON weather_sensor_sample_view TO PUBLIC;

COMMIT;
