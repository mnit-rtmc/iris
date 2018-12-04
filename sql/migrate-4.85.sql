\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.84.0', '4.85.0');

-- insert email_rate_limit_hours system attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('email_rate_limit_hours', '0');

COMMIT;
