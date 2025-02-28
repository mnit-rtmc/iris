\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.67.0', '5.68.0');

-- Add pixel_service flag to msg_pattern
ALTER TABLE iris.msg_pattern ADD COLUMN pixel_service BOOLEAN;
UPDATE iris.msg_pattern SET pixel_service = false;
ALTER TABLE iris.msg_pattern ALTER COLUMN pixel_service SET NOT NULL;

UPDATE iris.msg_pattern mp
    SET pixel_service = true
    FROM device_action_view da
    JOIN action_plan_view ap ON da.action_plan = ap.name
    WHERE sticky = true AND da.msg_pattern = mp.name;

DROP VIEW msg_pattern_view;
CREATE VIEW msg_pattern_view AS
    SELECT name, multi, flash_beacon, pixel_service, compose_hashtag
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

-- Change comm protocol 23 to CAP-NWS
UPDATE iris.comm_protocol
    SET description = 'CAP-NWS'
    WHERE id = 23;

UPDATE iris.comm_protocol
    SET description = 'CAP-IPAWS'
    WHERE id = 42;

COMMIT;
