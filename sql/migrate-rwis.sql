\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

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
    ('RWIS_slippery_1', 'SLIPPERY[nl]ROAD[nl]DETECTED[np]USE[nl]CAUTION', false),
    ('RWIS_slippery_2', 'SLIPPERY[nl]ROAD[nl]DETECTED[np]REDUCE[nl]SPEED', false),
    ('RWIS_slippery_3', 'ICE[nl]DETECTED[np]REDUCE[nl]SPEED', false),
    ('RWIS_windy_1', 'WIND GST[nl]>40 MPH[nl]DETECTED[np]USE[nl]CAUTION', false),
    ('RWIS_windy_2', 'WIND GST[nl]>60 MPH[nl]DETECTED[np]REDUCE[nl]SPEED', false),
    ('RWIS_visibility_1', 'REDUCED[nl]VISBLITY[nl]DETECTED[np]USE[nl]CAUTION', false),
    ('RWIS_visibility_2', 'LOW[nl]VISBLITY[nl]DETECTED[np]REDUCE[nl]SPEED', false);

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
    ('rwis_visibility_2_m', '402');

COMMIT;
