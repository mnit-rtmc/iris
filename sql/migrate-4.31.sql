\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.31.0'
	WHERE name = 'database_version';

CREATE OR REPLACE VIEW controller_view AS
	SELECT c.name, drop_id, comm_link, cabinet,
	       cnd.description AS condition, notes, cab.geo_loc, fail_time
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;
