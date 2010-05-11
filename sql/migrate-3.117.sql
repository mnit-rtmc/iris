\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.117.0'
	WHERE name = 'database_version';

INSERT INTO event.event_description VALUES(101, 'Sign BRIGHTNESS LOW');
INSERT INTO event.event_description VALUES(102,	'Sign BRIGHTNESS GOOD');
INSERT INTO event.event_description VALUES(103, 'Sign BRIGHTNESS HIGH');

CREATE TABLE event.brightness_sample (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	dms VARCHAR(10) NOT NULL REFERENCES iris._dms(name)
		ON DELETE CASCADE,
	photocell integer NOT NULL,
	output integer NOT NULL
);
