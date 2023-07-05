\set ON_ERROR_STOP

BEGIN;

ALTER SCHEMA public OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.43.0', '5.44.0');

-- Fix catalog_insert function
DROP FUNCTION iris.catalog_insert() CASCADE;

CREATE FUNCTION iris.catalog_insert() RETURNS TRIGGER AS
    $catalog_insert$
BEGIN
    INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
    INSERT INTO iris._catalog (name, seq_num, description)
         VALUES (NEW.name, NEW.seq_num, NEW.description);
    RETURN NEW;
END;
$catalog_insert$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_insert_trig
    INSTEAD OF INSERT ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_insert();

-- Move dms_hashtag values to iris.hashtag table
INSERT INTO iris.hashtag (resource_n, name, hashtag) (
    SELECT 'dms', dms, hashtag
    FROM iris.dms_hashtag
);

DROP VIEW dms_toll_zone_view;
DROP VIEW dms_hashtag_view;
DROP TABLE iris.dms_hashtag;

CREATE VIEW iris.dms_hashtag AS
    SELECT name AS dms, hashtag FROM iris.hashtag WHERE resource_n = 'dms';

CREATE FUNCTION iris.dms_hashtag_insert() RETURNS TRIGGER AS
    $dms_hashtag_insert$
BEGIN
    INSERT INTO iris.hashtag (resource_n, name, hashtag)
         VALUES ('dms', NEW.dms, NEW.hashtag);
    RETURN NEW;
END;
$dms_hashtag_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_hashtag_insert_trig
    INSTEAD OF INSERT ON iris.dms_hashtag
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_hashtag_insert();

CREATE FUNCTION iris.dms_hashtag_delete() RETURNS TRIGGER AS
    $dms_hashtag_delete$
BEGIN
    DELETE FROM iris.hashtag WHERE resource_n = 'dms' AND name = OLD.dms;
    IF FOUND THEN
        RETURN OLD;
    ELSE
        RETURN NULL;
    END IF;
END;
$dms_hashtag_delete$ LANGUAGE plpgsql;

CREATE TRIGGER dms_hashtag_delete_trig
    INSTEAD OF DELETE ON iris.dms_hashtag
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_hashtag_delete();

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

COMMIT;
