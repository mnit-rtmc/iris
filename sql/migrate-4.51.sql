\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.51.0'
	WHERE name = 'database_version';

-- Add comm_idle_disconnect system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_idle_disconnect_dms_sec', '-1');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_idle_disconnect_modem_sec', '20');

-- Add blank camera URLs
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_blank_url', '');

-- Add latency to encoder_type and encoder_type_view
ALTER TABLE encoder_type ADD COLUMN latency INTEGER;
UPDATE encoder_type SET latency = 50;
ALTER TABLE encoder_type ALTER COLUMN latency SET NOT NULL;
DROP VIEW encoder_type_view;
CREATE VIEW encoder_type_view AS
	SELECT name, http_path, rtsp_path, latency FROM iris.encoder_type;
GRANT SELECT ON encoder_type_view TO PUBLIC;
