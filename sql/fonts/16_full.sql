\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('16_full', 13, 16, 0, 4, 3, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_32', 1, 16, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_32', '16_full', 32, '16_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_33', 1, 16, 3, '222222A2');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_33', '16_full', 33, '16_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_34', 1, 16, 6, 'zzRAAAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_34', '16_full', 34, '16_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_35', 1, 16, 8, 'AAAAZmb//2Zm//9mZgAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_35', '16_full', 35, '16_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_36', 1, 16, 8, 'GBh+/9nY2P5/Gxub/34YGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_36', '16_full', 36, '16_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_37', 1, 16, 8, 'AOCh4wcGDBw4MGDgx4UHAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_37', '16_full', 37, '16_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_38', 1, 16, 8, 'OHzuxu58OHj8z8/Gxu9/OQ==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_38', '16_full', 38, '16_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_39', 1, 16, 2, '+AAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_39', '16_full', 39, '16_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_40', 1, 16, 4, 'N+zMzMzMznM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_40', '16_full', 40, '16_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_41', 1, 16, 4, 'znMzMzMzN+w=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_41', '16_full', 41, '16_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_42', 1, 16, 8, 'AAAY2/9+PBg8fv/bGAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_42', '16_full', 42, '16_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_43', 1, 16, 6, 'AAAAMMM//MMMAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_43', '16_full', 43, '16_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_44', 1, 16, 4, 'AAAAAAAABuw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_44', '16_full', 44, '16_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_45', 1, 16, 6, 'AAAAAAA//AAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_45', '16_full', 45, '16_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_46', 1, 16, 3, 'AAAAAAA2');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_46', '16_full', 46, '16_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_47', 1, 16, 8, 'AAMDBgYMDBgYMDBgYMDAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_47', '16_full', 47, '16_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_48', 1, 16, 8, 'PH7nw8PDw8PDw8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_48', '16_full', 48, '16_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_49', 1, 16, 6, 'Mc8MMMMMMMMMMM//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_49', '16_full', 49, '16_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_50', 1, 16, 8, 'PH7nwwMDBw4cOHDgwMD//w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_50', '16_full', 50, '16_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_51', 1, 16, 8, 'fP7HAwMDBx4eBwMDA8f+fA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_51', '16_full', 51, '16_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_52', 1, 16, 8, 'Bg4ePnbmxsb//wYGBgYGBg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_52', '16_full', 52, '16_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_53', 1, 16, 8, '///AwMDA/H4HAwMDA8f+fA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_53', '16_full', 53, '16_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_54', 1, 16, 8, 'Pn/jwMDA/P7Hw8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_54', '16_full', 54, '16_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_55', 1, 16, 8, '//8DAwMHBg4MHBg4MHBgYA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_55', '16_full', 55, '16_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_56', 1, 16, 8, 'PH7nw8PD535+58PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_56', '16_full', 56, '16_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_57', 1, 16, 8, 'PH7nw8PD438/AwMDA8f+fA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_57', '16_full', 57, '16_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_58', 1, 16, 3, 'AAGwGwAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_58', '16_full', 58, '16_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_59', 1, 16, 4, 'AAAGYAboAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_59', '16_full', 59, '16_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_60', 1, 16, 7, 'AAQYYYYYYMDAwMDAwIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_60', '16_full', 60, '16_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_61', 1, 16, 6, 'AAAAA//AA//AAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_61', '16_full', 61, '16_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_62', 1, 16, 7, 'AQMDAwMDAwYYYYYYIAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_62', '16_full', 62, '16_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_63', 1, 16, 8, 'fP7HAwMDBw4cGBgYAAAYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_63', '16_full', 63, '16_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_64', 1, 16, 10, 'Px/uHwPH8/zPM8zzPP8ewDgH+Pw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_64', '16_full', 64, '16_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_65', 1, 16, 8, 'GDx+58PDw8P//8PDw8PDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_65', '16_full', 65, '16_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_66', 1, 16, 8, '/P7Hw8PDx/7+x8PDw8f+/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_66', '16_full', 66, '16_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_67', 1, 16, 8, 'PH7nw8DAwMDAwMDAw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_67', '16_full', 67, '16_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_68', 1, 16, 8, '/P7Hw8PDw8PDw8PDw8f+/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_68', '16_full', 68, '16_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_69', 1, 16, 8, '///AwMDAwPj4wMDAwMD//w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_69', '16_full', 69, '16_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_70', 1, 16, 8, '///AwMDAwPj4wMDAwMDAwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_70', '16_full', 70, '16_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_71', 1, 16, 8, 'PH7nw8DAwMDHx8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_71', '16_full', 71, '16_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_72', 1, 16, 8, 'w8PDw8PDw///w8PDw8PDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_72', '16_full', 72, '16_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_73', 1, 16, 4, '/2ZmZmZmZv8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_73', '16_full', 73, '16_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_74', 1, 16, 7, 'BgwYMGDBgwYMGDx93xw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_74', '16_full', 74, '16_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_75', 1, 16, 8, 'w8PHxs7c+PD43MzOxsfDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_75', '16_full', 75, '16_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_76', 1, 16, 7, 'wYMGDBgwYMGDBgwYP/8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_76', '16_full', 76, '16_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_77', 1, 16, 12, 'wD4H8P+f37zzxjxjwDwDwDwDwDwDwDwD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_77', '16_full', 77, '16_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_78', 1, 16, 9, 'weD4fD8fj2ezzebx+Pw+HweD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_78', '16_full', 78, '16_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_79', 1, 16, 8, 'PH7nw8PDw8PDw8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_79', '16_full', 79, '16_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_80', 1, 16, 8, '/P7Hw8PDx/78wMDAwMDAwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_80', '16_full', 80, '16_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_81', 1, 16, 8, 'PH7nw8PDw8PDw8Pb3+5/Ow==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_81', '16_full', 81, '16_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_82', 1, 16, 8, '/P7Hw8PDx/78zMzGxsPDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_82', '16_full', 82, '16_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_83', 1, 16, 8, 'PH7nw8DA4Hw+BwMDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_83', '16_full', 83, '16_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_84', 1, 16, 8, '//8YGBgYGBgYGBgYGBgYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_84', '16_full', 84, '16_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_85', 1, 16, 8, 'w8PDw8PDw8PDw8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_85', '16_full', 85, '16_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_86', 1, 16, 8, 'w8PDw8PDw8PDZmZmPDwYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_86', '16_full', 86, '16_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_87', 1, 16, 12, 'wDwDwDwDwDwDwDwDxjxjzz73f+eeeeMM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_87', '16_full', 87, '16_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_88', 1, 16, 8, 'w8PDZmY8PBgYPDxmZsPDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_88', '16_full', 88, '16_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_89', 1, 16, 8, 'w8PDZmZmPDw8GBgYGBgYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_89', '16_full', 89, '16_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_90', 1, 16, 8, '//8DBgYMDBgYMDBgYMD//w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_90', '16_full', 90, '16_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_91', 1, 16, 4, '/8zMzMzMzP8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_91', '16_full', 91, '16_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_92', 1, 16, 8, 'AMDAYGAwMBgYDAwGBgMDAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_92', '16_full', 92, '16_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_93', 1, 16, 4, '/zMzMzMzM/8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_93', '16_full', 93, '16_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_94', 1, 16, 6, 'MezhAAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_94', '16_full', 94, '16_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('16_full_95', 1, 16, 6, 'AAAAAAAAAAAAAA//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('16_full_95', '16_full', 95, '16_full_95');
