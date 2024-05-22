\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.53.0', '5.54.0');

INSERT INTO iris.system_attribute (name, value)
    VALUES ('vid_max_duration_sec', '0');

-- Add base column to resource_type
ALTER TABLE iris.resource_type ADD COLUMN base BOOLEAN;
UPDATE iris.resource_type SET base = false;
UPDATE iris.resource_type SET base = true
    WHERE name IN ('action_plan', 'alert_config', 'beacon', 'camera',
                   'controller', 'detector', 'dms', 'gate_arm', 'incident',
                   'lcs', 'parking_area', 'permission', 'ramp_meter',
                   'system_attribute', 'toll_zone', 'weather_sensor');
ALTER TABLE iris.resource_type ALTER COLUMN base SET NOT NULL;

CREATE FUNCTION iris.resource_is_base(VARCHAR(16)) RETURNS BOOLEAN AS
    $resource_is_base$
SELECT EXISTS (
    SELECT 1
    FROM iris.resource_type
    WHERE name = $1 AND base = true
);
$resource_is_base$ LANGUAGE sql;

-- Delete permissions that are not for "base" resources
DELETE FROM iris.permission WHERE resource_n NOT IN (
    'action_plan', 'alert_config', 'beacon', 'camera', 'controller',
    'detector', 'dms', 'gate_arm', 'incident', 'lcs', 'parking_area',
    'permission', 'ramp_meter', 'system_attribute', 'toll_zone',
    'weather_sensor'
);

ALTER TABLE iris.permission ADD CONSTRAINT base_resource_ck
    CHECK (iris.resource_is_base(resource_n));

-- Add ignore_auto_fail to action_plan
DROP VIEW action_plan_view;
ALTER TABLE iris.action_plan ADD COLUMN ignore_auto_fail BOOLEAN;
UPDATE iris.action_plan SET ignore_auto_fail = false;
ALTER TABLE iris.action_plan ALTER COLUMN ignore_auto_fail SET NOT NULL;

CREATE VIEW action_plan_view AS
    SELECT name, description, group_n, sync_actions, sticky, ignore_auto_fail,
           active, default_phase, phase
    FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

-- Add landmark support for detector labels
DROP VIEW detector_auto_fail_view;
DROP VIEW detector_event_view;
DROP VIEW detector_view;
DROP VIEW detector_label_view;
DROP FUNCTION iris.detector_label(VARCHAR(6), VARCHAR(4), VARCHAR(6),
    VARCHAR(4), VARCHAR(2), CHAR, SMALLINT, BOOLEAN);

CREATE FUNCTION iris.landmark_abbrev(VARCHAR(24)) RETURNS TEXT
    AS $landmark_abbrev$
DECLARE
    lmrk TEXT;
    lmrk2 TEXT;
BEGIN
    lmrk = initcap($1);
    -- Replace common words
    lmrk = replace(lmrk, 'Of ', '');
    lmrk = replace(lmrk, 'Miles', 'MI');
    lmrk = replace(lmrk, 'Mile', 'MI');
    -- Remove whitespace and non-printable characters
    lmrk = regexp_replace(lmrk, '[^[:graph:]]', '', 'g');
    IF length(lmrk) > 6 THEN
        -- Remove lower-case vowels
        lmrk = regexp_replace(lmrk, '[aeiouy]', '', 'g');
    END IF;
    IF length(lmrk) > 6 THEN
        -- Remove all punctuation
        lmrk = regexp_replace(lmrk, '[[:punct:]]', '', 'g');
    END IF;
    lmrk2 = lmrk;
    IF length(lmrk) > 6 THEN
        -- Remove letters
        lmrk = regexp_replace(lmrk, '[[:alpha:]]', '', 'g');
    END IF;
    IF length(lmrk) > 0 THEN
        RETURN left(lmrk, 6);
    ELSE
        RETURN left(lmrk2, 6);
    END IF;
END;
$landmark_abbrev$ LANGUAGE plpgsql;

CREATE FUNCTION iris.root_lbl(rd VARCHAR(6), rdir VARCHAR(4), xst VARCHAR(6),
    xdir VARCHAR(4), xmod VARCHAR(2), lmark VARCHAR(24)) RETURNS TEXT AS
$$
    SELECT rd || '/' || COALESCE(
        xdir || replace(xmod, '@', '') || xst,
        iris.landmark_abbrev(lmark)
    ) || rdir;
$$ LANGUAGE sql;

CREATE FUNCTION iris.detector_label(TEXT, CHAR, SMALLINT, BOOLEAN) RETURNS TEXT
    AS $detector_label$
DECLARE
    root ALIAS FOR $1;
    lcode ALIAS FOR $2;
    lane_number ALIAS FOR $3;
    abandoned ALIAS FOR $4;
    lnum VARCHAR(2);
    suffix VARCHAR(5);
BEGIN
    lnum = '';
    IF lane_number > 0 THEN
        lnum = TO_CHAR(lane_number, 'FM9');
    END IF;
    suffix = '';
    IF abandoned THEN
        suffix = '-ABND';
    END IF;
    RETURN COALESCE(
        root || lcode || lnum || suffix,
        'FUTURE'
    );
END;
$detector_label$ LANGUAGE plpgsql;

CREATE VIEW detector_label_view AS
    SELECT d.name AS det_id,
           iris.detector_label(
               iris.root_lbl(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod, l.landmark),
               d.lane_code, d.lane_number, d.abandoned
           ) AS label, rnd.geo_loc
    FROM iris.detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_view AS
    SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
           dl.label, dl.geo_loc, l.rd || '_' || l.road_dir AS cor_id,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           d.lane_number, d.field_length, lc.description AS lane_type,
           d.lane_code, d.abandoned, d.force_fail, d.auto_fail, c.condition,
           d.fake, d.notes
    FROM iris.detector d
    LEFT JOIN detector_label_view dl ON d.name = dl.det_id
    LEFT JOIN geo_loc_view l ON dl.geo_loc = l.name
    LEFT JOIN iris.lane_code lc ON d.lane_code = lc.lcode
    LEFT JOIN controller_view c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW detector_event_view AS
    SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
    FROM event.detector_event e
    JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
    JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE VIEW detector_auto_fail_view AS
    WITH af AS (SELECT device_id, event_desc_id, count(*) AS event_count,
                max(event_date) AS last_fail
                FROM event.detector_event
                GROUP BY device_id, event_desc_id)
    SELECT device_id, label, ed.description, event_count, last_fail
    FROM af
    JOIN event.event_description ed ON af.event_desc_id = ed.event_desc_id
    JOIN detector_label_view dl ON af.device_id = dl.det_id;
GRANT SELECT ON detector_auto_fail_view TO PUBLIC;

COMMIT;
