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

-- Remove 'lanes blocked' (4) and 'lanes affected' (12) impacts
UPDATE iris.inc_impact SET description = 'both shoulders blocked' WHERE id = 4;
UPDATE iris.inc_advice SET impact = 4 WHERE impact = 5;
UPDATE iris.inc_impact SET description = 'left shoulder blocked' WHERE id = 5;
UPDATE iris.inc_advice SET impact = 5 WHERE impact = 6;
UPDATE iris.inc_impact SET description = 'right shoulder blocked' WHERE id = 6;
UPDATE iris.inc_advice SET impact = 6 WHERE impact = 7;
UPDATE iris.inc_impact SET description = 'all lanes affected' WHERE id = 7;
UPDATE iris.inc_advice SET impact = 7 WHERE impact = 8;
UPDATE iris.inc_impact SET description = 'left lanes affected' WHERE id = 8;
UPDATE iris.inc_advice SET impact = 8 WHERE impact = 9;
UPDATE iris.inc_impact SET description = 'right lanes affected' WHERE id = 9;
UPDATE iris.inc_advice SET impact = 9 WHERE impact = 10;
UPDATE iris.inc_impact SET description = 'center lanes affected' WHERE id = 10;
UPDATE iris.inc_advice SET impact = 10 WHERE impact = 11;
UPDATE iris.inc_impact SET description = 'both shoulders affected' WHERE id = 11;
UPDATE iris.inc_advice SET impact = 11 WHERE impact = 13;
UPDATE iris.inc_impact SET description = 'left shoulder affected' WHERE id = 12;
UPDATE iris.inc_advice SET impact = 12 WHERE impact = 14;
UPDATE iris.inc_impact SET description = 'right shoulder affected' WHERE id = 13;
UPDATE iris.inc_advice SET impact = 13 WHERE impact = 15;
UPDATE iris.inc_impact SET description = 'all free flowing' WHERE id = 14;
UPDATE iris.inc_advice SET impact = 14 WHERE impact = 16;
DELETE FROM iris.inc_impact WHERE id > 14;

COMMIT;
