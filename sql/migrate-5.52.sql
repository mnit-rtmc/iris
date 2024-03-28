\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.51.0', '5.52.0');

-- Improve security stuff
ALTER FUNCTION iris.multi_tags_str(INTEGER)
    SET search_path = pg_catalog, pg_temp;

-- Rename user resource type to i_user
INSERT INTO iris.resource_type ('name') VALUES ('i_user');
UPDATE iris.permission SET resource_n = 'i_user' WHERE resource_n = 'user';
UPDATE iris.privilege SET type_n = 'i_user' WHERE type_n = 'user';
DELETE FROM iris.resource_type WHERE name = 'user';

COMMIT;
