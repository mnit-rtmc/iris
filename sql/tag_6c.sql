\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Delete any old ASTMv6 events (just in case)
DELETE FROM event.tag_read_event WHERE tag_type = 3;

-- Reuse ASTMv6 tag type for 6C protocol
UPDATE event.tag_type SET description = '6C' WHERE id = 3;

COMMIT;
