\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.70.0', '5.71.0');

INSERT INTO iris.system_attribute (name, value)
    VALUES ('incident_max_sign_miles', '0.0');

DELETE FROM iris.system_attribute WHERE name = 'uptime_log_enable';

COMMIT;
