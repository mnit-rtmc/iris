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

-- Add software_make and software_model to sign_config
ALTER TABLE iris.sign_config ADD COLUMN software_make VARCHAR(32);
UPDATE iris.sign_config SET software_make = '';
ALTER TABLE iris.sign_config ALTER COLUMN software_make SET NOT NULL;
ALTER TABLE iris.sign_config ADD COLUMN software_model VARCHAR(32);
UPDATE iris.sign_config SET software_model = '';
ALTER TABLE iris.sign_config ALTER COLUMN software_model SET NOT NULL;

COMMIT;
