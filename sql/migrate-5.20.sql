\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.19.0', '5.20.0');

ALTER TABLE event.ipaws_deployer DROP COLUMN lat;
ALTER TABLE event.ipaws_deployer DROP COLUMN lon;
ALTER TABLE event.ipaws_deployer DROP COLUMN area_threshold;
ALTER TABLE event.ipaws_deployer DROP COLUMN sign_group;
ALTER TABLE event.ipaws_deployer DROP COLUMN quick_message;
ALTER TABLE event.ipaws_deployer DROP COLUMN deployed;
ALTER TABLE event.ipaws_deployer DROP COLUMN active;

UPDATE event.ipaws_deployer SET pre_alert_time = 0
	WHERE pre_alert_time IS NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN pre_alert_time SET NOT NULL;

UPDATE event.ipaws_deployer SET post_alert_time = 0
	WHERE post_alert_time IS NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN post_alert_time SET NOT NULL;

UPDATE event.ipaws_deployer SET msg_priority = 5 WHERE msg_priority IS NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN msg_priority SET NOT NULL;

ALTER TABLE event.ipaws_deployer ALTER COLUMN config SET NOT NULL;

UPDATE event.ipaws_deployer SET was_deployed = false WHERE was_deployed IS NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN was_deployed SET NOT NULL;

ALTER TABLE event.ipaws_deployer ALTER COLUMN gen_time SET NOT NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN alert_start SET NOT NULL;
ALTER TABLE event.ipaws_deployer ALTER COLUMN alert_end SET NOT NULL;

ALTER TABLE event.ipaws_deployer ADD COLUMN alert_state INTEGER;
UPDATE event.ipaws_deployer SET alert_state = 4; -- expired
ALTER TABLE event.ipaws_deployer ALTER COLUMN alert_state SET NOT NULL;

COMMIT;
