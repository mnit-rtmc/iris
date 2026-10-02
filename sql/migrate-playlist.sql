\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Limit number of entries in a playlist
ALTER TABLE iris.play_list_entry
    ADD CONSTRAINT ordinal_ck
    CHECK (ordinal >= 0 AND ordinal <= 300);

COMMIT;
