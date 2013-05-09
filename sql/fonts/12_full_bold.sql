\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('12_full_bold', 8, 12, 0, 4, 3, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_32', 1, 12, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_32', '12_full_bold', 32, '12_full_bold_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_33', 1, 12, 3, '2222w2A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_33', '12_full_bold', 33, '12_full_bold_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_34', 1, 12, 5, '3tIAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_34', '12_full_bold', 34, '12_full_bold_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_35', 1, 12, 8, 'AABm//9mZv//ZgAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_35', '12_full_bold', 35, '12_full_bold_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_36', 1, 12, 8, 'GH7/2Nj+fxsb/34Y');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_36', '12_full_bold', 36, '12_full_bold_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_37', 1, 12, 7, 'AYMYccMMOOGMGAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_37', '12_full_bold', 37, '12_full_bold_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_38', 1, 12, 7, 'cfNmxx42782b+9A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_38', '12_full_bold', 38, '12_full_bold_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_39', 1, 12, 2, '+AAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_39', '12_full_bold', 39, '12_full_bold_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_40', 1, 12, 4, 'NszMzMxj');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_40', '12_full_bold', 40, '12_full_bold_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_41', 1, 12, 4, 'xjMzMzNs');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_41', '12_full_bold', 41, '12_full_bold_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_42', 1, 12, 8, 'ABjbfjwYPH7bGAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_42', '12_full_bold', 42, '12_full_bold_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_43', 1, 12, 6, 'AAMMM//MMMAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_43', '12_full_bold', 43, '12_full_bold_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_44', 1, 12, 4, 'AAAAAAZs');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_44', '12_full_bold', 44, '12_full_bold_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_45', 1, 12, 5, 'AAAAf+AAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_45', '12_full_bold', 45, '12_full_bold_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_46', 1, 12, 3, 'AAAAA2A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_46', '12_full_bold', 46, '12_full_bold_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_47', 1, 12, 7, 'BgwwYYMMGGDDBgA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_47', '12_full_bold', 47, '12_full_bold_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_48', 1, 12, 7, 'ff8ePHjx48eP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_48', '12_full_bold', 48, '12_full_bold_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_49', 1, 12, 4, 'JuZmZmb/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_49', '12_full_bold', 49, '12_full_bold_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_50', 1, 12, 7, 'ff4YMGOOMMGD//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_50', '12_full_bold', 50, '12_full_bold_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_51', 1, 12, 7, 'ff4YMGOHAwcP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_51', '12_full_bold', 51, '12_full_bold_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_52', 1, 12, 7, 'HHn3bNm//wwYMGA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_52', '12_full_bold', 52, '12_full_bold_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_53', 1, 12, 7, '//8GDB+fgwcP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_53', '12_full_bold', 53, '12_full_bold_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_54', 1, 12, 7, 'ff8ODB+/48eP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_54', '12_full_bold', 54, '12_full_bold_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_55', 1, 12, 6, '//DDGGGMMMYY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_55', '12_full_bold', 55, '12_full_bold_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_56', 1, 12, 7, 'ff8ePG+fY8eP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_56', '12_full_bold', 56, '12_full_bold_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_57', 1, 12, 7, 'ff8ePH/fgwcP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_57', '12_full_bold', 57, '12_full_bold_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_58', 1, 12, 3, 'AGwGwAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_58', '12_full_bold', 58, '12_full_bold_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_59', 1, 12, 4, 'AAZgBugA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_59', '12_full_bold', 59, '12_full_bold_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_60', 1, 12, 6, 'ADGMYwwYMGDA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_60', '12_full_bold', 60, '12_full_bold_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_61', 1, 12, 5, 'AAH/gB/4AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_61', '12_full_bold', 61, '12_full_bold_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_62', 1, 12, 6, 'AwYMGDDGMYwA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_62', '12_full_bold', 62, '12_full_bold_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_63', 1, 12, 6, 'e/jDDHOMMAMM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_63', '12_full_bold', 63, '12_full_bold_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_64', 1, 12, 8, 'fv/Dz9/T09/OwP58');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_64', '12_full_bold', 64, '12_full_bold_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_65', 1, 12, 8, 'GDx+58PD///Dw8PD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_65', '12_full_bold', 65, '12_full_bold_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_66', 1, 12, 8, '/P7Hw8b8/MbDx/78');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_66', '12_full_bold', 66, '12_full_bold_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_67', 1, 12, 7, 'PP+eDBgwYMHN+eA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_67', '12_full_bold', 67, '12_full_bold_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_68', 1, 12, 7, '/f8ePHjx48eP/+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_68', '12_full_bold', 68, '12_full_bold_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_69', 1, 12, 7, '//8GDB8+YMGD//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_69', '12_full_bold', 69, '12_full_bold_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_70', 1, 12, 7, '//8GDB8+YMGDBgA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_70', '12_full_bold', 70, '12_full_bold_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_71', 1, 12, 7, 'ff8eDBnz48eP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_71', '12_full_bold', 71, '12_full_bold_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_72', 1, 12, 6, 'zzzzz//zzzzz');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_72', '12_full_bold', 72, '12_full_bold_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_73', 1, 12, 4, '/2ZmZmb/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_73', '12_full_bold', 73, '12_full_bold_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_74', 1, 12, 6, 'DDDDDDDDDz/e');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_74', '12_full_bold', 74, '12_full_bold_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_75', 1, 12, 7, 'x5s2zZ48bNmbNjA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_75', '12_full_bold', 75, '12_full_bold_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_76', 1, 12, 6, 'wwwwwwwwww//');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_76', '12_full_bold', 76, '12_full_bold_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_77', 1, 12, 10, 'wPh/P//e8zwPA8DwPA8D');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_77', '12_full_bold', 77, '12_full_bold_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_78', 1, 12, 8, 'w+Pj8/Pb28/Px8fD');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_78', '12_full_bold', 78, '12_full_bold_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_79', 1, 12, 7, 'ff8ePHjx48eP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_79', '12_full_bold', 79, '12_full_bold_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_80', 1, 12, 7, '/f8ePH//YMGDBgA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_80', '12_full_bold', 80, '12_full_bold_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_81', 1, 12, 8, 'fP7GxsbGxsbezv57');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_81', '12_full_bold', 81, '12_full_bold_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_82', 1, 12, 7, '/f8ePH//bM2bHjA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_82', '12_full_bold', 82, '12_full_bold_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_83', 1, 12, 7, 'ff8eDB+fgweP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_83', '12_full_bold', 83, '12_full_bold_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_84', 1, 12, 6, '//MMMMMMMMMM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_84', '12_full_bold', 84, '12_full_bold_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_85', 1, 12, 7, 'x48ePHjx48eP++A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_85', '12_full_bold', 85, '12_full_bold_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_86', 1, 12, 7, 'x48ePHjbNmxw4IA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_86', '12_full_bold', 86, '12_full_bold_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_87', 1, 12, 10, 'wPA8DwPA8DzPt3+f4zDM');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_87', '12_full_bold', 87, '12_full_bold_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_88', 1, 12, 7, 'x48bZscONm2PHjA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_88', '12_full_bold', 88, '12_full_bold_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_89', 1, 12, 8, 'w8PDZmY8PBgYGBgY');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_89', '12_full_bold', 89, '12_full_bold_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_90', 1, 12, 7, '//wYMMMMMMGD//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_90', '12_full_bold', 90, '12_full_bold_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_91', 1, 12, 4, '/8zMzMz/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_91', '12_full_bold', 91, '12_full_bold_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_92', 1, 12, 7, 'wYGDAwYGDAwYGDA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_92', '12_full_bold', 92, '12_full_bold_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_93', 1, 12, 4, '/zMzMzP/');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_93', '12_full_bold', 93, '12_full_bold_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_94', 1, 12, 6, 'MezhAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_94', '12_full_bold', 94, '12_full_bold_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('12_full_bold_95', 1, 12, 5, 'AAAAAAAAP/A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('12_full_bold_95', '12_full_bold', 95, '12_full_bold_95');
