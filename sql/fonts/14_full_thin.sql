\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
	char_spacing, version_id) VALUES ('14_full_thin', 12, 14, 0, 6, 3, 0);

INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_32', 1, 14, 5, 'AAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_32', '14_full_thin', 32, '14_full_thin_32');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_33', 1, 14, 2, '///w8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_33', '14_full_thin', 33, '14_full_thin_33');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_34', 1, 14, 6, 'zzzzzzAAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_34', '14_full_thin', 34, '14_full_thin_34');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_35', 1, 14, 8, 'ZmZm//9mZmZm//9mZmY=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_35', '14_full_thin', 35, '14_full_thin_35');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_36', 1, 14, 8, 'GH7bmJjYfD4bGRnbfhg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_36', '14_full_thin', 36, '14_full_thin_36');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_37', 1, 14, 8, 'AAABY2YMGDBmxoAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_37', '14_full_thin', 37, '14_full_thin_37');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_38', 1, 14, 10, 'HA+GMYxjHYPA4HgzDG8Of4+w');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_38', '14_full_thin', 38, '14_full_thin_38');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_39', 1, 14, 4, 'b/ckAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_39', '14_full_thin', 39, '14_full_thin_39');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_40', 1, 14, 6, 'DGMYwwwwwwYMGDA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_40', '14_full_thin', 40, '14_full_thin_40');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_41', 1, 14, 6, 'wYMGDDDDDDGMYwA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_41', '14_full_thin', 41, '14_full_thin_41');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_42', 1, 14, 8, 'QmY8//88ZkIAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_42', '14_full_thin', 42, '14_full_thin_42');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_43', 1, 14, 8, 'AAAYGBgY//8YGBgYAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_43', '14_full_thin', 43, '14_full_thin_43');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_44', 1, 14, 4, 'AAAAAG/3JA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_44', '14_full_thin', 44, '14_full_thin_44');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_45', 1, 14, 8, 'AAAAAAAA//8AAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_45', '14_full_thin', 45, '14_full_thin_45');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_46', 1, 14, 4, 'AAAAAAb/YA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_46', '14_full_thin', 46, '14_full_thin_46');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_47', 1, 14, 8, 'AAABAwYMGDBgwIAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_47', '14_full_thin', 47, '14_full_thin_47');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_48', 1, 14, 8, 'PH7Dw8PHz9vz48PDfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_48', '14_full_thin', 48, '14_full_thin_48');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_49', 1, 14, 6, 'Mc8sMMMMMMMM//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_49', '14_full_thin', 49, '14_full_thin_49');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_50', 1, 14, 8, 'PH7DwwMDBgwYMGDA//8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_50', '14_full_thin', 50, '14_full_thin_50');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_51', 1, 14, 8, 'PH7HAwMDHh4DAwPHfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_51', '14_full_thin', 51, '14_full_thin_51');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_52', 1, 14, 9, 'AQGBweGxmY2G///AwGAwGA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_52', '14_full_thin', 52, '14_full_thin_52');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_53', 1, 14, 8, '///AwMD8/gcDAwPHfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_53', '14_full_thin', 53, '14_full_thin_53');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_54', 1, 14, 8, 'Hj5gwMDA3P7nw8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_54', '14_full_thin', 54, '14_full_thin_54');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_55', 1, 14, 8, '//8DAwYGDAwYGDAwMDA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_55', '14_full_thin', 55, '14_full_thin_55');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_56', 1, 14, 8, 'PH7nw8NmPH7nw8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_56', '14_full_thin', 56, '14_full_thin_56');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_57', 1, 14, 8, 'PH7nw8Pnfz8DA4PHfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_57', '14_full_thin', 57, '14_full_thin_57');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_58', 1, 14, 4, 'AG/2AG/2AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_58', '14_full_thin', 58, '14_full_thin_58');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_59', 1, 14, 4, 'AG/2AG/3JA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_59', '14_full_thin', 59, '14_full_thin_59');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_60', 1, 14, 7, 'AgwwwwwwMDAwMDAgAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_60', '14_full_thin', 60, '14_full_thin_60');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_61', 1, 14, 7, 'AAAAD//AAP/8AAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_61', '14_full_thin', 61, '14_full_thin_61');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_62', 1, 14, 7, 'gYGBgYGBhhhhhggAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_62', '14_full_thin', 62, '14_full_thin_62');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_63', 1, 14, 8, 'PH7DgwMDBgwYGBgAGBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_63', '14_full_thin', 63, '14_full_thin_63');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_64', 1, 14, 11, 'P8gGAMfZ+2NsbY2xt/Z3QAQCf4A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_64', '14_full_thin', 64, '14_full_thin_64');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_65', 1, 14, 8, 'GDxmw8PDw///w8PDw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_65', '14_full_thin', 65, '14_full_thin_65');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_66', 1, 14, 8, '/P7Hw8PG/PzGw8PH/vw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_66', '14_full_thin', 66, '14_full_thin_66');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_67', 1, 14, 8, 'PH7nw8DAwMDAwMPnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_67', '14_full_thin', 67, '14_full_thin_67');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_68', 1, 14, 8, '/P7Hw8PDw8PDw8PH/vw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_68', '14_full_thin', 68, '14_full_thin_68');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_69', 1, 14, 7, '//8GDBg/fsGDBg//wA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_69', '14_full_thin', 69, '14_full_thin_69');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_70', 1, 14, 7, '//8GDBg/fsGDBgwYAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_70', '14_full_thin', 70, '14_full_thin_70');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_71', 1, 14, 8, 'PH7nw8DAwM/Pw8Pjfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_71', '14_full_thin', 71, '14_full_thin_71');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_72', 1, 14, 8, 'w8PDw8PD///Dw8PDw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_72', '14_full_thin', 72, '14_full_thin_72');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_73', 1, 14, 6, '//MMMMMMMMMM//A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_73', '14_full_thin', 73, '14_full_thin_73');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_74', 1, 14, 8, 'AwMDAwMDAwMDA8PHfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_74', '14_full_thin', 74, '14_full_thin_74');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_75', 1, 14, 8, 'wcPGzNjw4ODw2MzGw8E=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_75', '14_full_thin', 75, '14_full_thin_75');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_76', 1, 14, 7, 'wYMGDBgwYMGDBg//wA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_76', '14_full_thin', 76, '14_full_thin_76');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_77', 1, 14, 10, 'wPh/P3vM8zzPA8DwPA8DwPAw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_77', '14_full_thin', 77, '14_full_thin_77');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_78', 1, 14, 8, 'w8Pj4/PT28vPx8fDw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_78', '14_full_thin', 78, '14_full_thin_78');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_79', 1, 14, 8, 'PH7Dw8PDw8PDw8PDfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_79', '14_full_thin', 79, '14_full_thin_79');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_80', 1, 14, 8, '/P7Dw8PD/vzAwMDAwMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_80', '14_full_thin', 80, '14_full_thin_80');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_81', 1, 14, 9, 'Pj+weDweDweDwezzePex9A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_81', '14_full_thin', 81, '14_full_thin_81');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_82', 1, 14, 8, '/P7Dw8PD/vzg8NjMxsM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_82', '14_full_thin', 82, '14_full_thin_82');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_83', 1, 14, 8, 'PH7DweBwPB4HA4PHfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_83', '14_full_thin', 83, '14_full_thin_83');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_84', 1, 14, 8, '//8YGBgYGBgYGBgYGBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_84', '14_full_thin', 84, '14_full_thin_84');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_85', 1, 14, 8, 'w8PDw8PDw8PDw8PDfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_85', '14_full_thin', 85, '14_full_thin_85');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_86', 1, 14, 9, 'weDweDwbGYzGYzGYxsHAQA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_86', '14_full_thin', 86, '14_full_thin_86');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_87', 1, 14, 10, 'wPA8DwPA8DwPM8zzPe/P4fAw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_87', '14_full_thin', 87, '14_full_thin_87');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_88', 1, 14, 8, 'w8PDw8NmPDxmw8PDw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_88', '14_full_thin', 88, '14_full_thin_88');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_89', 1, 14, 8, 'w8PDw8PDZjwYGBgYGBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_89', '14_full_thin', 89, '14_full_thin_89');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_90', 1, 14, 8, '//8DAwMGDBgwYMDA//8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_90', '14_full_thin', 90, '14_full_thin_90');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_91', 1, 14, 8, 'GDx+25kYGBgYGBgYGBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_91', '14_full_thin', 91, '14_full_thin_91');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_92', 1, 14, 8, 'AACAwGAwGAwGAwEAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_92', '14_full_thin', 92, '14_full_thin_92');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_93', 1, 14, 8, 'GBgYGBgYGBgYmdt+PBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_93', '14_full_thin', 93, '14_full_thin_93');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_94', 1, 14, 8, 'GDxmw4EAAAAAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_94', '14_full_thin', 94, '14_full_thin_94');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_95', 1, 14, 7, 'AAAAAAAAAAAAAA//wA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_95', '14_full_thin', 95, '14_full_thin_95');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_96', 1, 14, 3, 'AAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_96', '14_full_thin', 96, '14_full_thin_96');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_97', 1, 14, 8, 'AAAAAD9/48PDw8Pnfzs=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_97', '14_full_thin', 97, '14_full_thin_97');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_98', 1, 14, 8, 'wMDAwPz+x8PDw8PH/vw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_98', '14_full_thin', 98, '14_full_thin_98');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_99', 1, 14, 8, 'AAAAAD5/48DAwMDjfz4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_99', '14_full_thin', 99, '14_full_thin_99');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_100', 1, 14, 8, 'AwMDA3//w8PDw8PD/3s=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_100', '14_full_thin', 100, '14_full_thin_100');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_101', 1, 14, 8, 'AAAAADx+58P//sDBfz4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_101', '14_full_thin', 101, '14_full_thin_101');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_102', 1, 14, 7, 'HHzJgx+/GDBgwYMGAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_102', '14_full_thin', 102, '14_full_thin_102');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_103', 1, 14, 8, 'AAAAAH//w8P/fwOD/34=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_103', '14_full_thin', 103, '14_full_thin_103');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_104', 1, 14, 8, 'wMDAwN7/48PDw8PDw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_104', '14_full_thin', 104, '14_full_thin_104');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_105', 1, 14, 2, '8P//8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_105', '14_full_thin', 105, '14_full_thin_105');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_106', 1, 14, 7, 'BgwAAGDBgwYMHD/vgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_106', '14_full_thin', 106, '14_full_thin_106');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_107', 1, 14, 8, 'wMDAwMLGzNjw8NjMxsM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_107', '14_full_thin', 107, '14_full_thin_107');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_108', 1, 14, 2, '////8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_108', '14_full_thin', 108, '14_full_thin_108');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_109', 1, 14, 10, 'AAAAAACzP/zPM8zzPM8zzPMw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_109', '14_full_thin', 109, '14_full_thin_109');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_110', 1, 14, 8, 'AAAAANz+58PDw8PDw8M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_110', '14_full_thin', 110, '14_full_thin_110');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_111', 1, 14, 8, 'AAAAADx+58PDw8Pnfjw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_111', '14_full_thin', 111, '14_full_thin_111');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_112', 1, 14, 8, 'AAAAAP7/w8P//sDAwMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_112', '14_full_thin', 112, '14_full_thin_112');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_113', 1, 14, 8, 'AAAAAH//w8P/fwMDAwM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_113', '14_full_thin', 113, '14_full_thin_113');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_114', 1, 14, 8, 'AAAAAL7/4cDAwMDAwMA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_114', '14_full_thin', 114, '14_full_thin_114');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_115', 1, 14, 7, 'AAAAA8/w4Pj4HD/PAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_115', '14_full_thin', 115, '14_full_thin_115');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_116', 1, 14, 6, 'MMMM//MMMMMMPHA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_116', '14_full_thin', 116, '14_full_thin_116');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_117', 1, 14, 8, 'AAAAAMPDw8PDw8PH/3s=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_117', '14_full_thin', 117, '14_full_thin_117');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_118', 1, 14, 8, 'AAAAAIGBw8NmZiQ8GBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_118', '14_full_thin', 118, '14_full_thin_118');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_119', 1, 14, 10, 'AAAAAADA8DwPA8DzPM8z89hg');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_119', '14_full_thin', 119, '14_full_thin_119');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_120', 1, 14, 7, 'AAAADHjxtjhxtjx4wA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_120', '14_full_thin', 120, '14_full_thin_120');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_121', 1, 14, 8, 'AAAAAMPDw2Y8GBgYGBg=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_121', '14_full_thin', 121, '14_full_thin_121');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_122', 1, 14, 8, 'AAAAAP//BgwYMGDA//8=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_122', '14_full_thin', 122, '14_full_thin_122');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_123', 1, 14, 8, 'AAAAEDBg//9gMBAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_123', '14_full_thin', 123, '14_full_thin_123');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_124', 1, 14, 2, '////8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_124', '14_full_thin', 124, '14_full_thin_124');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_125', 1, 14, 8, 'AAAACAwG//8GDAgAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_125', '14_full_thin', 125, '14_full_thin_125');
INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('14_full_thin_126', 1, 14, 1, 'AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('14_full_thin_126', '14_full_thin', 126, '14_full_thin_126');
