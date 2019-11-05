\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.8.0', '5.9.0');

-- Change Junction to Jct in road_modifier table
UPDATE iris.road_modifier SET modifier = 'N Jct' WHERE id = 5;
UPDATE iris.road_modifier SET modifier = 'S Jct' WHERE id = 6;
UPDATE iris.road_modifier SET modifier = 'E Jct' WHERE id = 7;
UPDATE iris.road_modifier SET modifier = 'W Jct' WHERE id = 8;

CREATE OR REPLACE FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT)
	RETURNS TEXT AS $geo_location$
DECLARE
	roadway ALIAS FOR $1;
	road_dir ALIAS FOR $2;
	cross_mod ALIAS FOR $3;
	cross_street ALIAS FOR $4;
	cross_dir ALIAS FOR $5;
	landmark ALIAS FOR $6;
	corridor TEXT;
	xloc TEXT;
	lmrk TEXT;
BEGIN
	corridor = trim(roadway || concat(' ', road_dir));
	xloc = trim(concat(cross_mod, ' ') || cross_street
	    || concat(' ', cross_dir));
	lmrk = replace('(' || landmark || ')', '()', '');
	RETURN NULLIF(trim(concat(corridor, ' ' || xloc, ' ' || lmrk)), '');
END;
$geo_location$ LANGUAGE plpgsql;

COMMIT;
