\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.26.0', '5.27.0');

-- Allow higher code points
ALTER TABLE iris.glyph DROP CONSTRAINT IF EXISTS glyph_code_point_ck;
ALTER TABLE iris.glyph
    ADD CONSTRAINT glyph_code_point_ck
    CHECK (code_point > 0 AND code_point < 65536);

-- Increase maximum glyph width to 32 pixels
ALTER TABLE iris.glyph DROP CONSTRAINT IF EXISTS glyph_width_ck;
ALTER TABLE iris.glyph
    ADD CONSTRAINT glyph_width_ck
    CHECK (width >= 0 AND width <= 32);

-- Add duration to sign events
DROP VIEW recent_sign_event_view;
DROP VIEW sign_event_view;

ALTER TABLE event.sign_event ADD COLUMN multi VARCHAR(1024);
ALTER TABLE event.sign_event ADD COLUMN duration INTEGER;
UPDATE event.sign_event SET multi = message::VARCHAR(1024);
ALTER TABLE event.sign_event DROP COLUMN message;

CREATE FUNCTION event.multi_message(VARCHAR(1024))
    RETURNS TEXT AS $multi_message$
DECLARE
    multi ALIAS FOR $1;
BEGIN
    RETURN regexp_replace(
        replace(
            replace(multi, '[nl]', E'\n'),
            '[np]', E'\n'
        ),
        '\[.+?\]', ' ', 'g'
    );
END;
$multi_message$ LANGUAGE plpgsql;

CREATE VIEW sign_event_view AS
    SELECT event_id, event_date, description, device_id,
           event.multi_message(multi) as message, multi, owner, duration
    FROM event.sign_event JOIN event.event_description
    ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
    SELECT event_id, event_date, description, device_id, message, multi,
           owner, duration
    FROM sign_event_view
    WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

-- Add (supported) multi tag bits
DROP VIEW sign_detail_view;

CREATE TABLE iris.multi_tag (
    bit INTEGER PRIMARY KEY,
    tag VARCHAR(3) UNIQUE NOT NULL
);
ALTER TABLE iris.multi_tag ADD CONSTRAINT multi_tag_bit_ck
    CHECK (bit >= 0 AND bit < 32);

COPY iris.multi_tag (bit, tag) FROM stdin;
0	cb
1	cf
2	fl
3	fo
4	g
5	hc
6	jl
7	jp
8	ms
9	mvt
10	nl
11	np
12	pt
13	sc
14	f1
15	f2
16	f3
17	f4
18	f5
19	f6
20	f7
21	f8
22	f9
23	f10
24	f11
25	f12
26	tr
27	cr
28	pb
\.

CREATE FUNCTION iris.multi_tags(INTEGER)
    RETURNS SETOF iris.multi_tag AS $multi_tags$
DECLARE
    mt RECORD;
    b INTEGER;
BEGIN
    FOR mt IN SELECT bit, tag FROM iris.multi_tag LOOP
        b = 1 << mt.bit;
        IF ($1 & b) = b THEN
            RETURN NEXT mt;
        END IF;
    END LOOP;
END;
$multi_tags$ LANGUAGE plpgsql;

CREATE FUNCTION iris.multi_tags_str(INTEGER)
    RETURNS text AS $multi_tags_str$
DECLARE
    bits ALIAS FOR $1;
BEGIN
    RETURN (
        SELECT string_agg(mt.tag, ', ') FROM (
            SELECT bit, tag FROM iris.multi_tags(bits) ORDER BY bit
        ) AS mt
    );
END;
$multi_tags_str$ LANGUAGE plpgsql;

CREATE VIEW sign_detail_view AS
    SELECT name, dt.description AS dms_type, portable, technology, sign_access,
           legend, beacon_type, hardware_make, hardware_model, software_make,
           software_model,iris.multi_tags_str(supported_tags) AS supported_tags,
           max_pages, max_multi_len, beacon_activation_flag, pixel_service_flag
    FROM iris.sign_detail
    JOIN iris.dms_type dt ON sign_detail.dms_type = dt.id;
GRANT SELECT ON sign_detail_view TO PUBLIC;

COMMIT;
