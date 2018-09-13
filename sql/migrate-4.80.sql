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

ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_number_ck
	CHECK (g_number > 0 AND g_number <= 999);

-- Drop old stuff
DROP VIEW glyph_view;
DROP TRIGGER font_ck_trig ON iris.font;
DROP FUNCTION iris.font_ck();
DROP TRIGGER glyph_ck_trig ON iris.glyph;
DROP FUNCTION iris.glyph_ck();
DROP TRIGGER graphic_ck_trig ON iris.graphic;
DROP FUNCTION iris.graphic_ck();

-- Copy width from graphic to glyph
ALTER TABLE iris.glyph ADD COLUMN width INTEGER;
UPDATE iris.glyph
   SET width = g.width
  FROM iris.graphic g
 WHERE glyph.graphic = g.name;
ALTER TABLE iris.glyph ALTER COLUMN width SET NOT NULL;

-- Copy pixels from graphic to glyph
ALTER TABLE iris.glyph ADD COLUMN pixels VARCHAR(128);
UPDATE iris.glyph
   SET pixels = g.pixels
  FROM iris.graphic g
 WHERE glyph.graphic = g.name;
ALTER TABLE iris.glyph ALTER COLUMN pixels SET NOT NULL;

-- Remove graphic column from glyph
ALTER TABLE iris.glyph DROP COLUMN graphic;

-- Remove glyph graphics
DELETE FROM iris.graphic WHERE g_number IS NULL;
ALTER TABLE iris.graphic ALTER COLUMN g_number SET NOT NULL;

-- Create new glyph_ck trigger
CREATE FUNCTION iris.glyph_ck() RETURNS TRIGGER AS
	$glyph_ck$
DECLARE
	f_width INTEGER;
BEGIN
	SELECT width INTO f_width FROM iris.font WHERE name = NEW.font;
	IF f_width > 0 AND f_width != NEW.width THEN
		RAISE EXCEPTION 'width does not match font';
	END IF;
	RETURN NEW;
END;
$glyph_ck$ LANGUAGE plpgsql;

CREATE TRIGGER glyph_ck_trig
	BEFORE INSERT OR UPDATE ON iris.glyph
	FOR EACH ROW EXECUTE PROCEDURE iris.glyph_ck();

-- Create new font_ck trigger
CREATE FUNCTION iris.font_ck() RETURNS TRIGGER AS
	$font_ck$
DECLARE
	g_width INTEGER;
BEGIN
	IF NEW.width > 0 THEN
		SELECT width INTO g_width FROM iris.glyph WHERE font = NEW.name;
		IF FOUND AND NEW.width != g_width THEN
			RAISE EXCEPTION 'width does not match glyph';
		END IF;
	END IF;
	RETURN NEW;
END;
$font_ck$ LANGUAGE plpgsql;

CREATE TRIGGER font_ck_trig
	BEFORE UPDATE ON iris.font
	FOR EACH ROW EXECUTE PROCEDURE iris.font_ck();

-- Add width and pixels to glyph_view
CREATE VIEW glyph_view AS
	SELECT name, font, code_point, width, pixels
	FROM iris.glyph;
GRANT SELECT ON glyph_view TO PUBLIC;

COMMIT;
