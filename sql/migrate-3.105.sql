\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.105.0'
	WHERE name = 'database_version';

CREATE TABLE iris.plan_state (
	id INTEGER PRIMARY KEY,
	description VARCHAR(12) NOT NULL
);

COPY iris.plan_state (id, description) FROM stdin;
0	undeployed
1	deploying
2	deployed
3	undeploying
\.

ALTER TABLE iris.action_plan ADD COLUMN state INTEGER
	REFERENCES iris.plan_state;
UPDATE iris.action_plan SET state = 0;
UPDATE iris.action_plan SET state = 2 WHERE deployed = true;
ALTER TABLE iris.action_plan ALTER COLUMN state SET NOT NULL;
ALTER TABLE iris.action_plan DROP COLUMN deployed;

ALTER TABLE iris.dms_action ADD COLUMN state INTEGER
	REFERENCES iris.plan_state;
UPDATE iris.dms_action SET state = 0;
UPDATE iris.dms_action SET state = 2 WHERE on_deploy = true;
ALTER TABLE iris.dms_action ALTER COLUMN state SET NOT NULL;
ALTER TABLE iris.dms_action DROP COLUMN on_deploy;

ALTER TABLE iris.lane_action ADD COLUMN state INTEGER
	REFERENCES iris.plan_state;
UPDATE iris.lane_action SET state = 0;
UPDATE iris.lane_action SET state = 2 WHERE on_deploy = true;
ALTER TABLE iris.lane_action ALTER COLUMN state SET NOT NULL;
ALTER TABLE iris.lane_action DROP COLUMN on_deploy;

ALTER TABLE iris.action_plan ADD COLUMN deploying_secs INTEGER;
UPDATE iris.action_plan SET deploying_secs = 0;
ALTER TABLE iris.action_plan ALTER COLUMN deploying_secs SET NOT NULL;
ALTER TABLE iris.action_plan ADD COLUMN undeploying_secs INTEGER;
UPDATE iris.action_plan SET undeploying_secs = 0;
ALTER TABLE iris.action_plan ALTER COLUMN undeploying_secs SET NOT NULL;

ALTER TABLE iris.dms_action ADD COLUMN a_priority INTEGER;
UPDATE iris.dms_action SET a_priority = priority;
ALTER TABLE iris.dms_action ALTER COLUMN a_priority SET NOT NULL;
ALTER TABLE iris.dms_action ADD COLUMN r_priority INTEGER;
UPDATE iris.dms_action SET r_priority = priority;
ALTER TABLE iris.dms_action ALTER COLUMN r_priority SET NOT NULL;
ALTER TABLE iris.dms_action DROP COLUMN priority;

CREATE TABLE iris.day_plan (
	name VARCHAR(10) PRIMARY KEY
);

CREATE TABLE iris.day_plan_holiday (
	day_plan VARCHAR(10) NOT NULL REFERENCES iris.day_plan,
	holiday VARCHAR(32) NOT NULL REFERENCES iris.holiday
);

INSERT INTO iris.day_plan (name) VALUES ('DEFAULT');
INSERT INTO iris.day_plan_holiday (day_plan, holiday)
	(SELECT 'DEFAULT', name FROM iris.holiday);

ALTER TABLE iris.time_action ADD COLUMN day_plan VARCHAR(10)
	REFERENCES iris.day_plan;
UPDATE iris.time_action SET day_plan = 'DEFAULT';
ALTER TABLE iris.time_action ALTER COLUMN day_plan SET NOT NULL;
