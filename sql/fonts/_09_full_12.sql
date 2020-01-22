\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('_09_full_12', 17, 12, 0, 2, 2, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
_09_full_12_32	_09_full_12	32	1	AAA=
_09_full_12_33	_09_full_12	33	2	qqiA
_09_full_12_34	_09_full_12	34	3	tAAAAAA=
_09_full_12_35	_09_full_12	35	5	Ur6lfUoAAAA=
_09_full_12_36	_09_full_12	36	5	I6lHFK4gAAA=
_09_full_12_37	_09_full_12	37	5	BnQiIXMAAAA=
_09_full_12_38	_09_full_12	38	6	IUUIYliidBAA
_09_full_12_39	_09_full_12	39	1	wAA=
_09_full_12_40	_09_full_12	40	3	KkkiIAA=
_09_full_12_41	_09_full_12	41	3	iJJKgAA=
_09_full_12_42	_09_full_12	42	5	ASriOqQAAAA=
_09_full_12_43	_09_full_12	43	5	AAhPkIAAAAA=
_09_full_12_44	_09_full_12	44	3	AAACUAA=
_09_full_12_45	_09_full_12	45	4	AADwAAAA
_09_full_12_46	_09_full_12	46	2	AACA
_09_full_12_47	_09_full_12	47	5	CEQiIRCAAAA=
_09_full_12_48	_09_full_12	48	5	dGMYxjFwAAA=
_09_full_12_49	_09_full_12	49	3	WSSS4AA=
_09_full_12_50	_09_full_12	50	5	dEIiIhD4AAA=
_09_full_12_51	_09_full_12	51	5	dEITBDFwAAA=
_09_full_12_52	_09_full_12	52	5	EZUpfEIQAAA=
_09_full_12_53	_09_full_12	53	5	/CEPBDFwAAA=
_09_full_12_54	_09_full_12	54	5	dGEPRjFwAAA=
_09_full_12_55	_09_full_12	55	5	+EIhEIhAAAA=
_09_full_12_56	_09_full_12	56	5	dGMXRjFwAAA=
_09_full_12_57	_09_full_12	57	5	dGMXhDFwAAA=
_09_full_12_58	_09_full_12	58	2	AiAA
_09_full_12_59	_09_full_12	59	3	ACCgAAA=
_09_full_12_60	_09_full_12	60	4	ASSEIQAA
_09_full_12_61	_09_full_12	61	4	AA8PAAAA
_09_full_12_62	_09_full_12	62	4	CEISSAAA
_09_full_12_63	_09_full_12	63	5	dEIREIAgAAA=
_09_full_12_64	_09_full_12	64	6	ehlrppngeAAA
_09_full_12_65	_09_full_12	65	5	IqMfxjGIAAA=
_09_full_12_66	_09_full_12	66	5	9GMfRjHwAAA=
_09_full_12_67	_09_full_12	67	5	dGEIQhFwAAA=
_09_full_12_68	_09_full_12	68	5	9GMYxjHwAAA=
_09_full_12_69	_09_full_12	69	5	/CEOQhD4AAA=
_09_full_12_70	_09_full_12	70	5	/CEOQhCAAAA=
_09_full_12_71	_09_full_12	71	5	dGEJxjFwAAA=
_09_full_12_72	_09_full_12	72	5	jGMfxjGIAAA=
_09_full_12_73	_09_full_12	73	3	6SSS4AA=
_09_full_12_74	_09_full_12	74	4	ERERGWAA
_09_full_12_75	_09_full_12	75	5	jGVMUlGIAAA=
_09_full_12_76	_09_full_12	76	4	iIiIiPAA
_09_full_12_77	_09_full_12	77	7	g46smTBgwYIAAAA=
_09_full_12_78	_09_full_12	78	6	xxpplljjhAAA
_09_full_12_79	_09_full_12	79	5	dGMYxjFwAAA=
_09_full_12_80	_09_full_12	80	5	9GMfQhCAAAA=
_09_full_12_81	_09_full_12	81	5	dGMYxrJoQAA=
_09_full_12_82	_09_full_12	82	5	9GMfUlGIAAA=
_09_full_12_83	_09_full_12	83	5	dGEHBDFwAAA=
_09_full_12_84	_09_full_12	84	5	+QhCEIQgAAA=
_09_full_12_85	_09_full_12	85	5	jGMYxjFwAAA=
_09_full_12_86	_09_full_12	86	5	jGMYqUQgAAA=
_09_full_12_87	_09_full_12	87	7	gwYMGTJq1UQAAAA=
_09_full_12_88	_09_full_12	88	5	jGKiKjGIAAA=
_09_full_12_89	_09_full_12	89	5	jGKiEIQgAAA=
_09_full_12_90	_09_full_12	90	5	+EQiIRD4AAA=
_09_full_12_91	_09_full_12	91	3	8kkk4AA=
_09_full_12_92	_09_full_12	92	5	hBCCCEEIAAA=
_09_full_12_93	_09_full_12	93	3	5JJJ4AA=
_09_full_12_94	_09_full_12	94	5	IqIAAAAAAAA=
_09_full_12_95	_09_full_12	95	4	AAAAAA8A
_09_full_12_96	_09_full_12	96	2	pAAA
_09_full_12_97	_09_full_12	97	5	AADgvjNoAAA=
_09_full_12_98	_09_full_12	98	5	hCEPRjmwAAA=
_09_full_12_99	_09_full_12	99	4	AAB4iHAA
_09_full_12_100	_09_full_12	100	5	CEIXxjNoAAA=
_09_full_12_101	_09_full_12	101	5	AADox9BwAAA=
_09_full_12_102	_09_full_12	102	4	NET0REAA
_09_full_12_103	_09_full_12	103	5	AAAGzjF4YuA=
_09_full_12_104	_09_full_12	104	5	hCELZjGIAAA=
_09_full_12_105	_09_full_12	105	3	AQSSQAA=
_09_full_12_106	_09_full_12	106	4	ABARERGW
_09_full_12_107	_09_full_12	107	5	hCMqYpKIAAA=
_09_full_12_108	_09_full_12	108	3	SSSSQAA=
_09_full_12_109	_09_full_12	109	7	AAAACtpkyZIAAAA=
_09_full_12_110	_09_full_12	110	5	AAALZjGIAAA=
_09_full_12_111	_09_full_12	111	5	AAAHRjFwAAA=
_09_full_12_112	_09_full_12	112	5	AAALZjH0IQA=
_09_full_12_113	_09_full_12	113	5	AAAGzjF4QhA=
_09_full_12_114	_09_full_12	114	4	AAC8iIAA
_09_full_12_115	_09_full_12	115	5	AADosFFwAAA=
_09_full_12_116	_09_full_12	116	4	AETkRDAA
_09_full_12_117	_09_full_12	117	5	AAAIxjNoAAA=
_09_full_12_118	_09_full_12	118	5	AAAIxiogAAA=
_09_full_12_119	_09_full_12	119	7	AAAACDBk1UQAAAA=
_09_full_12_120	_09_full_12	120	5	AAAIqIqIAAA=
_09_full_12_121	_09_full_12	121	5	AAAIxjNoQuA=
_09_full_12_122	_09_full_12	122	5	AAHxERD4AAA=
_09_full_12_123	_09_full_12	123	3	aSiSYAA=
_09_full_12_124	_09_full_12	124	1	94A=
_09_full_12_125	_09_full_12	125	3	ySKSwAA=
\.

COMMIT;
