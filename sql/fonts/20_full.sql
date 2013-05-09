\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('20_full', 15, 20, 0, 5, 3, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_48', 1, 20, 9, 'Pj+4+DweDweDweDweDweDweDwfHfx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_48', '20_full', 48, '20_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_49', 1, 20, 6, 'Mc88MMMMMMMMMMMMMM//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_49', '20_full', 49, '20_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_50', 1, 20, 10, 'Px/uHwMAwDAMBwOBwOBwOBwOAwDAMA///w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_50', '20_full', 50, '20_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_51', 1, 20, 10, 'Px/uHwMAwDAMAwHB4HgHAMAwDAPA+Hf4/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_51', '20_full', 51, '20_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_52', 1, 20, 10, 'AwHA8Hw7HM4zDMMwz///AwDAMAwDAMAwDA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_52', '20_full', 52, '20_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_53', 1, 20, 10, '///8AwDAMAwDAP8f4BwDAMAwDAMA8H/5/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_53', '20_full', 53, '20_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_54', 1, 20, 10, 'P5/+DwDAMAwDAP8/7B8DwPA8DwPA+Hf4/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_54', '20_full', 54, '20_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_55', 1, 20, 9, '///AYDAYHAwOBgcDA4GBwMBgcDAYDAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_55', '20_full', 55, '20_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_56', 1, 20, 10, 'Px/uHwPA8DwPh3+Px/uHwPA8DwPA+Hf4/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_56', '20_full', 56, '20_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('20_full_57', 1, 20, 10, 'Px/uHwPA8DwPA+Df8/wDAMAwDAMA8H/5/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('20_full_57', '20_full', 57, '20_full_57');
