\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.79.0', '4.80.0');

ALTER TABLE iris.font DROP CONSTRAINT font_height_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_width_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_line_sp_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_char_sp_ck;

ALTER TABLE iris.font
	ADD CONSTRAINT font_number_ck
	CHECK (f_number > 0 AND f_number <= 255);
ALTER TABLE iris.font
	ADD CONSTRAINT font_height_ck
	CHECK (height > 0 AND height <= 30);
ALTER TABLE iris.font
	ADD CONSTRAINT font_width_ck
	CHECK (width >= 0 AND width <= 12);
ALTER TABLE iris.font
	ADD CONSTRAINT font_line_sp_ck
	CHECK (line_spacing >= 0 AND line_spacing <= 9);
ALTER TABLE iris.font
	ADD CONSTRAINT font_char_sp_ck
	CHECK (char_spacing >= 0 AND char_spacing <= 6);

COMMIT;
