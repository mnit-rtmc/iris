\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('18_full', 14, 18, 0, 5, 3, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_32', 1, 18, 1, 'AAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_32', '18_full', 32, '18_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_33', 1, 18, 3, '2222222A2A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_33', '18_full', 33, '18_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_34', 1, 18, 6, 'zzRAAAAAAAAAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_34', '18_full', 34, '18_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_35', 1, 18, 8, 'AAAAZmb//2ZmZv//ZmYAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_35', '18_full', 35, '18_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_36', 1, 18, 8, 'GBh+/9vY2Pj+fx8bG9v/fhgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_36', '18_full', 36, '18_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_37', 1, 18, 8, 'AADgoeMHBgwcODBg4MeFBwAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_37', '18_full', 37, '18_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_38', 1, 18, 9, 'OD47mMzj4eBgeH7z+exmMx3ffZxA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_38', '18_full', 38, '18_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_39', 1, 18, 2, '+AAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_39', '18_full', 39, '18_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_40', 1, 18, 4, 'N27MzMzMzOZz');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_40', '18_full', 40, '18_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_41', 1, 18, 4, 'zmczMzMzM3bs');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_41', '18_full', 41, '18_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_42', 1, 18, 8, 'AAAYGJnb/348PH7/25kYGAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_42', '18_full', 42, '18_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_43', 1, 18, 6, 'AAAAAMMM//MMMAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_43', '18_full', 43, '18_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_44', 1, 18, 4, 'AAAAAAAAAAbs');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_44', '18_full', 44, '18_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_45', 1, 18, 6, 'AAAAAAAA//AAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_45', '18_full', 45, '18_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_46', 1, 18, 3, 'AAAAAAAA2A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_46', '18_full', 46, '18_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_47', 1, 18, 8, 'AwMHBgYODBwYGDgwcGBg4MDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_47', '18_full', 47, '18_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_48', 1, 18, 8, 'PH7nw8PDw8PDw8PDw8PD5348');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_48', '18_full', 48, '18_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_49', 1, 18, 6, 'Mc88MMMMMMMMMMMM//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_49', '18_full', 49, '18_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_50', 1, 18, 9, 'Pj+4+DAYDAYHBwcHBwcHAwGA///A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_50', '18_full', 50, '18_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_51', 1, 18, 9, 'fn+w4DAYDAYHDweA4DAYDAeH/z8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_51', '18_full', 51, '18_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_52', 1, 18, 9, 'BgcHh8dnMxmMxn//4MBgMBgMBgMA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_52', '18_full', 52, '18_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_53', 1, 18, 9, '///wGAwGAwH8fwHAYDAYDAeH/z8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_53', '18_full', 53, '18_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_54', 1, 18, 9, 'Pz/4eAwGAwH8/2HweDweDwfHfx8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_54', '18_full', 54, '18_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_55', 1, 18, 8, '//8DBwYGDgwMHBgYODAwcGBg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_55', '18_full', 55, '18_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_56', 1, 18, 9, 'Pj+4+DweD47+Pj+4+DweDwfHfx8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_56', '18_full', 56, '18_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_57', 1, 18, 9, 'Pj+4+DweDwfDf5/AYDAYDAeH/z8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_57', '18_full', 57, '18_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_58', 1, 18, 3, 'AAA2A2AAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_58', '18_full', 58, '18_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_59', 1, 18, 4, 'AAAAZgBugAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_59', '18_full', 59, '18_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_60', 1, 18, 8, 'AAEDBw4cOHDg4HA4HA4HAwEA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_60', '18_full', 60, '18_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_61', 1, 18, 6, 'AAAAAA//AA//AAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_61', '18_full', 61, '18_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_62', 1, 18, 8, 'AIDA4HA4HA4HBw4cOHDgwIAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_62', '18_full', 62, '18_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_63', 1, 18, 9, 'fn+w4DAYDAYHBwcHAwGAwAAAGAwA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_63', '18_full', 63, '18_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_64', 1, 18, 10, 'Px/uHwPA8fz/M8zzPM8/x7AMA4B/j8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_64', '18_full', 64, '18_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_65', 1, 18, 9, 'CA4Pju4+DweDwf//+DweDweDweDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_65', '18_full', 65, '18_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_66', 1, 18, 9, '/n+w+DweDw/+/2HweDweDweH/38A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_66', '18_full', 66, '18_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_67', 1, 18, 9, 'Pj+4+DwGAwGAwGAwGAwGAwfHfx8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_67', '18_full', 67, '18_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_68', 1, 18, 9, '/n+w+DweDweDweDweDweDweH/38A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_68', '18_full', 68, '18_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_69', 1, 18, 9, '///wGAwGAwH4/GAwGAwGAwGA///A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_69', '18_full', 69, '18_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_70', 1, 18, 9, '///wGAwGAwH4/GAwGAwGAwGAwGAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_70', '18_full', 70, '18_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_71', 1, 18, 9, 'Pj+4+DwGAwGAwGHw+DweDwfHfx8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_71', '18_full', 71, '18_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_72', 1, 18, 8, 'w8PDw8PDw8P//8PDw8PDw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_72', '18_full', 72, '18_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_73', 1, 18, 4, '/2ZmZmZmZmb/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_73', '18_full', 73, '18_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_74', 1, 18, 7, 'BgwYMGDBgwYMGDBg8fd8cA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_74', '18_full', 74, '18_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_75', 1, 18, 9, 'weDw+Gx2c3Hw8Hw3GcxmOw2HweDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_75', '18_full', 75, '18_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_76', 1, 18, 8, 'wMDAwMDAwMDAwMDAwMDAwP//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_76', '18_full', 76, '18_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_77', 1, 18, 12, 'wD4H8P8P+f37zzxjxjwDwDwDwDwDwDwDwDwD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_77', '18_full', 77, '18_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_78', 1, 18, 10, 'wPA+D4Pw/D2PY8zzPG8bw/D8HwfA8DA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_78', '18_full', 78, '18_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_79', 1, 18, 9, 'Pj+4+DweDweDweDweDweDwfHfx8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_79', '18_full', 79, '18_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_80', 1, 18, 9, '/n+w+DweDw/+/mAwGAwGAwGAwGAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_80', '18_full', 80, '18_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_81', 1, 18, 9, 'Pj+4+DweDweDweDweDwebz/Of57A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_81', '18_full', 81, '18_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_82', 1, 18, 9, '/n+w+DweDw/+/mYzGcxmOw2HweDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_82', '18_full', 82, '18_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_83', 1, 18, 9, 'Pz/4eAwGAwHAfh+A4DAYDAeH/z8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_83', '18_full', 83, '18_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_84', 1, 18, 8, '//8YGBgYGBgYGBgYGBgYGBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_84', '18_full', 84, '18_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_85', 1, 18, 9, 'weDweDweDweDweDweDweDwfHfx8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_85', '18_full', 85, '18_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_86', 1, 18, 9, 'weDweDweDweDwfHYzuNh8HA4CAQA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_86', '18_full', 86, '18_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_87', 1, 18, 12, 'wDwDwDwDwDwDwDwDwDxjxjzz73//f+eeMMMM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_87', '18_full', 87, '18_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_88', 1, 18, 9, 'weDwfHYzGdx8HA4PjuYzG4+DweDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_88', '18_full', 88, '18_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_89', 1, 18, 8, 'w8PD52Zmfjw8PBgYGBgYGBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_89', '18_full', 89, '18_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_90', 1, 18, 9, '///AYDA4GBwcHA4ODgYHAwGA///A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_90', '18_full', 90, '18_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_91', 1, 18, 5, '//GMYxjGMYxjGP/A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_91', '18_full', 91, '18_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_92', 1, 18, 8, 'wMDgYGBwMDgYGBwMDgYGBwMD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_92', '18_full', 92, '18_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_93', 1, 18, 5, '/8YxjGMYxjGMY//A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_93', '18_full', 93, '18_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_94', 1, 18, 7, 'EHH3fHBAAAAAAAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_94', '18_full', 94, '18_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('18_full_95', 1, 18, 7, 'AAAAAAAAAAAAAAAAAAD//A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('18_full_95', '18_full', 95, '18_full_95');
