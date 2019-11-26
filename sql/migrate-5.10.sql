\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.9.0', '5.10.0');

-- DROP abbrev from inc_descriptor
CREATE OR REPLACE VIEW inc_descriptor_view AS
	SELECT id.name, ed.description AS event_description, detail,
	       lt.description AS lane_type, multi
	FROM iris.inc_descriptor id
	JOIN event.event_description ed ON id.event_desc_id = ed.event_desc_id
	LEFT JOIN iris.lane_type lt ON id.lane_type = lt.id;
GRANT SELECT ON inc_descriptor_view TO PUBLIC;

ALTER TABLE iris.inc_descriptor DROP COLUMN abbrev;

-- DROP abbrev from inc_locator
CREATE OR REPLACE VIEW inc_locator_view AS
	SELECT il.name, rng.description AS range, branched, picked, multi
	FROM iris.inc_locator il
	LEFT JOIN iris.inc_range rng ON il.range = rng.id;
GRANT SELECT ON inc_locator_view TO PUBLIC;

ALTER TABLE iris.inc_locator DROP COLUMN abbrev;

-- DROP abbrev from inc_advice
CREATE OR REPLACE VIEW inc_advice_view AS
	SELECT a.name, imp.description AS impact, lt.description AS lane_type,
	       rng.description AS range, open_lanes, impacted_lanes, multi
	FROM iris.inc_advice a
	LEFT JOIN iris.inc_impact imp ON a.impact = imp.id
	LEFT JOIN iris.inc_range rng ON a.range = rng.id
	LEFT JOIN iris.lane_type lt ON a.lane_type = lt.id;
GRANT SELECT ON inc_advice_view TO PUBLIC;

ALTER TABLE iris.inc_advice DROP COLUMN abbrev;

-- DELETE incident_clear_advice_abbrev system attribute
DELETE FROM iris.system_attribute WHERE name = 'incident_clear_advice_abbrev';

COMMIT;
