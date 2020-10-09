\set ON_ERROR_STOP
BEGIN;

-- Enable PostGIS extension
CREATE EXTENSION postgis;

SET SESSION AUTHORIZATION 'tms';

-- Add System Attributes
INSERT INTO iris.system_attribute (name, value) VALUES
		('ipaws_priority_weight_urgency', 1.0),
		('ipaws_priority_weight_severity', 1.0),
		('ipaws_priority_weight_certainty', 1.0),
		('ipaws_deploy_auto_mode', false),
		('ipaws_deploy_auto_timeout_secs', 0),
		('ipaws_sign_thresh_auto_meters', 1000),
		('ipaws_sign_thresh_opt_meters', 4000),
		('push_notification_timeout_secs', 900);

-- Extend sonar_type field to allow longer names
ALTER TABLE iris.sonar_type ALTER COLUMN name TYPE varchar(32);

-- Add new SONAR types
INSERT INTO iris.sonar_type (name) VALUES
		('cap_response_type'),
		('cap_urgency'),
		('ipaws'),
		('ipaws_alert_config'),
		('ipaws_alert_deployer'),
		('push_notification');

-- Add IPAWS sign message source
INSERT INTO iris.sign_msg_source (bit, source) VALUES (13, 'ipaws');

-- Reserve IPAWS Alert comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (42, 'IPAWS Alert');

-- Drop comm_link_view so we can alter the URI field length below
DROP VIEW public.comm_link_view;

-- Extend allowed length of URI field in comm_link table
ALTER TABLE iris.comm_link ALTER COLUMN uri TYPE character varying(256);

-- Recreate comm_link_view with the altered table
CREATE VIEW comm_link_view AS
	SELECT cl.name, cl.description, uri, poll_enabled,
	       cp.description AS protocol, cc.description AS comm_config,
	       modem, timeout_ms, poll_period_sec
	FROM iris.comm_link cl
	JOIN iris.comm_config cc ON cl.comm_config = cc.name
	JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;

-- IPAWS Alert Event table
CREATE TABLE event.ipaws
(
    name text PRIMARY KEY,
    identifier text,
    sender text,
    sent_date timestamp with time zone,
    status text,
    message_type text,
    scope text,
    codes text[],
    note text,
    alert_references text[],
    incidents text[],
    categories text[],
    event text,
    response_types text[],
    urgency text,
    severity text,
    certainty text,
    audience text,
    effective_date timestamp with time zone,
    onset_date timestamp with time zone,
    expiration_date timestamp with time zone,
    sender_name text,
    headline text,
    alert_description text,
    instruction text,
    parameters jsonb,
    area jsonb,
    geo_poly geography(multipolygon),
	geo_loc varchar(20),
    purgeable boolean,
	last_processed timestamp with time zone
);

-- IPAWS Alert Deployer table
CREATE TABLE event.ipaws_alert_deployer (
	name varchar(20) PRIMARY KEY,
	gen_time timestamp with time zone,
	approved_time timestamp with time zone,
	alert_id text REFERENCES event.ipaws(name),
	geo_loc varchar(20),
	alert_start timestamp with time zone,
	alert_end timestamp with time zone,
	config varchar(24),
	sign_group varchar(20),
	quick_message varchar(20),
	pre_alert_time integer,
	post_alert_time integer,
	auto_dms text[],
	optional_dms text[],
	deployed_dms text[],
	area_threshold double precision,
	auto_multi text,
	deployed_multi text,
	msg_priority integer,
	approved_by varchar(15),
	deployed boolean,
	was_deployed boolean,
	active boolean DEFAULT false,
	replaces varchar(24)
);

-- IPAWS Alert Config table
CREATE TABLE iris.ipaws_alert_config (
	name varchar(24) PRIMARY KEY,
	event text,
	sign_group varchar(20) REFERENCES iris.sign_group(name),
	quick_message varchar(20) REFERENCES iris.quick_message(name),
	pre_alert_time integer DEFAULT 6,
	post_alert_time integer DEFAULT 0
);

-- CAP response types table
CREATE TABLE iris.cap_response_type (
	name varchar(24) PRIMARY KEY,
	event text,
	response_type text,
	multi text
);

-- CAP urgency values table
CREATE TABLE iris.cap_urgency (
	name varchar(24) PRIMARY KEY,
	event text,
	urgency text,
	multi text
);

-- Push Notification table
-- NOTE that we don't have a foreign key linking addressed_by to the i_user
-- table so we can put 'auto' in there
CREATE TABLE event.push_notification (
	name varchar(30) PRIMARY KEY,
	ref_object_type varchar(32) REFERENCES iris.sonar_type(name),
	ref_object_name text,
	needs_write boolean,
	sent_time timestamp with time zone,
	title text,
	description text,
	addressed_by varchar(15),
	addressed_time timestamp with time zone
);

-- Add capability and privileges
INSERT INTO iris.capability (name, enabled) VALUES
		('ipaws_admin', true),
		('ipaws_deploy', true),
		('ipaws_tab', true);

-- Drop role privilege view to modify type_n field length
DROP VIEW public.role_privilege_view;

ALTER TABLE iris.privilege ALTER COLUMN type_n TYPE varchar(32);

-- Recreate view
CREATE VIEW role_privilege_view AS
    SELECT role, role_capability.capability, type_n, obj_n, group_n, attr_n,
	       write
	FROM iris.role
	JOIN iris.role_capability ON role.name = role_capability.role
	JOIN iris.capability ON role_capability.capability = capability.name
	JOIN iris.privilege ON privilege.capability = capability.name
	WHERE role.enabled = 't' AND capability.enabled = 't';
GRANT SELECT ON role_privilege_view TO PUBLIC;

INSERT INTO iris.privilege (name,capability,type_n,obj_n,attr_n,group_n,write) VALUES
						   ('PRV_001C','base','push_notification','','','',false),
						   ('PRV_001D','base','push_notification','','','',true),
						   ('PRV_009A','ipaws_admin','cap_response_type','','','',true),
						   ('PRV_009B','ipaws_admin','cap_urgency','','','',true),
						   ('PRV_009C','ipaws_admin','ipaws','','','',true),
						   ('PRV_009D','ipaws_admin','ipaws_alert_deployer','','','',true),
						   ('PRV_009E','ipaws_admin','ipaws_alert_config','','','',true),
						   ('PRV_009F','ipaws_deploy','ipaws_alert_deployer','','','',true),
						   ('PRV_009G','ipaws_tab','cap_response_type','','','',false),
						   ('PRV_009H','ipaws_tab','cap_urgency','','','',false),
						   ('PRV_009I','ipaws_tab','ipaws','','','',false),
						   ('PRV_009J','ipaws_tab','ipaws_alert_deployer','','','',false),
						   ('PRV_009K','ipaws_tab','ipaws_alert_config','','','',false);

INSERT INTO iris.role_capability (role, capability) VALUES
		('administrator', 'ipaws_admin'),
        ('administrator', 'ipaws_tab'),
        ('operator', 'ipaws_deploy'),
        ('operator', 'ipaws_tab');

-- Commit changes
COMMIT;
