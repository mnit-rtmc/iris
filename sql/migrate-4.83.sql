\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.82.0', '4.83.0');

-- Add Cohu Helios protocol
INSERT INTO iris.comm_protocol VALUES (40, 'Cohu Helios PTZ');

-- Fixed permission
GRANT SELECT ON detector_auto_fail_view TO PUBLIC;

-- Add login domains
CREATE TABLE iris.domain (
	name VARCHAR(15) PRIMARY KEY,
	cidr VARCHAR(64) NOT NULL,
	enabled BOOLEAN NOT NULL
);

COPY iris.domain (name, cidr, enabled) FROM stdin;
any_ipv4	0.0.0.0/0	t
any_ipv6	::0/0	t
\.

-- Add user/domain mappings
CREATE TABLE iris.i_user_domain (
	i_user VARCHAR(15) NOT NULL REFERENCES iris.i_user,
	domain VARCHAR(15) NOT NULL REFERENCES iris.domain
);
ALTER TABLE iris.i_user_domain ADD PRIMARY KEY (i_user, domain);

INSERT INTO iris.i_user_domain (i_user, domain)
	(SELECT name, 'any_ipv4' FROM iris.i_user);
INSERT INTO iris.i_user_domain (i_user, domain)
	(SELECT name, 'any_ipv6' FROM iris.i_user);

-- Add domain to sonar type lut
INSERT INTO iris.sonar_type (name) VALUES ('domain');

-- Add password change events
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (205, 'Client CHANGE PASSWORD');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (206, 'Client FAIL PASSWORD');

COMMIT;
