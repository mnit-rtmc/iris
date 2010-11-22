\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.124.0'
	WHERE name = 'database_version';

CREATE VIEW lcs_array_view AS
	SELECT name, shift, notes, lcs_lock
	FROM iris.lcs_array;
GRANT SELECT ON lcs_array_view TO PUBLIC;

CREATE VIEW lcs_view AS
	SELECT name, lcs_array, lane
	FROM iris.lcs;
GRANT SELECT ON lcs_view TO PUBLIC;
