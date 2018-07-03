\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.75.0', '4.76.0');

CREATE VIEW graphic_view AS
	SELECT name, g_number, bpp, height, width, pixels
	FROM iris.graphic;
GRANT SELECT ON graphic_view TO PUBLIC;

CREATE VIEW font_view AS
	SELECT name, f_number, height, width, line_spacing, char_spacing,
	       version_id
	FROM iris.font;
GRANT SELECT ON font_view TO PUBLIC;

CREATE VIEW glyph_view AS
	SELECT name, font, code_point, graphic
	FROM iris.glyph;
GRANT SELECT ON glyph_view TO PUBLIC;

COMMIT;
