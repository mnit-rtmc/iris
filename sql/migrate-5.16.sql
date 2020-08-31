\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.15.0', '5.16.0');

-- Create comm_config table
CREATE TABLE iris.comm_config (
	name VARCHAR(10) PRIMARY KEY,
	description VARCHAR(20) NOT NULL UNIQUE,
	protocol SMALLINT NOT NULL REFERENCES iris.comm_protocol(id),
	modem BOOLEAN NOT NULL,
	timeout_ms INTEGER NOT NULL,
	poll_period_sec INTEGER NOT NULL,
	long_poll_period_sec INTEGER NOT NULL,
	idle_disconnect_sec INTEGER NOT NULL,
	no_response_disconnect_sec INTEGER NOT NULL
);

ALTER TABLE iris.comm_config
	ADD CONSTRAINT poll_period_ck
	CHECK (poll_period_sec >= 5
	       AND long_poll_period_sec >= poll_period_sec);

CREATE VIEW comm_config_view AS
	SELECT cc.name, cc.description, cp.description AS protocol, modem,
	       timeout_ms, poll_period_sec, long_poll_period_sec,
	       idle_disconnect_sec, no_response_disconnect_sec
	FROM iris.comm_config cc
	JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_config_view TO PUBLIC;

DROP VIEW comm_link_view;

-- Populate comm_config table from existing comm_link records
INSERT INTO iris.comm_config (name, description, protocol, modem, timeout_ms,
	poll_period_sec, long_poll_period_sec, idle_disconnect_sec,
	no_response_disconnect_sec)
SELECT 'cfg_' || ROW_NUMBER() OVER (ORDER BY protocol, modem, poll_period),
       'dsc_' || ROW_NUMBER() OVER (ORDER BY protocol, modem, poll_period),
       protocol, modem, timeout, poll_period, GREATEST(poll_period, 300), 0, 0
FROM iris.comm_link
GROUP BY (protocol, modem, timeout, poll_period);

-- Set idle_disconnect_sec for DMS protocols
UPDATE iris.comm_config SET idle_disconnect_sec = sa.value::integer
  FROM iris.system_attribute sa
 WHERE protocol IN (0, 7, 9, 11)
   AND sa.name = 'comm_idle_disconnect_dms_sec';

-- Set idle_disconnect_sec for GPS protocols
UPDATE iris.comm_config SET idle_disconnect_sec = sa.value::integer
  FROM iris.system_attribute sa
 WHERE protocol IN (37, 38, 39)
   AND sa.name = 'comm_idle_disconnect_gps_sec';

-- Set idle_disconnect_sec for modems
UPDATE iris.comm_config SET idle_disconnect_sec = sa.value::integer
  FROM iris.system_attribute sa
 WHERE modem = true
   AND sa.name = 'comm_idle_disconnect_modem_sec';

-- Add comm_config column to comm_link table
ALTER TABLE iris.comm_link
 ADD COLUMN comm_config VARCHAR(10) REFERENCES iris.comm_config;

UPDATE iris.comm_link cl SET comm_config = cc.name
  FROM iris.comm_config cc
 WHERE cl.modem = cc.modem
   AND cl.protocol = cc.protocol
   AND cl.poll_period = cc.poll_period_sec
   AND cl.timeout = cc.timeout_ms;

ALTER TABLE iris.comm_link ALTER COLUMN comm_config SET NOT NULL;

-- Drop columns moved to comm_config table
ALTER TABLE iris.comm_link DROP COLUMN modem;
ALTER TABLE iris.comm_link DROP COLUMN protocol;
ALTER TABLE iris.comm_link DROP COLUMN poll_period;
ALTER TABLE iris.comm_link DROP COLUMN timeout;

-- Create updated view
CREATE VIEW comm_link_view AS
	SELECT cl.name, cl.description, uri, poll_enabled,
	       cp.description AS protocol, cc.description AS comm_config,
	       modem, timeout_ms, poll_period_sec
	FROM iris.comm_link cl
	JOIN iris.comm_config cc ON cl.comm_config = cc.name
	JOIN iris.comm_protocol cp ON cc.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;

INSERT INTO iris.sonar_type (name) VALUES ('comm_config');

INSERT INTO iris.privilege (name, capability, type_n, obj_n, attr_n, group_n, write)
SELECT name || 'x', capability, 'comm_config', '', '', '', write
  FROM iris.privilege
 WHERE type_n = 'comm_link' AND obj_n = '' AND attr_n = '' AND group_n = '';

-- Delete comm_idle_disconnect system attributes
DELETE FROM iris.system_attribute WHERE name = 'comm_idle_disconnect_dms_sec';
DELETE FROM iris.system_attribute WHERE name = 'comm_idle_disconnect_gps_sec';
DELETE FROM iris.system_attribute WHERE name = 'comm_idle_disconnect_modem_sec';

-- Delete DMSXML op timeout system attributes
DELETE FROM iris.system_attribute WHERE name = 'dmsxml_op_timeout_secs';
DELETE FROM iris.system_attribute WHERE name = 'dmsxml_modem_op_timeout_secs';

-- Delete Gstreamer version system attribute
DELETE FROM iris.system_attribute WHERE name = 'gstreamer_version_windows';

COMMIT;
