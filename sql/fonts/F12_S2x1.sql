\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('F12_S2x1', 4, 12, 0, 2, 1, 12345);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_32', 1, 12, 2, 'AAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_32', 'F12_S2x1', 32, 'F12_S2x1_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_33', 1, 12, 1, '/oA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_33', 'F12_S2x1', 33, 'F12_S2x1_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_34', 1, 12, 3, 'toAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_34', 'F12_S2x1', 34, 'F12_S2x1_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_35', 1, 12, 5, 'Ur6lfUoAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_35', 'F12_S2x1', 35, 'F12_S2x1_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_36', 1, 12, 5, 'I6lHFK4gAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_36', 'F12_S2x1', 36, 'F12_S2x1_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_37', 1, 12, 5, 'zkQiIROYAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_37', 'F12_S2x1', 37, 'F12_S2x1_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_38', 1, 12, 6, 'IUUIYliidBAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_38', 'F12_S2x1', 38, 'F12_S2x1_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_39', 1, 12, 3, 'SQAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_39', 'F12_S2x1', 39, 'F12_S2x1_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_40', 1, 12, 3, 'KkkiIAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_40', 'F12_S2x1', 40, 'F12_S2x1_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_41', 1, 12, 3, 'iJJKgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_41', 'F12_S2x1', 41, 'F12_S2x1_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_42', 1, 12, 5, 'IpUYxUogAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_42', 'F12_S2x1', 42, 'F12_S2x1_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_43', 1, 12, 5, 'AABCfIQAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_43', 'F12_S2x1', 43, 'F12_S2x1_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_44', 1, 12, 3, 'AAACUAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_44', 'F12_S2x1', 44, 'F12_S2x1_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_45', 1, 12, 4, 'AAAPAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_45', 'F12_S2x1', 45, 'F12_S2x1_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_46', 1, 12, 3, 'AAAGwAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_46', 'F12_S2x1', 46, 'F12_S2x1_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_47', 1, 12, 5, 'CEQiEQiEAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_47', 'F12_S2x1', 47, 'F12_S2x1_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_48', 1, 12, 5, 'dGMYxjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_48', 'F12_S2x1', 48, 'F12_S2x1_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_49', 1, 12, 3, 'WSSS4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_49', 'F12_S2x1', 49, 'F12_S2x1_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_50', 1, 12, 5, 'dEIiIhD4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_50', 'F12_S2x1', 50, 'F12_S2x1_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_51', 1, 12, 5, 'dEITBDFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_51', 'F12_S2x1', 51, 'F12_S2x1_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_52', 1, 12, 5, 'EZUpfEIQAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_52', 'F12_S2x1', 52, 'F12_S2x1_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_53', 1, 12, 5, '/CEPBDFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_53', 'F12_S2x1', 53, 'F12_S2x1_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_54', 1, 12, 5, 'dGEPRjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_54', 'F12_S2x1', 54, 'F12_S2x1_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_55', 1, 12, 5, '+EIhEIhAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_55', 'F12_S2x1', 55, 'F12_S2x1_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_56', 1, 12, 5, 'dGMXRjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_56', 'F12_S2x1', 56, 'F12_S2x1_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_57', 1, 12, 5, 'dGMXhDFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_57', 'F12_S2x1', 57, 'F12_S2x1_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_58', 1, 12, 2, 'AIgA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_58', 'F12_S2x1', 58, 'F12_S2x1_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_59', 1, 12, 3, 'AAQUAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_59', 'F12_S2x1', 59, 'F12_S2x1_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_60', 1, 12, 4, 'ABJIQhAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_60', 'F12_S2x1', 60, 'F12_S2x1_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_61', 1, 12, 4, 'AADw8AAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_61', 'F12_S2x1', 61, 'F12_S2x1_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_62', 1, 12, 4, 'AIQhJIAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_62', 'F12_S2x1', 62, 'F12_S2x1_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_63', 1, 12, 5, 'dEIREIAgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_63', 'F12_S2x1', 63, 'F12_S2x1_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_64', 1, 12, 6, 'AciBZllleAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_64', 'F12_S2x1', 64, 'F12_S2x1_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_65', 1, 12, 5, 'IqMfxjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_65', 'F12_S2x1', 65, 'F12_S2x1_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_66', 1, 12, 5, '9GMfRjHwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_66', 'F12_S2x1', 66, 'F12_S2x1_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_67', 1, 12, 5, 'dGEIQhFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_67', 'F12_S2x1', 67, 'F12_S2x1_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_68', 1, 12, 5, '9GMYxjHwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_68', 'F12_S2x1', 68, 'F12_S2x1_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_69', 1, 12, 5, '/CEOQhD4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_69', 'F12_S2x1', 69, 'F12_S2x1_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_70', 1, 12, 5, '/CEOQhCAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_70', 'F12_S2x1', 70, 'F12_S2x1_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_71', 1, 12, 5, 'dGEJxjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_71', 'F12_S2x1', 71, 'F12_S2x1_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_72', 1, 12, 5, 'jGMfxjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_72', 'F12_S2x1', 72, 'F12_S2x1_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_73', 1, 12, 3, '6SSS4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_73', 'F12_S2x1', 73, 'F12_S2x1_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_74', 1, 12, 4, 'ERERGWAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_74', 'F12_S2x1', 74, 'F12_S2x1_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_75', 1, 12, 5, 'jGVMUlGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_75', 'F12_S2x1', 75, 'F12_S2x1_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_76', 1, 12, 4, 'iIiIiPAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_76', 'F12_S2x1', 76, 'F12_S2x1_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_77', 1, 12, 7, 'g46smTBgwYIAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_77', 'F12_S2x1', 77, 'F12_S2x1_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_78', 1, 12, 5, 'jHOaznGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_78', 'F12_S2x1', 78, 'F12_S2x1_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_79', 1, 12, 5, 'dGMYxjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_79', 'F12_S2x1', 79, 'F12_S2x1_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_80', 1, 12, 5, '9GMfQhCAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_80', 'F12_S2x1', 80, 'F12_S2x1_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_81', 1, 12, 5, 'dGMYxrJoQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_81', 'F12_S2x1', 81, 'F12_S2x1_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_82', 1, 12, 5, '9GMfUlGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_82', 'F12_S2x1', 82, 'F12_S2x1_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_83', 1, 12, 5, 'dGEHBDFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_83', 'F12_S2x1', 83, 'F12_S2x1_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_84', 1, 12, 5, '+QhCEIQgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_84', 'F12_S2x1', 84, 'F12_S2x1_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_85', 1, 12, 5, 'jGMYxjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_85', 'F12_S2x1', 85, 'F12_S2x1_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_86', 1, 12, 5, 'jGMYqUQgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_86', 'F12_S2x1', 86, 'F12_S2x1_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_87', 1, 12, 7, 'gwYMGTJq44IAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_87', 'F12_S2x1', 87, 'F12_S2x1_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_88', 1, 12, 5, 'jGKiKjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_88', 'F12_S2x1', 88, 'F12_S2x1_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_89', 1, 12, 5, 'jGKiEIQgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_89', 'F12_S2x1', 89, 'F12_S2x1_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_90', 1, 12, 5, '+EQiIRD4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_90', 'F12_S2x1', 90, 'F12_S2x1_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_91', 1, 12, 3, '8kkk4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_91', 'F12_S2x1', 91, 'F12_S2x1_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_92', 1, 12, 5, 'hBCCEEIIQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_92', 'F12_S2x1', 92, 'F12_S2x1_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_93', 1, 12, 3, '5JJJ4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_93', 'F12_S2x1', 93, 'F12_S2x1_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_94', 1, 12, 5, 'IqIAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_94', 'F12_S2x1', 94, 'F12_S2x1_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_95', 1, 12, 4, 'AAAAAA8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_95', 'F12_S2x1', 95, 'F12_S2x1_95');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_96', 1, 12, 2, 'qQAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_96', 'F12_S2x1', 96, 'F12_S2x1_96');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_97', 1, 12, 5, 'AADgvjNoAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_97', 'F12_S2x1', 97, 'F12_S2x1_97');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_98', 1, 12, 5, 'hCEPRjmwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_98', 'F12_S2x1', 98, 'F12_S2x1_98');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_99', 1, 12, 4, 'AAB4iHAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_99', 'F12_S2x1', 99, 'F12_S2x1_99');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_100', 1, 12, 5, 'CEIXxjNoAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_100', 'F12_S2x1', 100, 'F12_S2x1_100');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_101', 1, 12, 5, 'AADox9CLgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_101', 'F12_S2x1', 101, 'F12_S2x1_101');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_102', 1, 12, 4, 'NET0REAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_102', 'F12_S2x1', 102, 'F12_S2x1_102');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_103', 1, 12, 5, 'AAAGzjF4YuA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_103', 'F12_S2x1', 103, 'F12_S2x1_103');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_104', 1, 12, 5, 'hCELZjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_104', 'F12_S2x1', 104, 'F12_S2x1_104');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_105', 1, 12, 3, 'AQSSQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_105', 'F12_S2x1', 105, 'F12_S2x1_105');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_106', 1, 12, 4, 'ABARERGW');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_106', 'F12_S2x1', 106, 'F12_S2x1_106');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_107', 1, 12, 5, 'hCMqYpKIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_107', 'F12_S2x1', 107, 'F12_S2x1_107');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_108', 1, 12, 3, 'SSSSQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_108', 'F12_S2x1', 108, 'F12_S2x1_108');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_109', 1, 12, 7, 'AAAACtpkyZIAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_109', 'F12_S2x1', 109, 'F12_S2x1_109');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_110', 1, 12, 5, 'AAALZjGIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_110', 'F12_S2x1', 110, 'F12_S2x1_110');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_111', 1, 12, 5, 'AAAHRjFwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_111', 'F12_S2x1', 111, 'F12_S2x1_111');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_112', 1, 12, 5, 'AAALZjH0IQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_112', 'F12_S2x1', 112, 'F12_S2x1_112');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_113', 1, 12, 5, 'AAAGzjF4QhA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_113', 'F12_S2x1', 113, 'F12_S2x1_113');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_114', 1, 12, 4, 'AAC8iIAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_114', 'F12_S2x1', 114, 'F12_S2x1_114');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_115', 1, 12, 5, 'AADowcGLgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_115', 'F12_S2x1', 115, 'F12_S2x1_115');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_116', 1, 12, 4, 'AETkRDAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_116', 'F12_S2x1', 116, 'F12_S2x1_116');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_117', 1, 12, 5, 'AAAIxjNoAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_117', 'F12_S2x1', 117, 'F12_S2x1_117');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_118', 1, 12, 5, 'AAAIxiogAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_118', 'F12_S2x1', 118, 'F12_S2x1_118');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_119', 1, 12, 7, 'AAAACDBk1UQAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_119', 'F12_S2x1', 119, 'F12_S2x1_119');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_120', 1, 12, 5, 'AAAIqIqIAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_120', 'F12_S2x1', 120, 'F12_S2x1_120');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_121', 1, 12, 5, 'AAAIxjNoQuA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_121', 'F12_S2x1', 121, 'F12_S2x1_121');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_122', 1, 12, 5, 'AAHxERD4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_122', 'F12_S2x1', 122, 'F12_S2x1_122');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_123', 1, 12, 3, 'aSiSYAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_123', 'F12_S2x1', 123, 'F12_S2x1_123');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_124', 1, 12, 1, '94A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_124', 'F12_S2x1', 124, 'F12_S2x1_124');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S2x1_125', 1, 12, 3, 'ySKSwAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S2x1_125', 'F12_S2x1', 125, 'F12_S2x1_125');
