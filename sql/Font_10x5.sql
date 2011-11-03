\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
	char_spacing, version_id) VALUES ('F10x5', 7, 10, 0, 4, 1, 7202);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_32', 1, 10, 3, 'AAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_32', 'F10x5', 32, 'F10x5_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_33', 1, 10, 1, '/kA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_33', 'F10x5', 33, 'F10x5_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_34', 1, 10, 4, 'VVUAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_34', 'F10x5', 34, 'F10x5_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_35', 1, 10, 5, 'Ur6lKV9SgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_35', 'F10x5', 35, 'F10x5_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_36', 1, 10, 5, 'I+lHFL4hAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_36', 'F10x5', 36, 'F10x5_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_37', 1, 10, 7, 'AMWQQQQTRgAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_37', 'F10x5', 37, 'F10x5_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_38', 1, 10, 6, 'IUUIYliidBA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_38', 'F10x5', 38, 'F10x5_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_39', 1, 10, 3, 'JKAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_39', 'F10x5', 39, 'F10x5_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_40', 1, 10, 3, 'KkkkRA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_40', 'F10x5', 40, 'F10x5_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_41', 1, 10, 3, 'iJJJUA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_41', 'F10x5', 41, 'F10x5_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_42', 1, 10, 5, 'ASrvuqQAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_42', 'F10x5', 42, 'F10x5_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_43', 1, 10, 5, 'AAhPkIAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_43', 'F10x5', 43, 'F10x5_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_44', 1, 10, 3, 'AABJQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_44', 'F10x5', 44, 'F10x5_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_45', 1, 10, 5, 'AAAPgAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_45', 'F10x5', 45, 'F10x5_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_46', 1, 10, 1, 'AEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_46', 'F10x5', 46, 'F10x5_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_47', 1, 10, 5, 'CEQiEQiEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_47', 'F10x5', 47, 'F10x5_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_48', 1, 10, 5, 'dGMZ1zGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_48', 'F10x5', 48, 'F10x5_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_49', 1, 10, 3, 'WSSSXA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_49', 'F10x5', 49, 'F10x5_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_50', 1, 10, 5, 'dGIRERCHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_50', 'F10x5', 50, 'F10x5_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_51', 1, 10, 5, 'dEITBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_51', 'F10x5', 51, 'F10x5_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_52', 1, 10, 5, 'EZUpfEIQgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_52', 'F10x5', 52, 'F10x5_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_53', 1, 10, 5, '/CEPBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_53', 'F10x5', 53, 'F10x5_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_54', 1, 10, 5, 'GREPRjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_54', 'F10x5', 54, 'F10x5_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_55', 1, 10, 5, '+EIiIQhCAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_55', 'F10x5', 55, 'F10x5_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_56', 1, 10, 5, 'dGMXRjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_56', 'F10x5', 56, 'F10x5_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_57', 1, 10, 5, 'dGMXhCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_57', 'F10x5', 57, 'F10x5_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_58', 1, 10, 3, 'AJAJAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_58', 'F10x5', 58, 'F10x5_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_59', 1, 10, 3, 'AJAJQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_59', 'F10x5', 59, 'F10x5_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_60', 1, 10, 4, 'ASSEIQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_60', 'F10x5', 60, 'F10x5_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_61', 1, 10, 5, 'AAHwfAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_61', 'F10x5', 61, 'F10x5_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_62', 1, 10, 5, 'AgggiIgAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_62', 'F10x5', 62, 'F10x5_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_63', 1, 10, 5, 'dEIiEIQBAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_63', 'F10x5', 63, 'F10x5_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_64', 1, 10, 5, 'dEIU1rWrgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_64', 'F10x5', 64, 'F10x5_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_65', 1, 10, 5, 'IqMY/jGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_65', 'F10x5', 65, 'F10x5_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_66', 1, 10, 5, '9GMfRjGPgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_66', 'F10x5', 66, 'F10x5_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_67', 1, 10, 5, 'dGEIQhCLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_67', 'F10x5', 67, 'F10x5_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_68', 1, 10, 5, '9GMYxjGPgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_68', 'F10x5', 68, 'F10x5_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_69', 1, 10, 5, '/CEPQhCHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_69', 'F10x5', 69, 'F10x5_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_70', 1, 10, 5, '/CEPQhCEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_70', 'F10x5', 70, 'F10x5_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_71', 1, 10, 5, 'dGEITjGLwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_71', 'F10x5', 71, 'F10x5_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_72', 1, 10, 5, 'jGMfxjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_72', 'F10x5', 72, 'F10x5_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_73', 1, 10, 3, '6SSSXA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_73', 'F10x5', 73, 'F10x5_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_74', 1, 10, 5, 'CEIQhCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_74', 'F10x5', 74, 'F10x5_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_75', 1, 10, 5, 'jGVMUlGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_75', 'F10x5', 75, 'F10x5_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_76', 1, 10, 5, 'hCEIQhCHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_76', 'F10x5', 76, 'F10x5_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_77', 1, 10, 7, 'g46smTBgwYME');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_77', 'F10x5', 77, 'F10x5_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_78', 1, 10, 5, 'jHOaznGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_78', 'F10x5', 78, 'F10x5_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_79', 1, 10, 5, 'dGMYxjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_79', 'F10x5', 79, 'F10x5_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_80', 1, 10, 5, '9GMfQhCEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_80', 'F10x5', 80, 'F10x5_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_81', 1, 10, 5, 'dGMYxjWTQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_81', 'F10x5', 81, 'F10x5_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_82', 1, 10, 5, '9GMfYpKMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_82', 'F10x5', 82, 'F10x5_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_83', 1, 10, 5, 'dGEHBCGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_83', 'F10x5', 83, 'F10x5_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_84', 1, 10, 5, '+QhCEIQhAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_84', 'F10x5', 84, 'F10x5_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_85', 1, 10, 5, 'jGMYxjGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_85', 'F10x5', 85, 'F10x5_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_86', 1, 10, 5, 'jGMYxjFRAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_86', 'F10x5', 86, 'F10x5_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_87', 1, 10, 7, 'gwYMGDJk1ccE');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_87', 'F10x5', 87, 'F10x5_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_88', 1, 10, 5, 'jGKiKjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_88', 'F10x5', 88, 'F10x5_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_89', 1, 10, 5, 'jGMVEIQhAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_89', 'F10x5', 89, 'F10x5_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_90', 1, 10, 6, '/BBCEIQgg/A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_90', 'F10x5', 90, 'F10x5_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_91', 1, 10, 4, '+IiIiI8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_91', 'F10x5', 91, 'F10x5_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_92', 1, 10, 5, 'hBCCEEIIQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_92', 'F10x5', 92, 'F10x5_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_93', 1, 10, 4, '8RERER8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_93', 'F10x5', 93, 'F10x5_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_94', 1, 10, 5, 'IqIAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_94', 'F10x5', 94, 'F10x5_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_95', 1, 10, 5, 'AAAAAAAHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_95', 'F10x5', 95, 'F10x5_95');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_96', 1, 10, 4, 'xjAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_96', 'F10x5', 96, 'F10x5_96');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_97', 1, 10, 5, 'AAAHBfGLwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_97', 'F10x5', 97, 'F10x5_97');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_98', 1, 10, 5, 'hCEPRjGPgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_98', 'F10x5', 98, 'F10x5_98');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_99', 1, 10, 4, 'AAB4iIc=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_99', 'F10x5', 99, 'F10x5_99');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_100', 1, 10, 5, 'CEIWzjGLwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_100', 'F10x5', 100, 'F10x5_100');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_101', 1, 10, 5, 'AAAHR9CDwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_101', 'F10x5', 101, 'F10x5_101');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_102', 1, 10, 5, 'MlCPIQhCAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_102', 'F10x5', 102, 'F10x5_102');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_103', 1, 10, 5, 'AAAHxeGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_103', 'F10x5', 103, 'F10x5_103');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_104', 1, 10, 5, 'hCELZjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_104', 'F10x5', 104, 'F10x5_104');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_105', 1, 10, 1, 'L8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_105', 'F10x5', 105, 'F10x5_105');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_106', 1, 10, 4, 'ABAREZY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_106', 'F10x5', 106, 'F10x5_106');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_107', 1, 10, 4, 'iIms6Zk=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_107', 'F10x5', 107, 'F10x5_107');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_108', 1, 10, 3, 'ySSSTA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_108', 'F10x5', 108, 'F10x5_108');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_109', 1, 10, 5, 'AAANVrGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_109', 'F10x5', 109, 'F10x5_109');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_110', 1, 10, 5, 'AAALZjGMQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_110', 'F10x5', 110, 'F10x5_110');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_111', 1, 10, 4, 'AABpmZY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_111', 'F10x5', 111, 'F10x5_111');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_112', 1, 10, 5, 'AAAPR9CEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_112', 'F10x5', 112, 'F10x5_112');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_113', 1, 10, 5, 'AAAHxeEIQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_113', 'F10x5', 113, 'F10x5_113');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_114', 1, 10, 4, 'AAC8iIg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_114', 'F10x5', 114, 'F10x5_114');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_115', 1, 10, 5, 'AAAHQcGLgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_115', 'F10x5', 115, 'F10x5_115');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_116', 1, 10, 5, 'ABCOIQhJgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_116', 'F10x5', 116, 'F10x5_116');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_117', 1, 10, 5, 'AAAIxjGbQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_117', 'F10x5', 117, 'F10x5_117');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_118', 1, 10, 5, 'AAAIxjFRAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_118', 'F10x5', 118, 'F10x5_118');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_119', 1, 10, 5, 'AAAIxjWqgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_119', 'F10x5', 119, 'F10x5_119');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_120', 1, 10, 4, 'AACZZpk=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_120', 'F10x5', 120, 'F10x5_120');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_121', 1, 10, 4, 'AACZcZY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_121', 'F10x5', 121, 'F10x5_121');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_122', 1, 10, 5, 'AAAPiIiHwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_122', 'F10x5', 122, 'F10x5_122');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_123', 1, 10, 4, 'NESEREM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_123', 'F10x5', 123, 'F10x5_123');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_124', 1, 10, 3, 'JJAJJA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_124', 'F10x5', 124, 'F10x5_124');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('F10x5_125', 1, 10, 4, 'wiISIiw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('F10x5_125', 'F10x5', 125, 'F10x5_125');
