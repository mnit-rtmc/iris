\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.84.0', '4.85.0');

-- add system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('email_rate_limit_hours', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('price_msg_event_purge_days', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_event_enable', 'true');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('meter_event_enable', 'true');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('alarm_event_purge_days', '0');

COMMIT;
