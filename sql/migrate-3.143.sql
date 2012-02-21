\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.143.0'
	WHERE name = 'database_version';

INSERT INTO iris.meter_algorithm (id, description)
	VALUES (3, 'K Adaptive Metering');

CREATE VIEW meter_action_view AS
	SELECT ramp_meter, ta.phase, time_of_day, day_plan, sched_date
	FROM iris.meter_action ma, iris.action_plan ap, iris.time_action ta
	WHERE ma.action_plan = ap.name
	AND ap.name = ta.action_plan
	AND active = true
	ORDER BY ramp_meter, time_of_day;
GRANT SELECT ON meter_action_view TO PUBLIC;
