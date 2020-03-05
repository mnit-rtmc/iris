\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.5.0', '5.6.0');

-- Add detector column to price_message_event table
ALTER TABLE event.price_message_event ADD COLUMN detector VARCHAR(20);

DROP VIEW price_message_event_view;
CREATE VIEW price_message_event_view AS
	SELECT event_id, event_date, event_description.description,
	       device_id, toll_zone, detector, price
	FROM event.price_message_event
	JOIN event.event_description
	ON price_message_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON price_message_event_view TO PUBLIC;

COMMIT;
