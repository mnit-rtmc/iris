\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('12_lower_9', 8, 12, 0, 2, 2, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_32', 1, 12, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_32', '12_lower_9', 32, '12_lower_9_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_33', 1, 12, 2, 'qqiA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_33', '12_lower_9', 33, '12_lower_9_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_34', 1, 12, 3, 'tAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_34', '12_lower_9', 34, '12_lower_9_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_35', 1, 12, 5, 'Ur6lfUoAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_35', '12_lower_9', 35, '12_lower_9_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_36', 1, 12, 5, 'I6lHFK4gAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_36', '12_lower_9', 36, '12_lower_9_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_37', 1, 12, 5, 'BnQiIXMAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_37', '12_lower_9', 37, '12_lower_9_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_38', 1, 12, 6, 'IUUIYliidBAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_38', '12_lower_9', 38, '12_lower_9_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_39', 1, 12, 1, 'wAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_39', '12_lower_9', 39, '12_lower_9_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_40', 1, 12, 3, 'KkkiIAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_40', '12_lower_9', 40, '12_lower_9_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_41', 1, 12, 3, 'iJJKgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_41', '12_lower_9', 41, '12_lower_9_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_42', 1, 12, 5, 'ASriOqQAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_42', '12_lower_9', 42, '12_lower_9_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_43', 1, 12, 5, 'AAhPkIAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_43', '12_lower_9', 43, '12_lower_9_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_44', 1, 12, 3, 'AAACUAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_44', '12_lower_9', 44, '12_lower_9_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_45', 1, 12, 4, 'AADwAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_45', '12_lower_9', 45, '12_lower_9_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_46', 1, 12, 2, 'AACA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_46', '12_lower_9', 46, '12_lower_9_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_47', 1, 12, 5, 'CEQiIRCAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_47', '12_lower_9', 47, '12_lower_9_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_48', 1, 12, 5, 'dGMYxjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_48', '12_lower_9', 48, '12_lower_9_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_49', 1, 12, 3, 'WSSS4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_49', '12_lower_9', 49, '12_lower_9_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_50', 1, 12, 5, 'dEIiIhD4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_50', '12_lower_9', 50, '12_lower_9_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_51', 1, 12, 5, 'dEITBDFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_51', '12_lower_9', 51, '12_lower_9_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_52', 1, 12, 5, 'EZUpfEIQAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_52', '12_lower_9', 52, '12_lower_9_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_53', 1, 12, 5, '/CEPBDFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_53', '12_lower_9', 53, '12_lower_9_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_54', 1, 12, 5, 'dGEPRjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_54', '12_lower_9', 54, '12_lower_9_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_55', 1, 12, 5, '+EIhEIhAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_55', '12_lower_9', 55, '12_lower_9_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_56', 1, 12, 5, 'dGMXRjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_56', '12_lower_9', 56, '12_lower_9_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_57', 1, 12, 5, 'dGMXhDFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_57', '12_lower_9', 57, '12_lower_9_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_58', 1, 12, 2, 'AiAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_58', '12_lower_9', 58, '12_lower_9_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_59', 1, 12, 3, 'ACCgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_59', '12_lower_9', 59, '12_lower_9_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_60', 1, 12, 4, 'ASSEIQAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_60', '12_lower_9', 60, '12_lower_9_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_61', 1, 12, 4, 'AA8PAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_61', '12_lower_9', 61, '12_lower_9_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_62', 1, 12, 4, 'CEISSAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_62', '12_lower_9', 62, '12_lower_9_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_63', 1, 12, 5, 'dEIREIAgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_63', '12_lower_9', 63, '12_lower_9_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_64', 1, 12, 6, 'ehlrppngeAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_64', '12_lower_9', 64, '12_lower_9_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_65', 1, 12, 5, 'IqMfxjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_65', '12_lower_9', 65, '12_lower_9_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_66', 1, 12, 5, '9GMfRjHwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_66', '12_lower_9', 66, '12_lower_9_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_67', 1, 12, 5, 'dGEIQhFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_67', '12_lower_9', 67, '12_lower_9_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_68', 1, 12, 5, '9GMYxjHwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_68', '12_lower_9', 68, '12_lower_9_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_69', 1, 12, 5, '/CEOQhD4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_69', '12_lower_9', 69, '12_lower_9_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_70', 1, 12, 5, '/CEOQhCAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_70', '12_lower_9', 70, '12_lower_9_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_71', 1, 12, 5, 'dGEJxjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_71', '12_lower_9', 71, '12_lower_9_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_72', 1, 12, 5, 'jGMfxjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_72', '12_lower_9', 72, '12_lower_9_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_73', 1, 12, 3, '6SSS4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_73', '12_lower_9', 73, '12_lower_9_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_74', 1, 12, 4, 'ERERGWAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_74', '12_lower_9', 74, '12_lower_9_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_75', 1, 12, 5, 'jGVMUlGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_75', '12_lower_9', 75, '12_lower_9_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_76', 1, 12, 4, 'iIiIiPAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_76', '12_lower_9', 76, '12_lower_9_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_77', 1, 12, 7, 'g46smTBgwYIAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_77', '12_lower_9', 77, '12_lower_9_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_78', 1, 12, 6, 'xxpplljjhAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_78', '12_lower_9', 78, '12_lower_9_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_79', 1, 12, 5, 'dGMYxjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_79', '12_lower_9', 79, '12_lower_9_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_80', 1, 12, 5, '9GMfQhCAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_80', '12_lower_9', 80, '12_lower_9_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_81', 1, 12, 5, 'dGMYxrJoQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_81', '12_lower_9', 81, '12_lower_9_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_82', 1, 12, 5, '9GMfUlGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_82', '12_lower_9', 82, '12_lower_9_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_83', 1, 12, 5, 'dGEHBDFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_83', '12_lower_9', 83, '12_lower_9_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_84', 1, 12, 5, '+QhCEIQgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_84', '12_lower_9', 84, '12_lower_9_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_85', 1, 12, 5, 'jGMYxjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_85', '12_lower_9', 85, '12_lower_9_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_86', 1, 12, 5, 'jGMYqUQgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_86', '12_lower_9', 86, '12_lower_9_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_87', 1, 12, 7, 'gwYMGTJq1UQAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_87', '12_lower_9', 87, '12_lower_9_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_88', 1, 12, 5, 'jGKiKjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_88', '12_lower_9', 88, '12_lower_9_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_89', 1, 12, 5, 'jGKiEIQgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_89', '12_lower_9', 89, '12_lower_9_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_90', 1, 12, 5, '+EQiIRD4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_90', '12_lower_9', 90, '12_lower_9_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_91', 1, 12, 3, '8kkk4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_91', '12_lower_9', 91, '12_lower_9_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_92', 1, 12, 5, 'hBCCCEEIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_92', '12_lower_9', 92, '12_lower_9_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_93', 1, 12, 3, '5JJJ4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_93', '12_lower_9', 93, '12_lower_9_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_94', 1, 12, 5, 'IqIAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_94', '12_lower_9', 94, '12_lower_9_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_95', 1, 12, 4, 'AAAAAA8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_95', '12_lower_9', 95, '12_lower_9_95');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_96', 1, 12, 2, 'pAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_96', '12_lower_9', 96, '12_lower_9_96');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_97', 1, 12, 5, 'AADgvjNoAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_97', '12_lower_9', 97, '12_lower_9_97');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_98', 1, 12, 5, 'hCEPRjmwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_98', '12_lower_9', 98, '12_lower_9_98');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_99', 1, 12, 4, 'AAB4iHAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_99', '12_lower_9', 99, '12_lower_9_99');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_100', 1, 12, 5, 'CEIXxjNoAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_100', '12_lower_9', 100, '12_lower_9_100');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_101', 1, 12, 5, 'AADox9BwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_101', '12_lower_9', 101, '12_lower_9_101');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_102', 1, 12, 4, 'NET0REAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_102', '12_lower_9', 102, '12_lower_9_102');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_103', 1, 12, 5, 'AAAGzjF4YuA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_103', '12_lower_9', 103, '12_lower_9_103');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_104', 1, 12, 5, 'hCELZjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_104', '12_lower_9', 104, '12_lower_9_104');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_105', 1, 12, 3, 'AQSSQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_105', '12_lower_9', 105, '12_lower_9_105');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_106', 1, 12, 4, 'ABARERGW');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_106', '12_lower_9', 106, '12_lower_9_106');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_107', 1, 12, 5, 'hCMqYpKIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_107', '12_lower_9', 107, '12_lower_9_107');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_108', 1, 12, 3, 'SSSSQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_108', '12_lower_9', 108, '12_lower_9_108');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_109', 1, 12, 7, 'AAAACtpkyZIAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_109', '12_lower_9', 109, '12_lower_9_109');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_110', 1, 12, 5, 'AAALZjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_110', '12_lower_9', 110, '12_lower_9_110');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_111', 1, 12, 5, 'AAAHRjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_111', '12_lower_9', 111, '12_lower_9_111');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_112', 1, 12, 5, 'AAALZjH0IQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_112', '12_lower_9', 112, '12_lower_9_112');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_113', 1, 12, 5, 'AAAGzjF4QhA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_113', '12_lower_9', 113, '12_lower_9_113');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_114', 1, 12, 4, 'AAC8iIAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_114', '12_lower_9', 114, '12_lower_9_114');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_115', 1, 12, 5, 'AADosFFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_115', '12_lower_9', 115, '12_lower_9_115');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_116', 1, 12, 4, 'AETkRDAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_116', '12_lower_9', 116, '12_lower_9_116');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_117', 1, 12, 5, 'AAAIxjNoAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_117', '12_lower_9', 117, '12_lower_9_117');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_118', 1, 12, 5, 'AAAIxiogAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_118', '12_lower_9', 118, '12_lower_9_118');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_119', 1, 12, 7, 'AAAACDBk1UQAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_119', '12_lower_9', 119, '12_lower_9_119');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_120', 1, 12, 5, 'AAAIqIqIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_120', '12_lower_9', 120, '12_lower_9_120');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_121', 1, 12, 5, 'AAAIxjNoQuA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_121', '12_lower_9', 121, '12_lower_9_121');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_122', 1, 12, 5, 'AAHxERD4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_122', '12_lower_9', 122, '12_lower_9_122');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_123', 1, 12, 3, 'aSiSYAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_123', '12_lower_9', 123, '12_lower_9_123');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_124', 1, 12, 1, '94A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_124', '12_lower_9', 124, '12_lower_9_124');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_lower_9_125', 1, 12, 3, 'ySKSwAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_lower_9_125', '12_lower_9', 125, '12_lower_9_125');
