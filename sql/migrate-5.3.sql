\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.2.0', '5.3.0');

-- Blocked lanes function
CREATE OR REPLACE FUNCTION event.incident_blocked_lanes(TEXT)
	RETURNS INTEGER AS $incident_blocked_lanes$
DECLARE
	impact ALIAS FOR $1;
	imp TEXT;
	lanes INTEGER;
BEGIN
	lanes = length(impact) - 2;
	IF lanes > 0 THEN
		imp = substring(impact FROM 2 FOR lanes);
		RETURN lanes - length(replace(imp, '!', ''));
	ELSE
		RETURN 0;
	END IF;
END;
$incident_blocked_lanes$ LANGUAGE plpgsql;

-- Blocked shoulders function
CREATE OR REPLACE FUNCTION event.incident_blocked_shoulders(TEXT)
	RETURNS INTEGER AS $incident_blocked_shoulders$
DECLARE
	impact ALIAS FOR $1;
	len INTEGER;
	imp TEXT;
BEGIN
	len = length(impact);
	IF len > 2 THEN
		imp = substring(impact FROM 1 FOR 1) ||
		      substring(impact FROM len FOR 1);
		RETURN 2 - length(replace(imp, '!', ''));
	ELSE
		RETURN 0;
	END IF;
END;
$incident_blocked_shoulders$ LANGUAGE plpgsql;

-- Add blocked_lanes / blocked_shoulders to incident_view
DROP VIEW incident_view;
CREATE VIEW incident_view AS
    SELECT event_id, name, event_date, ed.description, road, d.direction,
           impact, event.incident_blocked_lanes(impact) AS blocked_lanes,
           event.incident_blocked_shoulders(impact) AS blocked_shoulders,
           cleared, confirmed, camera, ln.description AS lane_type, detail,
           replaces, lat, lon
    FROM event.incident i
    LEFT JOIN event.event_description ed ON i.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_type ln ON i.lane_type = ln.id;
GRANT SELECT ON incident_view TO PUBLIC;

COMMIT;
