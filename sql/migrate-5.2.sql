\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.1.0', '5.2.0');

-- Add incident_clear_advice system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('incident_clear_advice_multi', 'JUST CLEARED');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('incident_clear_advice_abbrev', 'CLEARED');

COMMIT;
