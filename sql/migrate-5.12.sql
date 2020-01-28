\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.11.0', '5.12.0');

-- Re-add camera blank URL
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_blank_url', '')
	ON CONFLICT DO NOTHING;

COMMIT;
