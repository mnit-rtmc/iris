\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Remove detector as a base resource; use r_node instead
UPDATE iris.resource_type SET base = NULL WHERE name = 'r_node';

UPDATE iris.permission
    SET base_resource = 'r_node'
    WHERE base_resource = 'detector';

UPDATE iris.resource_type
    SET base = 'r_node'
    WHERE name IN ('detector', 'road');

DELETE FROM iris.resource_type WHERE name = 'station';

COMMIT;
