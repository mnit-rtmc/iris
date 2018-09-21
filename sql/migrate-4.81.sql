\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.80.0', '4.81.0');

-- Add GPS view
CREATE VIEW gps_view AS
	SELECT name, controller, pin, notes, latest_poll, latest_sample,
	       lat, lon
	FROM iris.gps;

COMMIT;
