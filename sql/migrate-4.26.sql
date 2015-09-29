\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.26.0'
	WHERE name = 'database_version';

-- Reserve Transcore E6 comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (32, 'TransCore E6');

-- add tag types
CREATE TABLE event.tag_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

-- add tag read events
CREATE TABLE event.tag_read_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	tag_type INTEGER NOT NULL REFERENCES event.tag_type,
	tag_id INTEGER NOT NULL,
	tag_reader VARCHAR(10) NOT NULL,
	toll_zone VARCHAR(20) REFERENCES iris.toll_zone
		ON DELETE SET NULL,
	tollway VARCHAR(16) NOT NULL,
	hov BOOLEAN NOT NULL,
	trip_id INTEGER
);

-- add tag_read_event_view
CREATE VIEW tag_read_event_view AS
	SELECT event_id, event_date, event_description.description,
	       tag_type.description AS tag_type, tag_id, tag_reader, toll_zone,
	       tollway, hov, trip_id
	FROM event.tag_read_event
	JOIN event.event_description
	ON   tag_read_event.event_desc_id = event_description.event_desc_id
	JOIN event.tag_type
	ON   tag_read_event.tag_type = tag_type.id;
GRANT SELECT ON tag_read_event_view TO PUBLIC;

-- added tag read event descriptions
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (601, 'Tag Read');

-- populate tag type LUT
COPY event.tag_type (id, description) FROM stdin;
0	ASTMv6
1	SeGo
\.

-- add Axis JPEG encoder type
INSERT INTO iris.encoder_type VALUES (7, 'Axis JPEG');

-- add dmsxml reinit system attributes
INSERT INTO iris.system_attribute (name, value) VALUES ('dmsxml_reinit_detect', false);
INSERT INTO iris.system_attribute (name, value) VALUES ('email_recipient_dmsxml_reinit', '');
