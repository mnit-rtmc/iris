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

-- Make msg_current not null
ALTER TABLE iris._dms ALTER COLUMN msg_current SET NOT NULL;

-- Add controller condition to dms_message_view
CREATE OR REPLACE VIEW dms_message_view AS
	SELECT d.name, cc.description AS condition, multi, beacon_enabled,
	       iris.sign_msg_sources(source) AS sources, duration, deploy_time,
	       owner
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

COMMIT;
