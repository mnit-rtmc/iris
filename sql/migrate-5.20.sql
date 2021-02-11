\set ON_ERROR_STOP

BEGIN;
CREATE SCHEMA cap;
ALTER SCHEMA cap OWNER TO tms;
COMMIT;

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.19.0', '5.20.0');

INSERT INTO iris.system_attribute (name, value) VALUES
	('alert_clear_secs', '300'),
	('alert_sign_thresh_auto_meters', '1000'),
	('alert_sign_thresh_opt_meters', '4000'),
	('cap_alert_purge_days', '7'),
	('cap_xml_save_enable', 'true'),
	('clearguide_key', '');
DELETE FROM iris.system_attribute WHERE name LIKE 'ipaws_%';

DROP TABLE iris.cap_response;
DROP TABLE iris.cap_urgency;
DROP TABLE iris.ipaws_config;
DROP TABLE event.ipaws_deployer;
DROP TABLE event.ipaws_alert;
DROP TABLE event.notification;

DELETE FROM iris.role_capability WHERE capability = 'ipaws_admin';
DELETE FROM iris.role_capability WHERE capability = 'ipaws_deploy';
DELETE FROM iris.role_capability WHERE capability = 'ipaws_tab';

DELETE FROM iris.privilege WHERE capability LIKE 'ipaws_%';
DELETE FROM iris.privilege WHERE type_n = 'notification';

DELETE FROM iris.sonar_type
	WHERE name IN ('cap_response', 'cap_urgency', 'ipaws_alert',
	               'ipaws_config', 'ipaws_deployer', 'notification');

INSERT INTO iris.sonar_type (name) VALUES ('alert_config'), ('alert_info');

UPDATE iris.capability SET name = 'alert_admin' WHERE name = 'ipaws_admin';
UPDATE iris.capability SET name = 'alert_deploy' WHERE name = 'ipaws_deploy';
UPDATE iris.capability SET name = 'alert_tab' WHERE name = 'ipaws_tab';

INSERT INTO iris.privilege (name, capability, type_n, write) VALUES
	('PRV_009A', 'alert_admin', 'alert_config', true),
	('PRV_009B', 'alert_deploy', 'alert_info', true),
	('PRV_009C', 'alert_tab', 'alert_config', false),
	('PRV_009D', 'alert_tab', 'alert_info', false);

INSERT INTO iris.role_capability (role, capability) VALUES
	('administrator', 'alert_admin'),
	('administrator', 'alert_tab'),
	('operator', 'alert_deploy'),
	('operator', 'alert_tab');

UPDATE iris.sign_msg_source SET source = 'alert' WHERE bit = 6;
INSERT INTO iris.sign_msg_source (bit, source) VALUES (13, 'clearguide');

CREATE TABLE cap.status (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY cap.status (id, description) FROM stdin;
0	unknown
1	actual
2	exercise
3	system
4	test
5	draft
\.

CREATE TABLE cap.msg_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY cap.msg_type (id, description) FROM stdin;
0	unknown
1	alert
2	update
3	cancel
4	ack
5	error
\.

CREATE TABLE cap.scope (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY cap.scope (id, description) FROM stdin;
0	unknown
1	public
2	restricted
3	private
\.

CREATE TABLE cap.category (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY cap.category (id, description) FROM stdin;
0	geo
1	met
2	safety
3	security
4	rescue
5	fire
6	health
7	env
8	transport
9	infra
10	CBRNE
11	other
\.

CREATE TABLE cap.event (
	code VARCHAR(3) PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

COPY cap.event (code, description) FROM stdin;
ADR	Administrative Message
AVA	Avalanche Watch
AVW	Avalanche Warning
BLU	Blue Alert
BZW	Blizzard Warning
BWY	Brisk Wind Advisory
CAE	Child Abduction Emergency
CDW	Civil Danger Warning
CEM	Civil Emergency Message
CFA	Coastal Flood Watch
CFW	Coastal Flood Warning
DMO	Practice/Demo Warning
DSW	Dust Storm Warning
EAN	Emergency Action Notification
EAT	Emergency Action Termination
EQW	Earthquake Warning
EVI	Evacuation Immediate
EWW	Extreme Wind Warning
FFA	Flash Flood Watch
FFS	Flash Flood Statement
FFW	Flash Flood Warning
FGY	Dense Fog Advisory
FLA	Flood Watch
FLS	Flood Statement
FLW	Flood Warning
FLY	Flood Advisory
FRW	Fire Warning
FSW	Flash Freeze Warning
FZW	Freeze Warning
GLA	Gale Watch
GLW	Gale Warning
HLS	Hurricane Statement
HMW	Hazardous Materials Warning
HUA	Hurricane Watch
HUW	Hurricane Warning
HWA	High Wind Watch
HWW	High Wind Warning
LAE	Local Area Emergency
LEW	Law Enforcement Warning
MWS	Marine Weather Statement
NAT	National Audible Test
NIC	National Information Center
NMN	Network Message Notification
NPT	National Periodic Test
NUW	Nuclear Power Plant Warning
NST	National Silent Test
RHW	Radiological Hazard Warning
RMT	Required Monthly Test
RPS	Rip Current Statement
RWT	Required Weekly Test
SCY	Small Craft Advisory
SMW	Special Marine Warning
SPS	Special Weather Statement
SPW	Shelter in Place Warning
SQW	Snowsquall Warning
SSA	Storm Surge Watch
SSW	Storm Surge Warning
SUW	High Surf Warning
SUY	High Surf Advisory
SVA	Severe Thunderstorm Watch
SVR	Severe Thunderstorm Warning
SVS	Severe Weather Statement
TOA	Tornado Watch
TOE	911 Telephone Outage Emergency
TOR	Tornado Warning
TRA	Tropical Storm Watch
TRW	Tropical Storm Warning
TSA	Tsunami Watch
TSW	Tsunami Warning
VOW	Volcano Warning
WCY	Wind Chill Advisory
WIY	Wind Advisory
WSA	Winter Storm Watch
WSW	Winter Storm Warning
WWY	Winter Weather Advisory
\.

CREATE TABLE cap.response_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY cap.response_type (id, description) FROM stdin;
0	shelter
1	evacuate
2	prepare
3	execute
4	avoid
5	monitor
6	assess
7	allclear
8	none
\.

CREATE TABLE cap.urgency (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY cap.urgency (id, description) FROM stdin;
0	unknown
1	past
2	future
3	expected
4	immediate
\.

CREATE TABLE cap.severity (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY cap.severity(id, description) FROM stdin;
0	unknown
1	minor
2	moderate
3	severe
4	extreme
\.

CREATE TABLE cap.certainty (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY cap.certainty(id, description) FROM stdin;
0	unknown
1	unlikely
2	possible
3	likely
4	observed
\.

CREATE TABLE iris.alert_config (
	name VARCHAR(20) PRIMARY KEY,
	event VARCHAR(3) REFERENCES cap.event,
	response_type INTEGER REFERENCES cap.response_type,
	urgency INTEGER REFERENCES cap.urgency,
	sign_group VARCHAR(20) REFERENCES iris.sign_group,
	quick_message VARCHAR(20) REFERENCES iris.quick_message(name),
	pre_alert_hours INTEGER NOT NULL,
	post_alert_hours INTEGER NOT NULL,
	auto_deploy BOOLEAN NOT NULL
);

CREATE VIEW alert_config_view AS
	SELECT c.name, c.event, ev.description AS event_description,
	       rt.description AS response_type, urg.description AS urgency,
	       sign_group, quick_message, pre_alert_hours, post_alert_hours,
	       auto_deploy
	FROM iris.alert_config c
	JOIN cap.event ev ON c.event = ev.code
	JOIN cap.response_type rt ON c.response_type = rt.id
	JOIN cap.urgency urg ON c.urgency = urg.id;
GRANT SELECT ON alert_config_view TO PUBLIC;

CREATE TABLE cap.alert (
	identifier VARCHAR(128) PRIMARY KEY,
	alert JSONB NOT NULL,
	receive_date TIMESTAMP WITH time zone NOT NULL
);

CREATE TABLE iris.alert_state (
	id INTEGER PRIMARY KEY,
	description VARCHAR(12) NOT NULL
);

COPY iris.alert_state(id, description) FROM stdin;
0	pending
1	active
2	cleared
3	active_req
4	cleared_req
\.

CREATE TABLE cap.alert_info (
	name VARCHAR(20) PRIMARY KEY,
	alert VARCHAR(128) NOT NULL REFERENCES cap.alert(identifier),
	replaces VARCHAR(24) REFERENCES cap.alert_info,
	start_date TIMESTAMP WITH time zone,
	end_date TIMESTAMP WITH time zone,
	event VARCHAR(3) NOT NULL REFERENCES cap.event,
	response_type INTEGER NOT NULL REFERENCES cap.response_type,
	urgency INTEGER NOT NULL REFERENCES cap.urgency,
	severity INTEGER NOT NULL REFERENCES cap.severity,
	certainty INTEGER NOT NULL REFERENCES cap.certainty,
	headline VARCHAR(256),
	description VARCHAR(4096),
	instruction VARCHAR(4096),
	area_desc VARCHAR(256) NOT NULL,
	geo_poly geometry(multipolygon) NOT NULL,
	lat double precision NOT NULL,
	lon double precision NOT NULL,
	sign_group VARCHAR(20) NOT NULL REFERENCES iris.sign_group,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	alert_state INTEGER NOT NULL REFERENCES iris.alert_state
);

INSERT INTO iris.plan_phase (name, hold_time) VALUES
	('alert_before', 0),
	('alert_during', 0),
	('alert_after', 0);

UPDATE iris.comm_protocol SET description = 'CAP Feed' WHERE id = 42;
INSERT INTO iris.comm_protocol(id, description) VALUES (43, 'ClearGuide');

COMMIT;
