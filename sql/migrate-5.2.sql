\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.1.0', '5.2.0');

-- Add incident_clear_advice system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('incident_clear_advice_multi', 'JUST CLEARED');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('incident_clear_advice_abbrev', 'CLEARED');

-- Drop cleared column from incident advice table/view
DELETE FROM iris.inc_advice WHERE cleared = 't';
DROP VIEW inc_advice_view;
ALTER TABLE iris.inc_advice DROP COLUMN cleared;
CREATE VIEW inc_advice_view AS
	SELECT a.name, imp.description AS impact, lt.description AS lane_type,
	       rng.description AS range, impacted_lanes, open_lanes, multi,
	       abbrev
	FROM iris.inc_advice a
	LEFT JOIN iris.inc_impact imp ON a.impact = imp.id
	LEFT JOIN iris.inc_range rng ON a.range = rng.id
	LEFT JOIN iris.lane_type lt ON a.lane_type = lt.id;
GRANT SELECT ON inc_advice_view TO PUBLIC;

-- Updated incident impacts
UPDATE iris.inc_impact SET description = 'lanes blocked' WHERE id = 0;
UPDATE iris.inc_impact SET description = 'lanes affected' WHERE id = 7;
UPDATE iris.inc_impact SET description = 'free flowing' WHERE id = 14;

-- Add lane use device purpose
INSERT INTO iris.device_purpose (id, description) VALUES ('6', 'lane use');

-- Add FAIL DOMAIN event type
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (207, 'Client FAIL DOMAIN');

COMMIT;
