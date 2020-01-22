\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('11_full', 6, 11, 0, 3, 2, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
11_full_32	11_full	32	1	AAA=
11_full_33	11_full	33	2	qqoI
11_full_34	11_full	34	3	toAAAAA=
11_full_35	11_full	35	5	ApX1K+pQAA==
11_full_36	11_full	36	5	I6lKOKUriA==
11_full_37	11_full	37	5	BjYjEYjYwA==
11_full_38	11_full	38	6	IUUUIYliilZA
11_full_39	11_full	39	1	4AA=
11_full_40	11_full	40	3	L0kkzIA=
11_full_41	11_full	41	3	mZJJegA=
11_full_42	11_full	42	5	AAlXEdUgAA==
11_full_43	11_full	43	5	AAhCfIQgAA==
11_full_44	11_full	44	3	AAAAWgA=
11_full_45	11_full	45	5	AAAAfAAAAA==
11_full_46	11_full	46	2	AAAo
11_full_47	11_full	47	5	CEYjEYjEIA==
11_full_48	11_full	48	5	duMYxjGO3A==
11_full_49	11_full	49	3	WSSSS4A=
11_full_50	11_full	50	6	ezhBDGcwgg/A
11_full_51	11_full	51	6	ezBBDODBBzeA
11_full_52	11_full	52	6	GOayii/CCCCA
11_full_53	11_full	53	6	/ggg+DBBBzeA
11_full_54	11_full	54	6	ezggg+jhhzeA
11_full_55	11_full	55	5	+EIxGIxCEA==
11_full_56	11_full	56	6	ezhhzezhhzeA
11_full_57	11_full	57	6	ezhhxfBBBzeA
11_full_58	11_full	58	2	AooA
11_full_59	11_full	59	3	ACQWgAA=
11_full_60	11_full	60	5	AEZmYYYYQA==
11_full_61	11_full	61	5	AAAPg+AAAA==
11_full_62	11_full	62	5	BDDDDMzEAA==
11_full_63	11_full	63	6	ezhBDGMIIAIA
11_full_64	11_full	64	6	ezhlrppngweA
11_full_65	11_full	65	6	Mezhh/hhhhhA
11_full_66	11_full	66	6	+jhhj+jhhj+A
11_full_67	11_full	67	6	ezgggggggzeA
11_full_68	11_full	68	6	+jhhhhhhhj+A
11_full_69	11_full	69	5	/CEIchCEPg==
11_full_70	11_full	70	5	/CEIchCEIA==
11_full_71	11_full	71	6	ezgggnhhhzeA
11_full_72	11_full	72	5	jGMY/jGMYg==
11_full_73	11_full	73	3	6SSSS4A=
11_full_74	11_full	74	5	CEIQhCGO3A==
11_full_75	11_full	75	6	hjms44smijhA
11_full_76	11_full	76	5	hCEIQhCEPg==
11_full_77	11_full	77	7	g4+92TJgwYMGCA==
11_full_78	11_full	78	6	hxx5ptlnjjhA
11_full_79	11_full	79	6	ezhhhhhhhzeA
11_full_80	11_full	80	6	+jhhj+gggggA
11_full_81	11_full	81	6	ezhhhhhhlydA
11_full_82	11_full	82	6	+jhhj+kmijhA
11_full_83	11_full	83	6	ezggweDBBzeA
11_full_84	11_full	84	5	+QhCEIQhCA==
11_full_85	11_full	85	6	hhhhhhhhhzeA
11_full_86	11_full	86	6	hhhhzSSSeMMA
11_full_87	11_full	87	7	gwYMGDJk3avdEA==
11_full_88	11_full	88	6	hhzSeMeSzhhA
11_full_89	11_full	89	5	jGO1OIQhCA==
11_full_90	11_full	90	6	/BDGEMIYwg/A
11_full_91	11_full	91	3	8kkkk4A=
11_full_92	11_full	92	5	hDCGEMIYQg==
11_full_93	11_full	93	3	5JJJJ4A=
11_full_94	11_full	94	5	I7cQAAAAAA==
11_full_95	11_full	95	5	AAAAAAAAPg==
\.

COMMIT;
