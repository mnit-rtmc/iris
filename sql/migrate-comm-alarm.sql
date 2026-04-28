\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Change alarm base resource to system_attribute
UPDATE iris.resource_type SET base = 'system_attribute' WHERE name = 'alarm';

-- Remove controller as a base resource; use comm_config instead
UPDATE iris.resource_type SET base = NULL WHERE name = 'comm_config';

UPDATE iris.permission
    SET base_resource = 'comm_config'
    WHERE base_resource = 'controller';

UPDATE iris.resource_type
    SET base = 'comm_config'
    WHERE name IN ('comm_link', 'controller', 'geo_loc', 'gps', 'modem');

COMMIT;
