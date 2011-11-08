\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('F18_S5x3', 6, 18, 0, 5, 3, 12345);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_32', 1, 18, 1, 'AAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_32', 'F18_S5x3', 32, 'F18_S5x3_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_33', 1, 18, 2, '////8PA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_33', 'F18_S5x3', 33, 'F18_S5x3_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_34', 1, 18, 6, 'zzzAAAAAAAAAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_34', 'F18_S5x3', 34, 'F18_S5x3_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_35', 1, 18, 8, 'AAAAZmb//2ZmZmb//2ZmAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_35', 'F18_S5x3', 35, 'F18_S5x3_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_36', 1, 18, 8, 'GBh+/9vY2Ph8Ph8bG9v/fhgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_36', 'F18_S5x3', 36, 'F18_S5x3_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_37', 1, 18, 8, 'AADj4+YGDAwYGDAwYGfHxwAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_37', 'F18_S5x3', 37, 'F18_S5x3_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_38', 1, 18, 8, 'HD5zYGBgYGBw8dvPzs7e/3Mj');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_38', 'F18_S5x3', 38, 'F18_S5x3_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_39', 1, 18, 3, 'fwAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_39', 'F18_S5x3', 39, 'F18_S5x3_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_40', 1, 18, 4, 'N27MzMzMzOZz');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_40', 'F18_S5x3', 40, 'F18_S5x3_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_41', 1, 18, 4, 'zmczMzMzM3bs');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_41', 'F18_S5x3', 41, 'F18_S5x3_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_42', 1, 18, 8, 'AAAYGJnb/348PH7/25kYGAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_42', 'F18_S5x3', 42, 'F18_S5x3_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_43', 1, 18, 6, 'AAAAAAMMM//MMMAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_43', 'F18_S5x3', 43, 'F18_S5x3_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_44', 1, 18, 3, 'AAAAAAAD+A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_44', 'F18_S5x3', 44, 'F18_S5x3_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_45', 1, 18, 5, 'AAAAAAAH/gAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_45', 'F18_S5x3', 45, 'F18_S5x3_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_46', 1, 18, 2, 'AAAAA8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_46', 'F18_S5x3', 46, 'F18_S5x3_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_47', 1, 18, 8, 'AwMDBwYODAwcGDgwMHBg4MDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_47', 'F18_S5x3', 47, 'F18_S5x3_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_48', 1, 18, 7, 'OPu+PHjx48ePHjx48fd8cA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_48', 'F18_S5x3', 48, 'F18_S5x3_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_49', 1, 18, 4, 'Ju5mZmZmZmb/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_49', 'F18_S5x3', 49, 'F18_S5x3_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_50', 1, 18, 8, 'PH7nw4MDAwMHDhw4cODAwP//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_50', 'F18_S5x3', 50, 'F18_S5x3_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_51', 1, 18, 8, 'PH7nwwMDBw48DgcDAwPD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_51', 'F18_S5x3', 51, 'F18_S5x3_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_52', 1, 18, 8, 'Bg4ePnbmxsb//wYGBgYGBgYG');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_52', 'F18_S5x3', 52, 'F18_S5x3_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_53', 1, 18, 8, '///AwMDAwPz+BwMDAwMDx/58');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_53', 'F18_S5x3', 53, 'F18_S5x3_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_54', 1, 18, 8, 'Pn/jwMDAwMD8/sfDw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_54', 'F18_S5x3', 54, 'F18_S5x3_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_55', 1, 18, 7, '//wYcMGHDBhwwYcMGHDBgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_55', 'F18_S5x3', 55, 'F18_S5x3_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_56', 1, 18, 7, 'OPu+PHj7vjj7vjx48fd8cA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_56', 'F18_S5x3', 56, 'F18_S5x3_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_57', 1, 18, 8, 'PH7nw8PDw+N/PwMDAwMDx/58');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_57', 'F18_S5x3', 57, 'F18_S5x3_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_58', 1, 18, 2, 'AAPA8AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_58', 'F18_S5x3', 58, 'F18_S5x3_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_59', 1, 18, 3, 'AAADYAfwAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_59', 'F18_S5x3', 59, 'F18_S5x3_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_60', 1, 18, 8, 'AAABAwcOHDhw4OBwOBwOBwMB');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_60', 'F18_S5x3', 60, 'F18_S5x3_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_61', 1, 18, 5, 'AAAAAB/4AA/8AAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_61', 'F18_S5x3', 61, 'F18_S5x3_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_62', 1, 18, 8, 'AACAwOBwOBwOBwcOHDhw4MCA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_62', 'F18_S5x3', 62, 'F18_S5x3_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_63', 1, 18, 8, 'PH7nwwMDAwcGDgwcGBgAABgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_63', 'F18_S5x3', 63, 'F18_S5x3_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_64', 1, 18, 10, 'Px/uHwPA8fz/M8zzPM8/x7AMA4B/j+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_64', 'F18_S5x3', 64, 'F18_S5x3_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_65', 1, 18, 8, 'PH7nw8PDw8P//8PDw8PDw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_65', 'F18_S5x3', 65, 'F18_S5x3_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_66', 1, 18, 8, '/P7Hw8PDx/7+x8PDw8PDx/78');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_66', 'F18_S5x3', 66, 'F18_S5x3_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_67', 1, 18, 8, 'Pn/jwMDAwMDAwMDAwMDA438+');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_67', 'F18_S5x3', 67, 'F18_S5x3_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_68', 1, 18, 8, '/P7Hw8PDw8PDw8PDw8PDx/78');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_68', 'F18_S5x3', 68, 'F18_S5x3_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_69', 1, 18, 8, '///AwMDAwPj4wMDAwMDAwP//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_69', 'F18_S5x3', 69, 'F18_S5x3_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_70', 1, 18, 8, '///AwMDAwPj4wMDAwMDAwMDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_70', 'F18_S5x3', 70, 'F18_S5x3_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_71', 1, 18, 8, 'PH7nw8DAwMDAx8fDw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_71', 'F18_S5x3', 71, 'F18_S5x3_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_72', 1, 18, 7, 'x48ePHjx4///Hjx48ePHjA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_72', 'F18_S5x3', 72, 'F18_S5x3_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_73', 1, 18, 4, '/2ZmZmZmZmb/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_73', 'F18_S5x3', 73, 'F18_S5x3_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_74', 1, 18, 6, 'DDDDDDDDDDDDDDDH+8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_74', 'F18_S5x3', 74, 'F18_S5x3_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_75', 1, 18, 8, 'w8fGzszc2Pjw+NjczM7Gx8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_75', 'F18_S5x3', 75, 'F18_S5x3_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_76', 1, 18, 7, 'wYMGDBgwYMGDBgwYMGD//A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_76', 'F18_S5x3', 76, 'F18_S5x3_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_77', 1, 18, 10, 'wPA+H4fz/P//e8zzPA8DwPA8DwPA8DA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_77', 'F18_S5x3', 77, 'F18_S5x3_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_78', 1, 18, 9, 'weD4fD8fj2ezzebx+Pw+HweDweDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_78', 'F18_S5x3', 78, 'F18_S5x3_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_79', 1, 18, 8, 'PH7nw8PDw8PDw8PDw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_79', 'F18_S5x3', 79, 'F18_S5x3_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_80', 1, 18, 8, '/P7Hw8PDxv78wMDAwMDAwMDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_80', 'F18_S5x3', 80, 'F18_S5x3_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_81', 1, 18, 8, 'PH7nw8PDw8PDw8PDw8vvfj8D');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_81', 'F18_S5x3', 81, 'F18_S5x3_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_82', 1, 18, 8, '/P7Hw8PDx/78zsfDw8PDw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_82', 'F18_S5x3', 82, 'F18_S5x3_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_83', 1, 18, 8, 'Pn/jwMDAwOB8PgcDAwMDx/58');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_83', 'F18_S5x3', 83, 'F18_S5x3_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_84', 1, 18, 8, '//8YGBgYGBgYGBgYGBgYGBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_84', 'F18_S5x3', 84, 'F18_S5x3_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_85', 1, 18, 8, 'w8PDw8PDw8PDw8PDw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_85', 'F18_S5x3', 85, 'F18_S5x3_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_86', 1, 18, 8, 'w8PDw8PnZmZmZn48PDwYGBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_86', 'F18_S5x3', 86, 'F18_S5x3_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_87', 1, 18, 10, 'wPA8DwPA8DwPA8DzPM973v//P89hmGA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_87', 'F18_S5x3', 87, 'F18_S5x3_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_88', 1, 18, 8, 'w8PD52ZmfjwYGDx+Zmbnw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_88', 'F18_S5x3', 88, 'F18_S5x3_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_89', 1, 18, 8, 'w8PD52Zmfjw8PBgYGBgYGBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_89', 'F18_S5x3', 89, 'F18_S5x3_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F18_S5x3_90', 1, 18, 8, '//8DAwcGDgwcGDgwcGDgwP//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F18_S5x3_90', 'F18_S5x3', 90, 'F18_S5x3_90');
