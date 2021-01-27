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

CREATE TABLE iris.cap_status (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY iris.cap_status (id, description) FROM stdin;
0	unknown
1	actual
2	exercise
3	system
4	test
5	draft
\.

CREATE TABLE iris.cap_msg_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY iris.cap_msg_type (id, description) FROM stdin;
0	unknown
1	alert
2	update
3	cancel
4	ack
5	error
\.

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

DROP TABLE event.ipaws_alert;

-- IPAWS Alert Event table
CREATE TABLE event.ipaws_alert (
	name text PRIMARY KEY,
	identifier text UNIQUE NOT NULL,
	sender text,
	sent_date TIMESTAMP WITH time zone,
	status INTEGER NOT NULL REFERENCES iris.cap_status,
	msg_type INTEGER NOT NULL REFERENCES iris.cap_msg_type,
	scope text,
	codes text[],
	note text,
	alert_references text[],
	incidents text[],
	categories text[],
	event text,
	response_types text[],
	urgency INTEGER NOT NULL REFERENCES iris.cap_urgency,
	severity INTEGER NOT NULL REFERENCES iris.cap_severity,
	certainty INTEGER NOT NULL REFERENCES iris.cap_certainty,
	audience text,
	effective_date TIMESTAMP WITH time zone,
	onset_date TIMESTAMP WITH time zone,
	expiration_date TIMESTAMP WITH time zone,
	sender_name text,
	headline text,
	alert_description text,
	instruction text,
	parameters jsonb,
	area jsonb,
	geo_poly geography(multipolygon),
	lat double precision,
	lon double precision,
	purgeable BOOLEAN,
	last_processed TIMESTAMP WITH time zone
);

COMMIT;
