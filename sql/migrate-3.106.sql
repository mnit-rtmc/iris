\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.106.0'
	WHERE name = 'database_version';

UPDATE event.event_description SET description = 'Incident DEBRIS'
	WHERE event_desc_id = 23;
UPDATE event.event_description SET description = 'Incident ROADWORK'
	WHERE event_desc_id = 24;

ALTER TABLE event.incident ADD COLUMN camera VARCHAR(10);

CREATE TABLE event.incident_update (
	event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	incident VARCHAR(16) NOT NULL REFERENCES event.incident(name),
	event_date timestamp WITH time zone NOT NULL,
	impact VARCHAR(20) NOT NULL,
	cleared BOOLEAN NOT NULL
);

CREATE FUNCTION event.incident_update_trig() RETURNS "trigger" AS
'
BEGIN
	INSERT INTO event.incident_update
		(incident, event_date, impact, cleared)
	VALUES (NEW.name, now(), NEW.impact, NEW.cleared);
	RETURN NEW;
END;' LANGUAGE plpgsql;

CREATE TRIGGER incident_update_trigger
	AFTER UPDATE ON event.incident
	FOR EACH ROW EXECUTE PROCEDURE event.incident_update_trig();
