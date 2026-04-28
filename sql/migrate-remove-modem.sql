\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Remove modem devices
DROP VIEW modem_view;
DROP TABLE iris.modem;
DELETE FROM iris.resource_type WHERE name = 'modem';

COMMIT;
