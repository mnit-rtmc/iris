\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.7.0'
	WHERE name = 'database_version';

CREATE FUNCTION iris.r_node_left(INTEGER, INTEGER, BOOLEAN, INTEGER)
	RETURNS INTEGER AS '
DECLARE node_type ALIAS FOR $1;
DECLARE lanes ALIAS FOR $2;
DECLARE attach_side ALIAS FOR $3;
DECLARE shift ALIAS FOR $4;
BEGIN
	IF attach_side = TRUE THEN
		RETURN shift;
	END IF;
	IF node_type = 0 THEN
		RETURN shift - lanes;
	END IF;
	RETURN shift;
END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION iris.r_node_right(INTEGER, INTEGER, BOOLEAN, INTEGER)
	RETURNS INTEGER AS '
DECLARE node_type ALIAS FOR $1;
DECLARE lanes ALIAS FOR $2;
DECLARE attach_side ALIAS FOR $3;
DECLARE shift ALIAS FOR $4;
BEGIN
	IF attach_side = FALSE THEN
		RETURN shift;
	END IF;
	IF node_type = 0 THEN
		RETURN shift + lanes;
	END IF;
	RETURN shift;
END;'
LANGUAGE PLPGSQL;

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

ALTER TABLE iris.r_node ADD CONSTRAINT left_edge_ck
	CHECK (iris.r_node_left(node_type, lanes, attach_side, shift) >= 1);
ALTER TABLE iris.r_node ADD CONSTRAINT right_edge_ck
	CHECK (iris.r_node_right(node_type, lanes, attach_side, shift) <= 9);

ALTER TABLE iris.r_node ADD COLUMN abandoned BOOLEAN;
UPDATE iris.r_node SET abandoned = FALSE;
ALTER TABLE iris.r_node ALTER COLUMN abandoned SET NOT NULL;
ALTER TABLE iris.r_node ADD CONSTRAINT active_ck
	CHECK (active = FALSE OR abandoned = FALSE);

DROP VIEW r_node_view;
CREATE VIEW r_node_view AS
	SELECT n.name, roadway, road_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, n.above,
	tr.name AS transition, n.lanes, n.attach_side, n.shift, n.active,
	n.abandoned, n.station_id, n.speed_limit, n.notes
	FROM iris.r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN iris.r_node_type nt ON n.node_type = nt.n_type
	JOIN iris.r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;
