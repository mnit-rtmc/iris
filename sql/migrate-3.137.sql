\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.137.0'
	WHERE name = 'database_version';

ALTER TABLE iris.holiday DROP COLUMN period;

CREATE TABLE iris.plan_phase (
	name VARCHAR(12) PRIMARY KEY,
	hold_time INTEGER NOT NULL,
	next_phase VARCHAR(12) REFERENCES iris.plan_phase
);

INSERT INTO iris.plan_phase (name, hold_time)
	(SELECT description, 0 FROM iris.plan_state);
UPDATE iris.plan_phase SET hold_time = deploying_secs
	FROM iris.action_plan
	WHERE plan_phase.name = 'deploying' AND deploying_secs > 0;
UPDATE iris.plan_phase SET next_phase = 'deployed'
	WHERE name = 'deploying';
UPDATE iris.plan_phase SET hold_time = undeploying_secs
	FROM iris.action_plan
	WHERE plan_phase.name = 'undeploying' AND undeploying_secs > 0;
UPDATE iris.plan_phase SET next_phase = 'undeployed'
	WHERE name = 'undeploying';

DROP VIEW time_action_view;
DROP VIEW action_plan_view;

ALTER TABLE iris.action_plan DROP COLUMN deploying_secs;
ALTER TABLE iris.action_plan DROP COLUMN undeploying_secs;
ALTER TABLE iris.action_plan
	ADD COLUMN default_phase VARCHAR(12) REFERENCES iris.plan_phase;
ALTER TABLE iris.action_plan
	ADD COLUMN phase VARCHAR(12) REFERENCES iris.plan_phase;
UPDATE iris.action_plan SET default_phase = 'undeployed';
UPDATE iris.action_plan SET phase = plan_state.description
	FROM iris.plan_state WHERE state = id;
ALTER TABLE iris.action_plan ALTER COLUMN default_phase SET NOT NULL;
ALTER TABLE iris.action_plan ALTER COLUMN phase SET NOT NULL;
ALTER TABLE iris.action_plan DROP COLUMN state;

ALTER TABLE iris.time_action
	ADD COLUMN phase VARCHAR(12) REFERENCES iris.plan_phase;
UPDATE iris.time_action SET phase = 'undeployed' WHERE deploy = false;
UPDATE iris.time_action SET phase = 'deployed' WHERE deploy = true;
ALTER TABLE iris.time_action ALTER COLUMN phase SET NOT NULL;
ALTER TABLE iris.time_action DROP COLUMN deploy;

ALTER TABLE iris.dms_action
	ADD COLUMN phase VARCHAR(12) REFERENCES iris.plan_phase;
UPDATE iris.dms_action SET phase = description
	FROM iris.plan_state WHERE state = id;
ALTER TABLE iris.dms_action ALTER COLUMN phase SET NOT NULL;
ALTER TABLE iris.dms_action DROP COLUMN state;

ALTER TABLE iris.lane_action
	ADD COLUMN phase VARCHAR(12) REFERENCES iris.plan_phase;
UPDATE iris.lane_action SET phase = description
	FROM iris.plan_state WHERE state = id;
ALTER TABLE iris.lane_action ALTER COLUMN phase SET NOT NULL;
ALTER TABLE iris.lane_action DROP COLUMN state;

ALTER TABLE iris.meter_action
	ADD COLUMN phase VARCHAR(12) REFERENCES iris.plan_phase;
UPDATE iris.meter_action SET phase = description
	FROM iris.plan_state WHERE state = id;
ALTER TABLE iris.meter_action ALTER COLUMN phase SET NOT NULL;
ALTER TABLE iris.meter_action DROP COLUMN state;

DROP TABLE iris.plan_state;

CREATE VIEW action_plan_view AS
	SELECT name, description, sync_actions, active, default_phase, phase
	FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

CREATE VIEW time_action_view AS
	SELECT name, action_plan, day_plan, hour_min(minute) AS time_of_day,
	phase
	FROM iris.time_action;
GRANT SELECT ON time_action_view TO PUBLIC;

INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d)
	VALUES ('prv_phs0', 'policy_admin', 'plan_phase(/.*)?', true, false,
	false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d)
	VALUES ('prv_phs1', 'policy_admin', 'plan_phase/.*', false, true,
	true, true);
