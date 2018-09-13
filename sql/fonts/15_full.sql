\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('15_full', 21, 15, 0, 6, 3, 1);

INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_32', 1, 15, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_32', '15_full', 32, '15_full_32');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_33', 1, 15, 2, '////PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_33', '15_full', 33, '15_full_33');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_34', 1, 15, 6, 'zzRAAAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_34', '15_full', 34, '15_full_34');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_35', 1, 15, 10, 'AAADMMz///MwzDM///zMMwAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_35', '15_full', 35, '15_full_35');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_36', 1, 15, 8, 'GH7/2djYfD4fGxub/34Y');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_36', '15_full', 36, '15_full_36');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_37', 1, 15, 10, 'AB4EhSN5gMBgMBgMBnsShIHgAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_37', '15_full', 37, '15_full_37');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_38', 1, 15, 10, 'GA8GYZhmHwOA4Hw5jG8Owx/z7A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_38', '15_full', 38, '15_full_38');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_39', 1, 15, 2, '+AAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_39', '15_full', 39, '15_full_39');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_40', 1, 15, 4, 'NmbMzMzGZjA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_40', '15_full', 40, '15_full_40');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_41', 1, 15, 4, 'xmYzMzM2ZsA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_41', '15_full', 41, '15_full_41');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_42', 1, 15, 8, 'AAAY2/9+PBg8fv/bGAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_42', '15_full', 42, '15_full_42');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_43', 1, 15, 8, 'AAAAABgYGP//GBgYAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_43', '15_full', 43, '15_full_43');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_44', 1, 15, 4, 'AAAAAAAAboA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_44', '15_full', 44, '15_full_44');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_45', 1, 15, 6, 'AAAAAAA//AAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_45', '15_full', 45, '15_full_45');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_46', 1, 15, 3, 'AAAAAAGw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_46', '15_full', 46, '15_full_46');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_47', 1, 15, 8, 'AAMDBgYMDBgYMDBgYMDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_47', '15_full', 47, '15_full_47');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_48', 1, 15, 8, 'PH7nw8PDw8PDw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_48', '15_full', 48, '15_full_48');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_49', 1, 15, 5, 'M7xjGMYxjGM94A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_49', '15_full', 49, '15_full_49');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_50', 1, 15, 10, 'Hw/mDQMAwHB4eDgcBgOAwD///A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_50', '15_full', 50, '15_full_50');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_51', 1, 15, 9, 'fn+w4DAYHDweA4DAYDw/+fg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_51', '15_full', 51, '15_full_51');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_52', 1, 15, 9, 'AwODw2MzGw2G///AwGAwGAw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_52', '15_full', 52, '15_full_52');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_53', 1, 15, 9, '///wGAwH8fwHAYDAYDw/+fg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_53', '15_full', 53, '15_full_53');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_54', 1, 15, 9, 'Pz/4eAwGA3n+4+DweD47+Pg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_54', '15_full', 54, '15_full_54');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_55', 1, 15, 8, '//8DAwYGDAwYGBgwMDAw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_55', '15_full', 55, '15_full_55');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_56', 1, 15, 8, 'PH7nw8NmPH7nw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_56', '15_full', 56, '15_full_56');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_57', 1, 15, 9, 'Pj/4+DwfDf5/AYDAYDw/+fg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_57', '15_full', 57, '15_full_57');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_58', 1, 15, 3, 'AAGwGwAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_58', '15_full', 58, '15_full_58');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_59', 1, 15, 4, 'AAAGYAboAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_59', '15_full', 59, '15_full_59');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_60', 1, 15, 7, 'AAQYYYYYYGBgYGBgQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_60', '15_full', 60, '15_full_60');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_61', 1, 15, 6, 'AAAAA//AA//AAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_61', '15_full', 61, '15_full_61');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_62', 1, 15, 7, 'AQMDAwMDAwwwwwwQAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_62', '15_full', 62, '15_full_62');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_63', 1, 15, 8, 'fP7HAwMDBw4cGBgAABgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_63', '15_full', 63, '15_full_63');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_64', 1, 15, 10, 'Px/uHwPH8/zPM8zz/HsA4B/j8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_64', '15_full', 64, '15_full_64');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_65', 1, 15, 8, 'GDx+58PDw8P//8PDw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_65', '15_full', 65, '15_full_65');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_66', 1, 15, 8, '/P7Hw8PG/P7Hw8PDx/78');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_66', '15_full', 66, '15_full_66');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_67', 1, 15, 8, 'PH7nw8DAwMDAwMDD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_67', '15_full', 67, '15_full_67');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_68', 1, 15, 8, '/P7Hw8PDw8PDw8PDx/78');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_68', '15_full', 68, '15_full_68');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_69', 1, 15, 8, '///AwMDA/PzAwMDAwP//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_69', '15_full', 69, '15_full_69');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_70', 1, 15, 8, '///AwMDA+PjAwMDAwMDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_70', '15_full', 70, '15_full_70');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_71', 1, 15, 8, 'PH7nw8DAwMDHx8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_71', '15_full', 71, '15_full_71');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_72', 1, 15, 8, 'w8PDw8PD///Dw8PDw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_72', '15_full', 72, '15_full_72');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_73', 1, 15, 4, '/2ZmZmZmb/A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_73', '15_full', 73, '15_full_73');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_74', 1, 15, 7, 'BgwYMGDBgwYMHj7vjgA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_74', '15_full', 74, '15_full_74');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_75', 1, 15, 8, 'w8PHztz48PD43M7Gx8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_75', '15_full', 75, '15_full_75');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_76', 1, 15, 7, 'wYMGDBgwYMGDBgwf/4A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_76', '15_full', 76, '15_full_76');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_77', 1, 15, 11, 'wHwfx/3995zxHgPAeA8B4DwHgPAY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_77', '15_full', 77, '15_full_77');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_78', 1, 15, 9, 'weD4fD8ej2eTzeLx+Hw+DwY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_78', '15_full', 78, '15_full_78');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_79', 1, 15, 8, 'PH7nw8PDw8PDw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_79', '15_full', 79, '15_full_79');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_80', 1, 15, 8, '/P7Hw8PH/vzAwMDAwMDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_80', '15_full', 80, '15_full_80');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_81', 1, 15, 8, 'PH7nw8PDw8PDw9vf7n87');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_81', '15_full', 81, '15_full_81');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_82', 1, 15, 8, '/P7Hw8PH/vzMzMbGw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_82', '15_full', 82, '15_full_82');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_83', 1, 15, 8, 'PH7nw8DgfB4HAwPD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_83', '15_full', 83, '15_full_83');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_84', 1, 15, 8, '//8YGBgYGBgYGBgYGBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_84', '15_full', 84, '15_full_84');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_85', 1, 15, 8, 'w8PDw8PDw8PDw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_85', '15_full', 85, '15_full_85');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_86', 1, 15, 8, 'w8PDw8PDw8NmZmY8PBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_86', '15_full', 86, '15_full_86');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_87', 1, 15, 12, 'wDwDwDwDwDwDwDwDxjxj73f+eeeeMMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_87', '15_full', 87, '15_full_87');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_88', 1, 15, 8, 'w8PDZmY8PBg8PGZmw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_88', '15_full', 88, '15_full_88');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_89', 1, 15, 8, 'w8PDZmZmPDwYGBgYGBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_89', '15_full', 89, '15_full_89');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_90', 1, 15, 8, '//8DAwYGDBgwYGDAwP//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_90', '15_full', 90, '15_full_90');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_91', 1, 15, 5, '//GMYxjGMYx/4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_91', '15_full', 91, '15_full_91');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_92', 1, 15, 8, 'AMDAYGAwMBgYDAwGBgMD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_92', '15_full', 92, '15_full_92');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_93', 1, 15, 5, '/8YxjGMYxjH/4A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_93', '15_full', 93, '15_full_93');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_94', 1, 15, 8, 'GDxmw4EAAAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_94', '15_full', 94, '15_full_94');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('15_full_95', 1, 15, 7, 'AAAAAAAAAAAAAAAf/4A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('15_full_95', '15_full', 95, '15_full_95');

COMMIT;
