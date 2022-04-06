\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.31.0', '5.32.0');

INSERT INTO iris.system_attribute (name, value)
    VALUES ('weather_sensor_event_purge_days', '14');

-- Add new permissions for administrator
COPY iris.permission (role, resource_n, access_n) FROM stdin;
\.

COMMIT;
