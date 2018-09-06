\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('26_nimbus', 21, 26, 0, 9, 5, 0);

INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_32', 1, 26, 2, 'AAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_32', '26_nimbus', 32, '26_nimbus_32');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_33', 1, 26, 3, '////+22222A//A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_33', '26_nimbus', 33, '26_nimbus_33');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_34', 1, 26, 6, 'zzzzzzzzzAAAAAAAAAAAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_34', '26_nimbus', 34, '26_nimbus_34');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_35', 1, 26, 15, 'AAAMYBjAMYDDAYYDDAYYDDD//f/wYwDGAwwGGAww//3/+GMAxgGMAhgMMBhgMMBhgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_35', '26_nimbus', 35, '26_nimbus_35');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_36', 1, 26, 14, 'AwAMAf4P/Ht7zO4x+MfjAcwH8A/wB/AN4DPAxwMMDDww+MNjHczz/4f8AwAMAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_36', '26_nimbus', 36, '26_nimbus_36');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_37', 1, 26, 24, 'AAAAHgBAPwDAYwDAYYGAwYGAwYMAwYMAwYIAwYYAYYQAYwwAfwg4Hhh+ABDGADDDACHDAGGDAGGD
AMGDAMGDAIHDAYDDAQDGAwB+AgA8');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_37', '26_nimbus', 37, '26_nimbus_37');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_38', 1, 26, 18, 'A8AD+AD/AHHAHDgGDgHDgHDAHHADuAB8AB4AD4APcMOeccOcYHc4H44D44B44B4cB4eH8P/eP+OD
4PA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_38', '26_nimbus', 38, '26_nimbus_38');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_39', 1, 26, 2, '///AAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_39', '26_nimbus', 39, '26_nimbus_39');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_40', 1, 26, 6, 'DGGMMMYYYYwwwwwwYYYYMMMGGDA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_40', '26_nimbus', 40, '26_nimbus_40');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_41', 1, 26, 6, 'wYYMMMGGGGDDDDDDGGGGMMMYYwA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_41', '26_nimbus', 41, '26_nimbus_41');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_42', 1, 26, 10, 'DAMAw7f/x4HgzGGYYAAAAAAAAAAAAAAAAAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_42', '26_nimbus', 42, '26_nimbus_42');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_43', 1, 26, 14, 'AAAAAAAAAAAADAAwAMADAAwAMADAAwP////AwAMADAAwAMADAAwAMAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_43', '26_nimbus', 43, '26_nimbus_43');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_44', 1, 26, 3, 'AAAAAAAAAAH/eA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_44', '26_nimbus', 44, '26_nimbus_44');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_45', 1, 26, 8, 'AAAAAAAAAAAAAAAAAP//AAAAAAAAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_45', '26_nimbus', 45, '26_nimbus_45');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_46', 1, 26, 3, 'AAAAAAAAAAA//A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_46', '26_nimbus', 46, '26_nimbus_46');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_47', 1, 26, 9, 'AYDAYGAwGBgMBgMDAYDAwGAwMBgMBgYDAYGAwGAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_47', '26_nimbus', 47, '26_nimbus_47');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_48', 1, 26, 15, 'AAAPgH+A/4OHjgccBzgOYB3AO4B/AH4A/AH4A/AH4B/AOYBzgOcBjgcOHh/4H+APgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_48', '26_nimbus', 48, '26_nimbus_48');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_49', 1, 26, 8, 'AAcHBw////8HBwcHBwcHBwcHBwcHBwcHBwc=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_49', '26_nimbus', 49, '26_nimbus_49');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_50', 1, 26, 14, 'AAAfAf8P/nh5wPYB2AfgH4BwAcAPADgDwB8B8A+AeAPAHgBwAYAOAD//////8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_50', '26_nimbus', 50, '26_nimbus_50');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_51', 1, 26, 14, 'AAA/Af8P/nh5wOYB2AdgHABgA4D8A+AP4AfABwAcAD4A+APgHcB3g4/+H/A/AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_51', '26_nimbus', 51, '26_nimbus_51');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_52', 1, 26, 14, 'AAABgA4AOAHgD4A2AdgGYDGBxgYYOGDBhwY4GMBj//////8AYAGABgAYAGABgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_52', '26_nimbus', 52, '26_nimbus_52');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_53', 1, 26, 14, 'AAD/4/+P/jAAwAMAHABwAY4G/h/8eHnA8AHABwAcAHAB+AfgH4DnB5/8P+A+AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_53', '26_nimbus', 53, '26_nimbus_53');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_54', 1, 26, 14, 'AAAfAP8H/jw44HcB3AdgAYAOfDv8//vg7wH8B+AfgD4A2AdwHcBzg4/8H+AfAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_54', '26_nimbus', 54, '26_nimbus_54');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_55', 1, 26, 14, 'AAP//////wAcAGADgAwAcAOADgBwAcAGADgAwAcAHABwA4AOADgA4AcAHABwAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_55', '26_nimbus', 55, '26_nimbus_55');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_56', 1, 26, 14, 'AAAfAf8P/Dh5wOcB3AdwHcDnh4/8D+D/x4ecB+AfgH4A+APgHcB3g4/+H/A/AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_56', '26_nimbus', 56, '26_nimbus_56');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_57', 1, 26, 14, 'AAAfAf4P/Hh5wOYB+AfgH4B+AfgHcD3h8/7H9w+cAHAB+AZgOcDnBw/4P+A+AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_57', '26_nimbus', 57, '26_nimbus_57');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_58', 1, 26, 3, 'AAAH/4AAAAH/eA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_58', '26_nimbus', 58, '26_nimbus_58');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_59', 1, 26, 3, 'AAAH/4AAAH/4AA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_59', '26_nimbus', 59, '26_nimbus_59');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_60', 1, 26, 15, 'AAAAAAAAAAAAAAAAAYAPAHwD4B8A+AfAHgA8AD4AHwAPgAfAA+AB4ADAAAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_60', '26_nimbus', 60, '26_nimbus_60');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_61', 1, 26, 13, 'AAAAAAAAAAAAAAAAAAAH//////AAAAAAAAD//////gAAAAAAAAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_61', '26_nimbus', 61, '26_nimbus_61');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_62', 1, 26, 15, 'AAAAAAAAAAAAAAAwAHgAfAA+AB8AD4AHwAPAB4A+AfAPgHwD4A8AGAAAAAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_62', '26_nimbus', 62, '26_nimbus_62');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_63', 1, 26, 13, 'D4H+H/jh7gdwGwD4BwAwA4A8AcAcAcAMAOAGADABgAAAAAAAGADABgAwAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_63', '26_nimbus', 63, '26_nimbus_63');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_64', 1, 26, 22, 'AH4AD/4AeDwDgDgcAHDgQOMH3Zx/5mHHjY4ONjA4+cDD5wMPmAxuYGGZgYZnDjGceYM//Az94BhA
MHABwOAOAfDwA/+AAfgA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_64', '26_nimbus', 64, '26_nimbus_64');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_65', 1, 26, 18, 'AeAAeAAeAA/AA/AA3ABzgBzgBjgDjgDhwDhwDBwHA4HA4H/4P/8P/8MAccAccAOcAO4AO4AH4AHw
AHA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_65', '26_nimbus', 65, '26_nimbus_65');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_66', 1, 26, 17, '//B//j//nAPOAOcAe4AdwA7gDnAHOAcf/w//x//zgD3ADuAHcAH4APwAfgB3ADuAPf/8//x/+AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_66', '26_nimbus', 66, '26_nimbus_66');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_67', 1, 26, 19, 'AfgA/8A//A+Hw8A8cAOeADuAB3AADgADgABwAA4AAcAAOAAHAABwAO4AHcADvADjgBx4BwfD4H/4
B/4APwA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_67', '26_nimbus', 67, '26_nimbus_67');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_68', 1, 26, 18, '//A//w//44B84A84Ae4AO4AO4AH4AH4AH4AH4AH4AH4AH4AH4AH4AG4AO4AO4Ae4A84B4//4//w/
/AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_68', '26_nimbus', 68, '26_nimbus_68');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_69', 1, 26, 15, '///////+AAwAGAAwAGAAwAGAAwAH/+//3/+wAGAAwAGAAwAGAAwAGAAwAH///////A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_69', '26_nimbus', 69, '26_nimbus_69');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_70', 1, 26, 14, '///////wAMADAAwAMADAAwAMAD/+//v/7AAwAMADAAwAMADAAwAMADAAwAMAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_70', '26_nimbus', 70, '26_nimbus_70');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_71', 1, 26, 20, 'APgAP/AP/4HwfB4Bw8AOOADnAA5wAAcAAHAABgAA4B/+Af/gH/cAB3AAdwAHcAB3gA84APHAHx8H
8P/zB/4wH4M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_71', '26_nimbus', 71, '26_nimbus_71');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_72', 1, 26, 17, '4APwAfgA/AB+AD8AH4APwAfgA/AB+AD/////////gA/AB+AD8AH4APwAfgA/AB+AD8AH4APwAcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_72', '26_nimbus', 72, '26_nimbus_72');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_73', 1, 26, 3, '/////////////A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_73', '26_nimbus', 73, '26_nimbus_73');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_74', 1, 26, 12, 'AHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHwHwHwHwH4O8ef8f8Hw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_74', '26_nimbus', 74, '26_nimbus_74');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_75', 1, 26, 17, '4AfwB7gHnAOOA8cDw4PBw8DjwHHAOeAd8A/8B+4D54HhwODwcDw4DhwHjgHHAPOAPcAe4AfwAcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_75', '26_nimbus', 75, '26_nimbus_75');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_76', 1, 26, 14, '4AOADgA4AOADgA4AOADgA4AOADgA4AOADgA4AOADgA4AOADgA4AOAD//////8A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_76', '26_nimbus', 76, '26_nimbus_76');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_77', 1, 26, 21, '8AD/wAf+AD/wA//AH/4A37AG/YB37gO/cBn5gM/MDn5wY/ODH4w4/HHH44w/DGH4Zw/DuH4dg/Bs
H4Pg/B4H4HA/A4HA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_77', '26_nimbus', 77, '26_nimbus_77');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_78', 1, 26, 17, '8AP4Af4A/wB/wD/gH7gP3AfnA/OB+OD8cH4cPw4fg4/B5+Bz8B34DvwD/gH/AH+AP8AP4AfwAcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_78', '26_nimbus', 78, '26_nimbus_78');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_79', 1, 26, 21, 'AfwAP/gD/+A+D4HAHhwAceADzgAOcABzgAH4AA/AAH4AA/AAH4AA/AAH8AA7gAOcABzwAePADh8A
8HwfAf/wB/8AD+AA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_79', '26_nimbus', 79, '26_nimbus_79');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_80', 1, 26, 15, '//H/8//2APwA+AHwA+AHwA+AHwA+AP//3/8//GAAwAGAAwAGAAwAGAAwAGAAwAGAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_80', '26_nimbus', 80, '26_nimbus_80');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_81', 1, 26, 21, 'AfwAP/gD/+A+D4HAPhwAceADzgAOcABzgAH4AA/AAH4AA/AAH4AA/AAH8AA7gAOcABzwGePA/h8D
8HwfAf/8B//wD+PA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_81', '26_nimbus', 81, '26_nimbus_81');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_82', 1, 26, 17, '//h//j//mAHsAHYAOwAdgA7AB2ADsAHYAc//x//j//mAHsAHYAOwAdgA7AB2ADsAHYAOwANgAcA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_82', '26_nimbus', 82, '26_nimbus_82');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_83', 1, 26, 17, 'A/AH/gf/h4PjgHOAOcAO4AdwADwAD4AH/AH/wB/wAPwADwAD8AH4AGwANwA7wBzwPD/+D/wB+AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_83', '26_nimbus', 83, '26_nimbus_83');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_84', 1, 26, 18, '/////////AOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAAOAA
OAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_84', '26_nimbus', 84, '26_nimbus_84');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_85', 1, 26, 17, '4APwAfgA/AB+AD8AH4APwAfgA/AB+AD8AH4APwAfgA/AB+AD8AH8AO4AdwBzwHj4eD/8D/gB+AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_85', '26_nimbus', 85, '26_nimbus_85');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_86', 1, 26, 18, '4AH4AP4AOcAOcAOcAeeAcOAcOAcOA4HA4HA4HAwDBwDhwDhwDjgBzgBzgBzAA3AA/AA+AAeAAeAA
eAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_86', '26_nimbus', 86, '26_nimbus_86');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_87', 1, 26, 26, '4B4B+AeAfgHgH4B4B+A/A9wPwOcDcDnAzA5wcwOcHODDhzhw4YYcOGGHDjhxwY4cYGMHOBzAzgdw
M4HcDsA2A7ANgHwD4B8A+AfAPgHgBwB4AcAOAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_87', '26_nimbus', 87, '26_nimbus_87');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_88', 1, 26, 18, '8APcAOeAcPAcHA4Hh4DhwDzwBzgB/AA/AA+AAeAAeAA/AA/ABzgDzgDhwHh4HA4PA8OAccAe8AO4
APA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_88', '26_nimbus', 88, '26_nimbus_88');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_89', 1, 26, 18, '4AH4APcAOcAeOAcOA4HA4HBwDhwDjgBzgB3AA/AA+AAeAAcAAcAAcAAcAAcAAcAAcAAcAAcAAcAA
cAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_89', '26_nimbus', 89, '26_nimbus_89');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_nimbus_90', 1, 26, 16, 'f/9//3//AA8ADgAeABwAOAB4AHAA4AHgAcADwAeABwAPAB4AHAA8AHgAcADwAP///////w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_nimbus_90', '26_nimbus', 90, '26_nimbus_90');

COMMIT;
