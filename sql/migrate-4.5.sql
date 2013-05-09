\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.5.0'
	WHERE name = 'database_version';

CREATE OR REPLACE VIEW cabinet_view AS
	SELECT name, style, geo_loc, mile
	FROM iris.cabinet;
GRANT SELECT ON cabinet_view TO PUBLIC;
