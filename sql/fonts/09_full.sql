\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('09_full', 4, 9, 0, 2, 2, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
09_full_32	09_full	32	1	AAA=
09_full_33	09_full	33	2	qqiA
09_full_34	09_full	34	3	tAAAAA==
09_full_35	09_full	35	5	Ur6lfUoA
09_full_36	09_full	36	5	I6lHFK4g
09_full_37	09_full	37	5	BjIiImMA
09_full_38	09_full	38	6	IUUIYliidA==
09_full_39	09_full	39	1	wAA=
09_full_40	09_full	40	3	KkkiIA==
09_full_41	09_full	41	3	iJJKgA==
09_full_42	09_full	42	5	ASriOqQA
09_full_43	09_full	43	5	AAhPkIAA
09_full_44	09_full	44	3	AAACgA==
09_full_45	09_full	45	4	AADwAAA=
09_full_46	09_full	46	2	AACA
09_full_47	09_full	47	5	CEQiIRCA
09_full_48	09_full	48	5	dGMYxjFw
09_full_49	09_full	49	3	WSSS4A==
09_full_50	09_full	50	5	dEIiIhD4
09_full_51	09_full	51	5	dEITBDFw
09_full_52	09_full	52	5	EZUpfEIQ
09_full_53	09_full	53	5	/CEPBDFw
09_full_54	09_full	54	5	dGEPRjFw
09_full_55	09_full	55	5	+EIhEIhA
09_full_56	09_full	56	5	dGMXRjFw
09_full_57	09_full	57	5	dGMXhDFw
09_full_58	09_full	58	2	AiAA
09_full_59	09_full	59	3	ACCgAA==
09_full_60	09_full	60	4	ASSEIQA=
09_full_61	09_full	61	4	AA8PAAA=
09_full_62	09_full	62	4	CEISSAA=
09_full_63	09_full	63	5	dEIREIAg
09_full_64	09_full	64	6	ehlrppngeA==
09_full_65	09_full	65	5	IqMfxjGI
09_full_66	09_full	66	5	9GMfRjHw
09_full_67	09_full	67	5	dGEIQhFw
09_full_68	09_full	68	5	9GMYxjHw
09_full_69	09_full	69	5	/CEOQhD4
09_full_70	09_full	70	5	/CEOQhCA
09_full_71	09_full	71	5	dGEJxjFw
09_full_72	09_full	72	5	jGMfxjGI
09_full_73	09_full	73	3	6SSS4A==
09_full_74	09_full	74	4	ERERGWA=
09_full_75	09_full	75	5	jGVMUlGI
09_full_76	09_full	76	4	iIiIiPA=
09_full_77	09_full	77	7	g46smTBgwYI=
09_full_78	09_full	78	6	xxpplljjhA==
09_full_79	09_full	79	5	dGMYxjFw
09_full_80	09_full	80	5	9GMfQhCA
09_full_81	09_full	81	5	dGMYxrJo
09_full_82	09_full	82	5	9GMfUlGI
09_full_83	09_full	83	5	dGEHBDFw
09_full_84	09_full	84	5	+QhCEIQg
09_full_85	09_full	85	5	jGMYxjFw
09_full_86	09_full	86	5	jGMYqUQg
09_full_87	09_full	87	7	gwYMGTJq1UQ=
09_full_88	09_full	88	5	jGKiKjGI
09_full_89	09_full	89	5	jGKiEIQg
09_full_90	09_full	90	5	+EQiIRD4
09_full_91	09_full	91	3	8kkk4A==
09_full_92	09_full	92	5	hBCCCEEI
09_full_93	09_full	93	3	5JJJ4A==
09_full_94	09_full	94	5	IqIAAAAA
09_full_95	09_full	95	5	AAAAAAD4
\.

COMMIT;
