\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.19.0', '5.20.0');

ALTER TABLE event.ipaws_deployer DROP COLUMN lat;
ALTER TABLE event.ipaws_deployer DROP COLUMN lon;
ALTER TABLE event.ipaws_deployer DROP COLUMN area_threshold;
ALTER TABLE event.ipaws_deployer DROP COLUMN sign_group;
ALTER TABLE event.ipaws_deployer DROP COLUMN quick_message;
ALTER TABLE event.ipaws_deployer DROP COLUMN deployed;
ALTER TABLE event.ipaws_deployer DROP COLUMN active;

UPDATE event.ipaws_deployer SET pre_alert_time = 0
	WHERE pre_alert_time IS NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN pre_alert_time SET NOT NULL;

UPDATE event.ipaws_deployer SET post_alert_time = 0
	WHERE post_alert_time IS NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN post_alert_time SET NOT NULL;

UPDATE event.ipaws_deployer SET msg_priority = 5 WHERE msg_priority IS NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN msg_priority SET NOT NULL;

ALTER TABLE event.ipaws_deployer ALTER COLUMN config SET NOT NULL;

UPDATE event.ipaws_deployer SET was_deployed = false WHERE was_deployed IS NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN was_deployed SET NOT NULL;

ALTER TABLE event.ipaws_deployer ALTER COLUMN gen_time SET NOT NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN alert_start SET NOT NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN alert_end SET NOT NULL;

ALTER TABLE event.ipaws_deployer ADD COLUMN alert_state INTEGER;
UPDATE event.ipaws_deployer SET alert_state = 4; -- expired
ALTER TABLE event.ipaws_deployer ALTER COLUMN alert_state SET NOT NULL;

DROP TABLE iris.cap_urgency;

CREATE TABLE iris.cap_urgency (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY iris.cap_urgency (id, description) FROM stdin;
0	unknown
1	past
2	future
3	expected
4	immediate
\.

CREATE TABLE iris.cap_severity (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY iris.cap_severity(id, description) FROM stdin;
0	unknown
1	minor
2	moderate
3	severe
4	extreme
\.

CREATE TABLE iris.cap_certainty (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY iris.cap_certainty(id, description) FROM stdin;
0	unknown
1	unlikely
2	possible
3	likely
4	observed
\.

CREATE TABLE iris.cap_urgency_fld (
	name VARCHAR(24) PRIMARY KEY,
	event text,
	urgency INTEGER NOT NULL REFERENCES iris.cap_urgency,
	multi VARCHAR(64)
);

DELETE FROM iris.sonar_type WHERE name = 'cap_urgency';
INSERT INTO iris.sonar_type (name) VALUES ('cap_urgency_fld');

UPDATE iris.privilege
	SET type_n = 'cap_urgency_fld'
	WHERE type_n = 'cap_urgency';

ALTER TABLE iris.ipaws_alert DROP COLUMN urgency;
ALTER TABLE iris.ipaws_alert
	ADD COLUMN urgency INTEGER REFERENCES iris.cap_urgency;
UPDATE iris.ipaws_alert SET urgency = 0;
ALTER TABLE iris.ipaws_alert ALTER COLUMN urgency SET NOT NULL;

ALTER TABLE iris.ipaws_alert DROP COLUMN severity;
ALTER TABLE iris.ipaws_alert
	ADD COLUMN severity INTEGER REFERENCES iris.cap_severity;
UPDATE iris.ipaws_alert SET severity = 0;
ALTER TABLE iris.ipaws_alert ALTER COLUMN severity SET NOT NULL;

ALTER TABLE iris.ipaws_alert DROP COLUMN certainty;
ALTER TABLE iris.ipaws_alert
	ADD COLUMN certainty INTEGER REFERENCES iris.cap_certainty;
UPDATE iris.ipaws_alert SET certainty = 0;
ALTER TABLE iris.ipaws_alert ALTER COLUMN certainty SET NOT NULL;

COMMIT;
