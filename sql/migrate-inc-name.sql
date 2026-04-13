\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Increase incident name length to 32 characters max
DROP VIEW incident_update_view;
DROP VIEW incident_view;

ALTER TABLE event.incident
    ALTER COLUMN name TYPE VARCHAR,
    ALTER COLUMN replaces TYPE VARCHAR;

ALTER TABLE event.incident ADD CONSTRAINT incident_name_check
    CHECK (LENGTH(name) <= 32);

ALTER TABLE event.incident_update
    ALTER COLUMN incident TYPE VARCHAR;

CREATE VIEW incident_view AS
    SELECT i.id, name, event_date, ed.description, road, d.direction,
           impact, event.incident_blocked_lanes(impact) AS blocked_lanes,
           event.incident_blocked_shoulders(impact) AS blocked_shoulders,
           cleared, confirmed, user_id, camera, lc.description AS lane_type,
           detail, replaces, lat, lon
    FROM event.incident i
    LEFT JOIN event.event_description ed ON i.event_desc = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_code lc ON i.lane_code = lc.lcode;
GRANT SELECT ON incident_view TO PUBLIC;

CREATE VIEW incident_update_view AS
    SELECT iu.id, name, iu.event_date, ed.description, road,
           d.direction, iu.impact, iu.cleared, iu.confirmed, iu.user_id,
           camera, lc.description AS lane_type, detail, replaces, lat, lon
    FROM event.incident i
    JOIN event.incident_update iu ON i.name = iu.incident
    LEFT JOIN event.event_description ed ON i.event_desc = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_code lc ON i.lane_code = lc.lcode;
GRANT SELECT ON incident_update_view TO PUBLIC;

COMMIT;
