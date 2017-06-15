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

DROP VIEW quick_message_view;
DROP VIEW dms_toll_zone_view;
DROP VIEW iris.quick_message_toll_zone;
DROP VIEW iris.quick_message_priced;
DROP VIEW iris.quick_message_open;
DROP VIEW iris.quick_message_closed;

ALTER TABLE iris.quick_message ALTER COLUMN multi TYPE VARCHAR(1024);
ALTER TABLE iris.sign_message ALTER COLUMN multi TYPE VARCHAR(1024);

CREATE VIEW quick_message_view AS
	SELECT name, sign_group, multi FROM iris.quick_message;
GRANT SELECT ON quick_message_view TO PUBLIC;

CREATE VIEW iris.quick_message_priced AS
    SELECT name AS quick_message, 'priced'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzp,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzp%';

CREATE VIEW iris.quick_message_open AS
    SELECT name AS quick_message, 'open'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzo,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzo%';

CREATE VIEW iris.quick_message_closed AS
    SELECT name AS quick_message, 'closed'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzc,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzc%';

CREATE VIEW iris.quick_message_toll_zone AS
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_priced UNION ALL
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_open UNION ALL
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_closed;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, state, toll_zone, action_plan, dms_action_view.quick_message
    FROM dms_action_view
    JOIN iris.dms_sign_group
    ON dms_action_view.sign_group = dms_sign_group.sign_group
    JOIN iris.quick_message
    ON dms_action_view.quick_message = quick_message.name
    JOIN iris.quick_message_toll_zone
    ON dms_action_view.quick_message = quick_message_toll_zone.quick_message;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

COMMIT;
