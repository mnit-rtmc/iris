\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('13_full', 9, 13, 0, 4, 3, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
13_full_32	13_full	32	1	AAA=
13_full_33	13_full	33	1	/8g=
13_full_34	13_full	34	3	toAAAAA=
13_full_35	13_full	35	6	AASS/SS/SSAAAA==
13_full_36	13_full	36	7	EPtciRofCxInW+EA
13_full_37	13_full	37	7	AYMIMMMEGGGCGDAA
13_full_38	13_full	38	7	MPEiR4YcbIseFneg
13_full_39	13_full	39	1	4AA=
13_full_40	13_full	40	4	E2yIiIxjEA==
13_full_41	13_full	41	4	jGMRERNsgA==
13_full_42	13_full	42	7	AABGt8cEHH2sQAAA
13_full_43	13_full	43	5	AABCE+QhAAAA
13_full_44	13_full	44	3	AAAAAWg=
13_full_45	13_full	45	5	AAAAA+AAAAAA
13_full_46	13_full	46	3	AAAAAGw=
13_full_47	13_full	47	6	BBDCGEMIYQwggA==
13_full_48	13_full	48	6	ezhhhhhhhhhzeA==
13_full_49	13_full	49	3	WSSSSS4=
13_full_50	13_full	50	7	fY4IEGGGGGGCBA/g
13_full_51	13_full	51	7	fYwIECDHAwIEDjfA
13_full_52	13_full	52	7	DDjTLFC/ggQIECBA
13_full_53	13_full	53	7	/wIECB+BgQIEDjfA
13_full_54	13_full	54	7	fY4MCBA/Q4MGDjfA
13_full_55	13_full	55	6	/BBBDCGEMIYQQA==
13_full_56	13_full	56	7	fY4MGDjfY4MGDjfA
13_full_57	13_full	57	7	fY4MGDhfgQIGDjfA
13_full_58	13_full	58	2	AKKAAA==
13_full_59	13_full	59	3	AASC0AA=
13_full_60	13_full	60	6	ABDGMYwYMGDBAA==
13_full_61	13_full	61	5	AAAAfB8AAAAA
13_full_62	13_full	62	6	AgwYMGDGMYwgAA==
13_full_63	13_full	63	7	fY4IECDDDBAgAAEA
13_full_64	13_full	64	7	fY4M23Ro0bM+BgeA
13_full_65	13_full	65	7	EHG2ODB/wYMGDBgg
13_full_66	13_full	66	7	/Q4MGDD/Q4MGDD/A
13_full_67	13_full	67	7	fY4MCBAgQIECDjfA
13_full_68	13_full	68	7	+RocGDBgwYMGHG+A
13_full_69	13_full	69	7	/wIECBA+QIECBA/g
13_full_70	13_full	70	7	/wIECBA+QIECBAgA
13_full_71	13_full	71	7	fY4MCBAjwYMGDjfA
13_full_72	13_full	72	6	hhhhhh/hhhhhhA==
13_full_73	13_full	73	3	6SSSSS4=
13_full_74	13_full	74	6	BBBBBBBBBBhzeA==
13_full_75	13_full	75	7	gw40yxwwcLEyNDgg
13_full_76	13_full	76	6	gggggggggggg/A==
13_full_77	13_full	77	9	gOD49tnMRiMBgMBgMBgI
13_full_78	13_full	78	8	wcHhobGRmYmNhYeDgw==
13_full_79	13_full	79	7	fY4MGDBgwYMGDjfA
13_full_80	13_full	80	7	/Q4MGDD/QIECBAgA
13_full_81	13_full	81	7	fY4MGDBgwYMmLieg
13_full_82	13_full	82	7	/Q4MGDD/TI0KHBgg
13_full_83	13_full	83	7	fY4MCBgfAwIGDjfA
13_full_84	13_full	84	7	/iBAgQIECBAgQIEA
13_full_85	13_full	85	7	gwYMGDBgwYMGDjfA
13_full_86	13_full	86	7	gwYMGDBxokTYocEA
13_full_87	13_full	87	9	gMBgMBgMBgOTSS6VTuIg
13_full_88	13_full	88	7	gwcaJscEHGyLHBgg
13_full_89	13_full	89	7	gwcaJsUOCBAgQIEA
13_full_90	13_full	90	8	/wEBAwYMGDBgwICA/w==
13_full_91	13_full	91	4	+IiIiIiI8A==
13_full_92	13_full	92	6	ggwQYIMEGCDBBA==
13_full_93	13_full	93	4	8RERERER8A==
13_full_94	13_full	94	6	MezhAAAAAAAAAA==
13_full_95	13_full	95	5	AAAAAAAAAA+A
\.

COMMIT;
