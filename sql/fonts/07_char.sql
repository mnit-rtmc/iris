\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('07_char', 1, 7, 5, 0, 0, 7314);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
07_char_32	07_char	32	5	AAAAAAA=
07_char_33	07_char	33	5	IQhCAIA=
07_char_34	07_char	34	5	UoAAAAA=
07_char_35	07_char	35	5	Ur6vqUA=
07_char_36	07_char	36	5	I6jiuIA=
07_char_37	07_char	37	5	xkRETGA=
07_char_38	07_char	38	5	RSiKyaA=
07_char_39	07_char	39	5	IQAAAAA=
07_char_40	07_char	40	5	ERCEEEA=
07_char_41	07_char	41	5	QQQhEQA=
07_char_42	07_char	42	5	JVxHVIA=
07_char_43	07_char	43	5	AQnyEAA=
07_char_44	07_char	44	5	AAAAEQA=
07_char_45	07_char	45	5	AADgAAA=
07_char_46	07_char	46	5	AAAAAIA=
07_char_47	07_char	47	5	AEREQAA=
07_char_48	07_char	48	5	MlKUpMA=
07_char_49	07_char	49	5	IwhCEcA=
07_char_50	07_char	50	5	dEJkQ+A=
07_char_51	07_char	51	5	dEJgxcA=
07_char_52	07_char	52	5	EZUviEA=
07_char_53	07_char	53	5	/CDgxcA=
07_char_54	07_char	54	5	dGHoxcA=
07_char_55	07_char	55	5	+EQiEQA=
07_char_56	07_char	56	5	dGLoxcA=
07_char_57	07_char	57	5	dGLwxcA=
07_char_58	07_char	58	5	ABAEAAA=
07_char_59	07_char	59	5	AAgCIAA=
07_char_60	07_char	60	5	EREEEEA=
07_char_61	07_char	61	5	AD4PgAA=
07_char_62	07_char	62	5	QQQREQA=
07_char_63	07_char	63	5	ZIRCAIA=
07_char_64	07_char	64	5	dGdZwcA=
07_char_65	07_char	65	5	dGP4xiA=
07_char_66	07_char	66	5	9GPox8A=
07_char_67	07_char	67	5	dGEIRcA=
07_char_68	07_char	68	5	9GMYx8A=
07_char_69	07_char	69	5	/CHoQ+A=
07_char_70	07_char	70	5	/CHoQgA=
07_char_71	07_char	71	5	dGF4xeA=
07_char_72	07_char	72	5	jGP4xiA=
07_char_73	07_char	73	5	cQhCEcA=
07_char_74	07_char	74	5	EIQhSYA=
07_char_75	07_char	75	5	jKmKSiA=
07_char_76	07_char	76	5	hCEIQ+A=
07_char_77	07_char	77	5	jusYxiA=
07_char_78	07_char	78	5	jnNZziA=
07_char_79	07_char	79	5	dGMYxcA=
07_char_80	07_char	80	5	9GPoQgA=
07_char_81	07_char	81	5	dGMayaA=
07_char_82	07_char	82	5	9GPqSiA=
07_char_83	07_char	83	5	dGDgxcA=
07_char_84	07_char	84	5	+QhCEIA=
07_char_85	07_char	85	5	jGMYxcA=
07_char_86	07_char	86	5	jGMVKIA=
07_char_87	07_char	87	5	jGMa1UA=
07_char_88	07_char	88	5	jFRFRiA=
07_char_89	07_char	89	5	jFRCEIA=
07_char_90	07_char	90	5	+EREQ+A=
07_char_91	07_char	91	5	chCEIcA=
07_char_92	07_char	92	5	BBBBBAA=
07_char_93	07_char	93	5	cIQhCcA=
07_char_94	07_char	94	5	IqIAAAA=
07_char_95	07_char	95	5	AAAAA+A=
\.

COMMIT;
