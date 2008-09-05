\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

CREATE TEMP TABLE temp_alarm (
	name VARCHAR(10),
	description VARCHAR(24),
	controller VARCHAR(20),
	pin integer
);

CREATE TEMP SEQUENCE __a_seq MINVALUE 1;
INSERT INTO temp_alarm (name, description, controller, pin)
	(SELECT 'A' || trim(both FROM to_char(nextval('__a_seq'), '009')),
		notes::VARCHAR(24), controller, pin FROM alarm);

DROP TABLE alarm;

CREATE TABLE alarm (
	name VARCHAR(10) PRIMARY KEY,
	description VARCHAR(24) NOT NULL,
	controller VARCHAR(20) REFERENCES controller(name),
	pin integer NOT NULL,
	state BOOLEAN NOT NULL
);

INSERT INTO alarm (name, description, controller, pin, state)
	(SELECT name, description, controller, pin, false FROM temp_alarm);

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.controller, a.pin, c.comm_link,
		c.drop_id
	FROM alarm a LEFT JOIN controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

DELETE FROM vault_types WHERE "table" = 'alarm';

SET search_path = event, public, pg_catalog;

CREATE TABLE event.alarm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	alarm VARCHAR(10) NOT NULL REFERENCES alarm(name)
		ON DELETE CASCADE
);

CREATE VIEW public.alarm_event_view AS
	SELECT e.event_id, e.event_date, ed.description AS event_description,
		e.alarm, a.description
	FROM alarm_event e
	JOIN event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN alarm a ON e.alarm = a.name;
GRANT SELECT ON public.alarm_event_view TO PUBLIC;

INSERT INTO event.event_description (event_desc_id, description)
	VALUES (1, 'Alarm TRIGGERED');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (2, 'Alarm CLEARED');
