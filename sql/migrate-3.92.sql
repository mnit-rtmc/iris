\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.92.0' WHERE name = 'database_version';

CREATE TABLE iris.action_plan (
	name VARCHAR(8) PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	active BOOLEAN NOT NULL,
	deployed BOOLEAN NOT NULL
);

CREATE TABLE iris.time_action (
	name VARCHAR(12) PRIMARY KEY,
	action_plan VARCHAR(8) NOT NULL REFERENCES iris.action_plan,
	minute SMALLINT NOT NULL,
	deploy BOOLEAN NOT NULL
);

CREATE TABLE iris.dms_action (
	name VARCHAR(12) PRIMARY KEY,
	action_plan VARCHAR(8) NOT NULL REFERENCES iris.action_plan,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	on_deploy BOOLEAN NOT NULL,
	quick_message VARCHAR(20) NOT NULL REFERENCES iris.quick_message,
	priority INTEGER NOT NULL
);
