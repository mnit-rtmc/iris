\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('07_line', 2, 7, 0, 0, 2, 40473);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_32', 1, 7, 1, 'AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_32', '07_line', 32, '07_line_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_33', 1, 7, 2, 'qog=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_33', '07_line', 33, '07_line_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_34', 1, 7, 3, 'tAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_34', '07_line', 34, '07_line_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_35', 1, 7, 5, 'Ur6vqUA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_35', '07_line', 35, '07_line_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_36', 1, 7, 5, 'I6jiuIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_36', '07_line', 36, '07_line_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_37', 1, 7, 5, 'xkRETGA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_37', '07_line', 37, '07_line_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_38', 1, 7, 5, 'RSiKyaA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_38', '07_line', 38, '07_line_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_39', 1, 7, 1, 'wA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_39', '07_line', 39, '07_line_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_40', 1, 7, 3, 'KkiI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_40', '07_line', 40, '07_line_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_41', 1, 7, 3, 'iJKg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_41', '07_line', 41, '07_line_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_42', 1, 7, 7, 'EFEUFEUEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_42', '07_line', 42, '07_line_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_43', 1, 7, 5, 'AQnyEAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_43', '07_line', 43, '07_line_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_44', 1, 7, 3, 'AACg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_44', '07_line', 44, '07_line_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_45', 1, 7, 4, 'AA8AAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_45', '07_line', 45, '07_line_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_46', 1, 7, 2, 'AAg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_46', '07_line', 46, '07_line_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_47', 1, 7, 5, 'AEREQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_47', '07_line', 47, '07_line_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_48', 1, 7, 4, 'aZmZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_48', '07_line', 48, '07_line_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_49', 1, 7, 3, 'WSS4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_49', '07_line', 49, '07_line_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_50', 1, 7, 4, 'aRJI8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_50', '07_line', 50, '07_line_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_51', 1, 7, 4, 'aRYZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_51', '07_line', 51, '07_line_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_52', 1, 7, 5, 'EZUviEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_52', '07_line', 52, '07_line_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_53', 1, 7, 4, '+IYZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_53', '07_line', 53, '07_line_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_54', 1, 7, 4, 'aY6ZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_54', '07_line', 54, '07_line_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_55', 1, 7, 4, '8RIkQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_55', '07_line', 55, '07_line_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_56', 1, 7, 4, 'aZaZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_56', '07_line', 56, '07_line_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_57', 1, 7, 4, 'aZcZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_57', '07_line', 57, '07_line_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_58', 1, 7, 2, 'CIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_58', '07_line', 58, '07_line_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_59', 1, 7, 3, 'AQUA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_59', '07_line', 59, '07_line_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_60', 1, 7, 4, 'EkhCEA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_60', '07_line', 60, '07_line_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_61', 1, 7, 4, 'APDwAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_61', '07_line', 61, '07_line_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_62', 1, 7, 4, 'hCEkgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_62', '07_line', 62, '07_line_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_63', 1, 7, 4, 'aRIgIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_63', '07_line', 63, '07_line_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_64', 1, 7, 5, 'dGdZwcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_64', '07_line', 64, '07_line_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_65', 1, 7, 4, 'aZ+ZkA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_65', '07_line', 65, '07_line_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_66', 1, 7, 4, '6Z6Z4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_66', '07_line', 66, '07_line_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_67', 1, 7, 4, 'aYiJYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_67', '07_line', 67, '07_line_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_68', 1, 7, 4, '6ZmZ4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_68', '07_line', 68, '07_line_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_69', 1, 7, 4, '+I6I8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_69', '07_line', 69, '07_line_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_70', 1, 7, 4, '+I6IgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_70', '07_line', 70, '07_line_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_71', 1, 7, 4, 'aYuZcA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_71', '07_line', 71, '07_line_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_72', 1, 7, 4, 'mZ+ZkA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_72', '07_line', 72, '07_line_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_73', 1, 7, 3, '6SS4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_73', '07_line', 73, '07_line_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_74', 1, 7, 4, 'EREZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_74', '07_line', 74, '07_line_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_75', 1, 7, 4, 'maypkA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_75', '07_line', 75, '07_line_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_76', 1, 7, 4, 'iIiI8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_76', '07_line', 76, '07_line_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_77', 1, 7, 7, 'g46smTBggA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_77', '07_line', 77, '07_line_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_78', 1, 7, 5, 'jnNZziA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_78', '07_line', 78, '07_line_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_79', 1, 7, 4, 'aZmZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_79', '07_line', 79, '07_line_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_80', 1, 7, 4, '6Z6IgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_80', '07_line', 80, '07_line_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_81', 1, 7, 5, 'dGMayaA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_81', '07_line', 81, '07_line_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_82', 1, 7, 4, '6Z6pkA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_82', '07_line', 82, '07_line_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_83', 1, 7, 4, 'aYYZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_83', '07_line', 83, '07_line_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_84', 1, 7, 5, '+QhCEIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_84', '07_line', 84, '07_line_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_85', 1, 7, 4, 'mZmZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_85', '07_line', 85, '07_line_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_86', 1, 7, 5, 'jGMVKIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_86', '07_line', 86, '07_line_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_87', 1, 7, 7, 'gwYMmrVRAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_87', '07_line', 87, '07_line_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_88', 1, 7, 5, 'jFRFRiA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_88', '07_line', 88, '07_line_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_89', 1, 7, 5, 'jFRCEIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_89', '07_line', 89, '07_line_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_90', 1, 7, 5, '+EREQ+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_90', '07_line', 90, '07_line_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_91', 1, 7, 3, '8kk4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_91', '07_line', 91, '07_line_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_92', 1, 7, 5, 'BBBBBAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_92', '07_line', 92, '07_line_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_93', 1, 7, 3, '5JJ4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_93', '07_line', 93, '07_line_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_94', 1, 7, 5, 'IqIAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_94', '07_line', 94, '07_line_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_line_95', 1, 7, 5, 'AAAAA+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_line_95', '07_line', 95, '07_line_95');
