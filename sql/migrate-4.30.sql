\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.30.0'
	WHERE name = 'database_version';

-- Add index to tag_read_event
CREATE INDEX ON event.tag_read_event(tag_id);

-- Add indexes to price_message_event
CREATE INDEX ON event.price_message_event(event_date);
CREATE INDEX ON event.price_message_event(device_id);

-- add hidden field to sign_group
ALTER TABLE iris.sign_group ADD COLUMN hidden BOOLEAN;
UPDATE iris.sign_group SET hidden = false;
ALTER TABLE iris.sign_group ALTER COLUMN hidden SET NOT NULL;
