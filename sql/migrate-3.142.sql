\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.142.0'
	WHERE name = 'database_version';

CREATE TABLE event.client_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	host_port VARCHAR(64) NOT NULL,
	iris_user VARCHAR(15)
);

INSERT INTO event.event_description (event_desc_id, description)
	VALUES (201, 'Client CONNECT');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (202, 'Client AUTHENTICATE');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (203, 'Client FAIL AUTHENTICATION');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (204, 'Client DISCONNECT');

CREATE VIEW client_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.host_port,
		e.iris_user
	FROM event.client_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON client_event_view TO PUBLIC;

ALTER TABLE event.sign_event DROP CONSTRAINT sign_event_iris_user_fkey;
