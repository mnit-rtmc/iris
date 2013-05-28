\set ON_ERROR_STOP

-- This SQL script will automatically adjust r_node shift values to "center"
-- the r_nodes across each corridor.  Do not run this script while the IRIS
-- server is online.  Please backup the database before running this script.
--
-- psql tms -f shift_corridors.sql

SET SESSION AUTHORIZATION 'tms';

CREATE TEMP TABLE r_node_corridor AS
	SELECT roadway, road_dir, min(iris.r_node_left(node_type, lanes,
		attach_side, shift)) AS min_left, max(iris.r_node_right(
		node_type, lanes, attach_side, shift)) AS max_right
	FROM iris.r_node JOIN iris.geo_loc ON r_node.geo_loc = geo_loc.name
	GROUP BY roadway, road_dir;
ALTER TABLE r_node_corridor ADD COLUMN adj INTEGER;
UPDATE r_node_corridor SET adj = 5 - min_left - (max_right - min_left) / 2;
ALTER TABLE r_node_corridor ALTER COLUMN adj SET NOT NULL;

UPDATE iris.r_node SET shift = shift + r_node_corridor.adj
    FROM iris.geo_loc, r_node_corridor
    WHERE r_node.geo_loc = geo_loc.name
        AND geo_loc.roadway = r_node_corridor.roadway
        AND geo_loc.road_dir = r_node_corridor.road_dir;

DROP TABLE r_node_corridor;
