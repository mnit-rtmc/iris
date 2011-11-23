\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.139.0'
	WHERE name = 'database_version';

ALTER TABLE iris.action_plan ADD COLUMN sticky BOOLEAN;
UPDATE iris.action_plan SET sticky = sync_actions;
ALTER TABLE iris.action_plan ALTER COLUMN sticky SET NOT NULL;

DROP VIEW action_plan_view;
CREATE VIEW action_plan_view AS
	SELECT name, description, sync_actions, sticky, active, default_phase,
		phase
	FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;
