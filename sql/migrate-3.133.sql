\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.133.0'
	WHERE name = 'database_version';

INSERT INTO iris.comm_protocol (id, description) VALUES (16, 'Infinova D PTZ');

CREATE TABLE iris.modem (
	name VARCHAR(20) PRIMARY KEY,
	uri VARCHAR(64) NOT NULL,
	config VARCHAR(64) NOT NULL,
	timeout integer NOT NULL
);

CREATE VIEW modem_view AS
	SELECT name, uri, config, timeout
	FROM iris.modem;
GRANT SELECT ON modem_view TO PUBLIC;

INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES
	('prv_mdm0', 'maintenance', 'modem(/.*)?', true, false, false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d) VALUES
	('prv_mdm1', 'device_admin', 'modem/.*', false, true, true, true);

DROP VIEW comm_link_view;

ALTER TABLE iris.comm_link ADD COLUMN uri VARCHAR(64);
UPDATE iris.comm_link SET uri = url;
ALTER TABLE iris.comm_link ALTER COLUMN uri SET NOT NULL;
ALTER TABLE iris.comm_link DROP COLUMN url;

CREATE VIEW comm_link_view AS
	SELECT cl.name, cl.description, cl.uri, cp.description AS protocol,
	cl.timeout
	FROM iris.comm_link cl
	JOIN iris.comm_protocol cp ON cl.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;
