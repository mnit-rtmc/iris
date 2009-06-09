\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.89.0' WHERE name = 'database_version';

CREATE TABLE iris.graphic (
	name VARCHAR(20) PRIMARY KEY,
	bpp INTEGER NOT NULL,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	pixels TEXT NOT NULL
);

CREATE TABLE iris.font (
	name VARCHAR(16) PRIMARY KEY,
	f_number INTEGER UNIQUE NOT NULL,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	line_spacing INTEGER NOT NULL,
	char_spacing INTEGER NOT NULL,
	version_id INTEGER NOT NULL
);

CREATE TABLE iris.glyph (
	name VARCHAR(20) PRIMARY KEY,
	font VARCHAR(16) NOT NULL REFERENCES iris.font(name),
	code_point INTEGER NOT NULL,
	graphic VARCHAR(20) NOT NULL REFERENCES iris.graphic(name)
);

CREATE TABLE iris.lane_use_graphic (
	name VARCHAR(10) PRIMARY KEY,
	indication INTEGER NOT NULL REFERENCES iris.lane_use_indication,
	g_number INTEGER NOT NULL UNIQUE,
	graphic VARCHAR(20) NOT NULL REFERENCES iris.graphic(name),
	page INTEGER NOT NULL,
	on_time INTEGER NOT NULL
);

CREATE UNIQUE INDEX lane_use_graphic_ipage ON iris.lane_use_graphic
	USING btree (indication, page);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	(SELECT name, bpp, height, width, pixels FROM graphic);
INSERT INTO iris.font (name, f_number, height, width, line_spacing,
	char_spacing, version_id)
	(SELECT name, f_number, height, width, line_spacing, char_spacing,
	version_id FROM font);
INSERT INTO iris.glyph (name, font, code_point, graphic)
	(SELECT name, font, code_point, graphic FROM glyph);

DROP TABLE glyph;
DROP TABLE font;
DROP TABLE graphic;

DROP FUNCTION graphic_bpp(TEXT);
DROP FUNCTION graphic_height(TEXT);
DROP FUNCTION graphic_width(TEXT);
DROP FUNCTION font_height(TEXT);
DROP FUNCTION font_width(TEXT);
DROP FUNCTION font_glyph(TEXT);
DROP FUNCTION graphic_glyph(TEXT);

CREATE FUNCTION graphic_bpp(VARCHAR(20)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		b INTEGER;
	BEGIN SELECT INTO b bpp FROM iris.graphic WHERE name = n;
		RETURN b;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_height(VARCHAR(20)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM iris.graphic WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_width(VARCHAR(20)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM iris.graphic WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION glyph_font(VARCHAR(20)) RETURNS VARCHAR(16) AS '
	DECLARE n ALIAS FOR $1;
		f VARCHAR(16);
	BEGIN SELECT INTO f font FROM iris.glyph WHERE graphic = n;
		RETURN f;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_height(VARCHAR(16)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM iris.font WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_width(VARCHAR(16)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM iris.font WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_graphic(VARCHAR(16)) RETURNS VARCHAR(20) AS '
	DECLARE n ALIAS FOR $1;
		g TEXT;
	BEGIN SELECT INTO g graphic FROM iris.glyph WHERE font = n;
		RETURN g;
	END;'
LANGUAGE PLPGSQL;

ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_bpp_ck
	CHECK (bpp = 1 OR bpp = 8 OR bpp = 24);
ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_font_ck
	CHECK (glyph_font(name) IS NULL OR
		(font_height(glyph_font(name)) = height AND
		(font_width(glyph_font(name)) = 0 OR
		font_width(glyph_font(name)) = width)));

ALTER TABLE iris.glyph
	ADD CONSTRAINT glyph_bpp_ck
	CHECK (graphic_bpp(graphic) = 1);
ALTER TABLE iris.glyph
	ADD CONSTRAINT glyph_size_ck
	CHECK (font_height(font) = graphic_height(graphic) AND
		(font_width(font) = 0 OR
		font_width(font) = graphic_width(graphic)));

ALTER TABLE iris.font
	ADD CONSTRAINT font_height_ck
	CHECK (height > 0 AND height < 25);
ALTER TABLE iris.font
	ADD CONSTRAINT font_width_ck
	CHECK (width >= 0 AND width < 25);
ALTER TABLE iris.font
	ADD CONSTRAINT font_line_sp_ck
	CHECK (line_spacing >= 0 AND line_spacing < 9);
ALTER TABLE iris.font
	ADD CONSTRAINT font_char_sp_ck
	CHECK (char_spacing >= 0 AND char_spacing < 9);
ALTER TABLE iris.font
	ADD CONSTRAINT font_graphic_ck
	CHECK (font_graphic(name) IS NULL OR
		(graphic_height(font_graphic(name)) = height AND
		(width = 0 OR graphic_width(font_graphic(name)) = width)));
