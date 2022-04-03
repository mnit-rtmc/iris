\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.30.0', '5.31.0');

-- Add message source for standby tags
INSERT INTO iris.sign_msg_source (bit, source) VALUES (15, 'standby');

-- Add new permissions for administrator
COPY iris.permission (role, resource_n, access_n) FROM stdin;
administrator	camera	4
administrator	detector	4
administrator	ramp_meter	4
administrator	tag_reader	4
\.

-- Replace lane_type table with lane_code
DROP VIEW lane_type_view;

CREATE TABLE iris.lane_code (
    lcode VARCHAR(1) PRIMARY KEY,
    description VARCHAR(12) NOT NULL
);

COPY iris.lane_code (lcode, description) FROM stdin;
	Mainline
A	Auxiliary
B	Bypass
C	CD Lane
D	Shoulder
G	Green
H	HOV
K	Parking
M	Merge
O	Omnibus
P	Passage
Q	Queue
R	Reversible
T	HOT
V	Velocity
X	Exit
Y	Wrong Way
\.

CREATE VIEW lane_code_view AS
    SELECT lcode, description FROM iris.lane_code;
GRANT SELECT ON lane_code_view TO PUBLIC;

DROP VIEW detector_auto_fail_view;
DROP VIEW detector_event_view;
DROP VIEW detector_view;
DROP VIEW detector_label_view;
DROP VIEW iris.detector;
DROP FUNCTION iris.detector_insert();
DROP FUNCTION iris.detector_update();
DROP FUNCTION iris.detector_label(VARCHAR(6), VARCHAR(4), VARCHAR(6),
    VARCHAR(4), VARCHAR(2), SMALLINT, SMALLINT, BOOLEAN);

ALTER TABLE iris._detector
    ADD COLUMN lane_code VARCHAR(1) REFERENCES iris.lane_code;
UPDATE iris._detector AS d
    SET lane_code = lt.dcode::VARCHAR(1)
    FROM iris.lane_type lt
    WHERE d.lane_type = lt.id;
ALTER TABLE iris._detector ALTER COLUMN lane_code SET NOT NULL;
ALTER TABLE iris._detector DROP COLUMN lane_type;

CREATE VIEW iris.detector AS
    SELECT det.name, controller, pin, r_node, lane_code, lane_number,
           abandoned, force_fail, auto_fail, field_length, fake, notes
    FROM iris._detector det
    JOIN iris.controller_io cio ON det.name = cio.name;

CREATE FUNCTION iris.detector_insert() RETURNS TRIGGER AS
    $detector_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'detector', NEW.controller, NEW.pin);
    INSERT INTO iris._detector (
        name, r_node, lane_code, lane_number, abandoned, force_fail, auto_fail,
        field_length, fake, notes
    ) VALUES (
        NEW.name, NEW.r_node, NEW.lane_code, NEW.lane_number, NEW.abandoned,
        NEW.force_fail, NEW.auto_fail, NEW.field_length, NEW.fake, NEW.notes
    );
    RETURN NEW;
END;
$detector_insert$ LANGUAGE plpgsql;

CREATE TRIGGER detector_insert_trig
    INSTEAD OF INSERT ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_insert();

CREATE FUNCTION iris.detector_update() RETURNS TRIGGER AS
    $detector_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._detector
       SET r_node = NEW.r_node,
           lane_code = NEW.lane_code,
           lane_number = NEW.lane_number,
           abandoned = NEW.abandoned,
           force_fail = NEW.force_fail,
           auto_fail = NEW.auto_fail,
           field_length = NEW.field_length,
           fake = NEW.fake,
           notes = NEW.notes
     WHERE name = OLD.name;
    RETURN NEW;
END;
$detector_update$ LANGUAGE plpgsql;

CREATE TRIGGER detector_update_trig
    INSTEAD OF UPDATE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_update();

CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE TRIGGER detector_notify_trig
    AFTER UPDATE ON iris._detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_notify();

CREATE TRIGGER detector_table_notify_trig
    AFTER INSERT OR DELETE ON iris._detector
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE FUNCTION iris.detector_label(VARCHAR(6), VARCHAR(4), VARCHAR(6),
    VARCHAR(4), VARCHAR(2), CHAR, SMALLINT, BOOLEAN)
    RETURNS TEXT AS $detector_label$
DECLARE
    rd ALIAS FOR $1;
    rdir ALIAS FOR $2;
    xst ALIAS FOR $3;
    xdir ALIAS FOR $4;
    xmod ALIAS FOR $5;
    lcode ALIAS FOR $6;
    lane_number ALIAS FOR $7;
    abandoned ALIAS FOR $8;
    xmd VARCHAR(2);
    lnum VARCHAR(2);
    suffix VARCHAR(5);
BEGIN
    IF rd IS NULL OR xst IS NULL THEN
        RETURN 'FUTURE';
    END IF;
    lnum = '';
    IF lane_number > 0 THEN
        lnum = TO_CHAR(lane_number, 'FM9');
    END IF;
    xmd = '';
    IF xmod != '@' THEN
        xmd = xmod;
    END IF;
    suffix = '';
    IF abandoned THEN
        suffix = '-ABND';
    END IF;
    RETURN rd || '/' || xdir || xmd || xst || rdir || lcode || lnum ||
           suffix;
END;
$detector_label$ LANGUAGE plpgsql;

CREATE VIEW detector_label_view AS
    SELECT d.name AS det_id,
           iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
                               d.lane_code, d.lane_number, d.abandoned)
           AS label
    FROM iris.detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_view AS
    SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
           iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
           d.lane_code, d.lane_number, d.abandoned) AS label,
           rnd.geo_loc, l.rd || '_' || l.road_dir AS cor_id,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           d.lane_number, d.field_length, lc.description AS lane_type,
           d.lane_code, d.abandoned, d.force_fail, d.auto_fail, c.condition,
           d.fake, d.notes
    FROM iris.detector d
    LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
    LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
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

DROP VIEW incident_update_view;
DROP VIEW incident_view;

ALTER TABLE event.incident
    ADD COLUMN lane_code VARCHAR(1) REFERENCES iris.lane_code;
UPDATE event.incident AS i
    SET lane_code = lt.dcode::VARCHAR(1)
    FROM iris.lane_type lt
    WHERE i.lane_type = lt.id;
ALTER TABLE event.incident ALTER COLUMN lane_code SET NOT NULL;
ALTER TABLE event.incident DROP COLUMN lane_type;

CREATE VIEW incident_view AS
    SELECT event_id, name, event_date, ed.description, road, d.direction,
           impact, event.incident_blocked_lanes(impact) AS blocked_lanes,
           event.incident_blocked_shoulders(impact) AS blocked_shoulders,
           cleared, confirmed, camera, lc.description AS lane_type, detail,
           replaces, lat, lon
    FROM event.incident i
    LEFT JOIN event.event_description ed ON i.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_code lc ON i.lane_code = lc.lcode;
GRANT SELECT ON incident_view TO PUBLIC;

CREATE VIEW incident_update_view AS
    SELECT iu.event_id, name, iu.event_date, ed.description, road,
           d.direction, iu.impact, iu.cleared, iu.confirmed, camera,
           lc.description AS lane_type, detail, replaces, lat, lon
    FROM event.incident i
    JOIN event.incident_update iu ON i.name = iu.incident
    LEFT JOIN event.event_description ed ON i.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_code lc ON i.lane_code = lc.lcode;
GRANT SELECT ON incident_update_view TO PUBLIC;

DROP VIEW inc_descriptor_view;

ALTER TABLE iris.inc_descriptor
    ADD COLUMN lane_code VARCHAR(1) REFERENCES iris.lane_code;
UPDATE iris.inc_descriptor AS i
    SET lane_code = lt.dcode::VARCHAR(1)
    FROM iris.lane_type lt
    WHERE i.lane_type = lt.id;
ALTER TABLE iris.inc_descriptor ALTER COLUMN lane_code SET NOT NULL;
ALTER TABLE iris.inc_descriptor DROP COLUMN lane_type;

CREATE OR REPLACE FUNCTION iris.inc_descriptor_ck() RETURNS TRIGGER AS
    $inc_descriptor_ck$
BEGIN
    -- Only incident event IDs are allowed
    IF NEW.event_desc_id < 21 OR NEW.event_desc_id > 24 THEN
        RAISE EXCEPTION 'invalid incident event_desc_id';
    END IF;
    -- Only mainline, cd road, merge and exit lane types are allowed
    IF NEW.lane_code != '' AND NEW.lane_code != 'C' AND
       NEW.lane_code != 'M' AND NEW.lane_code != 'X' THEN
        RAISE EXCEPTION 'invalid incident lane_code';
    END IF;
    RETURN NEW;
END;
$inc_descriptor_ck$ LANGUAGE plpgsql;

CREATE VIEW inc_descriptor_view AS
    SELECT id.name, ed.description AS event_description, detail,
           lc.description AS lane_type, multi
    FROM iris.inc_descriptor id
    JOIN event.event_description ed ON id.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.lane_code lc ON id.lane_code = lc.lcode;
GRANT SELECT ON inc_descriptor_view TO PUBLIC;

DROP VIEW inc_advice_view;

ALTER TABLE iris.inc_advice
    ADD COLUMN lane_code VARCHAR(1) REFERENCES iris.lane_code;
UPDATE iris.inc_advice AS i
    SET lane_code = lt.dcode::VARCHAR(1)
    FROM iris.lane_type lt
    WHERE i.lane_type = lt.id;
ALTER TABLE iris.inc_advice ALTER COLUMN lane_code SET NOT NULL;
ALTER TABLE iris.inc_advice DROP COLUMN lane_type;

CREATE OR REPLACE FUNCTION iris.inc_advice_ck() RETURNS TRIGGER AS
    $inc_advice_ck$
BEGIN
    -- Only mainline, cd road, merge and exit lane codes are allowed
    IF NEW.lane_code != '' AND NEW.lane_code != 'C' AND
       NEW.lane_code != 'M' AND NEW.lane_code != 'X' THEN
        RAISE EXCEPTION 'invalid incident lane_code';
    END IF;
    RETURN NEW;
END;
$inc_advice_ck$ LANGUAGE plpgsql;

CREATE VIEW inc_advice_view AS
    SELECT a.name, imp.description AS impact, lc.description AS lane_type,
           rng.description AS range, open_lanes, impacted_lanes, multi
    FROM iris.inc_advice a
    LEFT JOIN iris.inc_impact imp ON a.impact = imp.id
    LEFT JOIN iris.inc_range rng ON a.range = rng.id
    LEFT JOIN iris.lane_code lc ON a.lane_code = lc.lcode;
GRANT SELECT ON inc_advice_view TO PUBLIC;

DROP TABLE iris.lane_type;

COMMIT;
