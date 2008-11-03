\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.80.0' WHERE name = 'database_version';

INSERT INTO system_attribute
	SELECT 'meter_green_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'meter_green_time';
INSERT INTO system_attribute
	SELECT 'meter_yellow_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'meter_yellow_time';
INSERT INTO system_attribute
	SELECT 'meter_min_red_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'meter_min_red_time';
INSERT INTO system_attribute
	SELECT 'dms_page_on_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'dms_page_on_time';
INSERT INTO system_attribute
	SELECT 'dms_page_off_secs', round(value / 10.0, 1)::varchar
	FROM system_policy WHERE name = 'dms_page_off_time';
INSERT INTO system_attribute
	SELECT 'incident_ring_1_miles', value::varchar
	FROM system_policy WHERE name = 'ring_radius_0';
INSERT INTO system_attribute
	SELECT 'incident_ring_2_miles', value::varchar
	FROM system_policy WHERE name = 'ring_radius_1';
INSERT INTO system_attribute
	SELECT 'incident_ring_3_miles', value::varchar
	FROM system_policy WHERE name = 'ring_radius_2';

DROP TABLE system_policy;
