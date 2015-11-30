\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.28.0'
	WHERE name = 'database_version';

-- add toll price limit system attributes
INSERT INTO iris.system_attribute (name, value) VALUES ('toll_min_price', '0.25');
INSERT INTO iris.system_attribute (name, value) VALUES ('toll_max_price', '8');

-- replace sign_message scheduled with source
DELETE FROM iris.sign_message;
ALTER TABLE iris.sign_message ADD COLUMN source INTEGER;
ALTER TABLE iris.sign_message ALTER COLUMN source SET NOT NULL;
ALTER TABLE iris.sign_message DROP COLUMN scheduled;

-- added price message event descriptions
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (651, 'Price DEPLOYED');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (652, 'Price VERIFIED');

-- add price_message_event table
CREATE TABLE event.price_message_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20) NOT NULL,
	toll_zone VARCHAR(20) NOT NULL,
	price NUMERIC(4,2) NOT NULL
);

-- add price_message_event_view
CREATE VIEW price_message_event_view AS
	SELECT event_id, event_date, event_description.description,
	       device_id, toll_zone, price
	FROM event.price_message_event
	JOIN event.event_description
	ON price_message_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON price_message_event_view TO PUBLIC;

-- add tag_reader_dms relation
CREATE TABLE iris.tag_reader_dms (
	tag_reader VARCHAR(10) NOT NULL REFERENCES iris._tag_reader,
	dms VARCHAR(10) NOT NULL REFERENCES iris._dms
);
