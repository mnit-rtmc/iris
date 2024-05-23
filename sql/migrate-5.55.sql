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

COMMIT;
