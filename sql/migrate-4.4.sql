\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.4.0'
	WHERE name = 'database_version';

DROP VIEW recent_sign_event_view;
DROP VIEW sign_event_view;
DROP FUNCTION event.message_line(text, integer);

CREATE VIEW sign_event_view AS
	SELECT event_id, event_date, description, device_id,
	       regexp_replace(replace(replace(message, '[nl]', E'\n'), '[np]',
	                      E'\n'), '\[.+?\]', ' ', 'g') AS message, iris_user
	FROM event.sign_event JOIN event.event_description
	ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
	SELECT event_id, event_date, description, device_id, message, iris_user
	FROM sign_event_view
	WHERE (CURRENT_TIMESTAMP - event_date) < interval '90 days';
GRANT SELECT ON recent_sign_event_view TO PUBLIC;
