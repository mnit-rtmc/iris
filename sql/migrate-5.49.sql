\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.48.0', '5.49.0');

-- Reserve protocol values
INSERT INTO iris.comm_protocol (id, description) VALUES (44, 'GPS Digi WR');
INSERT INTO iris.comm_protocol (id, description) VALUES (45, 'ONVIF PTZ');

-- DELETE camera_preset_store_enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'camera_preset_store_enable';

COMMIT;
