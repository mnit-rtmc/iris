\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Rename geo_locs which don't match controller names
INSERT INTO iris.geo_loc (
    name, resource_n, roadway, road_dir, cross_street, cross_dir, cross_mod,
    lat, lon, landmark
) (
    SELECT c.name, resource_n, roadway, road_dir, cross_street, cross_dir,
           cross_mod, lat, lon, landmark
    FROM iris.controller c
    JOIN iris.geo_loc g ON c.geo_loc = g.name
    WHERE c.name != c.geo_loc
);

UPDATE iris.controller SET geo_loc = name WHERE name != geo_loc;

DELETE FROM iris.geo_loc
WHERE resource_n = 'controller' AND name NOT IN (
    SELECT geo_loc FROM iris.controller
);

COMMIT;
