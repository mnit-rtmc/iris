\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('12_full', 7, 12, 0, 3, 2, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_32', 1, 12, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_32', '12_full', 32, '12_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_33', 1, 12, 2, 'qqqC');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_33', '12_full', 33, '12_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_34', 1, 12, 3, 'toAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_34', '12_full', 34, '12_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_35', 1, 12, 5, 'ABSvqV9SgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_35', '12_full', 35, '12_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_36', 1, 12, 5, 'I6lKOKUriAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_36', '12_full', 36, '12_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_37', 1, 12, 6, 'AwzCGMMYQzDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_37', '12_full', 37, '12_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_38', 1, 12, 6, 'IcUUcId1ni3d');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_38', '12_full', 38, '12_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_39', 1, 12, 1, '4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_39', '12_full', 39, '12_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_40', 1, 12, 3, 'LWkkyZA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_40', '12_full', 40, '12_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_41', 1, 12, 3, 'mTJJa0A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_41', '12_full', 41, '12_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_42', 1, 12, 5, 'AQlXEdUhAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_42', '12_full', 42, '12_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_43', 1, 12, 5, 'AQhCfIQhAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_43', '12_full', 43, '12_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_44', 1, 12, 3, 'AAAAC0A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_44', '12_full', 44, '12_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_45', 1, 12, 5, 'AAAAfAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_45', '12_full', 45, '12_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_46', 1, 12, 3, 'AAAAA2A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_46', '12_full', 46, '12_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_47', 1, 12, 5, 'CEYjEIxGIQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_47', '12_full', 47, '12_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_48', 1, 12, 5, 'duMYxjGMduA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_48', '12_full', 48, '12_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_49', 1, 12, 3, 'WSSSSXA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_49', '12_full', 49, '12_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_50', 1, 12, 6, 'ezhBDGMYwgg/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_50', '12_full', 50, '12_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_51', 1, 12, 6, 'ezBBDODBBBze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_51', '12_full', 51, '12_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_52', 1, 12, 6, 'GOayii/CCCCC');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_52', '12_full', 52, '12_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_53', 1, 12, 6, '/gggg+DBBBze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_53', '12_full', 53, '12_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_54', 1, 12, 6, 'ezggg+jhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_54', '12_full', 54, '12_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_55', 1, 12, 5, '+EIxCMQjEIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_55', '12_full', 55, '12_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_56', 1, 12, 6, 'ezhhzezhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_56', '12_full', 56, '12_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_57', 1, 12, 6, 'ezhhhxfBBBze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_57', '12_full', 57, '12_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_58', 1, 12, 2, 'AoKA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_58', '12_full', 58, '12_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_59', 1, 12, 3, 'ACQC0AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_59', '12_full', 59, '12_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_60', 1, 12, 5, 'AEZmYYYYQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_60', '12_full', 60, '12_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_61', 1, 12, 5, 'AAAPgB8AAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_61', '12_full', 61, '12_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_62', 1, 12, 5, 'BDDDDMzEAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_62', '12_full', 62, '12_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_63', 1, 12, 6, 'ezhBBDGMIIAI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_63', '12_full', 63, '12_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_64', 1, 12, 7, 'fY4M2nRo2Z8DA8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_64', '12_full', 64, '12_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_65', 1, 12, 6, 'Mezhhh/hhhhh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_65', '12_full', 65, '12_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_66', 1, 12, 6, '+jhhj+jhhhj+');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_66', '12_full', 66, '12_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_67', 1, 12, 6, 'ezggggggggze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_67', '12_full', 67, '12_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_68', 1, 12, 6, '+jhhhhhhhhj+');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_68', '12_full', 68, '12_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_69', 1, 12, 6, '/gggg8ggggg/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_69', '12_full', 69, '12_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_70', 1, 12, 6, '/gggg8gggggg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_70', '12_full', 70, '12_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_71', 1, 12, 6, 'ezggggnhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_71', '12_full', 71, '12_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_72', 1, 12, 5, 'jGMY/jGMYxA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_72', '12_full', 72, '12_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_73', 1, 12, 3, '6SSSSXA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_73', '12_full', 73, '12_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_74', 1, 12, 5, 'CEIQhCEMduA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_74', '12_full', 74, '12_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_75', 1, 12, 6, 'hjims44smijh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_75', '12_full', 75, '12_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_76', 1, 12, 5, 'hCEIQhCEIfA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_76', '12_full', 76, '12_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_77', 1, 12, 9, 'gOD49tnMRiMBgMBgMBA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_77', '12_full', 77, '12_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_78', 1, 12, 7, 'w4eNGzJmxY8OHBA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_78', '12_full', 78, '12_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_79', 1, 12, 6, 'ezhhhhhhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_79', '12_full', 79, '12_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_80', 1, 12, 6, '+jhhhj+ggggg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_80', '12_full', 80, '12_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_81', 1, 12, 6, 'ezhhhhhhhlyd');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_81', '12_full', 81, '12_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_82', 1, 12, 6, '+jhhhj+kmijh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_82', '12_full', 82, '12_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_83', 1, 12, 6, 'ezggweDBBBze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_83', '12_full', 83, '12_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_84', 1, 12, 5, '+QhCEIQhCEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_84', '12_full', 84, '12_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_85', 1, 12, 6, 'hhhhhhhhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_85', '12_full', 85, '12_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_86', 1, 12, 6, 'hhhhhzSSSeMM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_86', '12_full', 86, '12_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_87', 1, 12, 9, 'gMBgMBgMByaSXSqdxEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_87', '12_full', 87, '12_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_88', 1, 12, 6, 'hhzSeMMeSzhh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_88', '12_full', 88, '12_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_89', 1, 12, 7, 'gwcaJscECBAgQIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_89', '12_full', 89, '12_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_90', 1, 12, 6, '/BDCGMIYQwg/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_90', '12_full', 90, '12_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_91', 1, 12, 3, '8kkkknA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_91', '12_full', 91, '12_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_92', 1, 12, 5, 'hDCGEIYQwhA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_92', '12_full', 92, '12_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_93', 1, 12, 3, '5JJJJPA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_93', '12_full', 93, '12_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_94', 1, 12, 5, 'I7cQAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_94', '12_full', 94, '12_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_95', 1, 12, 5, 'AAAAAAAAAfA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_95', '12_full', 95, '12_full_95');
