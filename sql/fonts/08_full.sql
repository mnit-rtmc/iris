\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('08_full', 3, 8, 0, 2, 2, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
08_full_32	08_full	32	1	AA==
08_full_33	08_full	33	2	qqI=
08_full_34	08_full	34	3	tAAA
08_full_35	08_full	35	5	Ur6vqUA=
08_full_36	08_full	36	5	I6jiuIA=
08_full_37	08_full	37	5	xkRETGA=
08_full_38	08_full	38	5	RSiKym0=
08_full_39	08_full	39	1	wA==
08_full_40	08_full	40	3	KkkR
08_full_41	08_full	41	3	iJJU
08_full_42	08_full	42	5	ASrnVIA=
08_full_43	08_full	43	5	AQnyEAA=
08_full_44	08_full	44	3	AAAU
08_full_45	08_full	45	4	AA8AAA==
08_full_46	08_full	46	2	AAI=
08_full_47	08_full	47	5	CERCIhA=
08_full_48	08_full	48	4	aZmZlg==
08_full_49	08_full	49	3	WSSX
08_full_50	08_full	50	5	dEImQh8=
08_full_51	08_full	51	5	dEJghi4=
08_full_52	08_full	52	5	EZUviEI=
08_full_53	08_full	53	5	/CDghi4=
08_full_54	08_full	54	5	dGHoxi4=
08_full_55	08_full	55	5	+EQiEQg=
08_full_56	08_full	56	5	dGLoxi4=
08_full_57	08_full	57	5	dGMXhi4=
08_full_58	08_full	58	2	CIA=
08_full_59	08_full	59	3	AQUA
08_full_60	08_full	60	4	EkhCEA==
08_full_61	08_full	61	4	APDwAA==
08_full_62	08_full	62	4	hCEkgA==
08_full_63	08_full	63	5	dEIiEAQ=
08_full_64	08_full	64	5	dGdazg4=
08_full_65	08_full	65	5	dGMfxjE=
08_full_66	08_full	66	5	9GPoxj4=
08_full_67	08_full	67	5	dGEIQi4=
08_full_68	08_full	68	5	9GMYxj4=
08_full_69	08_full	69	5	/CHIQh8=
08_full_70	08_full	70	5	/CHIQhA=
08_full_71	08_full	71	5	dGEJxi4=
08_full_72	08_full	72	5	jGP4xjE=
08_full_73	08_full	73	3	6SSX
08_full_74	08_full	74	4	ERERlg==
08_full_75	08_full	75	5	jKmKSjE=
08_full_76	08_full	76	4	iIiIjw==
08_full_77	08_full	77	7	g46smTBgwQ==
08_full_78	08_full	78	5	jnNaznE=
08_full_79	08_full	79	5	dGMYxi4=
08_full_80	08_full	80	5	9GPoQhA=
08_full_81	08_full	81	5	dGMY1k0=
08_full_82	08_full	82	5	9GPqSjE=
08_full_83	08_full	83	5	dGDghi4=
08_full_84	08_full	84	5	+QhCEIQ=
08_full_85	08_full	85	5	jGMYxi4=
08_full_86	08_full	86	5	jGMYqUQ=
08_full_87	08_full	87	7	gwYMGTVqog==
08_full_88	08_full	88	5	jFRCKjE=
08_full_89	08_full	89	5	jFSiEIQ=
08_full_90	08_full	90	5	+ERCIh8=
08_full_91	08_full	91	3	8kkn
08_full_92	08_full	92	5	hBBCCCE=
08_full_93	08_full	93	3	5JJP
08_full_94	08_full	94	5	IqIAAAA=
08_full_95	08_full	95	5	AAAAAB8=
\.

COMMIT;
