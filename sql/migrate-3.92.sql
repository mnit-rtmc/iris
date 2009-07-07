\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.92.0' WHERE name = 'database_version';

DROP VIEW sign_text_view;

CREATE TABLE iris.sign_group (
	name VARCHAR(16) PRIMARY KEY,
	local BOOLEAN NOT NULL
);

CREATE TABLE iris.dms_sign_group (
	name VARCHAR(24) PRIMARY KEY,
	dms VARCHAR(10) NOT NULL REFERENCES iris._dms,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group
);

CREATE TABLE iris.sign_text (
	name VARCHAR(20) PRIMARY KEY,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	line smallint NOT NULL,
	message VARCHAR(24) NOT NULL,
	priority smallint NOT NULL,
	CONSTRAINT sign_text_line CHECK ((line >= 1) AND (line <= 12)),
	CONSTRAINT sign_text_priority CHECK
		((priority >= 1) AND (priority <= 99))
);

INSERT INTO iris.sign_group (name, local)
	(SELECT name, local FROM sign_group);
INSERT INTO iris.dms_sign_group (name, dms, sign_group)
	(SELECT name, dms, sign_group FROM dms_sign_group);
INSERT INTO iris.sign_text (name, sign_group, line, message, priority)
	(SELECT name, sign_group, line, message, priority FROM sign_text);

DROP TABLE sign_group;
DROP TABLE dms_sign_group;
DROP TABLE sign_text;

CREATE TABLE iris.action_plan (
	name VARCHAR(16) PRIMARY KEY,
	description VARCHAR(64) NOT NULL,
	active BOOLEAN NOT NULL,
	deployed BOOLEAN NOT NULL
);

CREATE TABLE iris.time_action (
	name VARCHAR(20) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	minute SMALLINT NOT NULL,
	deploy BOOLEAN NOT NULL
);

CREATE TABLE iris.dms_action (
	name VARCHAR(20) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	on_deploy BOOLEAN NOT NULL,
	quick_message VARCHAR(20) REFERENCES iris.quick_message,
	priority INTEGER NOT NULL
);

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, message, priority
	FROM iris.dms_sign_group dsg
	JOIN iris.sign_group sg ON dsg.sign_group = sg.name
	JOIN iris.sign_text st ON sg.name = st.sign_group;
GRANT SELECT ON sign_text_view TO PUBLIC;
