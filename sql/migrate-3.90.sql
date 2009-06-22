\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.90.0' WHERE name = 'database_version';

CREATE TABLE iris.i_user (
	name VARCHAR(15) PRIMARY KEY,
	dn text NOT NULL,
	full_name VARCHAR(31) NOT NULL
);

CREATE TABLE iris.role (
	name VARCHAR(15) PRIMARY KEY,
	enabled BOOLEAN NOT NULL
);

CREATE TABLE iris.privilege (
	name VARCHAR(8) PRIMARY KEY,
	role VARCHAR(15) NOT NULL REFERENCES iris.role,
	pattern VARCHAR(31) DEFAULT ''::VARCHAR NOT NULL,
	priv_r boolean DEFAULT false NOT NULL,
	priv_w boolean DEFAULT false NOT NULL,
	priv_c boolean DEFAULT false NOT NULL,
	priv_d boolean DEFAULT false NOT NULL
);

CREATE TABLE iris.i_user_role (
	i_user VARCHAR(15) NOT NULL REFERENCES iris.i_user(name),
	role VARCHAR(15) NOT NULL REFERENCES iris.role(name)
);

CREATE TEMP SEQUENCE temp_priv_number_seq;

INSERT INTO iris.i_user (name, dn, full_name)
	(SELECT name, dn, full_name FROM iris_user);
INSERT INTO iris.role (name, enabled) (SELECT name, true FROM role);
INSERT INTO iris.privilege (name, role, pattern, priv_r, priv_w, priv_c, priv_d)
	(SELECT 'prv_' || nextval('temp_priv_number_seq'), name, pattern,
	priv_r, priv_w, priv_c, priv_d FROM role);
INSERT INTO iris.i_user_role (i_user, role)
	(SELECT iris_user, role FROM iris_user_role);

ALTER TABLE event.sign_event DROP CONSTRAINT sign_event_iris_user_fkey;
ALTER TABLE event.sign_event ADD CONSTRAINT sign_event_iris_user_fkey
	FOREIGN KEY (iris_user) REFERENCES iris.i_user;

DROP TABLE iris_user_role;
DROP TABLE iris_user;
DROP TABLE role;
