\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.68.0', '4.69.0');

-- Fix trailing space in iris.geo_location function
CREATE OR REPLACE FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT)
	RETURNS TEXT AS $geo_location$
DECLARE
	roadway ALIAS FOR $1;
	road_dir ALIAS FOR $2;
	cross_mod ALIAS FOR $3;
	cross_street ALIAS FOR $4;
	cross_dir ALIAS FOR $5;
	landmark ALIAS FOR $6;
	res TEXT;
BEGIN
	res = trim(roadway || ' ' || road_dir);
	IF char_length(cross_street) > 0 THEN
		RETURN trim(concat(res || ' ', cross_mod || ' ', cross_street),
		            ' ' || cross_dir);
	ELSIF char_length(landmark) > 0 THEN
		RETURN concat(res || ' ', '(' || landmark || ')');
	ELSE
		RETURN res;
	END IF;
END;
$geo_location$ LANGUAGE plpgsql;

COMMIT;
