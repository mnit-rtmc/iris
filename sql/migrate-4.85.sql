\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.84.0', '4.85.0');

-- add system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('email_rate_limit_hours', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('price_message_event_purge_days', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_event_enable', 'true');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('meter_event_enable', 'true');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('alarm_event_purge_days', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('beacon_event_purge_days', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('client_event_purge_days', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('gate_arm_event_purge_days', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('tag_read_event_purge_days', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('sign_event_purge_days', '0');

-- Simplify fake detector specification
UPDATE iris._detector
   SET fake = NULL
 WHERE fake = '';
UPDATE iris._detector
   SET fake = regexp_replace(fake, '[#%].*', '')
 WHERE fake IS NOT NULL;
UPDATE iris._detector
   SET fake = regexp_replace(fake, '+', ' ', 'g')
 WHERE fake IS NOT NULL;

-- Add SierraGX protocol
UPDATE iris.comm_protocol SET description = 'SierraGX' WHERE id = 38;

COMMIT;
