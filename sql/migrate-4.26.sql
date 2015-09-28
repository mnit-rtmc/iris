\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.26.0'
	WHERE name = 'database_version';

-- Reserve Transcore E6 comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (32, 'TransCore E6');

-- add tag read events
CREATE TABLE event.tag_read_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	tag_id VARCHAR(16) NOT NULL,
	tag_reader VARCHAR(10) NOT NULL;
	toll_zone VARCHAR(20) REFERENCES iris.toll_zone
		ON DELETE SET NULL,
	corridor VARCHAR(16) NOT NULL,
	hov BOOLEAN NOT NULL,
	trip_id INTEGER
);

-- add tag_read_event_view
CREATE VIEW tag_read_event_view AS
	SELECT event_id, event_date, event_description.description, tag_id,
	       tag_reader, toll_zone, corridor, hov, trip_id
	FROM event.tag_read_event
	JOIN event.event_description
	ON tag_read_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON tag_read_event_view TO PUBLIC;

-- added tag read event descriptions
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (601, 'Tag Read');
