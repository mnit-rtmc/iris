\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.26.0', '5.27.0');

-- Increase maximum glyph width to 32 pixels
ALTER TABLE iris.glyph DROP CONSTRAINT glyph_width_ck;
ALTER TABLE iris.glyph
    ADD CONSTRAINT glyph_width_ck
    CHECK (width >= 0 AND width <= 32);

COMMIT;
