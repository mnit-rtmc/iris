\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('14_full_thin', 12, 14, 0, 6, 3, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
14_full_thin_32	14_full_thin	32	5	AAAAAAAAAAAA
14_full_thin_33	14_full_thin	33	2	///w8A==
14_full_thin_34	14_full_thin	34	6	zzzzzzAAAAAAAAA=
14_full_thin_35	14_full_thin	35	8	ZmZm//9mZmZm//9mZmY=
14_full_thin_36	14_full_thin	36	8	GH7bmJjYfD4bGRnbfhg=
14_full_thin_37	14_full_thin	37	8	AAABY2YMGDBmxoAAAAA=
14_full_thin_38	14_full_thin	38	10	HA+GMYxjHYPA4HgzDG8Of4+w
14_full_thin_39	14_full_thin	39	4	b/ckAAAAAA==
14_full_thin_40	14_full_thin	40	6	DGMYwwwwwwYMGDA=
14_full_thin_41	14_full_thin	41	6	wYMGDDDDDDGMYwA=
14_full_thin_42	14_full_thin	42	8	QmY8//88ZkIAAAAAAAA=
14_full_thin_43	14_full_thin	43	8	AAAYGBgY//8YGBgYAAA=
14_full_thin_44	14_full_thin	44	4	AAAAAG/3JA==
14_full_thin_45	14_full_thin	45	8	AAAAAAAA//8AAAAAAAA=
14_full_thin_46	14_full_thin	46	4	AAAAAAb/YA==
14_full_thin_47	14_full_thin	47	8	AAABAwYMGDBgwIAAAAA=
14_full_thin_48	14_full_thin	48	8	PH7Dw8PHz9vz48PDfjw=
14_full_thin_49	14_full_thin	49	6	Mc8sMMMMMMMM//A=
14_full_thin_50	14_full_thin	50	8	PH7DwwMDBgwYMGDA//8=
14_full_thin_51	14_full_thin	51	8	PH7HAwMDHh4DAwPHfjw=
14_full_thin_52	14_full_thin	52	9	AQGBweGxmY2G///AwGAwGA==
14_full_thin_53	14_full_thin	53	8	///AwMD8/gcDAwPHfjw=
14_full_thin_54	14_full_thin	54	8	Hj5gwMDA3P7nw8Pnfjw=
14_full_thin_55	14_full_thin	55	8	//8DAwYGDAwYGDAwMDA=
14_full_thin_56	14_full_thin	56	8	PH7nw8NmPH7nw8Pnfjw=
14_full_thin_57	14_full_thin	57	8	PH7nw8Pnfz8DA4PHfjw=
14_full_thin_58	14_full_thin	58	4	AG/2AG/2AA==
14_full_thin_59	14_full_thin	59	4	AG/2AG/3JA==
14_full_thin_60	14_full_thin	60	7	AgwwwwwwMDAwMDAgAA==
14_full_thin_61	14_full_thin	61	7	AAAAD//AAP/8AAAAAA==
14_full_thin_62	14_full_thin	62	7	gYGBgYGBhhhhhggAAA==
14_full_thin_63	14_full_thin	63	8	PH7DgwMDBgwYGBgAGBg=
14_full_thin_64	14_full_thin	64	11	P8gGAMfZ+2NsbY2xt/Z3QAQCf4A=
14_full_thin_65	14_full_thin	65	8	GDxmw8PDw///w8PDw8M=
14_full_thin_66	14_full_thin	66	8	/P7Hw8PG/PzGw8PH/vw=
14_full_thin_67	14_full_thin	67	8	PH7nw8DAwMDAwMPnfjw=
14_full_thin_68	14_full_thin	68	8	/P7Hw8PDw8PDw8PH/vw=
14_full_thin_69	14_full_thin	69	7	//8GDBg/fsGDBg//wA==
14_full_thin_70	14_full_thin	70	7	//8GDBg/fsGDBgwYAA==
14_full_thin_71	14_full_thin	71	8	PH7nw8DAwM/Pw8Pjfjw=
14_full_thin_72	14_full_thin	72	8	w8PDw8PD///Dw8PDw8M=
14_full_thin_73	14_full_thin	73	6	//MMMMMMMMMM//A=
14_full_thin_74	14_full_thin	74	8	AwMDAwMDAwMDA8PHfjw=
14_full_thin_75	14_full_thin	75	8	wcPGzNjw4ODw2MzGw8E=
14_full_thin_76	14_full_thin	76	7	wYMGDBgwYMGDBg//wA==
14_full_thin_77	14_full_thin	77	10	wPh/P3vM8zzPA8DwPA8DwPAw
14_full_thin_78	14_full_thin	78	8	w8Pj4/PT28vPx8fDw8M=
14_full_thin_79	14_full_thin	79	8	PH7Dw8PDw8PDw8PDfjw=
14_full_thin_80	14_full_thin	80	8	/P7Dw8PD/vzAwMDAwMA=
14_full_thin_81	14_full_thin	81	9	Pj+weDweDweDwezzePex9A==
14_full_thin_82	14_full_thin	82	8	/P7Dw8PD/vzg8NjMxsM=
14_full_thin_83	14_full_thin	83	8	PH7DweBwPB4HA4PHfjw=
14_full_thin_84	14_full_thin	84	8	//8YGBgYGBgYGBgYGBg=
14_full_thin_85	14_full_thin	85	8	w8PDw8PDw8PDw8PDfjw=
14_full_thin_86	14_full_thin	86	9	weDweDwbGYzGYzGYxsHAQA==
14_full_thin_87	14_full_thin	87	10	wPA8DwPA8DwPM8zzPe/P4fAw
14_full_thin_88	14_full_thin	88	8	w8PDw8NmPDxmw8PDw8M=
14_full_thin_89	14_full_thin	89	8	w8PDw8PDZjwYGBgYGBg=
14_full_thin_90	14_full_thin	90	8	//8DAwMGDBgwYMDA//8=
14_full_thin_91	14_full_thin	91	8	GDx+25kYGBgYGBgYGBg=
14_full_thin_92	14_full_thin	92	8	AACAwGAwGAwGAwEAAAA=
14_full_thin_93	14_full_thin	93	8	GBgYGBgYGBgYmdt+PBg=
14_full_thin_94	14_full_thin	94	8	GDxmw4EAAAAAAAAAAAA=
14_full_thin_95	14_full_thin	95	7	AAAAAAAAAAAAAA//wA==
14_full_thin_96	14_full_thin	96	3	AAAAAAAA
14_full_thin_97	14_full_thin	97	8	AAAAAD9/48PDw8Pnfzs=
14_full_thin_98	14_full_thin	98	8	wMDAwPz+x8PDw8PH/vw=
14_full_thin_99	14_full_thin	99	8	AAAAAD5/48DAwMDjfz4=
14_full_thin_100	14_full_thin	100	8	AwMDA3//w8PDw8PD/3s=
14_full_thin_101	14_full_thin	101	8	AAAAADx+58P//sDBfz4=
14_full_thin_102	14_full_thin	102	7	HHzJgx+/GDBgwYMGAA==
14_full_thin_103	14_full_thin	103	8	AAAAAH//w8P/fwOD/34=
14_full_thin_104	14_full_thin	104	8	wMDAwN7/48PDw8PDw8M=
14_full_thin_105	14_full_thin	105	2	8P//8A==
14_full_thin_106	14_full_thin	106	7	BgwAAGDBgwYMHD/vgA==
14_full_thin_107	14_full_thin	107	8	wMDAwMLGzNjw8NjMxsM=
14_full_thin_108	14_full_thin	108	2	////8A==
14_full_thin_109	14_full_thin	109	10	AAAAAACzP/zPM8zzPM8zzPMw
14_full_thin_110	14_full_thin	110	8	AAAAANz+58PDw8PDw8M=
14_full_thin_111	14_full_thin	111	8	AAAAADx+58PDw8Pnfjw=
14_full_thin_112	14_full_thin	112	8	AAAAAP7/w8P//sDAwMA=
14_full_thin_113	14_full_thin	113	8	AAAAAH//w8P/fwMDAwM=
14_full_thin_114	14_full_thin	114	8	AAAAAL7/4cDAwMDAwMA=
14_full_thin_115	14_full_thin	115	7	AAAAA8/w4Pj4HD/PAA==
14_full_thin_116	14_full_thin	116	6	MMMM//MMMMMMPHA=
14_full_thin_117	14_full_thin	117	8	AAAAAMPDw8PDw8PH/3s=
14_full_thin_118	14_full_thin	118	8	AAAAAIGBw8NmZiQ8GBg=
14_full_thin_119	14_full_thin	119	10	AAAAAADA8DwPA8DzPM8z89hg
14_full_thin_120	14_full_thin	120	7	AAAADHjxtjhxtjx4wA==
14_full_thin_121	14_full_thin	121	8	AAAAAMPDw2Y8GBgYGBg=
14_full_thin_122	14_full_thin	122	8	AAAAAP//BgwYMGDA//8=
14_full_thin_123	14_full_thin	123	8	AAAAEDBg//9gMBAAAAA=
14_full_thin_124	14_full_thin	124	2	////8A==
14_full_thin_125	14_full_thin	125	8	AAAACAwG//8GDAgAAAA=
14_full_thin_126	14_full_thin	126	1	AAA=
\.

COMMIT;
