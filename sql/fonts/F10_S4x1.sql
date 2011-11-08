\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('F10_S4x1', 10, 10, 0, 4, 1, 7202);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_32', 1, 10, 3, 'AAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_32', 'F10_S4x1', 32, 'F10_S4x1_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_33', 1, 10, 1, '/kA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_33', 'F10_S4x1', 33, 'F10_S4x1_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_34', 1, 10, 4, 'VVUAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_34', 'F10_S4x1', 34, 'F10_S4x1_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_35', 1, 10, 5, 'Ur6lKV9SgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_35', 'F10_S4x1', 35, 'F10_S4x1_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_36', 1, 10, 5, 'I+lHFL4hAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_36', 'F10_S4x1', 36, 'F10_S4x1_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_37', 1, 10, 7, 'AMWQQQQTRgAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_37', 'F10_S4x1', 37, 'F10_S4x1_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_38', 1, 10, 6, 'IUUIYliidBA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_38', 'F10_S4x1', 38, 'F10_S4x1_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_39', 1, 10, 3, 'JKAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_39', 'F10_S4x1', 39, 'F10_S4x1_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_40', 1, 10, 3, 'KkkkRA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_40', 'F10_S4x1', 40, 'F10_S4x1_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_41', 1, 10, 3, 'iJJJUA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_41', 'F10_S4x1', 41, 'F10_S4x1_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_42', 1, 10, 5, 'ASrvuqQAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_42', 'F10_S4x1', 42, 'F10_S4x1_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_43', 1, 10, 5, 'AAhPkIAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_43', 'F10_S4x1', 43, 'F10_S4x1_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_44', 1, 10, 3, 'AABJQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_44', 'F10_S4x1', 44, 'F10_S4x1_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_45', 1, 10, 5, 'AAAPgAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_45', 'F10_S4x1', 45, 'F10_S4x1_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_46', 1, 10, 1, 'AEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_46', 'F10_S4x1', 46, 'F10_S4x1_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_47', 1, 10, 5, 'CEQiEQiEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_47', 'F10_S4x1', 47, 'F10_S4x1_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_48', 1, 10, 5, 'dGMZ1zGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_48', 'F10_S4x1', 48, 'F10_S4x1_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_49', 1, 10, 3, 'WSSSXA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_49', 'F10_S4x1', 49, 'F10_S4x1_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_50', 1, 10, 5, 'dGIRERCHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_50', 'F10_S4x1', 50, 'F10_S4x1_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_51', 1, 10, 5, 'dEITBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_51', 'F10_S4x1', 51, 'F10_S4x1_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_52', 1, 10, 5, 'EZUpfEIQgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_52', 'F10_S4x1', 52, 'F10_S4x1_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_53', 1, 10, 5, '/CEPBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_53', 'F10_S4x1', 53, 'F10_S4x1_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_54', 1, 10, 5, 'GREPRjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_54', 'F10_S4x1', 54, 'F10_S4x1_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_55', 1, 10, 5, '+EIiIQhCAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_55', 'F10_S4x1', 55, 'F10_S4x1_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_56', 1, 10, 5, 'dGMXRjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_56', 'F10_S4x1', 56, 'F10_S4x1_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_57', 1, 10, 5, 'dGMXhCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_57', 'F10_S4x1', 57, 'F10_S4x1_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_58', 1, 10, 3, 'AJAJAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_58', 'F10_S4x1', 58, 'F10_S4x1_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_59', 1, 10, 3, 'AJAJQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_59', 'F10_S4x1', 59, 'F10_S4x1_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_60', 1, 10, 4, 'ASSEIQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_60', 'F10_S4x1', 60, 'F10_S4x1_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_61', 1, 10, 5, 'AAHwfAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_61', 'F10_S4x1', 61, 'F10_S4x1_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_62', 1, 10, 5, 'AgggiIgAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_62', 'F10_S4x1', 62, 'F10_S4x1_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_63', 1, 10, 5, 'dEIiEIQBAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_63', 'F10_S4x1', 63, 'F10_S4x1_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_64', 1, 10, 5, 'dEIU1rWrgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_64', 'F10_S4x1', 64, 'F10_S4x1_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_65', 1, 10, 5, 'IqMY/jGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_65', 'F10_S4x1', 65, 'F10_S4x1_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_66', 1, 10, 5, '9GMfRjGPgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_66', 'F10_S4x1', 66, 'F10_S4x1_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_67', 1, 10, 5, 'dGEIQhCLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_67', 'F10_S4x1', 67, 'F10_S4x1_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_68', 1, 10, 5, '9GMYxjGPgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_68', 'F10_S4x1', 68, 'F10_S4x1_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_69', 1, 10, 5, '/CEPQhCHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_69', 'F10_S4x1', 69, 'F10_S4x1_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_70', 1, 10, 5, '/CEPQhCEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_70', 'F10_S4x1', 70, 'F10_S4x1_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_71', 1, 10, 5, 'dGEITjGLwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_71', 'F10_S4x1', 71, 'F10_S4x1_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_72', 1, 10, 5, 'jGMfxjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_72', 'F10_S4x1', 72, 'F10_S4x1_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_73', 1, 10, 3, '6SSSXA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_73', 'F10_S4x1', 73, 'F10_S4x1_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_74', 1, 10, 5, 'CEIQhCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_74', 'F10_S4x1', 74, 'F10_S4x1_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_75', 1, 10, 5, 'jGVMUlGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_75', 'F10_S4x1', 75, 'F10_S4x1_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_76', 1, 10, 5, 'hCEIQhCHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_76', 'F10_S4x1', 76, 'F10_S4x1_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_77', 1, 10, 7, 'g46smTBgwYME');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_77', 'F10_S4x1', 77, 'F10_S4x1_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_78', 1, 10, 5, 'jHOaznGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_78', 'F10_S4x1', 78, 'F10_S4x1_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_79', 1, 10, 5, 'dGMYxjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_79', 'F10_S4x1', 79, 'F10_S4x1_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_80', 1, 10, 5, '9GMfQhCEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_80', 'F10_S4x1', 80, 'F10_S4x1_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_81', 1, 10, 5, 'dGMYxjWTQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_81', 'F10_S4x1', 81, 'F10_S4x1_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_82', 1, 10, 5, '9GMfYpKMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_82', 'F10_S4x1', 82, 'F10_S4x1_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_83', 1, 10, 5, 'dGEHBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_83', 'F10_S4x1', 83, 'F10_S4x1_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_84', 1, 10, 5, '+QhCEIQhAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_84', 'F10_S4x1', 84, 'F10_S4x1_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_85', 1, 10, 5, 'jGMYxjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_85', 'F10_S4x1', 85, 'F10_S4x1_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_86', 1, 10, 5, 'jGMYxjFRAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_86', 'F10_S4x1', 86, 'F10_S4x1_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_87', 1, 10, 7, 'gwYMGDJk1ccE');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_87', 'F10_S4x1', 87, 'F10_S4x1_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_88', 1, 10, 5, 'jGKiKjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_88', 'F10_S4x1', 88, 'F10_S4x1_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_89', 1, 10, 5, 'jGMVEIQhAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_89', 'F10_S4x1', 89, 'F10_S4x1_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_90', 1, 10, 6, '/BBCEIQgg/A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_90', 'F10_S4x1', 90, 'F10_S4x1_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_91', 1, 10, 4, '+IiIiI8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_91', 'F10_S4x1', 91, 'F10_S4x1_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_92', 1, 10, 5, 'hBCCEEIIQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_92', 'F10_S4x1', 92, 'F10_S4x1_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_93', 1, 10, 4, '8RERER8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_93', 'F10_S4x1', 93, 'F10_S4x1_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_94', 1, 10, 5, 'IqIAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_94', 'F10_S4x1', 94, 'F10_S4x1_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_95', 1, 10, 5, 'AAAAAAAHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_95', 'F10_S4x1', 95, 'F10_S4x1_95');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_96', 1, 10, 4, 'xjAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_96', 'F10_S4x1', 96, 'F10_S4x1_96');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_97', 1, 10, 5, 'AAAHBfGLwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_97', 'F10_S4x1', 97, 'F10_S4x1_97');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_98', 1, 10, 5, 'hCEPRjGPgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_98', 'F10_S4x1', 98, 'F10_S4x1_98');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_99', 1, 10, 4, 'AAB4iIc=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_99', 'F10_S4x1', 99, 'F10_S4x1_99');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_100', 1, 10, 5, 'CEIWzjGLwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_100', 'F10_S4x1', 100, 'F10_S4x1_100');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_101', 1, 10, 5, 'AAAHR9CDwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_101', 'F10_S4x1', 101, 'F10_S4x1_101');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_102', 1, 10, 5, 'MlCPIQhCAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_102', 'F10_S4x1', 102, 'F10_S4x1_102');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_103', 1, 10, 5, 'AAAHxeGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_103', 'F10_S4x1', 103, 'F10_S4x1_103');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_104', 1, 10, 5, 'hCELZjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_104', 'F10_S4x1', 104, 'F10_S4x1_104');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_105', 1, 10, 1, 'L8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_105', 'F10_S4x1', 105, 'F10_S4x1_105');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_106', 1, 10, 4, 'ABAREZY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_106', 'F10_S4x1', 106, 'F10_S4x1_106');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_107', 1, 10, 4, 'iIms6Zk=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_107', 'F10_S4x1', 107, 'F10_S4x1_107');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_108', 1, 10, 3, 'ySSSTA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_108', 'F10_S4x1', 108, 'F10_S4x1_108');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_109', 1, 10, 5, 'AAANVrGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_109', 'F10_S4x1', 109, 'F10_S4x1_109');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_110', 1, 10, 5, 'AAALZjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_110', 'F10_S4x1', 110, 'F10_S4x1_110');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_111', 1, 10, 4, 'AABpmZY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_111', 'F10_S4x1', 111, 'F10_S4x1_111');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_112', 1, 10, 5, 'AAAPR9CEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_112', 'F10_S4x1', 112, 'F10_S4x1_112');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_113', 1, 10, 5, 'AAAHxeEIQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_113', 'F10_S4x1', 113, 'F10_S4x1_113');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_114', 1, 10, 4, 'AAC8iIg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_114', 'F10_S4x1', 114, 'F10_S4x1_114');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_115', 1, 10, 5, 'AAAHQcGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_115', 'F10_S4x1', 115, 'F10_S4x1_115');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_116', 1, 10, 5, 'ABCOIQhJgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_116', 'F10_S4x1', 116, 'F10_S4x1_116');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_117', 1, 10, 5, 'AAAIxjGbQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_117', 'F10_S4x1', 117, 'F10_S4x1_117');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_118', 1, 10, 5, 'AAAIxjFRAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_118', 'F10_S4x1', 118, 'F10_S4x1_118');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_119', 1, 10, 5, 'AAAIxjWqgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_119', 'F10_S4x1', 119, 'F10_S4x1_119');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_120', 1, 10, 4, 'AACZZpk=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_120', 'F10_S4x1', 120, 'F10_S4x1_120');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_121', 1, 10, 4, 'AACZcZY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_121', 'F10_S4x1', 121, 'F10_S4x1_121');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_122', 1, 10, 5, 'AAAPiIiHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_122', 'F10_S4x1', 122, 'F10_S4x1_122');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_123', 1, 10, 4, 'NESEREM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_123', 'F10_S4x1', 123, 'F10_S4x1_123');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_124', 1, 10, 3, 'JJAJJA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_124', 'F10_S4x1', 124, 'F10_S4x1_124');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F10_S4x1_125', 1, 10, 4, 'wiISIiw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F10_S4x1_125', 'F10_S4x1', 125, 'F10_S4x1_125');
