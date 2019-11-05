\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.8.0', '5.9.0');

-- Change Junction to Jct in road_modifier table
UPDATE iris.road_modifier SET modifier = 'N Jct' WHERE id = 5;
UPDATE iris.road_modifier SET modifier = 'S Jct' WHERE id = 6;
UPDATE iris.road_modifier SET modifier = 'E Jct' WHERE id = 7;
UPDATE iris.road_modifier SET modifier = 'W Jct' WHERE id = 8;

COMMIT;
