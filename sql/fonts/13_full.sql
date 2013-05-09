\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('13_full', 9, 13, 0, 4, 3, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_32', 1, 13, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_32', '13_full', 32, '13_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_33', 1, 13, 1, '/8g=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_33', '13_full', 33, '13_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_34', 1, 13, 3, 'toAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_34', '13_full', 34, '13_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_35', 1, 13, 6, 'AASS/SS/SSAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_35', '13_full', 35, '13_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_36', 1, 13, 7, 'EPtciRofCxInW+EA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_36', '13_full', 36, '13_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_37', 1, 13, 7, 'AYMIMMMEGGGCGDAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_37', '13_full', 37, '13_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_38', 1, 13, 7, 'MPEiR4YcbIseFneg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_38', '13_full', 38, '13_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_39', 1, 13, 1, '4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_39', '13_full', 39, '13_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_40', 1, 13, 4, 'E2yIiIxjEA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_40', '13_full', 40, '13_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_41', 1, 13, 4, 'jGMRERNsgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_41', '13_full', 41, '13_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_42', 1, 13, 7, 'AABGt8cEHH2sQAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_42', '13_full', 42, '13_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_43', 1, 13, 5, 'AABCE+QhAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_43', '13_full', 43, '13_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_44', 1, 13, 3, 'AAAAAWg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_44', '13_full', 44, '13_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_45', 1, 13, 5, 'AAAAA+AAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_45', '13_full', 45, '13_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_46', 1, 13, 3, 'AAAAAGw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_46', '13_full', 46, '13_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_47', 1, 13, 6, 'BBDCGEMIYQwggA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_47', '13_full', 47, '13_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_48', 1, 13, 6, 'ezhhhhhhhhhzeA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_48', '13_full', 48, '13_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_49', 1, 13, 3, 'WSSSSS4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_49', '13_full', 49, '13_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_50', 1, 13, 7, 'fY4IEGGGGGGCBA/g');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_50', '13_full', 50, '13_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_51', 1, 13, 7, 'fYwIECDHAwIEDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_51', '13_full', 51, '13_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_52', 1, 13, 7, 'DDjTLFC/ggQIECBA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_52', '13_full', 52, '13_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_53', 1, 13, 7, '/wIECB+BgQIEDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_53', '13_full', 53, '13_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_54', 1, 13, 7, 'fY4MCBA/Q4MGDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_54', '13_full', 54, '13_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_55', 1, 13, 6, '/BBBDCGEMIYQQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_55', '13_full', 55, '13_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_56', 1, 13, 7, 'fY4MGDjfY4MGDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_56', '13_full', 56, '13_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_57', 1, 13, 7, 'fY4MGDhfgQIGDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_57', '13_full', 57, '13_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_58', 1, 13, 2, 'AKKAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_58', '13_full', 58, '13_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_59', 1, 13, 3, 'AASC0AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_59', '13_full', 59, '13_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_60', 1, 13, 6, 'ABDGMYwYMGDBAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_60', '13_full', 60, '13_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_61', 1, 13, 5, 'AAAAfB8AAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_61', '13_full', 61, '13_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_62', 1, 13, 6, 'AgwYMGDGMYwgAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_62', '13_full', 62, '13_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_63', 1, 13, 7, 'fY4IECDDDBAgAAEA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_63', '13_full', 63, '13_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_64', 1, 13, 7, 'fY4M23Ro0bM+BgeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_64', '13_full', 64, '13_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_65', 1, 13, 7, 'EHG2ODB/wYMGDBgg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_65', '13_full', 65, '13_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_66', 1, 13, 7, '/Q4MGDD/Q4MGDD/A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_66', '13_full', 66, '13_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_67', 1, 13, 7, 'fY4MCBAgQIECDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_67', '13_full', 67, '13_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_68', 1, 13, 7, '+RocGDBgwYMGHG+A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_68', '13_full', 68, '13_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_69', 1, 13, 7, '/wIECBA+QIECBA/g');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_69', '13_full', 69, '13_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_70', 1, 13, 7, '/wIECBA+QIECBAgA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_70', '13_full', 70, '13_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_71', 1, 13, 7, 'fY4MCBAjwYMGDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_71', '13_full', 71, '13_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_72', 1, 13, 6, 'hhhhhh/hhhhhhA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_72', '13_full', 72, '13_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_73', 1, 13, 3, '6SSSSS4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_73', '13_full', 73, '13_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_74', 1, 13, 6, 'BBBBBBBBBBhzeA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_74', '13_full', 74, '13_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_75', 1, 13, 7, 'gw40yxwwcLEyNDgg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_75', '13_full', 75, '13_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_76', 1, 13, 6, 'gggggggggggg/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_76', '13_full', 76, '13_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_77', 1, 13, 9, 'gOD49tnMRiMBgMBgMBgI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_77', '13_full', 77, '13_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_78', 1, 13, 8, 'wcHhobGRmYmNhYeDgw==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_78', '13_full', 78, '13_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_79', 1, 13, 7, 'fY4MGDBgwYMGDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_79', '13_full', 79, '13_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_80', 1, 13, 7, '/Q4MGDD/QIECBAgA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_80', '13_full', 80, '13_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_81', 1, 13, 7, 'fY4MGDBgwYMmLieg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_81', '13_full', 81, '13_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_82', 1, 13, 7, '/Q4MGDD/TI0KHBgg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_82', '13_full', 82, '13_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_83', 1, 13, 7, 'fY4MCBgfAwIGDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_83', '13_full', 83, '13_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_84', 1, 13, 7, '/iBAgQIECBAgQIEA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_84', '13_full', 84, '13_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_85', 1, 13, 7, 'gwYMGDBgwYMGDjfA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_85', '13_full', 85, '13_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_86', 1, 13, 7, 'gwYMGDBxokTYocEA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_86', '13_full', 86, '13_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_87', 1, 13, 9, 'gMBgMBgMBgOTSS6VTuIg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_87', '13_full', 87, '13_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_88', 1, 13, 7, 'gwcaJscEHGyLHBgg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_88', '13_full', 88, '13_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_89', 1, 13, 7, 'gwcaJsUOCBAgQIEA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_89', '13_full', 89, '13_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_90', 1, 13, 8, '/wEBAwYMGDBgwICA/w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_90', '13_full', 90, '13_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_91', 1, 13, 4, '+IiIiIiI8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_91', '13_full', 91, '13_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_92', 1, 13, 6, 'ggwQYIMEGCDBBA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_92', '13_full', 92, '13_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_93', 1, 13, 4, '8RERERER8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_93', '13_full', 93, '13_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_94', 1, 13, 6, 'MezhAAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_94', '13_full', 94, '13_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('13_full_95', 1, 13, 5, 'AAAAAAAAAA+A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('13_full_95', '13_full', 95, '13_full_95');
