\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.37.0', '5.38.0');

-- Remove DMS query message enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_querymsg_enable';

-- Add VSL (variable speed limit) device purpose
INSERT INTO iris.device_purpose (id, description) VALUES ('7', 'VSL');

COMMIT;
