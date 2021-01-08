\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.18.0', '5.19.0');

ALTER TABLE event.ipaws_alert ADD UNIQUE (identifier);
ALTER TABLE event.ipaws_alert ALTER COLUMN identifier SET NOT NULL;

ALTER TABLE event.ipaws_deployer DROP CONSTRAINT ipaws_deployer_alert_id_fkey;
ALTER TABLE event.ipaws_deployer ADD CONSTRAINT ipaws_deployer_alert_id_fkey
	FOREIGN KEY (alert_id) REFERENCES event.ipaws_alert (identifier);

COMMIT;
