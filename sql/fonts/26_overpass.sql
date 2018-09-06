\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('26_overpass', 20, 26, 0, 9, 5, 1);

INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_32', 1, 26, 3, 'AAAAAAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_32', '26_overpass', 32, '26_overpass_32');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_33', 1, 26, 5, 'c5znOc5zmMYxjGEAADv/+4A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_33', '26_overpass', 33, '26_overpass_33');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_34', 1, 26, 9, '4/H4/H4/H47DYbDAAAAAAAAAAAAAAAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_34', '26_overpass', 34, '26_overpass_34');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_35', 1, 26, 21, 'AcHADg4AcHADg4A4HAHBwA4OB///P//7///A4OAHBwBwOAODgBwcAODg//////+///weDgDg8AcH
ADg4AcHADg4AcHAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_35', '26_overpass', 35, '26_overpass_35');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_36', 1, 26, 17, 'AcAA4AP8A/+D/+PA+cAY4AhwADwAD8AD/gD/wA/wAHwADwADmAH8AO8A98Dx//h/+B/wAcAA4AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_36', '26_overpass', 36, '26_overpass_36');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_37', 1, 26, 29, 'P8AHA/8AcBw4B4HA4DgOBwOAcDgcA4HBwBwOHADgcOADhw4AH/jgAH+HAABgcAAAB4OAADh/AAOH
/AA8OPABw4HAHBwOAODAMA4GAYDgOBwHAMDAcAcOB4Af4DgAfgA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_37', '26_overpass', 37, '26_overpass_37');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_38', 1, 26, 20, 'B/wA/+AOHgHA4BwPAcDwHg4A4eAPPAB/wAPwAD4AD+AB/wA8eHeHz3A+7wH+4A/uAHzwB88AfngP98P/P/5x/4M=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_38', '26_overpass', 38, '26_overpass_38');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_39', 1, 26, 3, '////bAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_39', '26_overpass', 39, '26_overpass_39');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_40', 1, 26, 7, 'Djhxw4ccOHDjhw4cOHBw4cODhw4OHBw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_40', '26_overpass', 40, '26_overpass_40');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_41', 1, 26, 7, '4OHBw4cHDhw4OHDhw4ccOHDjhw44ccA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_41', '26_overpass', 41, '26_overpass_41');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_42', 1, 26, 16, 'A8ADwAPAQYJ5nv//f/4BgAfgD/AOcB54PDwYGAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_42', '26_overpass', 42, '26_overpass_42');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_43', 1, 26, 19, 'AAAAAAAAAAAAAAAAA8AAeAAPAAHgADwAB4AA8A///f//v//wDwAB4AA8AAeAAPAAHgADwAAAAAAAAAAAAAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_43', '26_overpass', 43, '26_overpass_43');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_44', 1, 26, 3, 'AAAAAAAAAAA//A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_44', '26_overpass', 44, '26_overpass_44');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_45', 1, 26, 9, 'AAAAAAAAAAAAAAAAAAAH////AAAAAAAAAAAAAAAA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_45', '26_overpass', 45, '26_overpass_45');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_46', 1, 26, 3, 'AAAAAAAAAAD//A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_46', '26_overpass', 46, '26_overpass_46');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_47', 1, 26, 13, 'ADgBwBwA4AcAcAOAOAHADgDgBwBwA4A4AcAcAOAHAHADgDgBwA4A4AcAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_47', '26_overpass', 47, '26_overpass_47');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_48', 1, 26, 18, 'D/wH/4Ph8PA8eAecAOcAO8AP8AP4AH4AH4AH4AH4AH4AH4AH4AH8AP8APcAOcAOeAePA8Ph8H/4D
/wA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_48', '26_overpass', 48, '26_overpass_48');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_49', 1, 26, 8, 'Bg4+/v4ODg4ODg4ODg4ODg4ODg4ODg4ODg4=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_49', '26_overpass', 49, '26_overpass_49');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_50', 1, 26, 17, 'H/wf/x8HzgH/AHkAHAAOAAcAB4ADwAPAA8AH4AfAD8APgA+ADwAPAA8ABwAHgAPAAf///////8A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_50', '26_overpass', 50, '26_overpass_50');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_51', 1, 26, 18, 'D/wH/4Ph8eA8eAeEAeAAOAAeAAeAA8AB8AfwAfwAf8AA+AAeAAPAAPAAHAAHYAP8APeAefh+P/8H/wA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_51', '26_overpass', 51, '26_overpass_51');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_52', 1, 26, 18, 'AAeAA+AA+AB+AD+AH+AHeAOeAeeA8eA4eBweDweHgeHAeOAeeAe/////////AAeAAeAAeAAeAAeAAeA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_52', '26_overpass', 52, '26_overpass_52');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_53', 1, 26, 18, 'P/8P/8P/8OAAeAAcAAcAAcAAcAAc/Af/wf/4/h8eAeAAeAAPAAPAAPAAPAAPAAOAAeYA8+B8//4f/gA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_53', '26_overpass', 53, '26_overpass_53');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_54', 1, 26, 17, 'ADwAfgB+AHwAeAB4AHgAOAA8ABwAHn4P/8f/8/B94B7wB/AD+ADcAG4ANwAbwBzgHnweH/8H/gA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_54', '26_overpass', 54, '26_overpass_54');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_55', 1, 26, 15, '///////4ADAA4AHABwAeADgA8AHAB4AOABwAeADgAcAHgA8AHAA4AHAA4AHAA4AHAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_55', '26_overpass', 55, '26_overpass_55');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_56', 1, 26, 17, 'D/gP/w8PjwHnAPOAOcAc4A54Dx4PB/8B/wH/weDx4D3gD+AD8AH4APwAfgA/gD/gPPg+P/4P/gA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_56', '26_overpass', 56, '26_overpass_56');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_57', 1, 26, 17, 'D/wP/w+HzwD3AD+AH8APwAfwA/gB3ADvAPfh+f/8f+4PxwAHgAOAA8ABwAHgAeAD4AfgB+ADwAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_57', '26_overpass', 57, '26_overpass_57');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_58', 1, 26, 3, 'AAAAf/YAAAD//A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_58', '26_overpass', 58, '26_overpass_58');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_59', 1, 26, 3, 'AAAAf/YAAAD//A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_59', '26_overpass', 59, '26_overpass_59');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_60', 1, 26, 18, 'AAAAAAAAAAAAAAAAADAAPAB/AH+A/wD/AP4A/gA+AA/gAP8AD/AAf4AH+AA/AAPAABAAAAAAAAAA
AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_60', '26_overpass', 60, '26_overpass_60');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_61', 1, 26, 18, 'AAAAAAAAAAAAAAAAAAAAAAAA/////////AAAAAAAAAAAA/////////AAAAAAAAAAAAAAAAAAAAAA
AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_61', '26_overpass', 61, '26_overpass_61');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_62', 1, 26, 18, 'AAAAAAAAAAAAAAAwAA8AA/gAf4AD/AA/wAH+AB/AAPAB/AH8A/wD+Af4A/AA8AAwAAAAAAAAAAAA
AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_62', '26_overpass', 62, '26_overpass_62');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_63', 1, 26, 14, 'D/z/88HeA3AHwB4AQAEABAAwAcAPAHgDwB4AeAHABwAcAAAAAAcAPgD4A+APAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_63', '26_overpass', 63, '26_overpass_63');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_64', 1, 26, 25, 'AP/4Af/+AfAPgeAB4cAAeeBwHOD+xuD/43D78bjweHhwPDw4Dh4cBh8eAw+PA4fDgcPhweO48f+cP9+PD8eDgcAB4AAAeAAAHwAAB//AAP/gAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_64', '26_overpass', 64, '26_overpass_64');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_65', 1, 26, 20, 'AHAAB4AA+AAPwAD8AB/AAd4AHOADjgA48AeHAHBwBweA8DgOA4DgPB4Bwf/+H//j//44APOAB3gAdwAH8AA/AAM=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_65', '26_overpass', 65, '26_overpass_65');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_66', 1, 26, 18, '//g//4//84A84Ae4AO4AO4AO4AO4Ae4A8//4//w//84A+4AO4AP4AH4AH4AH4AH4AP4Ae//8//4/
/gA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_66', '26_overpass', 66, '26_overpass_66');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_67', 1, 26, 19, 'B/8B//B8Hx4B94Ac8AAcAAeAAOAAHAADgABwAA4AAcAAOAAHAADgAB4AA8AAOAAHgADwA88AePg+D/+A/+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_67', '26_overpass', 67, '26_overpass_67');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_68', 1, 26, 19, '/+Af/wP/+HAfjgD5wA84APcADuAB3AA/gAfwAH4AD8AB+AA/AA/gAfwAO4AHcAHuAHnAHzgPx//w
//gf/AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_68', '26_overpass', 68, '26_overpass_68');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_69', 1, 26, 16, '//7//v/+4ADgAOAA4ADgAOAA4ADgAP/g/+D/4OAA4ADgAOAA4ADgAOAA4ADgAP///////w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_69', '26_overpass', 69, '26_overpass_69');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_70', 1, 26, 15, '////////AA4AHAA4AHAA4AHAA4AH/w/+H/w4AHAA4AHAA4AHAA4AHAA4AHAA4AHAAA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_70', '26_overpass', 70, '26_overpass_70');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_71', 1, 26, 20, 'B/8A//wfB8PAHjgAx4AAcAAPAADgAA4AAOAADgAA4AAOAP/gD/4A/+AAfwAH8AB3AA94AOPAHj4B
4fh8D/+Af/A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_71', '26_overpass', 71, '26_overpass_71');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_72', 1, 26, 18, '4AH4AH4AH4AH4AH4AH4AH4AH4AH4AH4AH/////////4AH4AH4AH4AH4AH4AH4AH4AH4AH4AH4AH4
AHA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_72', '26_overpass', 72, '26_overpass_72');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_73', 1, 26, 3, '/////////////A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_73', '26_overpass', 73, '26_overpass_73');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_74', 1, 26, 16, 'AAcABwAHAAcABwAHAAcABwAHAAcABwAHAAcABwAHAAcABwAHAAcABwAPIA/wHng+f/w/+A==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_74', '26_overpass', 74, '26_overpass_74');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_75', 1, 26, 19, '4AccAeOAeHAeDgOBwOA4PAcPAOPAHHgDnwB38A/vAfngPh4Hg8DgPBwDg4B4cAcOAPHADzgB5wAe
4APcADw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_75', '26_overpass', 75, '26_overpass_75');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_76', 1, 26, 16, '4ADgAOAA4ADgAOAA4ADgAOAA4ADgAOAA4ADgAOAA4ADgAOAA4ADgAOAA4ADgAP///////w==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_76', '26_overpass', 76, '26_overpass_76');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_77', 1, 26, 22, 'wAAPgAB+AAH8AA/wAD/gAf+AB/8AH/wA//AD/uAd+4B35wPfnA5+eHn44cfjxx+HOH4c4fh/h+D8
H4PwfgeB+B4H4HAfgMBw');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_77', '26_overpass', 77, '26_overpass_77');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_78', 1, 26, 19, '4AD+AB/gA/wAf8AP+AH/gD94B+cA/PAfjwPx4H4eD8HB+Dw/A8fgOPwHn4B78Ad+AP/AD/gB/wAf
4AH8ADw=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_78', '26_overpass', 78, '26_overpass_78');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_79', 1, 26, 21, 'B/8Af/wHwfB4A8eADzgAOcAB/gAH4AA/AAH4AA/AAH4AA/AAH4AA/AAH4AA/AAH8AA7gAPcABzwA
ePAHg+D4D/+AP/gA');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_79', '26_overpass', 79, '26_overpass_79');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_80', 1, 26, 18, '//g//4//84A+4AP4AP4AH4AH4AH4AH4AP4AO4A+//8//4//g4AA4AA4AA4AA4AA4AA4AA4AA4AA4
AAA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_80', '26_overpass', 80, '26_overpass_80');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_81', 1, 26, 21, 'B/8Af/4Hwfh4A8eADzwAOcAB/gAH8AA/AAH4AA/AAH4AA/AAH4AA/AAH4AA/gAH8AA7gDPeA5zwH
+PAfg+D8D//AP/8A');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_81', '26_overpass', 81, '26_overpass_81');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_82', 1, 26, 18, '//g//8//+4Ae4AP4AH4AH4AH4AH4AH4AP4Ae//8//4//g4Hg4Hg4Dw4Dw4B44B44A84A84Ae4Ae4
APA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_82', '26_overpass', 82, '26_overpass_82');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_83', 1, 26, 17, 'D/wP/48HzwD3gCOAAcAA8AA8AB+AB/AB/wA/wAf4AHwAHwADgAHAAOAAMwA/gB3gHvwfP/8H/wA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_83', '26_overpass', 83, '26_overpass_83');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_84', 1, 26, 17, '////////4DwAHgAPAAeAA8AB4ADwAHgAPAAeAA8AB4ADwAHgAPAAeAA8AB4ADwAHgAPAAeAA8AA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_84', '26_overpass', 84, '26_overpass_84');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_85', 1, 26, 19, '4AD8AB+AA/AAfgAPwAH4AD8AB+AA/AAfgAPwAH4AD8AB+AA/AAfgAPwAH4AD8AB+AA/gA94A8+A+
P/+D/+A=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_85', '26_overpass', 85, '26_overpass_85');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_86', 1, 26, 20, '4AB/AAdwAPcADngA44AeOAHDwBwcA8HAOB4DgOB4DgcA8HAHDwBw4AeOADjgA5wAHcAB3AAfgAD4
AA+AAPAABwA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_86', '26_overpass', 86, '26_overpass_86');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_87', 1, 26, 27, '4A4A/gHAH8A8A7gPgHcB8B7gPgOeBuBzwdwOODuDxwc4eOHHDhw44cPHHDg4wc8HODnA5wc4HOB3A9gO4D8B/AfgPwD4A+AfAHwB4A+APADgBwAcAOADgA==');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_87', '26_overpass', 87, '26_overpass_87');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_88', 1, 26, 18, 'cAPeAOOAePAcHg8Hg4Dx4BzwB7gA/gA/AAfAAOAAfAA/AA/gB7wBzwDx4Dg4HA8PAcOAeeAPcAP8AHA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_88', '26_overpass', 88, '26_overpass_88');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_89', 1, 26, 20, '4AB/AA94AOeAHjwDwcA4HgeA4HAPDwBw4AeeADnAAfwAH4AA8AAPAADwAA8AAPAADwAA8AAPAADw
AA8AAPAADwA=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_89', '26_overpass', 89, '26_overpass_89');
INSERT INTO iris.graphic (name, color_scheme, height, width, pixels)
    VALUES ('26_overpass_90', 1, 26, 19, 'f//v//3//4AA8AAcAAeAAeAAeAAOAAPAAPAAPAAHAAHgAHgAHgADgADwADwADwABwAB4AB4AB/////////w=');
INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('26_overpass_90', '26_overpass', 90, '26_overpass_90');

COMMIT;
