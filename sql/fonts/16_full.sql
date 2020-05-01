\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('16_full', 13, 16, 0, 4, 3, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
16_full_32	16_full	32	1	AAA=
16_full_33	16_full	33	3	222222A2
16_full_34	16_full	34	6	zzRAAAAAAAAAAAAA
16_full_35	16_full	35	8	AAAAZmb//2Zm//9mZgAAAA==
16_full_36	16_full	36	8	GBh+/9nY2P5/Gxub/34YGA==
16_full_37	16_full	37	8	AOCh4wcGDBw4MGDgx4UHAA==
16_full_38	16_full	38	8	OHzuxu58OHj8z8/Gxu9/OQ==
16_full_39	16_full	39	2	+AAAAA==
16_full_40	16_full	40	4	N+zMzMzMznM=
16_full_41	16_full	41	4	znMzMzMzN+w=
16_full_42	16_full	42	8	AAAY2/9+PBg8fv/bGAAAAA==
16_full_43	16_full	43	6	AAAAMMM//MMMAAAA
16_full_44	16_full	44	4	AAAAAAAABuw=
16_full_45	16_full	45	6	AAAAAAA//AAAAAAA
16_full_46	16_full	46	3	AAAAAAA2
16_full_47	16_full	47	8	AAMDBgYMDBgYMDBgYMDAAA==
16_full_48	16_full	48	8	PH7nw8PDw8PDw8PDw+d+PA==
16_full_49	16_full	49	6	Mc8MMMMMMMMMMM//
16_full_50	16_full	50	8	PH7nwwMDBw4cOHDgwMD//w==
16_full_51	16_full	51	8	fP7HAwMDBx4eBwMDA8f+fA==
16_full_52	16_full	52	8	Bg4ePnbmxsb//wYGBgYGBg==
16_full_53	16_full	53	8	///AwMDA/H4HAwMDA8f+fA==
16_full_54	16_full	54	8	Pn/jwMDA/P7Hw8PDw+d+PA==
16_full_55	16_full	55	8	//8DAwMHBg4MHBg4MHBgYA==
16_full_56	16_full	56	8	PH7nw8PD535+58PDw+d+PA==
16_full_57	16_full	57	8	PH7nw8PD438/AwMDA8f+fA==
16_full_58	16_full	58	3	AAGwGwAA
16_full_59	16_full	59	4	AAAGYAboAAA=
16_full_60	16_full	60	7	AAQYYYYYYMDAwMDAwIA=
16_full_61	16_full	61	6	AAAAA//AA//AAAAA
16_full_62	16_full	62	7	AQMDAwMDAwYYYYYYIAA=
16_full_63	16_full	63	8	fP7HAwMDBw4cGBgYAAAYGA==
16_full_64	16_full	64	10	Px/uHwPH8/zPM8zzPP8ewDgH+Pw=
16_full_65	16_full	65	8	GDx+58PDw8P//8PDw8PDww==
16_full_66	16_full	66	8	/P7Hw8PDx/7+x8PDw8f+/A==
16_full_67	16_full	67	8	PH7nw8DAwMDAwMDAw+d+PA==
16_full_68	16_full	68	8	/P7Hw8PDw8PDw8PDw8f+/A==
16_full_69	16_full	69	8	///AwMDAwPj4wMDAwMD//w==
16_full_70	16_full	70	8	///AwMDAwPj4wMDAwMDAwA==
16_full_71	16_full	71	8	PH7nw8DAwMDHx8PDw+d+PA==
16_full_72	16_full	72	8	w8PDw8PDw///w8PDw8PDww==
16_full_73	16_full	73	4	/2ZmZmZmZv8=
16_full_74	16_full	74	7	BgwYMGDBgwYMGDx93xw=
16_full_75	16_full	75	8	w8PHxs7c+PD43MzOxsfDww==
16_full_76	16_full	76	7	wYMGDBgwYMGDBgwYP/8=
16_full_77	16_full	77	12	wD4H8P+f37zzxjxjwDwDwDwDwDwDwDwD
16_full_78	16_full	78	9	weD4fD8fj2ezzebx+Pw+HweD
16_full_79	16_full	79	8	PH7nw8PDw8PDw8PDw+d+PA==
16_full_80	16_full	80	8	/P7Hw8PDx/78wMDAwMDAwA==
16_full_81	16_full	81	8	PH7nw8PDw8PDw8Pb3+5/Ow==
16_full_82	16_full	82	8	/P7Hw8PDx/78zMzGxsPDww==
16_full_83	16_full	83	8	PH7nw8DA4Hw+BwMDw+d+PA==
16_full_84	16_full	84	8	//8YGBgYGBgYGBgYGBgYGA==
16_full_85	16_full	85	8	w8PDw8PDw8PDw8PDw+d+PA==
16_full_86	16_full	86	8	w8PDw8PDw8PDZmZmPDwYGA==
16_full_87	16_full	87	12	wDwDwDwDwDwDwDwDxjxjzz73f+eeeeMM
16_full_88	16_full	88	8	w8PDZmY8PBgYPDxmZsPDww==
16_full_89	16_full	89	8	w8PDZmZmPDw8GBgYGBgYGA==
16_full_90	16_full	90	8	//8DBgYMDBgYMDBgYMD//w==
16_full_91	16_full	91	4	/8zMzMzMzP8=
16_full_92	16_full	92	8	AMDAYGAwMBgYDAwGBgMDAA==
16_full_93	16_full	93	4	/zMzMzMzM/8=
16_full_94	16_full	94	6	MezhAAAAAAAAAAAA
16_full_95	16_full	95	6	AAAAAAAAAAAAAA//
\.

COMMIT;
