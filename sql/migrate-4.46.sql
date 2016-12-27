\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.46.0'
	WHERE name = 'database_version';

-- Add travel time event table
CREATE TABLE event.travel_time_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(10)
);

-- Add travel time event view
CREATE VIEW travel_time_event_view AS
	SELECT event_id, event_date, event_description.description, device_id
	FROM event.travel_time_event
	JOIN event.event_description
	ON travel_time_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON travel_time_event_view TO PUBLIC;

-- Add travel time event descriptions
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (701, 'TT Link too long');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (702, 'TT No data');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (703, 'TT No destination data');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (704, 'TT No origin data');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (705, 'TT No route');
