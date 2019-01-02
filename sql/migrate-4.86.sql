\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.85.0', '4.86.0');

-- Remove dash "-" from dir abbreviations
UPDATE iris.direction SET dir = 'NS' WHERE id = 5;
UPDATE iris.direction SET dir = 'EW' WHERE id = 6;

COMMIT;
