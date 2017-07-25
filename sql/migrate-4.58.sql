\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

-- Helper function to check and update database version from migrate scripts
CREATE OR REPLACE FUNCTION iris.update_version(TEXT, TEXT) RETURNS TEXT AS
	$update_version$
DECLARE
	ver_prev ALIAS FOR $1;
	ver_new ALIAS FOR $2;
	ver_db TEXT;
BEGIN
	SELECT value INTO ver_db FROM iris.system_attribute
		WHERE name = 'database_version';
	IF ver_db != ver_prev THEN
		RAISE EXCEPTION 'Cannot migrate database -- wrong version: %',
			ver_db;
	END IF;
	UPDATE iris.system_attribute SET value = ver_new
		WHERE name = 'database_version';
	RETURN ver_new;
END;
$update_version$ language plpgsql;

SELECT iris.update_version('4.57.0', '4.58.0');

COMMIT;
