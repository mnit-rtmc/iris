\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add drop_id check constraint
ALTER TABLE iris.controller
    ADD CONSTRAINT controller_drop_id_check
    CHECK (drop_id >= 0 AND drop_id <= 65535);

COMMIT;
