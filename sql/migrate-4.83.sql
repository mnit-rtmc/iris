\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.82.0', '4.83.0');

-- Add Cohu Helios protocol
INSERT INTO iris.comm_protocol VALUES (40, 'Cohu Helios PTZ');

-- Fixed permission
GRANT SELECT ON detector_auto_fail_view TO PUBLIC;

COMMIT;
