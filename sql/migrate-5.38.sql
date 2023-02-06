\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.37.0', '5.38.0');

-- NOTE: This has checks for MnDOT group names.
--       For other agencies, it may need agency-specific tweaks
CREATE FUNCTION sign_group_hashtag(TEXT) RETURNS TEXT AS $sgh$
DECLARE
    sign_group ALIAS FOR $1;
    words TEXT[];
    word TEXT;
    res TEXT := '#';
BEGIN
    IF sign_group = 'GATES' OR sign_group = 'MOA' THEN
        RETURN '#OneLine';
    ELSIF sign_group LIKE '%2LN%' THEN
        RETURN '#TwoLine';
    ELSIF sign_group IN (SELECT name FROM sign_group_small) THEN
        RETURN '#Small';
    ELSIF sign_group = 'VPARK' OR sign_group = 'TAD_GARAGE' THEN
        RETURN '#TadGarage';
    ELSIF sign_group LIKE '%TEST%' THEN
        RETURN '#Test';
    ELSIF sign_group LIKE '%PIXEL%' OR sign_group LIKE '%STD%' THEN
        RETURN '#ThreeLine';
    END IF;
    words = regexp_split_to_array(
        regexp_replace(sign_group, '(\d+)', '_\1_', 'g'),
        '_'
    );
    FOREACH word IN ARRAY words
    LOOP
        IF word = 'ALL' THEN
            RETURN res;
        ELSIF word = 'ACT' THEN
            res = res || 'a';
        ELSIF word = 'C' AND sign_group LIKE 'C_%' THEN
            res = res || 'Cty';
        ELSIF word = 'MNPASS' THEN
            res = res || 'Ezpass';
        ELSIF word = 'PX' OR word = 'REV' OR word = 'WIDE' THEN
            res = res || 'Px';
        ELSIF length(word) > 2 AND word != 'VDL' THEN
            res = res || initcap(word);
        ELSIF word != 'LG' AND word != 'SM' THEN
            res = res || upper(word);
        END IF;
    END LOOP;
    RETURN res;
END;
$sgh$ LANGUAGE plpgsql;

-- Make temp table for "small" sign groups
CREATE TEMP TABLE sign_group_small AS (
    SELECT st.sign_group AS name
    FROM iris.sign_text st
    JOIN iris.sign_group sg ON st.sign_group = sg.name
    WHERE local = false AND line > 4
    GROUP BY st.sign_group
);

-- Make temp table for associating sign groups with patterns
CREATE TEMP TABLE sign_group_pattern (
    sign_group VARCHAR(20) PRIMARY KEY,
    pattern VARCHAR(20) NOT NULL,
    hashtag VARCHAR(16)
);

-- By default, start all sign groups with 3 line pattern
INSERT INTO sign_group_pattern (sign_group, pattern) (
    SELECT name, '.3.LINE'
    FROM iris.sign_group
    GROUP BY name
);
UPDATE sign_group_pattern
    SET hashtag = sign_group_hashtag(sign_group)
    WHERE sign_group IN
(
    SELECT st.sign_group
    FROM iris.sign_text st
    JOIN iris.sign_group sg ON st.sign_group = sg.name
    WHERE local = true
    GROUP BY st.sign_group
);
UPDATE sign_group_pattern SET pattern = '.1.LINE' WHERE sign_group = 'GATES';
UPDATE sign_group_pattern SET pattern = '.2.LINE' WHERE sign_group IN (
    SELECT st.sign_group
    FROM iris.sign_text st
    JOIN iris.sign_group sg ON st.sign_group = sg.name
    WHERE line > 3
    GROUP BY st.sign_group
);
UPDATE sign_group_pattern SET pattern = '.2.PAGE' WHERE sign_group IN (
    SELECT st.sign_group
    FROM iris.sign_text st
    JOIN iris.sign_group sg ON st.sign_group = sg.name
    WHERE line > 4
    GROUP BY st.sign_group
);
UPDATE sign_group_pattern SET pattern = '.4.LINE' WHERE sign_group = 'V52N34';
UPDATE sign_group_pattern SET pattern = '.4.LINE.GARAGE' WHERE sign_group IN (
    'TAD_GARAGE', 'V394E16', 'V394E17'
);

-- Remove DMS query message enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_querymsg_enable';

-- Rename sign_text to msg_line
INSERT INTO iris.resource_type (name) VALUES ('msg_line');

UPDATE iris.privilege SET type_n = 'msg_line'
    WHERE type_n = 'sign_text';

DELETE FROM iris.resource_type WHERE name = 'sign_text';

DROP VIEW sign_group_text_view;
DROP VIEW sign_text_view;

CREATE TABLE iris.msg_line (
    name VARCHAR(10) PRIMARY KEY,
    msg_pattern VARCHAR(20) NOT NULL REFERENCES iris.msg_pattern,
    restrict_hashtag VARCHAR(16),
    line SMALLINT NOT NULL,
    multi VARCHAR(64) NOT NULL,
    rank SMALLINT NOT NULL,
    CONSTRAINT msg_line_line CHECK ((line >= 1) AND (line <= 12)),
    CONSTRAINT msg_line_rank CHECK ((rank >= 1) AND (rank <= 99))
);

-- temporary index for conflict checks
CREATE UNIQUE INDEX temp_conflict_idx
    ON iris.msg_line (msg_pattern, line, multi);

ALTER TABLE iris.msg_pattern ADD COLUMN compose_hashtag VARCHAR(16);

UPDATE iris.msg_pattern
    SET compose_hashtag = sign_group_hashtag(sign_group)
    WHERE sign_group IS NOT NULL;

-- Insert/update basic message patterns
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.1.LINE', '', '#OneLine')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#OneLine';
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.2.LINE', '[np]', '#TwoLine')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#TwoLine';
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.3.LINE', '', '#ThreeLine')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#ThreeLine';
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.4.LINE', '', '#FourLine')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#FourLine';
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.2.PAGE', '[np]', '#Small')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#Small';

-- Some MnDOT-specific patterns
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.3.LINE.370', '[tr156,1,214,56]', '#Ezpass370Px')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#Ezpass370Px';
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.3.LINE.390', '[tr1,1,224,70]', '#V94W08')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#V94W08';
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.3.LINE.592', '[tr243,1,350,96]', '#Ezpass592Px')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#Ezpass592Px';
INSERT INTO iris.msg_pattern (name, multi, compose_hashtag)
    VALUES ('.4.LINE.GARAGE', '[fo6][tr1,1,150,56]', '#TadGarage')
    ON CONFLICT (name) DO UPDATE SET compose_hashtag = '#TadGarage';

INSERT INTO iris.msg_line (
    name, msg_pattern, restrict_hashtag, line, multi, rank
) (
    SELECT DISTINCT ON (pattern, line, multi)
        'ml_' || ROW_NUMBER() OVER (ORDER BY name),
        pattern, hashtag, line, multi, rank
    FROM iris.sign_text st
    JOIN sign_group_pattern p ON st.sign_group = p.sign_group
    ORDER BY pattern, line, multi, hashtag DESC, rank
);

-- ON CONFLICT (msg_pattern, line, multi) DO NOTHING;

DROP INDEX iris.temp_conflict_idx;

CREATE VIEW msg_line_view AS
    SELECT name, msg_pattern, restrict_hashtag, line, multi, rank
    FROM iris.msg_line;
GRANT SELECT ON msg_line_view TO PUBLIC;

-- Replace sign groups with hashtags
DELETE FROM iris.privilege WHERE type_n = 'dms_sign_group';
DELETE FROM iris.privilege WHERE type_n = 'sign_group';
DELETE FROM iris.resource_type WHERE name = 'dms_sign_group';
DELETE FROM iris.resource_type WHERE name = 'sign_group';

DROP VIEW dms_sign_group_view;
DROP VIEW sign_group_view;
DROP VIEW msg_pattern_view;

DROP VIEW dms_toll_zone_view;
DROP VIEW dms_action_view;

ALTER TABLE iris.dms_action ADD COLUMN dms_hashtag VARCHAR(16);
UPDATE iris.dms_action
    SET dms_hashtag = sign_group_hashtag(sign_group);
ALTER TABLE iris.dms_action ALTER COLUMN dms_hashtag SET NOT NULL;
ALTER TABLE iris.dms_action DROP COLUMN sign_group;

CREATE VIEW dms_action_view AS
    SELECT name, action_plan, phase, dms_hashtag, msg_pattern,
           beacon_enabled, msg_priority
    FROM iris.dms_action;
GRANT SELECT ON dms_action_view TO PUBLIC;

CREATE TABLE iris.dms_hashtag (
    dms VARCHAR(20) NOT NULL REFERENCES iris._dms,
    hashtag VARCHAR(16) NOT NULL
);
ALTER TABLE iris.dms_hashtag ADD PRIMARY KEY (dms, hashtag);

INSERT INTO iris.dms_hashtag (
    SELECT dms, sign_group_hashtag(sign_group) AS hashtag
    FROM iris.dms_sign_group
) ON CONFLICT DO NOTHING;

CREATE VIEW dms_hashtag_view AS
    SELECT dms, hashtag
    FROM iris.dms_hashtag;
GRANT SELECT ON dms_hashtag_view TO PUBLIC;

ALTER TABLE iris.alert_config ADD COLUMN dms_hashtag VARCHAR(16);
UPDATE iris.alert_config
    SET dms_hashtag = sign_group_hashtag(sign_group)
    WHERE sign_group IS NOT NULL;
ALTER TABLE iris.alert_config DROP COLUMN sign_group;

ALTER TABLE iris.alert_message ADD COLUMN sign_config VARCHAR(16);
UPDATE iris.alert_message AS m
    SET sign_config = p.sign_config
    FROM iris.msg_pattern p
    WHERE m.msg_pattern = p.name;

ALTER TABLE cap.alert_info ADD COLUMN all_hashtag VARCHAR(16);
UPDATE cap.alert_info
    SET all_hashtag = sign_group_hashtag(sign_group);
ALTER TABLE cap.alert_info ALTER COLUMN all_hashtag SET NOT NULL;
ALTER TABLE cap.alert_info DROP COLUMN sign_group;

DROP VIEW lane_use_multi_view;

ALTER TABLE iris.lane_use_multi ADD COLUMN dms_hashtag VARCHAR(16);
UPDATE iris.lane_use_multi AS m
    SET dms_hashtag = compose_hashtag
    FROM iris.msg_pattern p
    WHERE m.msg_pattern = p.name;
ALTER TABLE iris.lane_use_multi ALTER COLUMN dms_hashtag SET NOT NULL;

CREATE VIEW lane_use_multi_view AS
    SELECT name, indication, msg_num, msg_pattern, dms_hashtag
    FROM iris.lane_use_multi;
GRANT SELECT ON lane_use_multi_view TO PUBLIC;

ALTER TABLE iris.msg_pattern DROP COLUMN sign_config;
ALTER TABLE iris.msg_pattern DROP COLUMN sign_group;

DROP TABLE iris.sign_text;
DROP TABLE iris.dms_sign_group;
DROP TABLE iris.sign_group;

CREATE VIEW msg_pattern_view AS
    SELECT name, multi, compose_hashtag
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, dh.hashtag, tz.state, toll_zone, action_plan, da.msg_pattern
    FROM dms_action_view da
    JOIN iris.dms_hashtag dh
    ON da.dms_hashtag = dh.hashtag
    JOIN iris.msg_pattern mp
    ON da.msg_pattern = mp.name
    JOIN iris.msg_pattern_toll_zone tz
    ON da.msg_pattern = tz.msg_pattern;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

DROP FUNCTION sign_group_hashtag(TEXT);

COMMIT;
