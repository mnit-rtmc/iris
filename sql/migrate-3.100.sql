\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.100.0' WHERE name = 'database_version';

ALTER TABLE iris.action_plan ADD COLUMN sync_actions BOOLEAN;
UPDATE iris.action_plan SET sync_actions = false;
ALTER TABLE iris.action_plan ALTER COLUMN sync_actions SET NOT NULL;
