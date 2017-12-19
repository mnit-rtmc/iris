\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.60.0', '4.61.0');

-- Make Pelco switcher protocol OBSOLETE
UPDATE iris.comm_protocol SET description = 'Pelco (OBSOLETE)' WHERE id = 12;

COMMIT;
