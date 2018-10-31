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

-- Add default values to privilege columns
ALTER TABLE iris.privilege ALTER COLUMN obj_n SET DEFAULT ''::VARCHAR;
ALTER TABLE iris.privilege ALTER COLUMN group_n SET DEFAULT ''::VARCHAR;
ALTER TABLE iris.privilege ALTER COLUMN attr_n SET DEFAULT ''::VARCHAR;

-- Add privileges for domain
INSERT INTO iris.privilege (name, capability, type_n, write)
	(SELECT 'prv_dom' || ROW_NUMBER() OVER (ORDER BY name), capability,
	 'domain', write
	 FROM iris.privilege
	 WHERE type_n = 'role');

-- Add password change events
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (205, 'Client CHANGE PASSWORD');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (206, 'Client FAIL PASSWORD');

-- Change iris_user to owner in sign_event
DROP VIEW recent_sign_event_view;
DROP VIEW sign_event_view;

ALTER TABLE event.sign_event ADD COLUMN owner VARCHAR(16);
UPDATE event.sign_event SET owner = iris_user;
ALTER TABLE event.sign_event DROP COLUMN iris_user;

CREATE VIEW sign_event_view AS
	SELECT event_id, event_date, description, device_id,
	       regexp_replace(replace(replace(message, '[nl]', E'\n'), '[np]',
	                      E'\n'), '\[.+?\]', ' ', 'g') AS message,
	       message AS multi, owner
	FROM event.sign_event JOIN event.event_description
	ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
	SELECT event_id, event_date, description, device_id, message, multi,
	       owner
	FROM sign_event_view
	WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

-- Change sign_message owner from 15 to 16 characters (for action plan names)
DROP VIEW sign_message_view;
ALTER TABLE iris.sign_message ALTER COLUMN owner TYPE VARCHAR(16);

CREATE VIEW sign_message_view AS
	SELECT name, sign_config, incident, multi, beacon_enabled, prefix_page,
	       msg_priority, iris.sign_msg_sources(source) AS sources, owner,
	       duration
	FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

COMMIT;
