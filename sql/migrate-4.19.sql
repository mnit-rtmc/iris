\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.19.0'
	WHERE name = 'database_version';

-- Replace obsolete Vicon protocol (id 5) with Pelco P (formerly id 30)
UPDATE iris.comm_protocol SET description = 'Pelco P' WHERE id = 5;
DELETE FROM iris.comm_protocol WHERE id = 30;

-- Add multi column to sign_event_view and recent_sign_event_view
DROP VIEW recent_sign_event_view;
DROP VIEW sign_event_view;

CREATE VIEW sign_event_view AS
	SELECT event_id, event_date, description, device_id,
	       regexp_replace(replace(replace(message, '[nl]', E'\n'), '[np]',
	                      E'\n'), '\[.+?\]', ' ', 'g') AS message,
	       message AS multi, iris_user
	FROM event.sign_event JOIN event.event_description
	ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
	SELECT event_id, event_date, description, device_id, message, multi,
	       iris_user
	FROM sign_event_view
	WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;
