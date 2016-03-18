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
