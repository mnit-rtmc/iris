\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add hashtag to map extent
ALTER TABLE iris.map_extent ADD COLUMN hashtag VARCHAR(16);

ALTER TABLE iris.map_extent ADD
    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$');

COMMIT;
