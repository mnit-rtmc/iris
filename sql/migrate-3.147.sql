\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.147.0'
	WHERE name = 'database_version';

INSERT INTO iris.system_attribute (name, value) VALUES ('client_units_si', 'true');
DELETE FROM iris.system_attribute WHERE name = 'temp_fahrenheit_enable';
INSERT INTO iris.system_attribute (name, value) VALUES ('rwis_high_wind_speed_kph', 0);
INSERT INTO iris.system_attribute (name, value) VALUES ('rwis_low_visibility_distance_m', 500);
INSERT INTO iris.system_attribute (name, value) VALUES ('rwis_obs_age_limit_secs', 240);
INSERT INTO iris.system_attribute (name, value) VALUES ('rwis_max_valid_wind_speed_kph', 282);

INSERT INTO iris.comm_protocol (id, description) values (25, 'DLI DIN Relay');
