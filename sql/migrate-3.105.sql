\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.105.0'
	WHERE name = 'database_version';

ALTER TABLE iris.action_plan ADD COLUMN state INTEGER;
UPDATE iris.action_plan SET state = 0;
UPDATE iris.action_plan SET state = 2 WHERE deployed = true;
ALTER TABLE iris.action_plan ALTER COLUMN state SET NOT NULL;
ALTER TABLE iris.action_plan DROP COLUMN deployed;

ALTER TABLE iris.dms_action ADD COLUMN state INTEGER;
UPDATE iris.dms_action SET state = 0;
UPDATE iris.dms_action SET state = 2 WHERE on_deploy = true;
ALTER TABLE iris.dms_action ALTER COLUMN state SET NOT NULL;
ALTER TABLE iris.dms_action DROP COLUMN on_deploy;

ALTER TABLE iris.lane_action ADD COLUMN state INTEGER;
UPDATE iris.lane_action SET state = 0;
UPDATE iris.lane_action SET state = 2 WHERE on_deploy = true;
ALTER TABLE iris.lane_action ALTER COLUMN state SET NOT NULL;
ALTER TABLE iris.lane_action DROP COLUMN on_deploy;
