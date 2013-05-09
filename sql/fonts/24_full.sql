\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('24_full', 16, 24, 0, 5, 4, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_48', 1, 24, 12, 'H4P8cO4HwDwDwDwDwDwDwDwDwDwDwDwDwDwDwDwD4HcOP8H4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_48', '24_full', 48, '24_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_49', 1, 24, 6, 'EMc88MMMMMMMMMMMMMMMMM//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_49', '24_full', 49, '24_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_50', 1, 24, 12, 'P4f84OwHADADADADAHAOAcA4BwDgHAOAcA4AwAwAwAwA////');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_50', '24_full', 50, '24_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_51', 1, 24, 12, 'P4f84OwHADADADADADAHAOA8A8AOAHADADADADADwH4Of8P4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_51', '24_full', 51, '24_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_52', 1, 24, 12, 'AMAcA8B8DsHMOMcM4MwMwMwM////AMAMAMAMAMAMAMAMAMAM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_52', '24_full', 52, '24_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_53', 1, 24, 12, '////wAwAwAwAwAwAwA4A/8f+AHADADADADADADADAH4O/8P4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_53', '24_full', 53, '24_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_54', 1, 24, 12, 'H+P/cD4AwAwAwAwAwAwA/4/84OwHwDwDwDwDwDwD4HcOP8H4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_54', '24_full', 54, '24_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_55', 1, 24, 11, '///8AYAwBgDAGAcAwDgGAcAwDgGAcAwDgGAcAwDgGAMA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_55', '24_full', 55, '24_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_56', 1, 24, 12, 'H4P8cO4HwDwDwDwD4HcOP8P8cO4HwDwDwDwDwDwD4HcOP8H4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_56', '24_full', 56, '24_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('24_full_57', 1, 24, 12, 'H4P8cO4HwDwDwDwDwDwD4Hf/P/ADADADADADADADAHwO/8f4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('24_full_57', '24_full', 57, '24_full_57');
