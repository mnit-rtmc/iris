\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('09_full', 4, 9, 0, 2, 2, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_32', 1, 9, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_32', '09_full', 32, '09_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_33', 1, 9, 2, 'qqiA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_33', '09_full', 33, '09_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_34', 1, 9, 3, 'tAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_34', '09_full', 34, '09_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_35', 1, 9, 5, 'Ur6lfUoA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_35', '09_full', 35, '09_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_36', 1, 9, 5, 'I6lHFK4g');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_36', '09_full', 36, '09_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_37', 1, 9, 5, 'BjIiImMA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_37', '09_full', 37, '09_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_38', 1, 9, 6, 'IUUIYliidA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_38', '09_full', 38, '09_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_39', 1, 9, 1, 'wAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_39', '09_full', 39, '09_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_40', 1, 9, 3, 'KkkiIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_40', '09_full', 40, '09_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_41', 1, 9, 3, 'iJJKgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_41', '09_full', 41, '09_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_42', 1, 9, 5, 'ASriOqQA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_42', '09_full', 42, '09_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_43', 1, 9, 5, 'AAhPkIAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_43', '09_full', 43, '09_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_44', 1, 9, 3, 'AAACgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_44', '09_full', 44, '09_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_45', 1, 9, 4, 'AADwAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_45', '09_full', 45, '09_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_46', 1, 9, 2, 'AACA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_46', '09_full', 46, '09_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_47', 1, 9, 5, 'CEQiIRCA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_47', '09_full', 47, '09_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_48', 1, 9, 5, 'dGMYxjFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_48', '09_full', 48, '09_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_49', 1, 9, 3, 'WSSS4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_49', '09_full', 49, '09_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_50', 1, 9, 5, 'dEIiIhD4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_50', '09_full', 50, '09_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_51', 1, 9, 5, 'dEITBDFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_51', '09_full', 51, '09_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_52', 1, 9, 5, 'EZUpfEIQ');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_52', '09_full', 52, '09_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_53', 1, 9, 5, '/CEPBDFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_53', '09_full', 53, '09_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_54', 1, 9, 5, 'dGEPRjFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_54', '09_full', 54, '09_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_55', 1, 9, 5, '+EIhEIhA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_55', '09_full', 55, '09_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_56', 1, 9, 5, 'dGMXRjFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_56', '09_full', 56, '09_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_57', 1, 9, 5, 'dGMXhDFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_57', '09_full', 57, '09_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_58', 1, 9, 2, 'AiAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_58', '09_full', 58, '09_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_59', 1, 9, 3, 'ACCgAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_59', '09_full', 59, '09_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_60', 1, 9, 4, 'ASSEIQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_60', '09_full', 60, '09_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_61', 1, 9, 4, 'AA8PAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_61', '09_full', 61, '09_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_62', 1, 9, 4, 'CEISSAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_62', '09_full', 62, '09_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_63', 1, 9, 5, 'dEIREIAg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_63', '09_full', 63, '09_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_64', 1, 9, 6, 'ehlrppngeA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_64', '09_full', 64, '09_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_65', 1, 9, 5, 'IqMfxjGI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_65', '09_full', 65, '09_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_66', 1, 9, 5, '9GMfRjHw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_66', '09_full', 66, '09_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_67', 1, 9, 5, 'dGEIQhFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_67', '09_full', 67, '09_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_68', 1, 9, 5, '9GMYxjHw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_68', '09_full', 68, '09_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_69', 1, 9, 5, '/CEOQhD4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_69', '09_full', 69, '09_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_70', 1, 9, 5, '/CEOQhCA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_70', '09_full', 70, '09_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_71', 1, 9, 5, 'dGEJxjFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_71', '09_full', 71, '09_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_72', 1, 9, 5, 'jGMfxjGI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_72', '09_full', 72, '09_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_73', 1, 9, 3, '6SSS4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_73', '09_full', 73, '09_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_74', 1, 9, 4, 'ERERGWA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_74', '09_full', 74, '09_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_75', 1, 9, 5, 'jGVMUlGI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_75', '09_full', 75, '09_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_76', 1, 9, 4, 'iIiIiPA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_76', '09_full', 76, '09_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_77', 1, 9, 7, 'g46smTBgwYI=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_77', '09_full', 77, '09_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_78', 1, 9, 6, 'xxpplljjhA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_78', '09_full', 78, '09_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_79', 1, 9, 5, 'dGMYxjFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_79', '09_full', 79, '09_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_80', 1, 9, 5, '9GMfQhCA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_80', '09_full', 80, '09_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_81', 1, 9, 5, 'dGMYxrJo');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_81', '09_full', 81, '09_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_82', 1, 9, 5, '9GMfUlGI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_82', '09_full', 82, '09_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_83', 1, 9, 5, 'dGEHBDFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_83', '09_full', 83, '09_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_84', 1, 9, 5, '+QhCEIQg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_84', '09_full', 84, '09_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_85', 1, 9, 5, 'jGMYxjFw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_85', '09_full', 85, '09_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_86', 1, 9, 5, 'jGMYqUQg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_86', '09_full', 86, '09_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_87', 1, 9, 7, 'gwYMGTJq1UQ=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_87', '09_full', 87, '09_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_88', 1, 9, 5, 'jGKiKjGI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_88', '09_full', 88, '09_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_89', 1, 9, 5, 'jGKiEIQg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_89', '09_full', 89, '09_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_90', 1, 9, 5, '+EQiIRD4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_90', '09_full', 90, '09_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_91', 1, 9, 3, '8kkk4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_91', '09_full', 91, '09_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_92', 1, 9, 5, 'hBCCCEEI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_92', '09_full', 92, '09_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_93', 1, 9, 3, '5JJJ4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_93', '09_full', 93, '09_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_94', 1, 9, 5, 'IqIAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_94', '09_full', 94, '09_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('09_full_95', 1, 9, 5, 'AAAAAAD4');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('09_full_95', '09_full', 95, '09_full_95');
