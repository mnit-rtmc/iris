\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.56.0', '5.57.0');

-- Add dms / weather sensor relation
CREATE TABLE iris.dms_weather_sensor (
    dms VARCHAR(20) NOT NULL REFERENCES iris._dms,
    weather_sensor VARCHAR(20) NOT NULL REFERENCES iris._weather_sensor
);
ALTER TABLE iris.dms_weather_sensor ADD PRIMARY KEY (dms, weather_sensor);

CREATE VIEW dms_weather_sensor_view AS
    SELECT dms, weather_sensor
    FROM iris.dms_weather_sensor;
GRANT SELECT ON dms_weather_sensor_view TO PUBLIC;

-- Add basic PDMS RWIS message patterns
INSERT INTO iris.msg_pattern (name, multi, flash_beacon) VALUES
    ('RWIS_slippery_1', '[rwis_slippery,1]SLIPPERY[nl]ROAD[nl]DETECTED[np]USE[nl]CAUTION', false),
    ('RWIS_slippery_2', '[rwis_slippery,2]SLIPPERY[nl]ROAD[nl]DETECTED[np]REDUCE[nl]SPEED', false),
    ('RWIS_slippery_3', '[rwis_slippery,3]ICE[nl]DETECTED[np]REDUCE[nl]SPEED', false),
    ('RWIS_windy_1', '[rwis_windy,1]WIND GST[nl]>40 MPH[nl]DETECTED[np]USE[nl]CAUTION', false),
    ('RWIS_windy_2', '[rwis_windy,2]WIND GST[nl]>60 MPH[nl]DETECTED[np]REDUCE[nl]SPEED', false),
    ('RWIS_visibility_1', '[rwis_visibility,1]REDUCED[nl]VISBLITY[nl]DETECTED[np]USE[nl]CAUTION', false),
    ('RWIS_visibility_2', '[rwis_visibility,2]LOW[nl]VISBLITY[nl]DETECTED[np]REDUCE[nl]SPEED', false),
    ('RWIS_flooding_1', '[rwis_flooding,1]FLOODING[nl]POSSIBLE[np]USE[nl]CAUTION', false),
    ('RWIS_flooding_2', '[rwis_flooding,2]FLASH[nl]FLOODING[np]USE[nl]CAUTION', false);

-- Delete system attributes for old unfinished RWIS code
DELETE FROM iris.system_attribute WHERE name = 'rwis_high_wind_speed_kph';
DELETE FROM iris.system_attribute WHERE name = 'rwis_low_visibility_distance_m';
DELETE FROM iris.system_attribute WHERE name = 'rwis_max_valid_wind_speed_kph';

-- Increase RWIS observation age limit to 15 minutes
UPDATE iris.system_attribute SET value = '900'
    WHERE name = 'rwis_obs_age_limit_secs';

-- Add RWIS threshold attributes
INSERT INTO iris.system_attribute (name, value) VALUES
    ('rwis_slippery_1_percent', '70'),
    ('rwis_slippery_2_degrees', '0'),
    ('rwis_slippery_3_percent', '60'),
    ('rwis_windy_1_kph', '64'),
    ('rwis_windy_2_kph', '96'),
    ('rwis_visibility_1_m', '1609'),
    ('rwis_visibility_2_m', '402'),
    ('rwis_flooding_1_mm', '6'),
    ('rwis_flooding_2_mm', '8');

-- Drop camera "streamable" column (replaced by hashtags)
DROP VIEW camera_view;
DROP VIEW iris.camera;

ALTER TABLE iris._camera DROP COLUMN streamable;

CREATE VIEW iris.camera AS
    SELECT c.name, geo_loc, controller, pin, notes, cam_num, cam_template,
           encoder_type, enc_address, enc_port, enc_mcast, enc_channel,
           publish, video_loss
    FROM iris._camera c
    JOIN iris.controller_io cio ON c.name = cio.name;

CREATE OR REPLACE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
    $camera_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'camera', NEW.controller, NEW.pin);
    INSERT INTO iris._camera (
        name, geo_loc, notes, cam_num, cam_template, encoder_type, enc_address,
        enc_port, enc_mcast, enc_channel, publish, video_loss
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num, NEW.cam_template,
        NEW.encoder_type, NEW.enc_address, NEW.enc_port, NEW.enc_mcast,
        NEW.enc_channel, NEW.publish, NEW.video_loss
    );
    RETURN NEW;
END;
$camera_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_insert();

CREATE OR REPLACE FUNCTION iris.camera_update() RETURNS TRIGGER AS
    $camera_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._camera
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           cam_num = NEW.cam_num,
           cam_template = NEW.cam_template,
           encoder_type = NEW.encoder_type,
           enc_address = NEW.enc_address,
           enc_port = NEW.enc_port,
           enc_mcast = NEW.enc_mcast,
           enc_channel = NEW.enc_channel,
           publish = NEW.publish,
           video_loss = NEW.video_loss
     WHERE name = OLD.name;
    RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.camera_update();

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW camera_view AS
    SELECT c.name, cam_num, c.cam_template, encoder_type, et.make, et.model,
           et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
           c.publish, c.video_loss, c.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           c.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
    FROM iris.camera c
    LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

COMMIT;
