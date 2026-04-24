\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Remove detector as a base resource; use road instead
UPDATE iris.resource_type SET base = NULL WHERE name = 'road';

UPDATE iris.permission
    SET base_resource = 'road'
    WHERE base_resource = 'detector';

UPDATE iris.resource_type
    SET base = 'road'
    WHERE name IN ('detector', 'map_extent', 'r_node');

DELETE FROM iris.resource_type WHERE name = 'station';

COMMIT;
