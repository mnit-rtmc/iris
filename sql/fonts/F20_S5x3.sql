\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('F20_S5x3', 8, 20, 0, 5, 3, 12345);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_48', 1, 20, 9, 'Pj+4+DweDweDweDweDweDweDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_48', 'F20_S5x3', 48, 'F20_S5x3_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_49', 1, 20, 4, 'Ju5mZmZmZmZm/w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_49', 'F20_S5x3', 49, 'F20_S5x3_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_50', 1, 20, 9, 'Pj+4+DgYDAYDA4ODg4ODg4GAwGA///A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_50', 'F20_S5x3', 50, 'F20_S5x3_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_51', 1, 20, 9, 'Pj+4+DAYDAYHBw8BwHAYDAYDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_51', 'F20_S5x3', 51, 'F20_S5x3_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_52', 1, 20, 9, 'BweHw2OxmczG42G///AwGAwGAwGAwGA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_52', 'F20_S5x3', 52, 'F20_S5x3_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_53', 1, 20, 9, '///wGAwGAwGA/n+A4DAYDAYDAeH/z8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_53', 'F20_S5x3', 53, 'F20_S5x3_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_54', 1, 20, 9, 'Pz/4eAwGAwGAwH8/2HweDweDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_54', 'F20_S5x3', 54, 'F20_S5x3_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_55', 1, 20, 9, '///AYHAwOBgcDAYHAwOBgcDAYHAwGAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_55', 'F20_S5x3', 55, 'F20_S5x3_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_56', 1, 20, 9, 'Pj+4+DweDwfHfx8f3HweDweDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_56', 'F20_S5x3', 56, 'F20_S5x3_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F20_S5x3_57', 1, 20, 9, 'Pj+4+DweDweD4b/P4DAYDAYDAeH/z8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F20_S5x3_57', 'F20_S5x3', 57, 'F20_S5x3_57');
