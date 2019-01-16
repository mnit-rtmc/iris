\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.85.0', '4.86.0');

-- Remove dash "-" from dir abbreviations
UPDATE iris.direction SET dir = 'NS' WHERE id = 5;
UPDATE iris.direction SET dir = 'EW' WHERE id = 6;

-- Replace parking_area_notify trigger
CREATE OR REPLACE FUNCTION iris.parking_area_notify() RETURNS TRIGGER AS
	$parking_area_notify$
BEGIN
	IF (NEW.time_stamp_static IS DISTINCT FROM OLD.time_stamp_static) THEN
		NOTIFY tms, 'parking_area';
	END IF;
	IF (NEW.time_stamp IS DISTINCT FROM OLD.time_stamp) THEN
		NOTIFY tms, 'parking_area_dynamic';
		NOTIFY tms, 'parking_area_archive';
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$parking_area_notify$ LANGUAGE plpgsql;

COMMIT;
