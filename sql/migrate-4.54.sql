\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

UPDATE iris.system_attribute SET value = '4.54.0'
	WHERE name = 'database_version';

DROP VIEW video_monitor_view;
CREATE VIEW video_monitor_view AS
	SELECT m.name, m.notes, mon_num, direct, restricted, monitor_style,
	       m.controller, m.pin, ctr.condition, ctr.comm_link, camera
	FROM iris.video_monitor m
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

COMMIT;
