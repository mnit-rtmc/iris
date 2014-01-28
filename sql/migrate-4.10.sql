\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.10.0'
	WHERE name = 'database_version';

ALTER TABLE iris.comm_link ADD COLUMN poll_enabled BOOLEAN;
UPDATE iris.comm_link SET poll_enabled = TRUE;
ALTER TABLE iris.comm_link ALTER COLUMN poll_enabled SET NOT NULL;

ALTER TABLE iris.comm_link ADD COLUMN poll_period INTEGER;
UPDATE iris.comm_link SET poll_period = 30;
ALTER TABLE iris.comm_link ALTER COLUMN poll_period SET NOT NULL;

DROP VIEW comm_link_view;
CREATE VIEW comm_link_view AS
	SELECT cl.name, cl.description, cl.uri, cp.description AS protocol,
		cl.poll_enabled, cl.poll_period, cl.timeout
	FROM iris.comm_link cl
	JOIN iris.comm_protocol cp ON cl.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;
