\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('07_char', 1, 7, 5, 0, 0, 7314);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_32', 1, 7, 5, 'AAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_32', '07_char', 32, '07_char_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_33', 1, 7, 5, 'IQhCAIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_33', '07_char', 33, '07_char_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_34', 1, 7, 5, 'UoAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_34', '07_char', 34, '07_char_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_35', 1, 7, 5, 'Ur6vqUA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_35', '07_char', 35, '07_char_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_36', 1, 7, 5, 'I6jiuIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_36', '07_char', 36, '07_char_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_37', 1, 7, 5, 'xkRETGA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_37', '07_char', 37, '07_char_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_38', 1, 7, 5, 'RSiKyaA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_38', '07_char', 38, '07_char_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_39', 1, 7, 5, 'IQAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_39', '07_char', 39, '07_char_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_40', 1, 7, 5, 'ERCEEEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_40', '07_char', 40, '07_char_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_41', 1, 7, 5, 'QQQhEQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_41', '07_char', 41, '07_char_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_42', 1, 7, 5, 'JVxHVIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_42', '07_char', 42, '07_char_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_43', 1, 7, 5, 'AQnyEAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_43', '07_char', 43, '07_char_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_44', 1, 7, 5, 'AAAAEQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_44', '07_char', 44, '07_char_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_45', 1, 7, 5, 'AADgAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_45', '07_char', 45, '07_char_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_46', 1, 7, 5, 'AAAAAIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_46', '07_char', 46, '07_char_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_47', 1, 7, 5, 'AEREQAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_47', '07_char', 47, '07_char_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_48', 1, 7, 5, 'MlKUpMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_48', '07_char', 48, '07_char_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_49', 1, 7, 5, 'IwhCEcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_49', '07_char', 49, '07_char_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_50', 1, 7, 5, 'dEJkQ+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_50', '07_char', 50, '07_char_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_51', 1, 7, 5, 'dEJgxcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_51', '07_char', 51, '07_char_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_52', 1, 7, 5, 'EZUviEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_52', '07_char', 52, '07_char_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_53', 1, 7, 5, '/CDgxcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_53', '07_char', 53, '07_char_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_54', 1, 7, 5, 'dGHoxcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_54', '07_char', 54, '07_char_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_55', 1, 7, 5, '+EQiEQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_55', '07_char', 55, '07_char_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_56', 1, 7, 5, 'dGLoxcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_56', '07_char', 56, '07_char_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_57', 1, 7, 5, 'dGLwxcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_57', '07_char', 57, '07_char_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_58', 1, 7, 5, 'ABAEAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_58', '07_char', 58, '07_char_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_59', 1, 7, 5, 'AAgCIAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_59', '07_char', 59, '07_char_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_60', 1, 7, 5, 'EREEEEA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_60', '07_char', 60, '07_char_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_61', 1, 7, 5, 'AD4PgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_61', '07_char', 61, '07_char_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_62', 1, 7, 5, 'QQQREQA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_62', '07_char', 62, '07_char_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_63', 1, 7, 5, 'ZIRCAIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_63', '07_char', 63, '07_char_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_64', 1, 7, 5, 'dGdZwcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_64', '07_char', 64, '07_char_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_65', 1, 7, 5, 'dGP4xiA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_65', '07_char', 65, '07_char_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_66', 1, 7, 5, '9GPox8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_66', '07_char', 66, '07_char_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_67', 1, 7, 5, 'dGEIRcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_67', '07_char', 67, '07_char_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_68', 1, 7, 5, '9GMYx8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_68', '07_char', 68, '07_char_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_69', 1, 7, 5, '/CHoQ+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_69', '07_char', 69, '07_char_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_70', 1, 7, 5, '/CHoQgA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_70', '07_char', 70, '07_char_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_71', 1, 7, 5, 'dGF4xeA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_71', '07_char', 71, '07_char_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_72', 1, 7, 5, 'jGP4xiA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_72', '07_char', 72, '07_char_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_73', 1, 7, 5, 'cQhCEcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_73', '07_char', 73, '07_char_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_74', 1, 7, 5, 'EIQhSYA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_74', '07_char', 74, '07_char_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_75', 1, 7, 5, 'jKmKSiA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_75', '07_char', 75, '07_char_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_76', 1, 7, 5, 'hCEIQ+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_76', '07_char', 76, '07_char_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_77', 1, 7, 5, 'jusYxiA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_77', '07_char', 77, '07_char_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_78', 1, 7, 5, 'jnNZziA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_78', '07_char', 78, '07_char_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_79', 1, 7, 5, 'dGMYxcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_79', '07_char', 79, '07_char_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_80', 1, 7, 5, '9GPoQgA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_80', '07_char', 80, '07_char_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_81', 1, 7, 5, 'dGMayaA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_81', '07_char', 81, '07_char_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_82', 1, 7, 5, '9GPqSiA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_82', '07_char', 82, '07_char_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_83', 1, 7, 5, 'dGDgxcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_83', '07_char', 83, '07_char_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_84', 1, 7, 5, '+QhCEIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_84', '07_char', 84, '07_char_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_85', 1, 7, 5, 'jGMYxcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_85', '07_char', 85, '07_char_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_86', 1, 7, 5, 'jGMVKIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_86', '07_char', 86, '07_char_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_87', 1, 7, 5, 'jGMa1UA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_87', '07_char', 87, '07_char_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_88', 1, 7, 5, 'jFRFRiA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_88', '07_char', 88, '07_char_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_89', 1, 7, 5, 'jFRCEIA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_89', '07_char', 89, '07_char_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_90', 1, 7, 5, '+EREQ+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_90', '07_char', 90, '07_char_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_91', 1, 7, 5, 'chCEIcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_91', '07_char', 91, '07_char_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_92', 1, 7, 5, 'BBBBBAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_92', '07_char', 92, '07_char_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_93', 1, 7, 5, 'cIQhCcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_93', '07_char', 93, '07_char_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_94', 1, 7, 5, 'IqIAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_94', '07_char', 94, '07_char_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('07_char_95', 1, 7, 5, 'AAAAA+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('07_char_95', '07_char', 95, '07_char_95');
