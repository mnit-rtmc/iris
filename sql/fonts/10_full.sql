\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('10_full', 5, 10, 0, 3, 2, 42549);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
10_full_32	10_full	32	1	AAA=
10_full_33	10_full	33	2	qqgg
10_full_34	10_full	34	3	tAAAAA==
10_full_35	10_full	35	5	ApX1K+pQAA==
10_full_36	10_full	36	5	I6lHFK4gAA==
10_full_37	10_full	37	5	BjIiERMYAA==
10_full_38	10_full	38	6	IUUIYliilZA=
10_full_39	10_full	39	1	wAA=
10_full_40	10_full	40	3	KkkkRA==
10_full_41	10_full	41	3	iJJJUA==
10_full_42	10_full	42	5	ASriOqQAAA==
10_full_43	10_full	43	5	AAhPkIAAAA==
10_full_44	10_full	44	3	AAACUA==
10_full_45	10_full	45	4	AADwAAA=
10_full_46	10_full	46	2	AACg
10_full_47	10_full	47	5	CEQiEQiEAA==
10_full_48	10_full	48	5	dGMYxjGLgA==
10_full_49	10_full	49	3	WSSSXA==
10_full_50	10_full	50	5	dGIRERCHwA==
10_full_51	10_full	51	5	dEITBCGLgA==
10_full_52	10_full	52	5	EZUpfEIQgA==
10_full_53	10_full	53	5	/CEPBCGLgA==
10_full_54	10_full	54	5	dGEPRjGLgA==
10_full_55	10_full	55	5	+EIhEIhCAA==
10_full_56	10_full	56	5	dGMXRjGLgA==
10_full_57	10_full	57	5	dGMYvCGLgA==
10_full_58	10_full	58	2	CgoA
10_full_59	10_full	59	3	ASASgA==
10_full_60	10_full	60	4	ASSEIQA=
10_full_61	10_full	61	4	AA8PAAA=
10_full_62	10_full	62	4	CEISSAA=
10_full_63	10_full	63	5	dEIREIQBAA==
10_full_64	10_full	64	6	ehlrpppngeA=
10_full_65	10_full	65	5	IqMY/jGMQA==
10_full_66	10_full	66	5	9GMfRjGPgA==
10_full_67	10_full	67	5	dGEIQhCLgA==
10_full_68	10_full	68	5	9GMYxjGPgA==
10_full_69	10_full	69	5	/CEOQhCHwA==
10_full_70	10_full	70	5	/CEOQhCEAA==
10_full_71	10_full	71	5	dGEITjGLgA==
10_full_72	10_full	72	5	jGMfxjGMQA==
10_full_73	10_full	73	3	6SSSXA==
10_full_74	10_full	74	4	EREREZY=
10_full_75	10_full	75	5	jGVMUlGMQA==
10_full_76	10_full	76	4	iIiIiI8=
10_full_77	10_full	77	7	g46smTBgwYME
10_full_78	10_full	78	6	hxxpplljjhA=
10_full_79	10_full	79	5	dGMYxjGLgA==
10_full_80	10_full	80	5	9GMfQhCEAA==
10_full_81	10_full	81	5	dGMYxjWTQA==
10_full_82	10_full	82	5	9GMfUlKMQA==
10_full_83	10_full	83	5	dGEHBCGLgA==
10_full_84	10_full	84	5	+QhCEIQhAA==
10_full_85	10_full	85	5	jGMYxjGLgA==
10_full_86	10_full	86	5	jGMYxUohAA==
10_full_87	10_full	87	7	gwYMGDJk1aqI
10_full_88	10_full	88	5	jGKiKjGMQA==
10_full_89	10_full	89	5	jGMVEIQhAA==
10_full_90	10_full	90	5	+EQiEQiHwA==
10_full_91	10_full	91	3	8kkknA==
10_full_92	10_full	92	5	hBCCEEIIQA==
10_full_93	10_full	93	3	5JJJPA==
10_full_94	10_full	94	5	IqIAAAAAAA==
10_full_95	10_full	95	5	AAAAAAAHwA==
\.

COMMIT;
