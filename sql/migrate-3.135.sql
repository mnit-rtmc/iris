\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.135.0'
	WHERE name = 'database_version';

DROP TRIGGER IF EXISTS incident_update_trigger ON event.incident;

CREATE TRIGGER incident_update_trigger
	AFTER INSERT OR UPDATE ON event.incident
	FOR EACH ROW EXECUTE PROCEDURE event.incident_update_trig();

ALTER TABLE event.incident ADD COLUMN replaces VARCHAR(16)
	REFERENCES event.incident(name);
