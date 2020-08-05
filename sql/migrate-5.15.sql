\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.14.0', '5.15.0');

-- Add station_id column to travel_time_event table
ALTER TABLE event.travel_time_event ADD COLUMN station_id VARCHAR(10);

DROP VIEW travel_time_event_view;
CREATE VIEW travel_time_event_view AS
	SELECT event_id, event_date, event_description.description, device_id,
	       station_id
	FROM event.travel_time_event
	JOIN event.event_description
	ON travel_time_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON travel_time_event_view TO PUBLIC;

COMMIT;
