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

-- Add rwis_sign table
CREATE TABLE iris.rwis_sign (
    name VARCHAR(32) PRIMARY KEY,
    rwis_conditions VARCHAR(256) NOT NULL DEFAULT '',
    msg_pattern VARCHAR(32)
);

-- Add basic PDMS RWIS message patterns
INSERT INTO iris.msg_pattern (name, multi, flash_beacon, compose_hashtag) VALUES
    ('RWIS_1_Slippery', 'SLIPPERY[nl]ROAD[nl]DETECTED[np]USE[nl]CAUTION', false, NULL),
    ('RWIS_2_ReducedVisib', 'REDUCED[nl]VISBLITY[nl]DETECTED[np]USE[nl]CAUTION', false, NULL),
    ('RWIS_3_Wind40mph', 'WIND GST[nl]>40 MPH[nl]DETECTED[np]USE[nl]CAUTION', false, NULL),
    ('RWIS_4_Wind60mph', 'WIND GST[nl]>60 MPH[nl]DETECTED[np]REDUCE[nl]SPEED', false, NULL),
    ('RWIS_5_VerySlippery', 'SLIPPERY[nl]ROAD[nl]DETECTED[np]REDUCE[nl]SPEED', false, NULL),
    ('RWIS_6_LowVisib', 'LOW[nl]VISBLITY[nl]DETECTED[np]REDUCE[nl]SPEED', false, NULL),
    ('RWIS_7_IceDetected', 'ICE[nl]DETECTED[np]REDUCE[nl]SPEED', false, NULL);

INSERT INTO iris.system_attribute (name, value) VALUES
    ('rwis_cycle_sec', '-1'),
    ('rwis_msg_priority', '9');

COMMIT;
