\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.54.0', '5.55.0');

-- Fix function permission problems
CREATE OR REPLACE FUNCTION iris.root_lbl(rd VARCHAR(6), rdir VARCHAR(4), xst VARCHAR(6),
    xdir VARCHAR(4), xmod VARCHAR(2), lmark VARCHAR(24)) RETURNS TEXT AS
$$
    SELECT rd || '/' || COALESCE(
        xdir || replace(xmod, '@', '') || xst,
        iris.landmark_abbrev(lmark)
    ) || rdir;
$$ LANGUAGE sql SECURITY DEFINER;

ALTER FUNCTION iris.root_lbl(VARCHAR(6), VARCHAR(4), VARCHAR(6), VARCHAR(4),
    VARCHAR(2), VARCHAR(24)
)
    SET search_path = pg_catalog, pg_temp;

CREATE OR REPLACE FUNCTION iris.parking_area_amenities(INTEGER)
    RETURNS SETOF iris.parking_area_amenities AS $parking_area_amenities$
DECLARE
    ms RECORD;
    b INTEGER;
BEGIN
    FOR ms IN SELECT bit, amenity FROM iris.parking_area_amenities LOOP
        b = 1 << ms.bit;
        IF ($1 & b) = b THEN
            RETURN NEXT ms;
        END IF;
    END LOOP;
END;
$parking_area_amenities$ LANGUAGE plpgsql SECURITY DEFINER;

ALTER FUNCTION iris.parking_area_amenities(INTEGER)
    SET search_path = pg_catalog, pg_temp;

ALTER TABLE iris.permission
    DROP CONSTRAINT base_resource_ck;
ALTER TABLE iris.permission
    ADD CONSTRAINT base_resource_ck
        CHECK (iris.resource_is_base(resource_n)) NOT VALID;

-- Add camera_hashtag view
CREATE VIEW iris.camera_hashtag AS
    SELECT name AS camera, hashtag
        FROM iris.hashtag
        WHERE resource_n = 'camera';

CREATE FUNCTION iris.camera_hashtag_insert() RETURNS TRIGGER AS
    $camera_hashtag_insert$
BEGIN
    INSERT INTO iris.hashtag (resource_n, name, hashtag)
         VALUES ('camera', NEW.camera, NEW.hashtag);
    RETURN NEW;
END;
$camera_hashtag_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_hashtag_insert_trig
    INSTEAD OF INSERT ON iris.camera_hashtag
    FOR EACH ROW EXECUTE FUNCTION iris.camera_hashtag_insert();

CREATE FUNCTION iris.camera_hashtag_delete() RETURNS TRIGGER AS
    $camera_hashtag_delete$
BEGIN
    DELETE FROM iris.hashtag WHERE resource_n = 'camera' AND name = OLD.camera;
    IF FOUND THEN
        RETURN OLD;
    ELSE
        RETURN NULL;
    END IF;
END;
$camera_hashtag_delete$ LANGUAGE plpgsql;

CREATE TRIGGER camera_hashtag_delete_trig
    INSTEAD OF DELETE ON iris.camera_hashtag
    FOR EACH ROW EXECUTE FUNCTION iris.camera_hashtag_delete();

-- Populate camera_hashtag with streamable cameras
INSERT INTO iris.camera_hashtag (camera, hashtag)
    SELECT name, '#LiveStream' FROM iris.camera WHERE streamable = 't';
INSERT INTO iris.camera_hashtag (camera, hashtag)
    SELECT name, '#Recorded' FROM iris.camera WHERE streamable = 't';

COMMIT;
