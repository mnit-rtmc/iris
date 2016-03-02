\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.33.0'
	WHERE name = 'database_version';

-- Reserve incident feed comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (34, 'Incident Feed');

-- add confirmed field to incident
ALTER TABLE event.incident ADD COLUMN confirmed BOOLEAN;
UPDATE event.incident SET confirmed = true;
ALTER TABLE event.incident ALTER COLUMN confirmed SET NOT NULL;

-- add confirmed field to incident_update
ALTER TABLE event.incident_update ADD COLUMN confirmed BOOLEAN;
UPDATE event.incident_update SET confirmed = true;
ALTER TABLE event.incident_update ALTER COLUMN confirmed SET NOT NULL;

-- replace incident_update_trig
CREATE OR REPLACE FUNCTION event.incident_update_trig() RETURNS TRIGGER AS
$incident_update_trig$
BEGIN
    INSERT INTO event.incident_update
               (incident, event_date, impact, cleared, confirmed)
        VALUES (NEW.name, now(), NEW.impact, NEW.cleared, NEW.confirmed);
    RETURN NEW;
END;
$incident_update_trig$ LANGUAGE plpgsql;

-- replace incident_view
DROP VIEW incident_view;
CREATE VIEW incident_view AS
    SELECT iu.event_id, name, iu.event_date, ed.description, road,
           d.direction, iu.impact, iu.cleared, iu.confirmed, camera,
           ln.description AS lane_type, detail, replaces, lat, lon
    FROM event.incident i
    JOIN event.incident_update iu ON i.name = iu.incident
    LEFT JOIN event.event_description ed ON i.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_type ln ON i.lane_type = ln.id;
GRANT SELECT ON incident_view TO PUBLIC;
