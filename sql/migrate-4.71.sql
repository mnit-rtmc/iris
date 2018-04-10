\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.70.0', '4.71.0');

-- Add group_n to action_plan
ALTER TABLE iris.action_plan ADD COLUMN group_n VARCHAR(16);

DROP VIEW action_plan_view;
CREATE VIEW action_plan_view AS
	SELECT name, description, group_n, sync_actions, sticky, active,
	       default_phase, phase
	FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

COMMIT;
