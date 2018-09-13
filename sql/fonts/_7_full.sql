\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('_7_full', 18, 7, 0, 3, 2, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
_7_full_32	_7_full	32	1	AA==
_7_full_33	_7_full	33	2	qog=
_7_full_34	_7_full	34	3	tAAA
_7_full_35	_7_full	35	5	Ur6vqUA=
_7_full_36	_7_full	36	5	I6jiuIA=
_7_full_37	_7_full	37	5	xkRETGA=
_7_full_38	_7_full	38	5	RSiKyaA=
_7_full_39	_7_full	39	1	wA==
_7_full_40	_7_full	40	3	KkiI
_7_full_41	_7_full	41	3	iJKg
_7_full_42	_7_full	42	7	EFEUFEUEAA==
_7_full_43	_7_full	43	5	AQnyEAA=
_7_full_44	_7_full	44	3	AACg
_7_full_45	_7_full	45	4	AA8AAA==
_7_full_46	_7_full	46	2	AAg=
_7_full_47	_7_full	47	5	AEREQAA=
_7_full_48	_7_full	48	4	aZmZYA==
_7_full_49	_7_full	49	3	WSS4
_7_full_50	_7_full	50	4	aRJI8A==
_7_full_51	_7_full	51	4	aRYZYA==
_7_full_52	_7_full	52	5	EZUviEA=
_7_full_53	_7_full	53	4	+IYZYA==
_7_full_54	_7_full	54	4	aY6ZYA==
_7_full_55	_7_full	55	4	8RIkQA==
_7_full_56	_7_full	56	4	aZaZYA==
_7_full_57	_7_full	57	4	aZcZYA==
_7_full_58	_7_full	58	2	CIA=
_7_full_59	_7_full	59	3	AQUA
_7_full_60	_7_full	60	4	EkhCEA==
_7_full_61	_7_full	61	4	APDwAA==
_7_full_62	_7_full	62	4	hCEkgA==
_7_full_63	_7_full	63	4	aRIgIA==
_7_full_64	_7_full	64	5	dGdZwcA=
_7_full_65	_7_full	65	4	aZ+ZkA==
_7_full_66	_7_full	66	4	6Z6Z4A==
_7_full_67	_7_full	67	4	aYiJYA==
_7_full_68	_7_full	68	4	6ZmZ4A==
_7_full_69	_7_full	69	4	+I6I8A==
_7_full_70	_7_full	70	4	+I6IgA==
_7_full_71	_7_full	71	4	aYuZcA==
_7_full_72	_7_full	72	4	mZ+ZkA==
_7_full_73	_7_full	73	3	6SS4
_7_full_74	_7_full	74	4	EREZYA==
_7_full_75	_7_full	75	4	maypkA==
_7_full_76	_7_full	76	4	iIiI8A==
_7_full_77	_7_full	77	7	g46smTBggA==
_7_full_78	_7_full	78	5	jnNZziA=
_7_full_79	_7_full	79	4	aZmZYA==
_7_full_80	_7_full	80	4	6Z6IgA==
_7_full_81	_7_full	81	5	dGMayaA=
_7_full_82	_7_full	82	4	6Z6pkA==
_7_full_83	_7_full	83	4	aYYZYA==
_7_full_84	_7_full	84	5	+QhCEIA=
_7_full_85	_7_full	85	4	mZmZYA==
_7_full_86	_7_full	86	5	jGMVKIA=
_7_full_87	_7_full	87	7	gwYMmrVRAA==
_7_full_88	_7_full	88	5	jFRFRiA=
_7_full_89	_7_full	89	5	jFRCEIA=
_7_full_90	_7_full	90	5	+EREQ+A=
_7_full_91	_7_full	91	3	8kk4
_7_full_92	_7_full	92	5	BBBBBAA=
_7_full_93	_7_full	93	3	5JJ4
_7_full_94	_7_full	94	5	IqIAAAA=
_7_full_95	_7_full	95	5	AAAAA+A=
\.

COMMIT;
