\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.74.0', '4.75.0');

-- Drop old view
DROP VIEW parking_area_view;

-- Change type of parking_area.amenities
ALTER TABLE iris.parking_area DROP COLUMN amenities;
ALTER TABLE iris.parking_area ADD COLUMN amenities INTEGER;

-- Add parking_area_amenities look-up table and function
CREATE TABLE iris.parking_area_amenities (
	bit INTEGER PRIMARY KEY,
	amenity VARCHAR(32) NOT NULL
);
ALTER TABLE iris.parking_area_amenities ADD CONSTRAINT amenity_bit_ck
	CHECK (bit >= 0 AND bit < 32);

CREATE FUNCTION iris.parking_area_amenities(INTEGER)
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
$parking_area_amenities$ LANGUAGE plpgsql;

-- Populate parking_area_amenities look-up table
COPY iris.parking_area_amenities (bit, amenity) FROM stdin;
0	Flush toilet
1	Assisted restroom
2	Drinking fountain
3	Shower
4	Picnic table
5	Picnic shelter
6	Pay phone
7	TTY pay phone
8	Wireless internet
9	ATM
10	Vending machine
11	Shop
12	Play area
13	Pet excercise area
14	Interpretive information
\.

-- Add updated parking_area_view
CREATE VIEW parking_area_view AS
	SELECT pa.name, site_id, time_stamp_static, relevant_highway,
	       reference_post, exit_id, facility_name, street_adr, city, state,
	       zip, time_zone, ownership, capacity, low_threshold,
	       (SELECT string_agg(a.amenity, ', ') FROM
	        (SELECT bit, amenity FROM iris.parking_area_amenities(amenities)
	         ORDER BY bit) AS a) AS amenities,
	       time_stamp, reported_available, true_available, trend, open,
	       trust_data, last_verification_check, verification_check_amplitude,
	       p1.camera AS camera_1, p2.camera AS camera_2,
	       p3.camera AS camera_3,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon, sa.value AS camera_image_base_url
	FROM iris.parking_area pa
	LEFT JOIN iris.camera_preset p1 ON preset_1 = p1.name
	LEFT JOIN iris.camera_preset p2 ON preset_2 = p2.name
	LEFT JOIN iris.camera_preset p3 ON preset_3 = p3.name
	LEFT JOIN geo_loc_view l ON pa.geo_loc = l.name
	LEFT JOIN iris.system_attribute sa ON sa.name = 'camera_image_base_url';
GRANT SELECT ON parking_area_view TO PUBLIC;

-- insert camera_image_base_url system attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_image_base_url', '');

COMMIT;
