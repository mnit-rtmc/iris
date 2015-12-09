\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.29.0'
	WHERE name = 'database_version';

-- Add tag_reader_dms_view
CREATE VIEW tag_reader_dms_view AS
	SELECT tag_reader, dms
	FROM iris.tag_reader_dms;
GRANT SELECT ON tag_reader_dms_view TO PUBLIC;
