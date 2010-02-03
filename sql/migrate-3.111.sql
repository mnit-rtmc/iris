\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.111.0'
	WHERE name = 'database_version';

ALTER TABLE event.incident ADD COLUMN lane_type smallint;
ALTER TABLE event.incident ADD CONSTRAINT incident_lane_type_fkey
	FOREIGN KEY (lane_type) REFERENCES iris.lane_type;
UPDATE event.incident SET lane_type = 1;
ALTER TABLE event.incident ALTER COLUMN lane_type SET NOT NULL;
