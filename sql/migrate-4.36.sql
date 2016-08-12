\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.36.0'
	WHERE name = 'database_version';

-- Add modem enabled boolean column
ALTER TABLE iris.modem ADD COLUMN enabled BOOLEAN;
UPDATE iris.modem SET enabled = TRUE;
ALTER TABLE iris.modem ALTER COLUMN enabled SET NOT NULL;

-- Add enabled column to modem_view
CREATE OR REPLACE VIEW modem_view AS
	SELECT name, uri, config, timeout, enabled
	FROM iris.modem;
GRANT SELECT ON modem_view TO PUBLIC;

-- delete old system attributes
DELETE FROM iris.system_attribute WHERE name = 'device_op_status_enable';
DELETE FROM iris.system_attribute WHERE name = 'dms_form';
