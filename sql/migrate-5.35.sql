\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.34.0', '5.35.0');

INSERT INTO iris.parking_area_amenities (bit, amenity)
    VALUES (15, 'Family restroom');

COMMIT;
