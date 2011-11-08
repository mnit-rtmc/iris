\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('F18_S5x3_THIN', 7, 18, 0, 5, 3, 12345);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_32', 1, 18, 1, 'AAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_32', 'F18_S5x3_THIN', 32, 'F18_S5x3_THIN_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_33', 1, 18, 1, '//5A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_33', 'F18_S5x3_THIN', 33, 'F18_S5x3_THIN_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_34', 1, 18, 3, 'toAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_34', 'F18_S5x3_THIN', 34, 'F18_S5x3_THIN_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_35', 1, 18, 5, 'AAAFKV9SlfUpQAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_35', 'F18_S5x3_THIN', 35, 'F18_S5x3_THIN_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_36', 1, 18, 7, 'ECDymRIkKDgoSJEiZTwQIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_36', 'F18_S5x3_THIN', 36, 'F18_S5x3_THIN_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_37', 1, 18, 7, 'AAWLIEECBBAggQIIE0aAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_37', 'F18_S5x3_THIN', 37, 'F18_S5x3_THIN_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_40', 1, 18, 3, 'KUkkkkkiRA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_40', 'F18_S5x3_THIN', 40, 'F18_S5x3_THIN_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_41', 1, 18, 3, 'iRJJJJJKUA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_41', 'F18_S5x3_THIN', 41, 'F18_S5x3_THIN_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_43', 1, 18, 5, 'AAAAAIQnyEIAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_43', 'F18_S5x3_THIN', 43, 'F18_S5x3_THIN_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_44', 1, 18, 2, 'AAAAAGA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_44', 'F18_S5x3_THIN', 44, 'F18_S5x3_THIN_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_45', 1, 18, 6, 'AAAAAAAAA/AAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_45', 'F18_S5x3_THIN', 45, 'F18_S5x3_THIN_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_46', 1, 18, 1, 'AABA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_46', 'F18_S5x3_THIN', 46, 'F18_S5x3_THIN_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_47', 1, 18, 7, 'AgQIIEECBBAggQIIEECBAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_47', 'F18_S5x3_THIN', 47, 'F18_S5x3_THIN_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_48', 1, 18, 6, 'MShhhhhhhhhhhhhhSMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_48', 'F18_S5x3_THIN', 48, 'F18_S5x3_THIN_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_49', 1, 18, 3, 'WSSSSSSSXA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_49', 'F18_S5x3_THIN', 49, 'F18_S5x3_THIN_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_50', 1, 18, 7, 'OIoIECBAggggggQQIECB/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_50', 'F18_S5x3_THIN', 50, 'F18_S5x3_THIN_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_51', 1, 18, 7, 'OIoIECBAgjgICBAgQMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_51', 'F18_S5x3_THIN', 51, 'F18_S5x3_THIN_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_52', 1, 18, 7, 'DChRIkiRQoX8ECBAgQIECA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_52', 'F18_S5x3_THIN', 52, 'F18_S5x3_THIN_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_53', 1, 18, 7, '/wIECBAgQPgICBAgQMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_53', 'F18_S5x3_THIN', 53, 'F18_S5x3_THIN_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_54', 1, 18, 7, 'OIoMCBAgQPkKDBgwYMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_54', 'F18_S5x3_THIN', 54, 'F18_S5x3_THIN_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_55', 1, 18, 7, '/gQIIEECBBAggQIIEECBAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_55', 'F18_S5x3_THIN', 55, 'F18_S5x3_THIN_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_56', 1, 18, 7, 'OIoMGDBgojiKDBgwYMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_56', 'F18_S5x3_THIN', 56, 'F18_S5x3_THIN_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_57', 1, 18, 7, 'OIoMGDBgwUJ8CBAgQMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_57', 'F18_S5x3_THIN', 57, 'F18_S5x3_THIN_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_58', 1, 18, 1, 'AIgA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_58', 'F18_S5x3_THIN', 58, 'F18_S5x3_THIN_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_59', 1, 18, 2, 'AABAYAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_59', 'F18_S5x3_THIN', 59, 'F18_S5x3_THIN_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_60', 1, 18, 6, 'AAAABCEIQgQIECBAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_60', 'F18_S5x3_THIN', 60, 'F18_S5x3_THIN_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_61', 1, 18, 6, 'AAAAAAA/AAA/AAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_61', 'F18_S5x3_THIN', 61, 'F18_S5x3_THIN_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_62', 1, 18, 6, 'AAAAgQIECBCEIQgAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_62', 'F18_S5x3_THIN', 62, 'F18_S5x3_THIN_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_63', 1, 18, 7, 'OIoIECBAggggQIECBAAAIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_63', 'F18_S5x3_THIN', 63, 'F18_S5x3_THIN_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_64', 1, 18, 7, 'OIoMGDHkyZMmTJjQIEBAeA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_64', 'F18_S5x3_THIN', 64, 'F18_S5x3_THIN_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_65', 1, 18, 7, 'OIoMGDBgwf8GDBgwYMGDBA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_65', 'F18_S5x3_THIN', 65, 'F18_S5x3_THIN_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_66', 1, 18, 7, '+QoMGDBgwvkKDBgwYMGF8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_66', 'F18_S5x3_THIN', 66, 'F18_S5x3_THIN_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_67', 1, 18, 7, 'OIoMCBAgQIECBAgQIEFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_67', 'F18_S5x3_THIN', 67, 'F18_S5x3_THIN_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_68', 1, 18, 7, '+QoMGDBgwYMGDBgwYMGF8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_68', 'F18_S5x3_THIN', 68, 'F18_S5x3_THIN_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_69', 1, 18, 7, '/wIECBAgQPkCBAgQIECB/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_69', 'F18_S5x3_THIN', 69, 'F18_S5x3_THIN_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_70', 1, 18, 7, '/wIECBAgQPkCBAgQIECBAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_70', 'F18_S5x3_THIN', 70, 'F18_S5x3_THIN_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_71', 1, 18, 7, 'OIoMCBAgQIEeDBgwYMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_71', 'F18_S5x3_THIN', 71, 'F18_S5x3_THIN_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_72', 1, 18, 7, 'gwYMGDBgwf8GDBgwYMGDBA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_72', 'F18_S5x3_THIN', 72, 'F18_S5x3_THIN_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_73', 1, 18, 3, '6SSSSSSSXA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_73', 'F18_S5x3_THIN', 73, 'F18_S5x3_THIN_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_74', 1, 18, 5, 'CEIQhCEIQhCEIRcA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_74', 'F18_S5x3_THIN', 74, 'F18_S5x3_THIN_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_75', 1, 18, 7, 'gwYMKFEiSOEiJEhQoUGDBA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_75', 'F18_S5x3_THIN', 75, 'F18_S5x3_THIN_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_76', 1, 18, 6, 'ggggggggggggggggg/A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_76', 'F18_S5x3_THIN', 76, 'F18_S5x3_THIN_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_77', 1, 18, 9, 'gMBweDotFlMpiMRgMBgMBgMBgMBA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_77', 'F18_S5x3_THIN', 77, 'F18_S5x3_THIN_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_78', 1, 18, 8, 'gYHBwaGhkZGJiYWFg4OBgYGB');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_78', 'F18_S5x3_THIN', 78, 'F18_S5x3_THIN_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_79', 1, 18, 7, 'OIoMGDBgwYMGDBgwYMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_79', 'F18_S5x3_THIN', 79, 'F18_S5x3_THIN_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_80', 1, 18, 7, '+QoMGDBgwvkCBAgQIECBAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_80', 'F18_S5x3_THIN', 80, 'F18_S5x3_THIN_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_81', 1, 18, 7, 'OIoMGDBgwYMGDBgwZMVEdA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_81', 'F18_S5x3_THIN', 81, 'F18_S5x3_THIN_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_82', 1, 18, 7, '+QoMGDBgwvkSFCgwYMGDBA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_82', 'F18_S5x3_THIN', 82, 'F18_S5x3_THIN_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_83', 1, 18, 7, 'OIoMCBAgIDgICBAgQMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_83', 'F18_S5x3_THIN', 83, 'F18_S5x3_THIN_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_84', 1, 18, 7, '/iBAgQIECBAgQIECBAgQIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_84', 'F18_S5x3_THIN', 84, 'F18_S5x3_THIN_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_85', 1, 18, 7, 'gwYMGDBgwYMGDBgwYMFEcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_85', 'F18_S5x3_THIN', 85, 'F18_S5x3_THIN_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_86', 1, 18, 7, 'gwYMGDBgokSJEUKFCggQIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_86', 'F18_S5x3_THIN', 86, 'F18_S5x3_THIN_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_87', 1, 18, 9, 'gMBgMBgMBgMBgMBiMRlMpotFQSCA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_87', 'F18_S5x3_THIN', 87, 'F18_S5x3_THIN_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_88', 1, 18, 7, 'gwYKJEUKFBAgoUKIkUGDBA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_88', 'F18_S5x3_THIN', 88, 'F18_S5x3_THIN_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_89', 1, 18, 7, 'gwYMFEiRFCggQIECBAgQIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_89', 'F18_S5x3_THIN', 89, 'F18_S5x3_THIN_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_THIN_90', 1, 18, 7, '/gQIIEECBBAggQIIEECB/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_THIN_90', 'F18_S5x3_THIN', 90, 'F18_S5x3_THIN_90');
