\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.16.0', '5.17.0');

DROP VIEW dms_message_view;
CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition,
	       fail_time IS NOT NULL AS failed, multi, beacon_enabled,
	       prefix_page, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, expire_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

COMMIT;
