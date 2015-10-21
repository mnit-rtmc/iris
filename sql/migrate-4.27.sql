\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.27.0'
	WHERE name = 'database_version';

-- add tollway column to toll_zone
ALTER TABLE iris.toll_zone ADD COLUMN tollway VARCHAR(16);

-- add tollway to toll_zone_view
CREATE OR REPLACE VIEW toll_zone_view AS
	SELECT name, start_id, end_id, tollway
	FROM iris.toll_zone;
GRANT SELECT ON toll_zone_view TO PUBLIC;

-- rename dms_op_status_enable sys attr to device_op_status_enable
UPDATE iris.system_attribute SET name = 'device_op_status_enable'
	WHERE name = 'dms_op_status_enable';

-- drop old tag_read_event_view
DROP VIEW tag_read_event_view;

-- drop tollway from event.tag_read_event table
ALTER TABLE event.tag_read_event DROP COLUMN tollway;

-- change tag_read_event_view to use tollway from toll_zone
CREATE VIEW tag_read_event_view AS
	SELECT event_id, event_date, event_description.description,
	       tag_type.description AS tag_type, tag_id, tag_reader,
	       toll_zone, tollway, hov, trip_id
	FROM event.tag_read_event
	JOIN event.event_description
	ON   tag_read_event.event_desc_id = event_description.event_desc_id
	JOIN event.tag_type
	ON   tag_read_event.tag_type = tag_type.id
	LEFT JOIN iris.toll_zone
	ON        tag_read_event.toll_zone = toll_zone.name;
GRANT SELECT ON tag_read_event_view TO PUBLIC;
