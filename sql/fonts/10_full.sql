\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('10_full', 5, 10, 0, 3, 2, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_32', 1, 10, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_32', '10_full', 32, '10_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_33', 1, 10, 2, 'qqgg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_33', '10_full', 33, '10_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_34', 1, 10, 3, 'tAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_34', '10_full', 34, '10_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_35', 1, 10, 5, 'ApX1K+pQAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_35', '10_full', 35, '10_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_36', 1, 10, 5, 'I6lHFK4gAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_36', '10_full', 36, '10_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_37', 1, 10, 5, 'BjIiERMYAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_37', '10_full', 37, '10_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_38', 1, 10, 6, 'IUUIYliilZA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_38', '10_full', 38, '10_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_39', 1, 10, 1, 'wAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_39', '10_full', 39, '10_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_40', 1, 10, 3, 'KkkkRA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_40', '10_full', 40, '10_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_41', 1, 10, 3, 'iJJJUA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_41', '10_full', 41, '10_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_42', 1, 10, 5, 'ASriOqQAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_42', '10_full', 42, '10_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_43', 1, 10, 5, 'AAhPkIAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_43', '10_full', 43, '10_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_44', 1, 10, 3, 'AAACUA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_44', '10_full', 44, '10_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_45', 1, 10, 4, 'AADwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_45', '10_full', 45, '10_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_46', 1, 10, 2, 'AACg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_46', '10_full', 46, '10_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_47', 1, 10, 5, 'CEQiEQiEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_47', '10_full', 47, '10_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_48', 1, 10, 5, 'dGMYxjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_48', '10_full', 48, '10_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_49', 1, 10, 3, 'WSSSXA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_49', '10_full', 49, '10_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_50', 1, 10, 5, 'dGIRERCHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_50', '10_full', 50, '10_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_51', 1, 10, 5, 'dEITBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_51', '10_full', 51, '10_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_52', 1, 10, 5, 'EZUpfEIQgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_52', '10_full', 52, '10_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_53', 1, 10, 5, '/CEPBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_53', '10_full', 53, '10_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_54', 1, 10, 5, 'dGEPRjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_54', '10_full', 54, '10_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_55', 1, 10, 5, '+EIhEIhCAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_55', '10_full', 55, '10_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_56', 1, 10, 5, 'dGMXRjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_56', '10_full', 56, '10_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_57', 1, 10, 5, 'dGMYvCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_57', '10_full', 57, '10_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_58', 1, 10, 2, 'CgoA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_58', '10_full', 58, '10_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_59', 1, 10, 3, 'ASASgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_59', '10_full', 59, '10_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_60', 1, 10, 4, 'ASSEIQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_60', '10_full', 60, '10_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_61', 1, 10, 4, 'AA8PAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_61', '10_full', 61, '10_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_62', 1, 10, 4, 'CEISSAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_62', '10_full', 62, '10_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_63', 1, 10, 5, 'dEIREIQBAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_63', '10_full', 63, '10_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_64', 1, 10, 6, 'ehlrpppngeA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_64', '10_full', 64, '10_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_65', 1, 10, 5, 'IqMY/jGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_65', '10_full', 65, '10_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_66', 1, 10, 5, '9GMfRjGPgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_66', '10_full', 66, '10_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_67', 1, 10, 5, 'dGEIQhCLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_67', '10_full', 67, '10_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_68', 1, 10, 5, '9GMYxjGPgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_68', '10_full', 68, '10_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_69', 1, 10, 5, '/CEOQhCHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_69', '10_full', 69, '10_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_70', 1, 10, 5, '/CEOQhCEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_70', '10_full', 70, '10_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_71', 1, 10, 5, 'dGEITjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_71', '10_full', 71, '10_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_72', 1, 10, 5, 'jGMfxjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_72', '10_full', 72, '10_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_73', 1, 10, 3, '6SSSXA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_73', '10_full', 73, '10_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_74', 1, 10, 4, 'EREREZY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_74', '10_full', 74, '10_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_75', 1, 10, 5, 'jGVMUlGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_75', '10_full', 75, '10_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_76', 1, 10, 4, 'iIiIiI8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_76', '10_full', 76, '10_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_77', 1, 10, 7, 'g46smTBgwYME');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_77', '10_full', 77, '10_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_78', 1, 10, 6, 'hxxpplljjhA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_78', '10_full', 78, '10_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_79', 1, 10, 5, 'dGMYxjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_79', '10_full', 79, '10_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_80', 1, 10, 5, '9GMfQhCEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_80', '10_full', 80, '10_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_81', 1, 10, 5, 'dGMYxjWTQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_81', '10_full', 81, '10_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_82', 1, 10, 5, '9GMfUlKMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_82', '10_full', 82, '10_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_83', 1, 10, 5, 'dGEHBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_83', '10_full', 83, '10_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_84', 1, 10, 5, '+QhCEIQhAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_84', '10_full', 84, '10_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_85', 1, 10, 5, 'jGMYxjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_85', '10_full', 85, '10_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_86', 1, 10, 5, 'jGMYxUohAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_86', '10_full', 86, '10_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_87', 1, 10, 7, 'gwYMGDJk1aqI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_87', '10_full', 87, '10_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_88', 1, 10, 5, 'jGKiKjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_88', '10_full', 88, '10_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_89', 1, 10, 5, 'jGMVEIQhAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_89', '10_full', 89, '10_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_90', 1, 10, 5, '+EQiEQiHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_90', '10_full', 90, '10_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_91', 1, 10, 3, '8kkknA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_91', '10_full', 91, '10_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_92', 1, 10, 5, 'hBCCEEIIQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_92', '10_full', 92, '10_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_93', 1, 10, 3, '5JJJPA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_93', '10_full', 93, '10_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_94', 1, 10, 5, 'IqIAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_94', '10_full', 94, '10_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('10_full_95', 1, 10, 5, 'AAAAAAAHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('10_full_95', '10_full', 95, '10_full_95');
