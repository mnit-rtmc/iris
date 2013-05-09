\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('20_full', 14, 20, 0, 5, 3, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_48', 1, 20, 9, 'Pj+4+DweDweDweDweDweDweDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_48', '20_full', 48, '20_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_49', 1, 20, 6, 'Mc88MMMMMMMMMMMMMM//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_49', '20_full', 49, '20_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_50', 1, 20, 9, 'Pj+4+DAYDAYDA4ODg4ODg4GAwGA///A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_50', '20_full', 50, '20_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_51', 1, 20, 9, 'Pj+4+DAYDAYDA4eDwHAYDAYDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_51', '20_full', 51, '20_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_52', 1, 20, 9, 'BgcHh8dnMxmMxmM///BgMBgMBgMBgMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_52', '20_full', 52, '20_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_53', 1, 20, 9, '///wGAwGAwGA/j+A4DAYDAYDAeH/z8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_53', '20_full', 53, '20_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_54', 1, 20, 9, 'Pz/4eAwGAwGA/n+w+DweDweDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_54', '20_full', 54, '20_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_55', 1, 20, 9, '///AYDAYHAwOBgcDA4GBwMBgcDAYDAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_55', '20_full', 55, '20_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_56', 1, 20, 9, 'Pj+4+DweDwfHfx8f3HweDweDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_56', '20_full', 56, '20_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_57', 1, 20, 9, 'Pj+4+DweDweD4b/P4DAYDAYDAeH/z8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_57', '20_full', 57, '20_full_57');
