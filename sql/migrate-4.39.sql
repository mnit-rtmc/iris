\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.39.0'
	WHERE name = 'database_version';

CREATE OR REPLACE VIEW iris.quick_message_priced AS
    SELECT name AS quick_message, 'priced'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzp,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzp%';
CREATE OR REPLACE VIEW iris.quick_message_open AS
    SELECT name AS quick_message, 'open'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzo,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzo%';
CREATE OR REPLACE VIEW iris.quick_message_closed AS
    SELECT name AS quick_message, 'closed'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzc,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzc%';
CREATE OR REPLACE VIEW iris.quick_message_toll_zone AS
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_priced UNION ALL
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_open UNION ALL
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_closed;

CREATE OR REPLACE VIEW dms_toll_zone_view AS
    SELECT dms, state, toll_zone, action_plan, dms_action_view.quick_message
    FROM dms_action_view
    JOIN iris.dms_sign_group
    ON dms_action_view.sign_group = dms_sign_group.sign_group
    JOIN iris.quick_message
    ON dms_action_view.quick_message = quick_message.name
    JOIN iris.quick_message_toll_zone
    ON dms_action_view.quick_message = quick_message_toll_zone.quick_message;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

-- Rename CBW protocol to match CommProtocol enum
UPDATE iris.comm_protocol SET description = 'CBW' WHERE id = 33;
