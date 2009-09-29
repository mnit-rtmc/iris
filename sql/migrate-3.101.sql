\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.101.0' WHERE name = 'database_version';

INSERT INTO iris.lane_use_indication (id, description)
	VALUES (14, 'HOV / HOT begins');

CREATE TABLE iris.system_attribute (
	name VARCHAR(32) PRIMARY KEY,
	value VARCHAR(64) NOT NULL
);

INSERT INTO iris.system_attribute (name, value)
	(SELECT name, value FROM system_attribute);

DROP TABLE system_attribute;

CREATE TABLE iris.comm_link (
	name VARCHAR(20) PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	url VARCHAR(64) NOT NULL,
	protocol smallint NOT NULL,
	timeout integer NOT NULL
);

INSERT INTO iris.comm_link (name, description, url, protocol, timeout)
	(SELECT name, description, url, protocol, timeout FROM comm_link);

ALTER TABLE iris.controller DROP CONSTRAINT controller_comm_link_fkey;
ALTER TABLE iris.controller ADD CONSTRAINT controller_comm_link_fkey
	FOREIGN KEY (comm_link) REFERENCES iris.comm_link;

DROP TABLE comm_link;
