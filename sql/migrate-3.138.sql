\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.138.0'
	WHERE name = 'database_version';

DROP VIEW time_action_view;

ALTER TABLE iris.time_action ALTER COLUMN day_plan DROP NOT NULL;
ALTER TABLE iris.time_action ADD COLUMN sched_date DATE;
ALTER TABLE iris.time_action ADD COLUMN time_of_day TIME WITHOUT TIME ZONE;
UPDATE iris.time_action SET time_of_day = hour_min(minute)::TIME WITHOUT TIME ZONE;
ALTER TABLE iris.time_action ALTER COLUMN time_of_day SET NOT NULL;
ALTER TABLE iris.time_action DROP COLUMN minute;

DROP FUNCTION hour_min(integer);

ALTER TABLE iris.time_action ADD CONSTRAINT time_action_date CHECK (
	((day_plan IS NULL) OR (sched_date IS NULL)) AND
	((day_plan IS NOT NULL) OR (sched_date IS NOT NULL))
);

CREATE VIEW time_action_view AS
	SELECT name, action_plan, day_plan, sched_date, time_of_day, phase
	FROM iris.time_action;
GRANT SELECT ON time_action_view TO PUBLIC;
