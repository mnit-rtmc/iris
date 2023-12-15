\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.49.0', '5.50.0');

-- Drop font constraints
ALTER TABLE iris.font DROP CONSTRAINT font_height_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_width_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_line_sp_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_char_sp_ck;

-- Add font constraints with updated values
ALTER TABLE iris.font
    ADD CONSTRAINT font_height_ck
    CHECK (height > 0 AND height <= 32);
ALTER TABLE iris.font
    ADD CONSTRAINT font_width_ck
    CHECK (width >= 0 AND width <= 32);
ALTER TABLE iris.font
    ADD CONSTRAINT font_line_sp_ck
    CHECK (line_spacing >= 0 AND line_spacing <= 16);
ALTER TABLE iris.font
    ADD CONSTRAINT font_char_sp_ck
    CHECK (char_spacing >= 0 AND char_spacing <= 8);

COMMIT;
