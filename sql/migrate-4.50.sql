\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.50.0'
	WHERE name = 'database_version';

-- Add camera condition URLs
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_construction_url', '');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_out_of_service_url', '');

-- Rename holiday table to day_matcher
CREATE TABLE iris.day_matcher (
	name VARCHAR(32) PRIMARY KEY,
	holiday BOOLEAN NOT NULL,
	month INTEGER NOT NULL,
	day INTEGER NOT NULL,
	week INTEGER NOT NULL,
	weekday INTEGER NOT NULL,
	shift INTEGER NOT NULL
);

INSERT INTO iris.day_matcher (name, holiday, month, day, week, weekday, shift)
	(SELECT name, true, month, day, week, weekday, shift FROM iris.holiday);
INSERT INTO iris.day_matcher (name, holiday, month, day, week, weekday, shift)
	VALUES ('Any Day', false, -1, 0, 0, 0, 0);

CREATE TABLE iris.day_plan_day_matcher (
	day_plan VARCHAR(10) NOT NULL REFERENCES iris.day_plan,
	day_matcher VARCHAR(32) NOT NULL REFERENCES iris.day_matcher
);

INSERT INTO iris.day_plan_day_matcher (day_plan, day_matcher)
	(SELECT day_plan, holiday FROM iris.day_plan_holiday);

-- Drop old holiday stuff
DROP TABLE iris.day_plan_holiday;
DROP TABLE iris.holiday;

-- Update sonar_type and privilege tables
INSERT INTO iris.sonar_type (name) VALUES ('day_matcher');
UPDATE iris.privilege SET type_n = 'day_matcher' WHERE type_n = 'holiday';
DELETE FROM iris.sonar_type WHERE name = 'holiday';

-- Add non-holiday day_matchers to all existing day_plans
INSERT INTO iris.day_plan_day_matcher (day_plan, day_matcher)
	(SELECT name, 'Any Day' FROM iris.day_plan);
