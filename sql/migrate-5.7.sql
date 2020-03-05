\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.6.0', '5.7.0');

-- Reorder inc_impact priorities
UPDATE iris.inc_impact SET description = 'lanes affected' WHERE id = 4;
UPDATE iris.inc_impact SET description = 'left lanes affected' WHERE id = 5;
UPDATE iris.inc_impact SET description = 'right lanes affected' WHERE id = 6;
UPDATE iris.inc_impact SET description = 'center lanes affected' WHERE id = 7;
UPDATE iris.inc_impact SET description = 'both shoulders blocked' WHERE id = 8;
UPDATE iris.inc_impact SET description = 'left shoulder blocked' WHERE id = 9;
UPDATE iris.inc_impact SET description = 'right shoulder blocked' WHERE id = 10;

-- Swap dance
UPDATE iris.inc_advice SET impact = 14 WHERE impact = 10;
UPDATE iris.inc_advice SET impact = 10 WHERE impact = 6;
UPDATE iris.inc_advice SET impact = 6 WHERE impact = 9;
UPDATE iris.inc_advice SET impact = 9 WHERE impact = 5;
UPDATE iris.inc_advice SET impact = 5 WHERE impact = 8;
UPDATE iris.inc_advice SET impact = 8 WHERE impact = 4;
UPDATE iris.inc_advice SET impact = 4 WHERE impact = 7;
UPDATE iris.inc_advice SET impact = 7 WHERE impact = 14;

COMMIT;
