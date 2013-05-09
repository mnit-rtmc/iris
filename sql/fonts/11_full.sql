\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('11_full', 6, 11, 0, 3, 2, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_32', 1, 11, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_32', '11_full', 32, '11_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_33', 1, 11, 2, 'qqoI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_33', '11_full', 33, '11_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_34', 1, 11, 3, 'toAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_34', '11_full', 34, '11_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_35', 1, 11, 5, 'ApX1K+pQAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_35', '11_full', 35, '11_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_36', 1, 11, 5, 'I6lKOKUriA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_36', '11_full', 36, '11_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_37', 1, 11, 5, 'BjYjEYjYwA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_37', '11_full', 37, '11_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_38', 1, 11, 6, 'IUUUIYliilZA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_38', '11_full', 38, '11_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_39', 1, 11, 1, '4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_39', '11_full', 39, '11_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_40', 1, 11, 3, 'L0kkzIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_40', '11_full', 40, '11_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_41', 1, 11, 3, 'mZJJegA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_41', '11_full', 41, '11_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_42', 1, 11, 5, 'AAlXEdUgAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_42', '11_full', 42, '11_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_43', 1, 11, 5, 'AAhCfIQgAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_43', '11_full', 43, '11_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_44', 1, 11, 3, 'AAAAWgA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_44', '11_full', 44, '11_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_45', 1, 11, 5, 'AAAAfAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_45', '11_full', 45, '11_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_46', 1, 11, 2, 'AAAo');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_46', '11_full', 46, '11_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_47', 1, 11, 5, 'CEYjEYjEIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_47', '11_full', 47, '11_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_48', 1, 11, 5, 'duMYxjGO3A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_48', '11_full', 48, '11_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_49', 1, 11, 3, 'WSSSS4A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_49', '11_full', 49, '11_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_50', 1, 11, 6, 'ezhBDGcwgg/A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_50', '11_full', 50, '11_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_51', 1, 11, 6, 'ezBBDODBBzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_51', '11_full', 51, '11_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_52', 1, 11, 6, 'GOayii/CCCCA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_52', '11_full', 52, '11_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_53', 1, 11, 6, '/ggg+DBBBzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_53', '11_full', 53, '11_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_54', 1, 11, 6, 'ezggg+jhhzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_54', '11_full', 54, '11_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_55', 1, 11, 5, '+EIxGIxCEA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_55', '11_full', 55, '11_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_56', 1, 11, 6, 'ezhhzezhhzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_56', '11_full', 56, '11_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_57', 1, 11, 6, 'ezhhxfBBBzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_57', '11_full', 57, '11_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_58', 1, 11, 2, 'AooA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_58', '11_full', 58, '11_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_59', 1, 11, 3, 'ACQWgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_59', '11_full', 59, '11_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_60', 1, 11, 5, 'AEZmYYYYQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_60', '11_full', 60, '11_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_61', 1, 11, 5, 'AAAPg+AAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_61', '11_full', 61, '11_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_62', 1, 11, 5, 'BDDDDMzEAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_62', '11_full', 62, '11_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_63', 1, 11, 6, 'ezhBDGMIIAIA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_63', '11_full', 63, '11_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_64', 1, 11, 6, 'ezhlrppngweA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_64', '11_full', 64, '11_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_65', 1, 11, 6, 'Mezhh/hhhhhA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_65', '11_full', 65, '11_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_66', 1, 11, 6, '+jhhj+jhhj+A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_66', '11_full', 66, '11_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_67', 1, 11, 6, 'ezgggggggzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_67', '11_full', 67, '11_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_68', 1, 11, 6, '+jhhhhhhhj+A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_68', '11_full', 68, '11_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_69', 1, 11, 5, '/CEIchCEPg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_69', '11_full', 69, '11_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_70', 1, 11, 5, '/CEIchCEIA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_70', '11_full', 70, '11_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_71', 1, 11, 6, 'ezgggnhhhzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_71', '11_full', 71, '11_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_72', 1, 11, 5, 'jGMY/jGMYg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_72', '11_full', 72, '11_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_73', 1, 11, 3, '6SSSS4A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_73', '11_full', 73, '11_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_74', 1, 11, 5, 'CEIQhCGO3A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_74', '11_full', 74, '11_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_75', 1, 11, 6, 'hjms44smijhA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_75', '11_full', 75, '11_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_76', 1, 11, 5, 'hCEIQhCEPg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_76', '11_full', 76, '11_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_77', 1, 11, 7, 'g4+92TJgwYMGCA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_77', '11_full', 77, '11_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_78', 1, 11, 6, 'hxx5ptlnjjhA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_78', '11_full', 78, '11_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_79', 1, 11, 6, 'ezhhhhhhhzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_79', '11_full', 79, '11_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_80', 1, 11, 6, '+jhhj+gggggA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_80', '11_full', 80, '11_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_81', 1, 11, 6, 'ezhhhhhhlydA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_81', '11_full', 81, '11_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_82', 1, 11, 6, '+jhhj+kmijhA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_82', '11_full', 82, '11_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_83', 1, 11, 6, 'ezggweDBBzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_83', '11_full', 83, '11_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_84', 1, 11, 5, '+QhCEIQhCA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_84', '11_full', 84, '11_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_85', 1, 11, 6, 'hhhhhhhhhzeA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_85', '11_full', 85, '11_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_86', 1, 11, 6, 'hhhhzSSSeMMA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_86', '11_full', 86, '11_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_87', 1, 11, 7, 'gwYMGDJk3avdEA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_87', '11_full', 87, '11_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_88', 1, 11, 6, 'hhzSeMeSzhhA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_88', '11_full', 88, '11_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_89', 1, 11, 5, 'jGO1OIQhCA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_89', '11_full', 89, '11_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_90', 1, 11, 6, '/BDGEMIYwg/A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_90', '11_full', 90, '11_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_91', 1, 11, 3, '8kkkk4A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_91', '11_full', 91, '11_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_92', 1, 11, 5, 'hDCGEMIYQg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_92', '11_full', 92, '11_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_93', 1, 11, 3, '5JJJJ4A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_93', '11_full', 93, '11_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_94', 1, 11, 5, 'I7cQAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_94', '11_full', 94, '11_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('11_full_95', 1, 11, 5, 'AAAAAAAAPg==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('11_full_95', '11_full', 95, '11_full_95');
