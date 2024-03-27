\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.51.0', '5.52.0');

-- Improve security stuff
ALTER FUNCTION iris.multi_tags_str(INTEGER)
    SET search_path = pg_catalog, pg_temp;

COMMIT;
