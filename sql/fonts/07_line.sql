\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('07_line', 2, 7, 0, 0, 2, 40473);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
07_line_32	07_line	32	1	AA==
07_line_33	07_line	33	2	qog=
07_line_34	07_line	34	3	tAAA
07_line_35	07_line	35	5	Ur6vqUA=
07_line_36	07_line	36	5	I6jiuIA=
07_line_37	07_line	37	5	xkRETGA=
07_line_38	07_line	38	5	RSiKyaA=
07_line_39	07_line	39	1	wA==
07_line_40	07_line	40	3	KkiI
07_line_41	07_line	41	3	iJKg
07_line_42	07_line	42	7	EFEUFEUEAA==
07_line_43	07_line	43	5	AQnyEAA=
07_line_44	07_line	44	3	AACg
07_line_45	07_line	45	4	AA8AAA==
07_line_46	07_line	46	2	AAg=
07_line_47	07_line	47	5	AEREQAA=
07_line_48	07_line	48	4	aZmZYA==
07_line_49	07_line	49	3	WSS4
07_line_50	07_line	50	4	aRJI8A==
07_line_51	07_line	51	4	aRYZYA==
07_line_52	07_line	52	5	EZUviEA=
07_line_53	07_line	53	4	+IYZYA==
07_line_54	07_line	54	4	aY6ZYA==
07_line_55	07_line	55	4	8RIkQA==
07_line_56	07_line	56	4	aZaZYA==
07_line_57	07_line	57	4	aZcZYA==
07_line_58	07_line	58	2	CIA=
07_line_59	07_line	59	3	AQUA
07_line_60	07_line	60	4	EkhCEA==
07_line_61	07_line	61	4	APDwAA==
07_line_62	07_line	62	4	hCEkgA==
07_line_63	07_line	63	4	aRIgIA==
07_line_64	07_line	64	5	dGdZwcA=
07_line_65	07_line	65	4	aZ+ZkA==
07_line_66	07_line	66	4	6Z6Z4A==
07_line_67	07_line	67	4	aYiJYA==
07_line_68	07_line	68	4	6ZmZ4A==
07_line_69	07_line	69	4	+I6I8A==
07_line_70	07_line	70	4	+I6IgA==
07_line_71	07_line	71	4	aYuZcA==
07_line_72	07_line	72	4	mZ+ZkA==
07_line_73	07_line	73	3	6SS4
07_line_74	07_line	74	4	EREZYA==
07_line_75	07_line	75	4	maypkA==
07_line_76	07_line	76	4	iIiI8A==
07_line_77	07_line	77	7	g46smTBggA==
07_line_78	07_line	78	5	jnNZziA=
07_line_79	07_line	79	4	aZmZYA==
07_line_80	07_line	80	4	6Z6IgA==
07_line_81	07_line	81	5	dGMayaA=
07_line_82	07_line	82	4	6Z6pkA==
07_line_83	07_line	83	4	aYYZYA==
07_line_84	07_line	84	5	+QhCEIA=
07_line_85	07_line	85	4	mZmZYA==
07_line_86	07_line	86	5	jGMVKIA=
07_line_87	07_line	87	7	gwYMmrVRAA==
07_line_88	07_line	88	5	jFRFRiA=
07_line_89	07_line	89	5	jFRCEIA=
07_line_90	07_line	90	5	+EREQ+A=
07_line_91	07_line	91	3	8kk4
07_line_92	07_line	92	5	BBBBBAA=
07_line_93	07_line	93	3	5JJ4
07_line_94	07_line	94	5	IqIAAAA=
07_line_95	07_line	95	5	AAAAA+A=
\.

COMMIT;
