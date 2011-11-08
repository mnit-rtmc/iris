\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('F12_S4x2', 5, 12, 0, 4, 2, 12345);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_32', 1, 12, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_32', 'F12_S4x2', 32, 'F12_S4x2_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_33', 1, 12, 3, 'SSSSQCA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_33', 'F12_S4x2', 33, 'F12_S4x2_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_34', 1, 12, 3, 'toAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_34', 'F12_S4x2', 34, 'F12_S4x2_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_35', 1, 12, 5, 'ABSvqV9SgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_35', 'F12_S4x2', 35, 'F12_S4x2_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_36', 1, 12, 5, 'AR9KMMUviAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_36', 'F12_S4x2', 36, 'F12_S4x2_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_37', 1, 12, 5, 'BnYjEYjE5gA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_37', 'F12_S4x2', 37, 'F12_S4x2_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_38', 1, 12, 6, 'AMSSWMd1ni3d');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_38', 'F12_S4x2', 38, 'F12_S4x2_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_39', 1, 12, 3, 'SQAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_39', 'F12_S4x2', 39, 'F12_S4x2_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_40', 1, 12, 3, 'LWkkyZA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_40', 'F12_S4x2', 40, 'F12_S4x2_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_41', 1, 12, 3, 'mTJJa0A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_41', 'F12_S4x2', 41, 'F12_S4x2_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_42', 1, 12, 7, 'EHCjZFjxomxQ4IA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_42', 'F12_S4x2', 42, 'F12_S4x2_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_43', 1, 12, 5, 'AAhCE+QhCAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_43', 'F12_S4x2', 43, 'F12_S4x2_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_44', 1, 12, 3, 'AAAAC0A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_44', 'F12_S4x2', 44, 'F12_S4x2_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_45', 1, 12, 4, 'AAAA8AAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_45', 'F12_S4x2', 45, 'F12_S4x2_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_46', 1, 12, 3, 'AAAAA2A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_46', 'F12_S4x2', 46, 'F12_S4x2_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_47', 1, 12, 5, 'AEIxGIxGIAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_47', 'F12_S4x2', 47, 'F12_S4x2_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_48', 1, 12, 5, 'duMYxjGMduA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_48', 'F12_S4x2', 48, 'F12_S4x2_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_49', 1, 12, 3, 'WSSSSXA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_49', 'F12_S4x2', 49, 'F12_S4x2_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_50', 1, 12, 6, 'ejBBDGMYwgg/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_50', 'F12_S4x2', 50, 'F12_S4x2_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_51', 1, 12, 6, 'ejBBDGDBBBje');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_51', 'F12_S4x2', 51, 'F12_S4x2_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_52', 1, 12, 6, 'GKSSii/CCCCC');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_52', 'F12_S4x2', 52, 'F12_S4x2_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_53', 1, 12, 6, '/gggg+DBBBje');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_53', 'F12_S4x2', 53, 'F12_S4x2_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_54', 1, 12, 6, 'exggg+jhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_54', 'F12_S4x2', 54, 'F12_S4x2_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_55', 1, 12, 5, '+EIxGIxGIQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_55', 'F12_S4x2', 55, 'F12_S4x2_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_56', 1, 12, 6, 'ezhhzezhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_56', 'F12_S4x2', 56, 'F12_S4x2_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_57', 1, 12, 6, 'ezhhhxfBBBje');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_57', 'F12_S4x2', 57, 'F12_S4x2_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_58', 1, 12, 2, 'AoKA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_58', 'F12_S4x2', 58, 'F12_S4x2_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_59', 1, 12, 3, 'ACQC0AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_59', 'F12_S4x2', 59, 'F12_S4x2_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_60', 1, 12, 5, 'AEZmYYYYQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_60', 'F12_S4x2', 60, 'F12_S4x2_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_61', 1, 12, 4, 'AADwDwAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_61', 'F12_S4x2', 61, 'F12_S4x2_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_62', 1, 12, 5, 'BDDDDMzEAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_62', 'F12_S4x2', 62, 'F12_S4x2_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_63', 1, 12, 6, 'ejBBBDGMIIAI');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_63', 'F12_S4x2', 63, 'F12_S4x2_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_64', 1, 12, 7, 'fY4MG7Vuy40DA8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_64', 'F12_S4x2', 64, 'F12_S4x2_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_65', 1, 12, 6, 'ezhhhh/hhhhh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_65', 'F12_S4x2', 65, 'F12_S4x2_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_66', 1, 12, 6, '+jhhj+jhhhj+');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_66', 'F12_S4x2', 66, 'F12_S4x2_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_67', 1, 12, 6, 'exggggggggxe');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_67', 'F12_S4x2', 67, 'F12_S4x2_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_68', 1, 12, 6, '8mjhhhhhhjm8');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_68', 'F12_S4x2', 68, 'F12_S4x2_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_69', 1, 12, 6, '/gggg8ggggg/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_69', 'F12_S4x2', 69, 'F12_S4x2_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_70', 1, 12, 6, '/gggg8gggggg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_70', 'F12_S4x2', 70, 'F12_S4x2_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_71', 1, 12, 6, 'exggggnhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_71', 'F12_S4x2', 71, 'F12_S4x2_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_72', 1, 12, 5, 'jGMY/jGMYxA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_72', 'F12_S4x2', 72, 'F12_S4x2_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_73', 1, 12, 1, '//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_73', 'F12_S4x2', 73, 'F12_S4x2_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_74', 1, 12, 4, 'ERERERE+');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_74', 'F12_S4x2', 74, 'F12_S4x2_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_75', 1, 12, 6, 'hhjms44smjhh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_75', 'F12_S4x2', 75, 'F12_S4x2_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_76', 1, 12, 5, 'hCEIQhCEIfA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_76', 'F12_S4x2', 76, 'F12_S4x2_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_77', 1, 12, 9, 'wfHtspnMRgMBgMBgMBA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_77', 'F12_S4x2', 77, 'F12_S4x2_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_78', 1, 12, 6, 'hxxpppllljjh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_78', 'F12_S4x2', 78, 'F12_S4x2_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_79', 1, 12, 6, 'ezhhhhhhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_79', 'F12_S4x2', 79, 'F12_S4x2_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_80', 1, 12, 6, '+jhhhj+ggggg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_80', 'F12_S4x2', 80, 'F12_S4x2_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_81', 1, 12, 6, 'ezhhhhhhlnzf');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_81', 'F12_S4x2', 81, 'F12_S4x2_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_82', 1, 12, 6, '+jhhhj+smjhh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_82', 'F12_S4x2', 82, 'F12_S4x2_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_83', 1, 12, 6, 'exggweDBBBje');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_83', 'F12_S4x2', 83, 'F12_S4x2_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_84', 1, 12, 5, '+QhCEIQhCEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_84', 'F12_S4x2', 84, 'F12_S4x2_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_85', 1, 12, 6, 'hhhhhhhhhhze');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_85', 'F12_S4x2', 85, 'F12_S4x2_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_86', 1, 12, 6, 'hhhhzSSSSeMM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_86', 'F12_S4x2', 86, 'F12_S4x2_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_87', 1, 12, 9, 'gMBgMBwaCSSSXTuNhEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_87', 'F12_S4x2', 87, 'F12_S4x2_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_88', 1, 12, 6, 'hhzSeMMeSzhh');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_88', 'F12_S4x2', 88, 'F12_S4x2_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_89', 1, 12, 7, 'gwcaJscECBAgQIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_89', 'F12_S4x2', 89, 'F12_S4x2_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('F12_S4x2_90', 1, 12, 6, '/BDCGMIYQwg/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('F12_S4x2_90', 'F12_S4x2', 90, 'F12_S4x2_90');
