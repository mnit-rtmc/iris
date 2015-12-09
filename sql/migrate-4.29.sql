\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.29.0'
	WHERE name = 'database_version';

-- Add tag_reader_dms_view
CREATE VIEW tag_reader_dms_view AS
	SELECT tag_reader, dms
	FROM iris.tag_reader_dms;
GRANT SELECT ON tag_reader_dms_view TO PUBLIC;

-- Add dms_action_view
CREATE VIEW dms_action_view AS
	SELECT name, action_plan, sign_group, phase, quick_message,
	       beacon_enabled, a_priority, r_priority
	FROM iris.dms_action;
GRANT SELECT ON dms_action_view TO PUBLIC;
