\set ON_ERROR_STOP

-- Adds system-attribute entry for dms_message_tooltip_enable.
-- This allows enabling a DMS tooltip that will show the current message on
-- a sign and who posted it.

SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.system_attribute (name, value) VALUES ('dms_message_tooltip_enable', 'false');

COMMIT;
