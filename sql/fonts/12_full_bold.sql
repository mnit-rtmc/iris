\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('12_full_bold', 8, 12, 0, 4, 3, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
12_full_bold_32	12_full_bold	32	1	AAA=
12_full_bold_33	12_full_bold	33	3	2222w2A=
12_full_bold_34	12_full_bold	34	5	3tIAAAAAAAA=
12_full_bold_35	12_full_bold	35	8	AABm//9mZv//ZgAA
12_full_bold_36	12_full_bold	36	8	GH7/2Nj+fxsb/34Y
12_full_bold_37	12_full_bold	37	7	AYMYccMMOOGMGAA=
12_full_bold_38	12_full_bold	38	7	cfNmxx42782b+9A=
12_full_bold_39	12_full_bold	39	2	+AAA
12_full_bold_40	12_full_bold	40	4	NszMzMxj
12_full_bold_41	12_full_bold	41	4	xjMzMzNs
12_full_bold_42	12_full_bold	42	8	ABjbfjwYPH7bGAAA
12_full_bold_43	12_full_bold	43	6	AAMMM//MMMAA
12_full_bold_44	12_full_bold	44	4	AAAAAAZs
12_full_bold_45	12_full_bold	45	5	AAAAf+AAAAA=
12_full_bold_46	12_full_bold	46	3	AAAAA2A=
12_full_bold_47	12_full_bold	47	7	BgwwYYMMGGDDBgA=
12_full_bold_48	12_full_bold	48	7	ff8ePHjx48eP++A=
12_full_bold_49	12_full_bold	49	4	JuZmZmb/
12_full_bold_50	12_full_bold	50	7	ff4YMGOOMMGD//A=
12_full_bold_51	12_full_bold	51	7	ff4YMGOHAwcP++A=
12_full_bold_52	12_full_bold	52	7	HHn3bNm//wwYMGA=
12_full_bold_53	12_full_bold	53	7	//8GDB+fgwcP++A=
12_full_bold_54	12_full_bold	54	7	ff8ODB+/48eP++A=
12_full_bold_55	12_full_bold	55	6	//DDGGGMMMYY
12_full_bold_56	12_full_bold	56	7	ff8ePG+fY8eP++A=
12_full_bold_57	12_full_bold	57	7	ff8ePH/fgwcP++A=
12_full_bold_58	12_full_bold	58	3	AGwGwAA=
12_full_bold_59	12_full_bold	59	4	AAZgBugA
12_full_bold_60	12_full_bold	60	6	ADGMYwwYMGDA
12_full_bold_61	12_full_bold	61	5	AAH/gB/4AAA=
12_full_bold_62	12_full_bold	62	6	AwYMGDDGMYwA
12_full_bold_63	12_full_bold	63	6	e/jDDHOMMAMM
12_full_bold_64	12_full_bold	64	8	fv/Dz9/T09/OwP58
12_full_bold_65	12_full_bold	65	8	GDx+58PD///Dw8PD
12_full_bold_66	12_full_bold	66	8	/P7Hw8b8/MbDx/78
12_full_bold_67	12_full_bold	67	7	PP+eDBgwYMHN+eA=
12_full_bold_68	12_full_bold	68	7	/f8ePHjx48eP/+A=
12_full_bold_69	12_full_bold	69	7	//8GDB8+YMGD//A=
12_full_bold_70	12_full_bold	70	7	//8GDB8+YMGDBgA=
12_full_bold_71	12_full_bold	71	7	ff8eDBnz48eP++A=
12_full_bold_72	12_full_bold	72	6	zzzzz//zzzzz
12_full_bold_73	12_full_bold	73	4	/2ZmZmb/
12_full_bold_74	12_full_bold	74	6	DDDDDDDDDz/e
12_full_bold_75	12_full_bold	75	7	x5s2zZ48bNmbNjA=
12_full_bold_76	12_full_bold	76	6	wwwwwwwwww//
12_full_bold_77	12_full_bold	77	10	wPh/P//e8zwPA8DwPA8D
12_full_bold_78	12_full_bold	78	8	w+Pj8/Pb28/Px8fD
12_full_bold_79	12_full_bold	79	7	ff8ePHjx48eP++A=
12_full_bold_80	12_full_bold	80	7	/f8ePH//YMGDBgA=
12_full_bold_81	12_full_bold	81	8	fP7GxsbGxsbezv57
12_full_bold_82	12_full_bold	82	7	/f8ePH//bM2bHjA=
12_full_bold_83	12_full_bold	83	7	ff8eDB+fgweP++A=
12_full_bold_84	12_full_bold	84	6	//MMMMMMMMMM
12_full_bold_85	12_full_bold	85	7	x48ePHjx48eP++A=
12_full_bold_86	12_full_bold	86	7	x48ePHjbNmxw4IA=
12_full_bold_87	12_full_bold	87	10	wPA8DwPA8DzPt3+f4zDM
12_full_bold_88	12_full_bold	88	7	x48bZscONm2PHjA=
12_full_bold_89	12_full_bold	89	8	w8PDZmY8PBgYGBgY
12_full_bold_90	12_full_bold	90	7	//wYMMMMMMGD//A=
12_full_bold_91	12_full_bold	91	4	/8zMzMz/
12_full_bold_92	12_full_bold	92	7	wYGDAwYGDAwYGDA=
12_full_bold_93	12_full_bold	93	4	/zMzMzP/
12_full_bold_94	12_full_bold	94	6	MezhAAAAAAAA
12_full_bold_95	12_full_bold	95	5	AAAAAAAAP/A=
\.

COMMIT;
