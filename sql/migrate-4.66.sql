\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.65.0', '4.66.0');

-- Drop bitmaps column from sign_message
ALTER TABLE iris.sign_message DROP COLUMN bitmaps;

-- Add system blank sign message
INSERT INTO iris.sign_message (name, multi, beacon_enabled, a_priority,
	r_priority, source)
	VALUES ('system_blank', '', false, 21, 1, 1);

-- Update all blank signs to the same sign message
UPDATE iris._dms SET msg_current = 'system_blank'
	WHERE multi = '' OR msg_current IS NULL;

ALTER TABLE iris._dms ALTER COLUMN msg_current SET NOT NULL;

COMMIT;
