\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.35.0'
	WHERE name = 'database_version';

-- Add dictionary system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('dict_allowed_scheme', '0');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('dict_banned_scheme', '0');

-- Add dictionary privileges
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES
	('prv_dic1', 'policy_admin', 'word(/.*)?', true, true, true, true);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES
	('prv_dic2', 'dms_tab', 'word(/.*)?', true, false, false, false);

-- Add word table
CREATE TABLE iris.word (
	name VARCHAR(24) PRIMARY KEY,
	abbr VARCHAR(12),
	allowed BOOLEAN DEFAULT false NOT NULL
);

-- Create incident descriptor table
CREATE TABLE iris.inc_descriptor (
	name VARCHAR(10) PRIMARY KEY,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	lane_type SMALLINT NOT NULL REFERENCES iris.lane_type(id),
	detail VARCHAR(8) REFERENCES event.incident_detail(name),
	cleared BOOLEAN NOT NULL,
	multi VARCHAR(64) NOT NULL
);

-- Create incident range lookup table
CREATE TABLE iris.inc_range (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

-- Create incident locator table
CREATE TABLE iris.inc_locator (
	name VARCHAR(10) PRIMARY KEY,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	range INTEGER NOT NULL REFERENCES iris.inc_range(id),
	branched BOOLEAN NOT NULL,
	pickable BOOLEAN NOT NULL,
	multi VARCHAR(64) NOT NULL
);

-- Create incident advice table
CREATE TABLE iris.inc_advice (
	name VARCHAR(10) PRIMARY KEY,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	range INTEGER NOT NULL REFERENCES iris.inc_range(id),
	lane_type SMALLINT NOT NULL REFERENCES iris.lane_type(id),
	impact VARCHAR(20) NOT NULL,
	cleared BOOLEAN NOT NULL,
	multi VARCHAR(64) NOT NULL
);

-- Add incident control privileges
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES ('prv_inc1', 'incident_control', 'inc_descriptor(/.*)?',
	true, false, false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES ('prv_inc2', 'incident_control', 'inc_locator(/.*)?',
	true, false, false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES ('prv_inc3', 'incident_control', 'inc_advice(/.*)?',
	true, false, false, false);

-- Add incident policy privileges
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES ('prv_inc4', 'policy_admin', 'inc_descriptor(/.*)?',
	false, true, true, true);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES ('prv_inc5', 'policy_admin', 'inc_locator(/.*)?',
	false, true, true, true);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES ('prv_inc6', 'policy_admin', 'inc_advice(/.*)?',
	false, true, true, true);

-- Populate incident range lookup table
COPY iris.inc_range (id, description) FROM stdin;
0	near
1	middle
2	far
\.
