SET SESSION AUTHORIZATION 'tms';

DROP TABLE font;
DROP TABLE character_list;
DROP TABLE character;

CREATE FUNCTION graphic_bpp(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		b INTEGER;
	BEGIN SELECT INTO b bpp FROM graphic WHERE name = n;
		RETURN b;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_height(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM font WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_width(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM font WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_height(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM graphic WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_width(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM graphic WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_glyph(TEXT) RETURNS TEXT AS '
	DECLARE n ALIAS FOR $1;
		f TEXT;
	BEGIN SELECT INTO f font FROM glyph WHERE graphic = n;
		RETURN f;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_glyph(TEXT) RETURNS TEXT AS '
	DECLARE n ALIAS FOR $1;
		g TEXT;
	BEGIN SELECT INTO g graphic FROM glyph WHERE font = n;
		RETURN g;
	END;'
LANGUAGE PLPGSQL;

CREATE TABLE graphic (
	name TEXT PRIMARY KEY,
	bpp INTEGER NOT NULL,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	pixels TEXT NOT NULL
);
CREATE TABLE font (
	name TEXT PRIMARY KEY,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	line_spacing INTEGER NOT NULL,
	char_spacing INTEGER NOT NULL,
	version_id INTEGER NOT NULL
);
ALTER TABLE font
	ADD CONSTRAINT font_height_ck
	CHECK (height > 0 AND height < 25);
ALTER TABLE font
	ADD CONSTRAINT font_width_ck
	CHECK (width >= 0 AND width < 25);
ALTER TABLE font
	ADD CONSTRAINT font_line_sp_ck
	CHECK (line_spacing >= 0 AND line_spacing < 9);
ALTER TABLE font
	ADD CONSTRAINT font_char_sp_ck
	CHECK (char_spacing >= 0 AND char_spacing < 9);
CREATE TABLE glyph (
	name TEXT PRIMARY KEY,
	font TEXT NOT NULL,
	code_point INTEGER NOT NULL,
	graphic TEXT NOT NULL
);
ALTER TABLE glyph
	ADD CONSTRAINT fk_glyph_font FOREIGN KEY (font) REFERENCES font(name);
ALTER TABLE glyph
	ADD CONSTRAINT fk_glyph_graphic FOREIGN KEY (graphic)
	REFERENCES graphic(name);
ALTER TABLE glyph
	ADD CONSTRAINT glyph_bpp_ck
	CHECK (graphic_bpp(graphic) = 1);
ALTER TABLE glyph
	ADD CONSTRAINT glyph_size_ck
	CHECK (font_height(font) = graphic_height(graphic) AND
		(font_width(font) = 0 OR
		font_width(font) = graphic_width(graphic)));
ALTER TABLE graphic
	ADD CONSTRAINT graphic_font_ck
	CHECK (font_glyph(name) IS NULL OR
		(font_height(font_glyph(name)) = height AND
		(font_width(font_glyph(name)) = 0 OR
		font_width(font_glyph(name)) = width)));
ALTER TABLE font
	ADD CONSTRAINT font_graphic_ck
	CHECK (graphic_glyph(name) IS NULL OR
		(graphic_height(graphic_glyph(name)) = height AND
		(width = 0 OR graphic_width(graphic_glyph(name)) = width)));

REVOKE ALL ON TABLE graphic FROM PUBLIC;
GRANT SELECT ON TABLE graphic TO PUBLIC;
REVOKE ALL ON TABLE font FROM PUBLIC;
GRANT SELECT ON TABLE font TO PUBLIC;
REVOKE ALL ON TABLE glyph FROM PUBLIC;
GRANT SELECT ON TABLE glyph TO PUBLIC;
