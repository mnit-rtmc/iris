\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.41.0'
	WHERE name = 'database_version';

-- Add version column to controller table
ALTER TABLE iris.controller ADD COLUMN version VARCHAR(64);

CREATE OR REPLACE VIEW controller_view AS
	SELECT c.name, drop_id, comm_link, cabinet,
	       cnd.description AS condition, notes, cab.geo_loc, fail_time,
	       version
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;
