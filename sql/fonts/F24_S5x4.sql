\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('F24_S5x4', 11, 24, 0, 5, 4, 12345);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_48', 1, 24, 12, 'H4P8cO4HwDwDwDwDwDwDwDwDwDwDwDwDwDwDwDwD4HcOP8H4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_48', 'F24_S5x4', 48, 'F24_S5x4_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_49', 1, 24, 6, 'EMc88MMMMMMMMMMMMMMMMM//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_49', 'F24_S5x4', 49, 'F24_S5x4_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_50', 1, 24, 12, 'f4/8wOAHADADADADAHAOAcA4BwDgHAOAcA4AwAwAwAwA////');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_50', 'F24_S5x4', 50, 'F24_S5x4_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_51', 1, 24, 12, 'P4f84OwHADADADADAHAOAcD4D4AcAOAHADADADADwH4Of8P4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_51', 'F24_S5x4', 51, 'F24_S5x4_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_52', 1, 24, 12, 'AcA8A8BsBsDMDMGMGMMMMMYMYM////AMAMAMAMAMAMAMAMAM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_52', 'F24_S5x4', 52, 'F24_S5x4_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_53', 1, 24, 12, '////wAwAwAwAwAwAwA/4/8AOAHADADADADADADADAH4O/8P4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_53', 'F24_S5x4', 53, 'F24_S5x4_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_54', 1, 24, 12, 'H+P/cD4AwAwAwAwAwAwA34/84OwHwDwDwDwDwDwD4HcOP8H4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_54', 'F24_S5x4', 54, 'F24_S5x4_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_55', 1, 24, 10, '///wDAMAwDAMBgGAwDAYBgMAwGAYDAMBgGAwDAMA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_55', 'F24_S5x4', 55, 'F24_S5x4_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_56', 1, 24, 12, 'H4P8cO4HwDwDwDwD4HcOP8P8cO4HwDwDwDwDwDwD4HcOP8H4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_56', 'F24_S5x4', 56, 'F24_S5x4_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F24_S5x4_57', 1, 24, 12, 'H4P8cO4HwDwDwDwDwDwD4DcHP/H7ADADADADADADAHwO/8f4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F24_S5x4_57', 'F24_S5x4', 57, 'F24_S5x4_57');
