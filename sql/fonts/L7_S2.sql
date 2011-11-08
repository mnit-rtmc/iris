\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('L7_S2', 2, 7, 0, 0, 2, 25883);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_32', 1, 7, 1, 'AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_32', 'L7_S2', 32, 'L7_S2_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_33', 1, 7, 3, 'SSQQ');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_33', 'L7_S2', 33, 'L7_S2_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_34', 1, 7, 3, 'toAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_34', 'L7_S2', 34, 'L7_S2_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_35', 1, 7, 5, 'Ur6vqUA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_35', 'L7_S2', 35, 'L7_S2_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_36', 1, 7, 5, 'I+ji+IA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_36', 'L7_S2', 36, 'L7_S2_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_37', 1, 7, 5, 'xkRETGA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_37', 'L7_S2', 37, 'L7_S2_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_38', 1, 7, 5, 'RSiKyaA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_38', 'L7_S2', 38, 'L7_S2_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_39', 1, 7, 3, 'SQAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_39', 'L7_S2', 39, 'L7_S2_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_40', 1, 7, 3, 'KkiI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_40', 'L7_S2', 40, 'L7_S2_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_41', 1, 7, 3, 'iJKg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_41', 'L7_S2', 41, 'L7_S2_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_42', 1, 7, 7, 'EFEUFEUEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_42', 'L7_S2', 42, 'L7_S2_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_43', 1, 7, 5, 'AQnyEAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_43', 'L7_S2', 43, 'L7_S2_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_44', 1, 7, 3, 'AASg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_44', 'L7_S2', 44, 'L7_S2_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_45', 1, 7, 4, 'AA8AAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_45', 'L7_S2', 45, 'L7_S2_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_46', 1, 7, 3, 'AAGw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_46', 'L7_S2', 46, 'L7_S2_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_47', 1, 7, 5, 'AEREQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_47', 'L7_S2', 47, 'L7_S2_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_48', 1, 7, 4, 'aZmZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_48', 'L7_S2', 48, 'L7_S2_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_49', 1, 7, 3, 'WSS4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_49', 'L7_S2', 49, 'L7_S2_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_50', 1, 7, 4, 'aRJI8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_50', 'L7_S2', 50, 'L7_S2_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_51', 1, 7, 4, 'aRYZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_51', 'L7_S2', 51, 'L7_S2_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_52', 1, 7, 5, 'EZUviEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_52', 'L7_S2', 52, 'L7_S2_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_53', 1, 7, 4, '+IYZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_53', 'L7_S2', 53, 'L7_S2_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_54', 1, 7, 4, 'aY6ZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_54', 'L7_S2', 54, 'L7_S2_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_55', 1, 7, 4, '8RIkQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_55', 'L7_S2', 55, 'L7_S2_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_56', 1, 7, 4, 'aZaZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_56', 'L7_S2', 56, 'L7_S2_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_57', 1, 7, 4, 'aZcZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_57', 'L7_S2', 57, 'L7_S2_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_58', 1, 7, 2, 'CIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_58', 'L7_S2', 58, 'L7_S2_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_59', 1, 7, 3, 'AQUA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_59', 'L7_S2', 59, 'L7_S2_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_60', 1, 7, 4, 'EkhCEA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_60', 'L7_S2', 60, 'L7_S2_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_61', 1, 7, 5, 'AD4PgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_61', 'L7_S2', 61, 'L7_S2_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_62', 1, 7, 4, 'hCEkgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_62', 'L7_S2', 62, 'L7_S2_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_63', 1, 7, 4, 'aRIgIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_63', 'L7_S2', 63, 'L7_S2_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_64', 1, 7, 5, 'dG9bwcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_64', 'L7_S2', 64, 'L7_S2_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_65', 1, 7, 4, 'aZ+ZkA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_65', 'L7_S2', 65, 'L7_S2_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_66', 1, 7, 4, '6Z6Z4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_66', 'L7_S2', 66, 'L7_S2_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_67', 1, 7, 4, 'aYiJYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_67', 'L7_S2', 67, 'L7_S2_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_68', 1, 7, 4, '6ZmZ4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_68', 'L7_S2', 68, 'L7_S2_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_69', 1, 7, 4, '+I6I8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_69', 'L7_S2', 69, 'L7_S2_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_70', 1, 7, 4, '+I6IgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_70', 'L7_S2', 70, 'L7_S2_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_71', 1, 7, 4, 'aYi5cA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_71', 'L7_S2', 71, 'L7_S2_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_72', 1, 7, 4, 'mZ+ZkA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_72', 'L7_S2', 72, 'L7_S2_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_73', 1, 7, 1, '/w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_73', 'L7_S2', 73, 'L7_S2_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_74', 1, 7, 4, 'EREZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_74', 'L7_S2', 74, 'L7_S2_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_75', 1, 7, 4, 'maypkA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_75', 'L7_S2', 75, 'L7_S2_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_76', 1, 7, 4, 'iIiI8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_76', 'L7_S2', 76, 'L7_S2_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_77', 1, 7, 7, 'g48dWrJkgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_77', 'L7_S2', 77, 'L7_S2_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_78', 1, 7, 5, 'jnNZziA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_78', 'L7_S2', 78, 'L7_S2_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_79', 1, 7, 4, 'aZmZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_79', 'L7_S2', 79, 'L7_S2_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_80', 1, 7, 4, '6Z6IgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_80', 'L7_S2', 80, 'L7_S2_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_81', 1, 7, 5, 'dGMayaA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_81', 'L7_S2', 81, 'L7_S2_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_82', 1, 7, 4, '6Z6pkA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_82', 'L7_S2', 82, 'L7_S2_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_83', 1, 7, 4, 'aYYZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_83', 'L7_S2', 83, 'L7_S2_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_84', 1, 7, 5, '+QhCEIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_84', 'L7_S2', 84, 'L7_S2_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_85', 1, 7, 4, 'mZmZYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_85', 'L7_S2', 85, 'L7_S2_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_86', 1, 7, 5, 'jGMVKIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_86', 'L7_S2', 86, 'L7_S2_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_87', 1, 7, 7, 'gwYMmrjggA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_87', 'L7_S2', 87, 'L7_S2_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_88', 1, 7, 5, 'jFRFRiA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_88', 'L7_S2', 88, 'L7_S2_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_89', 1, 7, 5, 'jFRCEIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_89', 'L7_S2', 89, 'L7_S2_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('L7_S2_90', 1, 7, 5, '+EREQ+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('L7_S2_90', 'L7_S2', 90, 'L7_S2_90');
