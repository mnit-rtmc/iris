\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.0.0', '5.1.0');

-- Remove cleared column from view
DROP VIEW inc_descriptor_view;
CREATE VIEW inc_descriptor_view AS
	SELECT id.name, ed.description AS event_description, detail,
	       lt.description AS lane_type, multi, abbrev
	FROM iris.inc_descriptor id
	JOIN event.event_description ed ON id.event_desc_id = ed.event_desc_id
	LEFT JOIN iris.lane_type lt ON id.lane_type = lt.id;
GRANT SELECT ON inc_descriptor_view TO PUBLIC;

-- Drop cleared column
ALTER TABLE iris.inc_descriptor DROP COLUMN cleared;

COMMIT;
