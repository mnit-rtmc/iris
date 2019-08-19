\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('4.89.0', '5.0.0');

ALTER TABLE iris.inc_locator RENAME COLUMN pickable TO picked;

CREATE TABLE iris.inc_impact (
	id INTEGER PRIMARY KEY,
	description VARCHAR(24) NOT NULL
);

COPY iris.inc_impact (id, description) FROM stdin;
0	all lanes blocked
1	left lanes blocked
2	right lanes blocked
3	center lanes blocked
4	lanes blocked
5	both shoulders blocked
6	left shoulder blocked
7	right shoulder blocked
8	all lanes affected
9	left lanes affected
10	right lanes affected
11	center lanes affected
12	lanes affected
13	both shoulders affected
14	left shoulder affected
15	right shoulder affected
16	all free flowing
\.

-- Update incident range look-up table
UPDATE iris.inc_range SET description = 'ahead' WHERE id = 0;
UPDATE iris.inc_range SET description = 'near' WHERE id = 1;
UPDATE iris.inc_range SET description = 'middle' WHERE id = 2;
INSERT INTO iris.inc_range (id, description) VALUES (3, 'far');

-- Adjust ranges in incident locator table
UPDATE iris.inc_locator SET range = 3 WHERE range = 2;
UPDATE iris.inc_locator SET range = 2 WHERE range = 1;
UPDATE iris.inc_locator SET range = 1 WHERE range = 0;

-- Adjust ranges in incident advice table
UPDATE iris.inc_advice SET range = 3 WHERE range = 2;
UPDATE iris.inc_advice SET range = 2 WHERE range = 1;
UPDATE iris.inc_advice SET range = 1 WHERE range = 0;

-- Add impacted_lanes and open lanes to incident advice table
ALTER TABLE iris.inc_advice ADD COLUMN impacted_lanes INTEGER;
ALTER TABLE iris.inc_advice ADD COLUMN open_lanes INTEGER;

UPDATE iris.inc_advice SET impacted_lanes = 1 WHERE multi LIKE '%LANE CLOSE%';
UPDATE iris.inc_advice SET impacted_lanes = 2 WHERE multi LIKE '%2 LANES CLOSE%';
UPDATE iris.inc_advice SET impacted_lanes = 3 WHERE multi LIKE '%3 LANES CLOSE%';
UPDATE iris.inc_advice SET impacted_lanes = 4 WHERE multi LIKE '%4 LANES CLOSE%';

UPDATE iris.inc_advice SET impacted_lanes = 1 WHERE multi = 'IN LEFT LANE';
UPDATE iris.inc_advice SET impacted_lanes = 2 WHERE multi LIKE '%IN LEFT 2%';
UPDATE iris.inc_advice SET impacted_lanes = 3 WHERE multi LIKE '%IN LEFT 3%';
UPDATE iris.inc_advice SET impacted_lanes = 4 WHERE multi LIKE '%IN LEFT 4%';

UPDATE iris.inc_advice SET impacted_lanes = 1 WHERE multi = 'IN RIGHT LANE';
UPDATE iris.inc_advice SET impacted_lanes = 2 WHERE multi LIKE '%IN RIGHT 2%';
UPDATE iris.inc_advice SET impacted_lanes = 3 WHERE multi LIKE '%IN RIGHT 3%';
UPDATE iris.inc_advice SET impacted_lanes = 4 WHERE multi LIKE '%IN RIGHT 4%';

UPDATE iris.inc_advice SET impacted_lanes = 1 WHERE multi = 'IN CENTER LANE';
UPDATE iris.inc_advice SET impacted_lanes = 1, open_lanes = 0
	WHERE multi = 'IN LANE';
UPDATE iris.inc_advice SET impacted_lanes = 2, open_lanes = 0
	WHERE multi = 'IN BOTH LANES';

UPDATE iris.inc_advice SET open_lanes = 1 WHERE multi LIKE '%REDUCED TO 1%';
UPDATE iris.inc_advice SET open_lanes = 2 WHERE multi LIKE '%REDUCED TO 2%';
UPDATE iris.inc_advice SET open_lanes = 3 WHERE multi LIKE '%REDUCED TO 3%';
UPDATE iris.inc_advice SET open_lanes = 4 WHERE multi LIKE '%REDUCED TO 4%';

ALTER TABLE iris.inc_advice DROP COLUMN impact;
ALTER TABLE iris.inc_advice ADD COLUMN impact INTEGER
	REFERENCES iris.inc_impact(id);
UPDATE iris.inc_advice SET impact = 0;
UPDATE iris.inc_advice SET impact = 1 WHERE multi LIKE 'LEFT % CLOSED';
UPDATE iris.inc_advice SET impact = 2 WHERE multi LIKE 'RIGHT % CLOSED';
UPDATE iris.inc_advice SET impact = 3 WHERE multi LIKE 'CENTER % CLOSED';
UPDATE iris.inc_advice SET impact = 4 WHERE multi = 'LANE CLOSED';
UPDATE iris.inc_advice SET impact = 4 WHERE multi LIKE 'REDUCED TO %';
UPDATE iris.inc_advice SET impact = 5 WHERE multi = 'ON BOTH SHOULDERS';
UPDATE iris.inc_advice SET impact = 6 WHERE multi = 'ON LEFT SHOULDER';
UPDATE iris.inc_advice SET impact = 7 WHERE multi = 'ON RIGHT SHOULDER';
UPDATE iris.inc_advice SET impact = 8 WHERE multi = 'IN ALL LANES';
UPDATE iris.inc_advice SET impact = 9 WHERE multi LIKE 'IN LEFT %';
UPDATE iris.inc_advice SET impact = 10 WHERE multi LIKE 'IN RIGHT %';
UPDATE iris.inc_advice SET impact = 11 WHERE multi LIKE 'IN CENTER %';
ALTER TABLE iris.inc_advice ALTER COLUMN impact SET NOT NULL;

-- Add incident descriptor view
CREATE VIEW inc_descriptor_view AS
	SELECT id.name, ed.description AS event_description, detail,
	       lt.description AS lane_type, cleared, multi, abbrev
	FROM iris.inc_descriptor id
	JOIN event.event_description ed ON id.event_desc_id = ed.event_desc_id
	LEFT JOIN iris.lane_type lt ON id.lane_type = lt.id;
GRANT SELECT ON inc_descriptor_view TO PUBLIC;

-- Add incident locator view
CREATE VIEW inc_locator_view AS
	SELECT il.name, rng.description AS range, branched, picked,
	       multi, abbrev
	FROM iris.inc_locator il
	LEFT JOIN iris.inc_range rng ON il.range = rng.id;
GRANT SELECT ON inc_locator_view TO PUBLIC;

-- Add incident advice view
CREATE VIEW inc_advice_view AS
	SELECT a.name, imp.description AS impact, impacted_lanes, open_lanes,
	       rng.description AS range, lt.description AS lane_type, cleared,
	       multi, abbrev
	FROM iris.inc_advice a
	LEFT JOIN iris.inc_impact imp ON a.impact = imp.id
	LEFT JOIN iris.inc_range rng ON a.range = rng.id
	LEFT JOIN iris.lane_type lt ON a.lane_type = lt.id;
GRANT SELECT ON inc_advice_view TO PUBLIC;

COMMIT;
