\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('F16_S4x3', 12, 16, 0, 4, 3, 12345);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_32', 1, 16, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_32', 'F16_S4x3', 32, 'F16_S4x3_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_33', 1, 16, 2, '////Dw==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_33', 'F16_S4x3', 33, 'F16_S4x3_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_34', 1, 16, 6, 'zziAAAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_34', 'F16_S4x3', 34, 'F16_S4x3_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_35', 1, 16, 8, 'AAAAZmb//2Zm//9mZgAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_35', 'F16_S4x3', 35, 'F16_S4x3_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_36', 1, 16, 8, 'GBh+/tjY2P5/Gxsbf34YGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_36', 'F16_S4x3', 36, 'F16_S4x3_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_37', 1, 16, 8, 'AOOj5gYMDBgYMDBgZ8XHAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_37', 'F16_S4x3', 37, 'F16_S4x3_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_38', 1, 16, 8, 'OHzuxu58OHj8z8/Gxu9/OQ==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_38', 'F16_S4x3', 38, 'F16_S4x3_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_39', 1, 16, 2, '+AAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_39', 'F16_S4x3', 39, 'F16_S4x3_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_40', 1, 16, 4, 'N+zMzMzMznM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_40', 'F16_S4x3', 40, 'F16_S4x3_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_41', 1, 16, 4, 'znMzMzMzN+w=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_41', 'F16_S4x3', 41, 'F16_S4x3_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_42', 1, 16, 8, 'GBg8PGZmw8PDw2ZmPDwYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_42', 'F16_S4x3', 42, 'F16_S4x3_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_43', 1, 16, 6, 'AAAAMMM//MMMAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_43', 'F16_S4x3', 43, 'F16_S4x3_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_44', 1, 16, 3, 'AAAAAAD+');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_44', 'F16_S4x3', 44, 'F16_S4x3_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_45', 1, 16, 6, 'AAAAAAA//AAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_45', 'F16_S4x3', 45, 'F16_S4x3_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_46', 1, 16, 2, 'AAAADw==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_46', 'F16_S4x3', 46, 'F16_S4x3_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_47', 1, 16, 8, 'AAMDBgYMDBgYMDBgYMDAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_47', 'F16_S4x3', 47, 'F16_S4x3_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_48', 1, 16, 8, 'PH7nw8PDw8PDw8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_48', 'F16_S4x3', 48, 'F16_S4x3_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_49', 1, 16, 6, 'EMc8MMMMMMMMMM//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_49', 'F16_S4x3', 49, 'F16_S4x3_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_50', 1, 16, 8, 'PH7nwwMHDhw4cODAwMD//w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_50', 'F16_S4x3', 50, 'F16_S4x3_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_51', 1, 16, 8, 'PH7nwwMDBx4eBwMDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_51', 'F16_S4x3', 51, 'F16_S4x3_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_52', 1, 16, 8, 'Bg4ePnbmxsb//wYGBgYGBg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_52', 'F16_S4x3', 52, 'F16_S4x3_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_53', 1, 16, 8, '///AwMDA/P4HAwMDA8f+fA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_53', 'F16_S4x3', 53, 'F16_S4x3_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_54', 1, 16, 8, 'Pn/jwMDAwPz+x8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_54', 'F16_S4x3', 54, 'F16_S4x3_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_55', 1, 16, 8, '//8DAwYGDAwYGDAwYGDAwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_55', 'F16_S4x3', 55, 'F16_S4x3_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_56', 1, 16, 8, 'PH7nw8PD535+58PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_56', 'F16_S4x3', 56, 'F16_S4x3_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_57', 1, 16, 8, 'PH7nw8PD438/AwMDA8f+fA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_57', 'F16_S4x3', 57, 'F16_S4x3_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_58', 1, 16, 3, 'AAGwGwAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_58', 'F16_S4x3', 58, 'F16_S4x3_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_59', 1, 16, 3, 'AA2AC0AA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_59', 'F16_S4x3', 59, 'F16_S4x3_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_60', 1, 16, 6, 'AABDGMYwwYMGDBAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_60', 'F16_S4x3', 60, 'F16_S4x3_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_61', 1, 16, 6, 'AAAAA//AA//AAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_61', 'F16_S4x3', 61, 'F16_S4x3_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_62', 1, 16, 6, 'AAgwYMGDDGMYwgAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_62', 'F16_S4x3', 62, 'F16_S4x3_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_63', 1, 16, 8, 'PH7nwwMDBw4cGBgYAAAYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_63', 'F16_S4x3', 63, 'F16_S4x3_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_64', 1, 16, 10, 'f7/+HwPH8/zPM8zzPP8ewDgH8Pw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_64', 'F16_S4x3', 64, 'F16_S4x3_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_65', 1, 16, 8, 'GDw8ZmZmw8P//8PDw8PDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_65', 'F16_S4x3', 65, 'F16_S4x3_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_66', 1, 16, 8, '/P7Hw8PDxvz8xsPDw8f+/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_66', 'F16_S4x3', 66, 'F16_S4x3_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_67', 1, 16, 8, 'PH7nw8DAwMDAwMDAw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_67', 'F16_S4x3', 67, 'F16_S4x3_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_68', 1, 16, 8, '/P7Hw8PDw8PDw8PDw8f+/A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_68', 'F16_S4x3', 68, 'F16_S4x3_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_69', 1, 16, 8, '///AwMDAwPj4wMDAwMD//w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_69', 'F16_S4x3', 69, 'F16_S4x3_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_70', 1, 16, 8, '///AwMDAwPj4wMDAwMDAwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_70', 'F16_S4x3', 70, 'F16_S4x3_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_71', 1, 16, 8, 'PH7nw8DAwMDHx8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_71', 'F16_S4x3', 71, 'F16_S4x3_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_72', 1, 16, 8, 'w8PDw8PDw///w8PDw8PDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_72', 'F16_S4x3', 72, 'F16_S4x3_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_73', 1, 16, 4, '/2ZmZmZmZv8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_73', 'F16_S4x3', 73, 'F16_S4x3_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_74', 1, 16, 6, 'DDDDDDDDDDDDDH+8');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_74', 'F16_S4x3', 74, 'F16_S4x3_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_75', 1, 16, 8, 'w8PGxszM2Pj42MzMxsbDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_75', 'F16_S4x3', 75, 'F16_S4x3_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_76', 1, 16, 6, 'wwwwwwwwwwwwww//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_76', 'F16_S4x3', 76, 'F16_S4x3_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_77', 1, 16, 12, 'wD4H8P+f37zzxjxjwDwDwDwDwDwDwDwD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_77', 'F16_S4x3', 77, 'F16_S4x3_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_78', 1, 16, 8, 'w8Pj4/Pz29vPz8fHw8PDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_78', 'F16_S4x3', 78, 'F16_S4x3_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_79', 1, 16, 8, 'PH7nw8PDw8PDw8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_79', 'F16_S4x3', 79, 'F16_S4x3_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_80', 1, 16, 8, '/P7Hw8PDx/78wMDAwMDAwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_80', 'F16_S4x3', 80, 'F16_S4x3_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_81', 1, 16, 8, 'PH7nw8PDw8PDw8vPz+d/PQ==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_81', 'F16_S4x3', 81, 'F16_S4x3_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_82', 1, 16, 8, '/P7Hw8PDx/782MzMxsbDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_82', 'F16_S4x3', 82, 'F16_S4x3_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_83', 1, 16, 8, 'PH7nw8DA4Hw+BwMDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_83', 'F16_S4x3', 83, 'F16_S4x3_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_84', 1, 16, 8, '//8YGBgYGBgYGBgYGBgYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_84', 'F16_S4x3', 84, 'F16_S4x3_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_85', 1, 16, 8, 'w8PDw8PDw8PDw8PDw+d+PA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_85', 'F16_S4x3', 85, 'F16_S4x3_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_86', 1, 16, 8, 'w8PDw8PDw8NmZmZmPDwYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_86', 'F16_S4x3', 86, 'F16_S4x3_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_87', 1, 16, 12, 'wDwDwDwDwDwDwDwDxjxjzzzz37eeeeMM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_87', 'F16_S4x3', 87, 'F16_S4x3_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_88', 1, 16, 8, 'w8PDZmY8PBgYPDxmZsPDww==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_88', 'F16_S4x3', 88, 'F16_S4x3_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_89', 1, 16, 8, 'w8PDZmZmPDw8GBgYGBgYGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_89', 'F16_S4x3', 89, 'F16_S4x3_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_90', 1, 16, 8, '//8DBgYMDBgYMDBgYMD//w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_90', 'F16_S4x3', 90, 'F16_S4x3_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_91', 1, 16, 4, '/8zMzMzMzP8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_91', 'F16_S4x3', 91, 'F16_S4x3_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_92', 1, 16, 8, 'AMDAYGAwMBgYDAwGBgMDAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_92', 'F16_S4x3', 92, 'F16_S4x3_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F16_S4x3_93', 1, 16, 4, '/zMzMzMzM/8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F16_S4x3_93', 'F16_S4x3', 93, 'F16_S4x3_93');
