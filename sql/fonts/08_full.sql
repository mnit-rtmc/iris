\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('08_full', 3, 8, 0, 2, 2, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_32', 1, 8, 1, 'AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_32', '08_full', 32, '08_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_33', 1, 8, 2, 'qqI=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_33', '08_full', 33, '08_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_34', 1, 8, 3, 'tAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_34', '08_full', 34, '08_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_35', 1, 8, 5, 'Ur6vqUA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_35', '08_full', 35, '08_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_36', 1, 8, 5, 'I6jiuIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_36', '08_full', 36, '08_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_37', 1, 8, 5, 'xkRETGA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_37', '08_full', 37, '08_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_38', 1, 8, 5, 'RSiKym0=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_38', '08_full', 38, '08_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_39', 1, 8, 1, 'wA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_39', '08_full', 39, '08_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_40', 1, 8, 3, 'KkkR');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_40', '08_full', 40, '08_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_41', 1, 8, 3, 'iJJU');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_41', '08_full', 41, '08_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_42', 1, 8, 5, 'ASrnVIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_42', '08_full', 42, '08_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_43', 1, 8, 5, 'AQnyEAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_43', '08_full', 43, '08_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_44', 1, 8, 3, 'AAAU');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_44', '08_full', 44, '08_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_45', 1, 8, 4, 'AA8AAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_45', '08_full', 45, '08_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_46', 1, 8, 2, 'AAI=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_46', '08_full', 46, '08_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_47', 1, 8, 5, 'CERCIhA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_47', '08_full', 47, '08_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_48', 1, 8, 4, 'aZmZlg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_48', '08_full', 48, '08_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_49', 1, 8, 3, 'WSSX');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_49', '08_full', 49, '08_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_50', 1, 8, 5, 'dEImQh8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_50', '08_full', 50, '08_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_51', 1, 8, 5, 'dEJghi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_51', '08_full', 51, '08_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_52', 1, 8, 5, 'EZUviEI=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_52', '08_full', 52, '08_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_53', 1, 8, 5, '/CDghi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_53', '08_full', 53, '08_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_54', 1, 8, 5, 'dGHoxi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_54', '08_full', 54, '08_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_55', 1, 8, 5, '+EQiEQg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_55', '08_full', 55, '08_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_56', 1, 8, 5, 'dGLoxi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_56', '08_full', 56, '08_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_57', 1, 8, 5, 'dGMXhi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_57', '08_full', 57, '08_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_58', 1, 8, 2, 'CIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_58', '08_full', 58, '08_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_59', 1, 8, 3, 'AQUA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_59', '08_full', 59, '08_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_60', 1, 8, 4, 'EkhCEA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_60', '08_full', 60, '08_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_61', 1, 8, 4, 'APDwAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_61', '08_full', 61, '08_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_62', 1, 8, 4, 'hCEkgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_62', '08_full', 62, '08_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_63', 1, 8, 5, 'dEIiEAQ=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_63', '08_full', 63, '08_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_64', 1, 8, 5, 'dGdazg4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_64', '08_full', 64, '08_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_65', 1, 8, 5, 'dGMfxjE=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_65', '08_full', 65, '08_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_66', 1, 8, 5, '9GPoxj4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_66', '08_full', 66, '08_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_67', 1, 8, 5, 'dGEIQi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_67', '08_full', 67, '08_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_68', 1, 8, 5, '9GMYxj4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_68', '08_full', 68, '08_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_69', 1, 8, 5, '/CHIQh8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_69', '08_full', 69, '08_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_70', 1, 8, 5, '/CHIQhA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_70', '08_full', 70, '08_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_71', 1, 8, 5, 'dGEJxi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_71', '08_full', 71, '08_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_72', 1, 8, 5, 'jGP4xjE=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_72', '08_full', 72, '08_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_73', 1, 8, 3, '6SSX');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_73', '08_full', 73, '08_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_74', 1, 8, 4, 'ERERlg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_74', '08_full', 74, '08_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_75', 1, 8, 5, 'jKmKSjE=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_75', '08_full', 75, '08_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_76', 1, 8, 4, 'iIiIjw==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_76', '08_full', 76, '08_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_77', 1, 8, 7, 'g46smTBgwQ==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_77', '08_full', 77, '08_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_78', 1, 8, 5, 'jnNaznE=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_78', '08_full', 78, '08_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_79', 1, 8, 5, 'dGMYxi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_79', '08_full', 79, '08_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_80', 1, 8, 5, '9GPoQhA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_80', '08_full', 80, '08_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_81', 1, 8, 5, 'dGMY1k0=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_81', '08_full', 81, '08_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_82', 1, 8, 5, '9GPqSjE=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_82', '08_full', 82, '08_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_83', 1, 8, 5, 'dGDghi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_83', '08_full', 83, '08_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_84', 1, 8, 5, '+QhCEIQ=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_84', '08_full', 84, '08_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_85', 1, 8, 5, 'jGMYxi4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_85', '08_full', 85, '08_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_86', 1, 8, 5, 'jGMYqUQ=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_86', '08_full', 86, '08_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_87', 1, 8, 7, 'gwYMGTVqog==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_87', '08_full', 87, '08_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_88', 1, 8, 5, 'jFRCKjE=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_88', '08_full', 88, '08_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_89', 1, 8, 5, 'jFSiEIQ=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_89', '08_full', 89, '08_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_90', 1, 8, 5, '+ERCIh8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_90', '08_full', 90, '08_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_91', 1, 8, 3, '8kkn');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_91', '08_full', 91, '08_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_92', 1, 8, 5, 'hBBCCCE=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_92', '08_full', 92, '08_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_93', 1, 8, 3, '5JJP');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_93', '08_full', 93, '08_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_94', 1, 8, 5, 'IqIAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_94', '08_full', 94, '08_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('08_full_95', 1, 8, 5, 'AAAAAB8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('08_full_95', '08_full', 95, '08_full_95');
