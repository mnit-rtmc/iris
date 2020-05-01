\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('18_full', 14, 18, 0, 5, 3, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
18_full_32	18_full	32	1	AAAA
18_full_33	18_full	33	3	2222222A2A==
18_full_34	18_full	34	6	zzRAAAAAAAAAAAAAAAA=
18_full_35	18_full	35	8	AAAAZmb//2ZmZv//ZmYAAAAA
18_full_36	18_full	36	8	GBh+/9vY2Pj+fx8bG9v/fhgY
18_full_37	18_full	37	8	AADgoeMHBgwcODBg4MeFBwAA
18_full_38	18_full	38	9	OD47mMzj4eBgeH7z+exmMx3ffZxA
18_full_39	18_full	39	2	+AAAAAA=
18_full_40	18_full	40	4	N27MzMzMzOZz
18_full_41	18_full	41	4	zmczMzMzM3bs
18_full_42	18_full	42	8	AAAYGJnb/348PH7/25kYGAAA
18_full_43	18_full	43	6	AAAAAMMM//MMMAAAAAA=
18_full_44	18_full	44	4	AAAAAAAAAAbs
18_full_45	18_full	45	6	AAAAAAAA//AAAAAAAAA=
18_full_46	18_full	46	3	AAAAAAAA2A==
18_full_47	18_full	47	8	AwMHBgYODBwYGDgwcGBg4MDA
18_full_48	18_full	48	8	PH7nw8PDw8PDw8PDw8PD5348
18_full_49	18_full	49	6	Mc88MMMMMMMMMMMM//A=
18_full_50	18_full	50	9	Pj+4+DAYDAYHBwcHBwcHAwGA///A
18_full_51	18_full	51	9	fn+w4DAYDAYHDweA4DAYDAeH/z8A
18_full_52	18_full	52	9	BgcHh8dnMxmMxn//4MBgMBgMBgMA
18_full_53	18_full	53	9	///wGAwGAwH8fwHAYDAYDAeH/z8A
18_full_54	18_full	54	9	Pz/4eAwGAwH8/2HweDweDwfHfx8A
18_full_55	18_full	55	8	//8DBwYGDgwMHBgYODAwcGBg
18_full_56	18_full	56	9	Pj+4+DweD47+Pj+4+DweDwfHfx8A
18_full_57	18_full	57	9	Pj+4+DweDwfDf5/AYDAYDAeH/z8A
18_full_58	18_full	58	3	AAA2A2AAAA==
18_full_59	18_full	59	4	AAAAZgBugAAA
18_full_60	18_full	60	8	AAEDBw4cOHDg4HA4HA4HAwEA
18_full_61	18_full	61	6	AAAAAA//AA//AAAAAAA=
18_full_62	18_full	62	8	AIDA4HA4HA4HBw4cOHDgwIAA
18_full_63	18_full	63	9	fn+w4DAYDAYHBwcHAwGAwAAAGAwA
18_full_64	18_full	64	10	Px/uHwPA8fz/M8zzPM8/x7AMA4B/j8A=
18_full_65	18_full	65	9	CA4Pju4+DweDwf//+DweDweDweDA
18_full_66	18_full	66	9	/n+w+DweDw/+/2HweDweDweH/38A
18_full_67	18_full	67	9	Pj+4+DwGAwGAwGAwGAwGAwfHfx8A
18_full_68	18_full	68	9	/n+w+DweDweDweDweDweDweH/38A
18_full_69	18_full	69	9	///wGAwGAwH4/GAwGAwGAwGA///A
18_full_70	18_full	70	9	///wGAwGAwH4/GAwGAwGAwGAwGAA
18_full_71	18_full	71	9	Pj+4+DwGAwGAwGHw+DweDwfHfx8A
18_full_72	18_full	72	8	w8PDw8PDw8P//8PDw8PDw8PD
18_full_73	18_full	73	4	/2ZmZmZmZmb/
18_full_74	18_full	74	7	BgwYMGDBgwYMGDBg8fd8cA==
18_full_75	18_full	75	9	weDw+Gx2c3Hw8Hw3GcxmOw2HweDA
18_full_76	18_full	76	8	wMDAwMDAwMDAwMDAwMDAwP//
18_full_77	18_full	77	12	wD4H8P8P+f37zzxjxjwDwDwDwDwDwDwDwDwD
18_full_78	18_full	78	10	wPA+D4Pw/D2PY8zzPG8bw/D8HwfA8DA=
18_full_79	18_full	79	9	Pj+4+DweDweDweDweDweDwfHfx8A
18_full_80	18_full	80	9	/n+w+DweDw/+/mAwGAwGAwGAwGAA
18_full_81	18_full	81	9	Pj+4+DweDweDweDweDwebz/Of57A
18_full_82	18_full	82	9	/n+w+DweDw/+/mYzGcxmOw2HweDA
18_full_83	18_full	83	9	Pz/4eAwGAwHAfh+A4DAYDAeH/z8A
18_full_84	18_full	84	8	//8YGBgYGBgYGBgYGBgYGBgY
18_full_85	18_full	85	9	weDweDweDweDweDweDweDwfHfx8A
18_full_86	18_full	86	9	weDweDweDweDwfHYzuNh8HA4CAQA
18_full_87	18_full	87	12	wDwDwDwDwDwDwDwDwDxjxjzz73//f+eeMMMM
18_full_88	18_full	88	9	weDwfHYzGdx8HA4PjuYzG4+DweDA
18_full_89	18_full	89	8	w8PD52Zmfjw8PBgYGBgYGBgY
18_full_90	18_full	90	9	///AYDA4GBwcHA4ODgYHAwGA///A
18_full_91	18_full	91	5	//GMYxjGMYxjGP/A
18_full_92	18_full	92	8	wMDgYGBwMDgYGBwMDgYGBwMD
18_full_93	18_full	93	5	/8YxjGMYxjGMY//A
18_full_94	18_full	94	7	EHH3fHBAAAAAAAAAAAAAAA==
18_full_95	18_full	95	7	AAAAAAAAAAAAAAAAAAD//A==
\.

COMMIT;
