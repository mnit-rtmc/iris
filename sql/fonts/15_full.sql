\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('15_full', 21, 15, 0, 6, 3, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
15_full_32	15_full	32	1	AAA=
15_full_33	15_full	33	2	////PA==
15_full_34	15_full	34	6	zzRAAAAAAAAAAAAA
15_full_35	15_full	35	10	AAADMMz///MwzDM///zMMwAAAA==
15_full_36	15_full	36	8	GH7/2djYfD4fGxub/34Y
15_full_37	15_full	37	10	AB4EhSN5gMBgMBgMBnsShIHgAA==
15_full_38	15_full	38	10	GA8GYZhmHwOA4Hw5jG8Owx/z7A==
15_full_39	15_full	39	2	+AAAAA==
15_full_40	15_full	40	4	NmbMzMzGZjA=
15_full_41	15_full	41	4	xmYzMzM2ZsA=
15_full_42	15_full	42	8	AAAY2/9+PBg8fv/bGAAA
15_full_43	15_full	43	8	AAAAABgYGP//GBgYAAAA
15_full_44	15_full	44	4	AAAAAAAAboA=
15_full_45	15_full	45	6	AAAAAAA//AAAAAAA
15_full_46	15_full	46	3	AAAAAAGw
15_full_47	15_full	47	8	AAMDBgYMDBgYMDBgYMDA
15_full_48	15_full	48	8	PH7nw8PDw8PDw8PD5348
15_full_49	15_full	49	5	M7xjGMYxjGM94A==
15_full_50	15_full	50	10	Hw/mDQMAwHB4eDgcBgOAwD///A==
15_full_51	15_full	51	9	fn+w4DAYHDweA4DAYDw/+fg=
15_full_52	15_full	52	9	AwODw2MzGw2G///AwGAwGAw=
15_full_53	15_full	53	9	///wGAwH8fwHAYDAYDw/+fg=
15_full_54	15_full	54	9	Pz/4eAwGA3n+4+DweD47+Pg=
15_full_55	15_full	55	8	//8DAwYGDAwYGBgwMDAw
15_full_56	15_full	56	8	PH7nw8NmPH7nw8PD5348
15_full_57	15_full	57	9	Pj/4+DwfDf5/AYDAYDw/+fg=
15_full_58	15_full	58	3	AAGwGwAA
15_full_59	15_full	59	4	AAAGYAboAAA=
15_full_60	15_full	60	7	AAQYYYYYYGBgYGBgQAA=
15_full_61	15_full	61	6	AAAAA//AA//AAAAA
15_full_62	15_full	62	7	AQMDAwMDAwwwwwwQAAA=
15_full_63	15_full	63	8	fP7HAwMDBw4cGBgAABgY
15_full_64	15_full	64	10	Px/uHwPH8/zPM8zz/HsA4B/j8A==
15_full_65	15_full	65	8	GDx+58PDw8P//8PDw8PD
15_full_66	15_full	66	8	/P7Hw8PG/P7Hw8PDx/78
15_full_67	15_full	67	8	PH7nw8DAwMDAwMDD5348
15_full_68	15_full	68	8	/P7Hw8PDw8PDw8PDx/78
15_full_69	15_full	69	8	///AwMDA/PzAwMDAwP//
15_full_70	15_full	70	8	///AwMDA+PjAwMDAwMDA
15_full_71	15_full	71	8	PH7nw8DAwMDHx8PD5348
15_full_72	15_full	72	8	w8PDw8PD///Dw8PDw8PD
15_full_73	15_full	73	4	/2ZmZmZmb/A=
15_full_74	15_full	74	7	BgwYMGDBgwYMHj7vjgA=
15_full_75	15_full	75	8	w8PHztz48PD43M7Gx8PD
15_full_76	15_full	76	7	wYMGDBgwYMGDBgwf/4A=
15_full_77	15_full	77	11	wHwfx/3995zxHgPAeA8B4DwHgPAY
15_full_78	15_full	78	9	weD4fD8ej2eTzeLx+Hw+DwY=
15_full_79	15_full	79	8	PH7nw8PDw8PDw8PD5348
15_full_80	15_full	80	8	/P7Hw8PH/vzAwMDAwMDA
15_full_81	15_full	81	8	PH7nw8PDw8PDw9vf7n87
15_full_82	15_full	82	8	/P7Hw8PH/vzMzMbGw8PD
15_full_83	15_full	83	8	PH7nw8DgfB4HAwPD5348
15_full_84	15_full	84	8	//8YGBgYGBgYGBgYGBgY
15_full_85	15_full	85	8	w8PDw8PDw8PDw8PD5348
15_full_86	15_full	86	8	w8PDw8PDw8NmZmY8PBgY
15_full_87	15_full	87	12	wDwDwDwDwDwDwDwDxjxj73f+eeeeMMA=
15_full_88	15_full	88	8	w8PDZmY8PBg8PGZmw8PD
15_full_89	15_full	89	8	w8PDZmZmPDwYGBgYGBgY
15_full_90	15_full	90	8	//8DAwYGDBgwYGDAwP//
15_full_91	15_full	91	5	//GMYxjGMYx/4A==
15_full_92	15_full	92	8	AMDAYGAwMBgYDAwGBgMD
15_full_93	15_full	93	5	/8YxjGMYxjH/4A==
15_full_94	15_full	94	8	GDxmw4EAAAAAAAAAAAAA
15_full_95	15_full	95	7	AAAAAAAAAAAAAAAf/4A=
\.

COMMIT;
