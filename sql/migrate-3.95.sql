\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.95.0' WHERE name = 'database_version';

CREATE TABLE iris.r_node_type (
	n_type integer PRIMARY KEY,
	name VARCHAR(12) NOT NULL
);

CREATE TABLE iris.r_node_transition (
	n_transition integer PRIMARY KEY,
	name VARCHAR(12) NOT NULL
);

INSERT INTO iris.r_node_type (n_type, name)
	(SELECT n_type, name FROM r_node_type);
INSERT INTO iris.r_node_transition (n_transition, name)
	(SELECT n_transition, name FROM r_node_transition);

ALTER TABLE iris.r_node DROP CONSTRAINT r_node_node_type_fkey;
ALTER TABLE iris.r_node DROP CONSTRAINT r_node_transition_fkey;

ALTER TABLE iris.r_node ADD CONSTRAINT r_node_node_type_fkey
	FOREIGN KEY (node_type) REFERENCES iris.r_node_type;
ALTER TABLE iris.r_node ADD CONSTRAINT r_node_transition_fkey
	FOREIGN KEY (transition) REFERENCES iris.r_node_transition;

DROP VIEW r_node_view;

CREATE VIEW r_node_view AS
	SELECT n.name, freeway, free_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, tr.name AS transition,
	n.lanes, n.attach_side, n.shift, n.station_id, n.speed_limit, n.notes
	FROM iris.r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN iris.r_node_type nt ON n.node_type = nt.n_type
	JOIN iris.r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

DROP TABLE r_node_type;
DROP TABLE r_node_transition;
