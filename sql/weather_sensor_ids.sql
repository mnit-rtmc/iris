# Add new columns
alter table iris._weather_sensor add column site_id varchar(20);
alter table iris._weather_sensor add column alt_id varchar(20);

# Update views, triggers, functions
drop view weather_sensor_view;
drop view  iris.weather_sensor ;

CREATE VIEW iris.weather_sensor AS SELECT
        m.name, site_id, alt_id, geo_loc, controller, pin, notes, settings, sample
        FROM iris._weather_sensor m JOIN iris._device_io d ON m.name = d.name;

drop function iris.weather_sensor_insert;
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

CREATE VIEW weather_sensor_view AS
        SELECT w.name, w.site_id, w.alt_id, w.notes, w.settings, w.sample, w.geo_loc,
               l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
               l.landmark, l.lat, l.lon, l.corridor, l.location,
               w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
        FROM iris.weather_sensor w
        LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
        LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

CREATE TRIGGER weather_sensor_insert_trig
    INSTEAD OF INSERT ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_insert();

drop function iris.weather_sensor_update;
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
               sample = NEW.sample,
               site_id = NEW.site_id,
               alt_id = NEW.alt_id
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

