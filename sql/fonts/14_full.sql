\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('14_full', 11, 14, 0, 4, 3, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_32', 1, 14, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_32', '14_full', 32, '14_full_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_33', 1, 14, 3, '22222A2A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_33', '14_full', 33, '14_full_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_34', 1, 14, 5, '3tIAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_34', '14_full', 34, '14_full_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_35', 1, 14, 8, 'AABmZv//Zmb//2ZmAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_35', '14_full', 35, '14_full_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_36', 1, 14, 8, 'GH7/2djY/n8bG5v/fhg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_36', '14_full', 36, '14_full_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_37', 1, 14, 8, 'AOCh4wcOHDhw4MeFBwA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_37', '14_full', 37, '14_full_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_38', 1, 14, 8, 'GDxmZmY8OHjNx8bG/3s=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_38', '14_full', 38, '14_full_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_39', 1, 14, 2, '+AAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_39', '14_full', 39, '14_full_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_40', 1, 14, 4, 'E2zMzMzGMQ==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_40', '14_full', 40, '14_full_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_41', 1, 14, 4, 'jGMzMzM2yA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_41', '14_full', 41, '14_full_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_42', 1, 14, 8, 'AAAYmdt+PDx+25kYAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_42', '14_full', 42, '14_full_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_43', 1, 14, 6, 'AAAMMM//MMMAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_43', '14_full', 43, '14_full_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_44', 1, 14, 4, 'AAAAAAAG6A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_44', '14_full', 44, '14_full_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_45', 1, 14, 5, 'AAAAA/8AAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_45', '14_full', 45, '14_full_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_46', 1, 14, 3, 'AAAAAA2A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_46', '14_full', 46, '14_full_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_47', 1, 14, 8, 'AwMHBg4MHDgwcGDgwMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_47', '14_full', 47, '14_full_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_48', 1, 14, 7, 'OPu+PHjx48ePH3fHAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_48', '14_full', 48, '14_full_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_49', 1, 14, 6, 'Mc8MMMMMMMMM//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_49', '14_full', 49, '14_full_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_50', 1, 14, 8, 'fP7HAwMHDhw4cODA//8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_50', '14_full', 50, '14_full_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_51', 1, 14, 8, 'fP7HAwMHHh4HAwPH/nw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_51', '14_full', 51, '14_full_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_52', 1, 14, 8, 'Bg4eNmbGxv//BgYGBgY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_52', '14_full', 52, '14_full_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_53', 1, 14, 8, '///AwMD8fgcDAwPH/nw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_53', '14_full', 53, '14_full_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_54', 1, 14, 8, 'Pn/jwMD8/sfDw8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_54', '14_full', 54, '14_full_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_55', 1, 14, 7, '//wYMGHDDhhww4YMAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_55', '14_full', 55, '14_full_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_56', 1, 14, 8, 'PH7nw8Pnfn7nw8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_56', '14_full', 56, '14_full_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_57', 1, 14, 8, 'PH7nw8Pjfz8DAwPH/nw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_57', '14_full', 57, '14_full_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_58', 1, 14, 3, 'AA2A2AAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_58', '14_full', 58, '14_full_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_59', 1, 14, 4, 'AABmAG6AAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_59', '14_full', 59, '14_full_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_60', 1, 14, 6, 'ABDGMYwwYMGDBAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_60', '14_full', 60, '14_full_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_61', 1, 14, 6, 'AAAA//AA//AAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_61', '14_full', 61, '14_full_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_62', 1, 14, 6, 'AgwYMGDDGMYwgAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_62', '14_full', 62, '14_full_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_63', 1, 14, 8, 'fP7HAwMHDhwYGAAAGBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_63', '14_full', 63, '14_full_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_64', 1, 14, 9, 'Pj+4+Dx+fyeTz+OwHAfx8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_64', '14_full', 64, '14_full_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_65', 1, 14, 8, 'GDx+58PDw///w8PDw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_65', '14_full', 65, '14_full_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_66', 1, 14, 8, '/P7Hw8PH/v7Hw8PH/vw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_66', '14_full', 66, '14_full_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_67', 1, 14, 8, 'PH7nw8DAwMDAwMPnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_67', '14_full', 67, '14_full_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_68', 1, 14, 8, '/P7Hw8PDw8PDw8PH/vw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_68', '14_full', 68, '14_full_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_69', 1, 14, 8, '///AwMDA+PjAwMDA//8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_69', '14_full', 69, '14_full_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_70', 1, 14, 8, '///AwMDA+PjAwMDAwMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_70', '14_full', 70, '14_full_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_71', 1, 14, 8, 'Pn/jwMDAwMfHw8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_71', '14_full', 71, '14_full_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_72', 1, 14, 7, 'x48ePHj//8ePHjx4wA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_72', '14_full', 72, '14_full_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_73', 1, 14, 4, '/2ZmZmZm/w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_73', '14_full', 73, '14_full_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_74', 1, 14, 7, 'BgwYMGDBgwYPH3fHAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_74', '14_full', 74, '14_full_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_75', 1, 14, 8, 'w8PHztz48PD43M7Hw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_75', '14_full', 75, '14_full_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_76', 1, 14, 7, 'wYMGDBgwYMGDBg//wA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_76', '14_full', 76, '14_full_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_77', 1, 14, 11, 'wHwfx/3995zxHgPAeA8B4DwHgMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_77', '14_full', 77, '14_full_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_78', 1, 14, 9, 'wfD4fj8ez2ebzePx+Hw+DA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_78', '14_full', 78, '14_full_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_79', 1, 14, 8, 'PH7nw8PDw8PDw8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_79', '14_full', 79, '14_full_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_80', 1, 14, 8, '/P7Hw8PH/vzAwMDAwMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_80', '14_full', 80, '14_full_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_81', 1, 14, 8, 'PH7nw8PDw8PD29/ufzs=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_81', '14_full', 81, '14_full_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_82', 1, 14, 8, '/P7Hw8PH/vzMzMbGw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_82', '14_full', 82, '14_full_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_83', 1, 14, 8, 'PH7nw8DgfD4HA8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_83', '14_full', 83, '14_full_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_84', 1, 14, 8, '//8YGBgYGBgYGBgYGBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_84', '14_full', 84, '14_full_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_85', 1, 14, 8, 'w8PDw8PDw8PDw8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_85', '14_full', 85, '14_full_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_86', 1, 14, 8, 'w8PDw8PDw8NmZjw8GBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_86', '14_full', 86, '14_full_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_87', 1, 14, 11, 'wHgPAeA8B4DwHiPu7dn/PeMYYwA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_87', '14_full', 87, '14_full_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_88', 1, 14, 8, 'w8PDZn48GBg8fmbDw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_88', '14_full', 88, '14_full_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_89', 1, 14, 8, 'w8PDZmY8PBgYGBgYGBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_89', '14_full', 89, '14_full_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_90', 1, 14, 8, '//8DAwcOHDhw4MDA//8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_90', '14_full', 90, '14_full_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_91', 1, 14, 4, '/8zMzMzM/w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_91', '14_full', 91, '14_full_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_92', 1, 14, 8, 'wMDgYHAwOBwMDgYHAwM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_92', '14_full', 92, '14_full_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_93', 1, 14, 4, '/zMzMzMz/w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_93', '14_full', 93, '14_full_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_94', 1, 14, 6, 'MezhAAAAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_94', '14_full', 94, '14_full_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('14_full_95', 1, 14, 6, 'AAAAAAAAAAAA//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('14_full_95', '14_full', 95, '14_full_95');
