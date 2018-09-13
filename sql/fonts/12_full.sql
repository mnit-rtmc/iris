\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('12_full', 7, 12, 0, 3, 2, 65535);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
12_full_32	12_full	32	2	AAAA
12_full_33	12_full	33	2	qqqC
12_full_34	12_full	34	3	toAAAAA=
12_full_35	12_full	35	5	ABSvqV9SgAA=
12_full_36	12_full	36	5	I6lKOKUriAA=
12_full_37	12_full	37	6	AwzCGMMYQzDA
12_full_38	12_full	38	6	IcUUcId1ni3d
12_full_39	12_full	39	1	4AA=
12_full_40	12_full	40	3	LWkkyZA=
12_full_41	12_full	41	3	mTJJa0A=
12_full_42	12_full	42	5	AQlXEdUhAAA=
12_full_43	12_full	43	5	AQhCfIQhAAA=
12_full_44	12_full	44	3	AAAAC0A=
12_full_45	12_full	45	5	AAAAfAAAAAA=
12_full_46	12_full	46	3	AAAAA2A=
12_full_47	12_full	47	5	CEYjEIxGIQA=
12_full_48	12_full	48	5	duMYxjGMduA=
12_full_49	12_full	49	3	WSSSSXA=
12_full_50	12_full	50	6	ezhBDGMYwgg/
12_full_51	12_full	51	6	ezBBDODBBBze
12_full_52	12_full	52	6	GOayii/CCCCC
12_full_53	12_full	53	6	/gggg+DBBBze
12_full_54	12_full	54	6	ezggg+jhhhze
12_full_55	12_full	55	5	+EIxCMQjEIA=
12_full_56	12_full	56	6	ezhhzezhhhze
12_full_57	12_full	57	6	ezhhhxfBBBze
12_full_58	12_full	58	2	AoKA
12_full_59	12_full	59	3	ACQC0AA=
12_full_60	12_full	60	5	AEZmYYYYQAA=
12_full_61	12_full	61	5	AAAPgB8AAAA=
12_full_62	12_full	62	5	BDDDDMzEAAA=
12_full_63	12_full	63	6	ezhBBDGMIIAI
12_full_64	12_full	64	7	fY4M2nRo2Z8DA8A=
12_full_65	12_full	65	6	Mezhhh/hhhhh
12_full_66	12_full	66	6	+jhhj+jhhhj+
12_full_67	12_full	67	6	ezggggggggze
12_full_68	12_full	68	6	+jhhhhhhhhj+
12_full_69	12_full	69	6	/gggg8ggggg/
12_full_70	12_full	70	6	/gggg8gggggg
12_full_71	12_full	71	6	ezggggnhhhze
12_full_72	12_full	72	5	jGMY/jGMYxA=
12_full_73	12_full	73	3	6SSSSXA=
12_full_74	12_full	74	5	CEIQhCEMduA=
12_full_75	12_full	75	6	hjims44smijh
12_full_76	12_full	76	5	hCEIQhCEIfA=
12_full_77	12_full	77	9	gOD49tnMRiMBgMBgMBA=
12_full_78	12_full	78	7	w4eNGzJmxY8OHBA=
12_full_79	12_full	79	6	ezhhhhhhhhze
12_full_80	12_full	80	6	+jhhhj+ggggg
12_full_81	12_full	81	6	ezhhhhhhhlyd
12_full_82	12_full	82	6	+jhhhj+kmijh
12_full_83	12_full	83	6	ezggweDBBBze
12_full_84	12_full	84	5	+QhCEIQhCEA=
12_full_85	12_full	85	6	hhhhhhhhhhze
12_full_86	12_full	86	6	hhhhhzSSSeMM
12_full_87	12_full	87	9	gMBgMBgMByaSXSqdxEA=
12_full_88	12_full	88	6	hhzSeMMeSzhh
12_full_89	12_full	89	7	gwcaJscECBAgQIA=
12_full_90	12_full	90	6	/BDCGMIYQwg/
12_full_91	12_full	91	3	8kkkknA=
12_full_92	12_full	92	5	hDCGEIYQwhA=
12_full_93	12_full	93	3	5JJJJPA=
12_full_94	12_full	94	5	I7cQAAAAAAA=
12_full_95	12_full	95	5	AAAAAAAAAfA=
\.

COMMIT;
