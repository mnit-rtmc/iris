\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('14_ledstar', 10, 14, 0, 6, 3, 63042);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
14_ledstar_32	14_ledstar	32	1	AAA=
14_ledstar_33	14_ledstar	33	2	///w8A==
14_ledstar_34	14_ledstar	34	6	zzzzzzAAAAAAAAA=
14_ledstar_35	14_ledstar	35	10	MwzDM///zMMwzDM///zMMwzA
14_ledstar_36	14_ledstar	36	8	GH7bmJjYfD4bGRnbfhg=
14_ledstar_37	14_ledstar	37	14	MAngbMMzGHjAxgAwAYAMMGHjDNgzwHoAwA==
14_ledstar_38	14_ledstar	38	11	i9FHKNUZoxRi8AAAAAAAAAAAAAA=
14_ledstar_39	14_ledstar	39	4	b/ckAAAAAA==
14_ledstar_40	14_ledstar	40	6	DGMYwwwwwwYMGDA=
14_ledstar_41	14_ledstar	41	6	wYMGDDDDDDGMYwA=
14_ledstar_42	14_ledstar	42	10	YYzB4///x4MxhgAAAAAAAAAA
14_ledstar_43	14_ledstar	43	8	AAAYGBgY//8YGBgYAAA=
14_ledstar_44	14_ledstar	44	4	AAAAAG/3JA==
14_ledstar_45	14_ledstar	45	10	AAAAAAAAAA///wAAAAAAAAAA
14_ledstar_46	14_ledstar	46	4	AAAAAAb/YA==
14_ledstar_47	14_ledstar	47	10	AEAwGAwGAwGAwGAwCAAAAAAA
14_ledstar_48	14_ledstar	48	10	Px/uHwPA8DwPA8DwPA+Hf4/A
14_ledstar_49	14_ledstar	49	6	Mc8sMMMMMMMM//A=
14_ledstar_50	14_ledstar	50	10	Hw/mDQMAwDAYDAYDAYDAf//w
14_ledstar_51	14_ledstar	51	10	Hw/mDQMAwDD4PgDANA2DP4fA
14_ledstar_52	14_ledstar	52	10	AQDAcDwbDMYzDP//8DAMAwDA
14_ledstar_53	14_ledstar	53	10	/7/sAwD/P+AcAwDAMA+Hf4/A
14_ledstar_54	14_ledstar	54	10	Hw/GAwDAMA3z/uHwPA+Hf4/A
14_ledstar_55	14_ledstar	55	10	///wDAMBgOAwGAYDAMBgGAYA
14_ledstar_56	14_ledstar	56	10	Px/uHwPA2GPx/uHwPA+Hf4/A
14_ledstar_57	14_ledstar	57	10	Px/uHwPA+Hf8+wDAMAwGPw+A
14_ledstar_58	14_ledstar	58	4	AG/2AG/2AA==
14_ledstar_59	14_ledstar	59	4	AG/2AG/3JA==
14_ledstar_60	14_ledstar	60	7	AgwwwwwwMDAwMDAgAA==
14_ledstar_61	14_ledstar	61	7	AAAAD//AAP/8AAAAAA==
14_ledstar_62	14_ledstar	62	7	gYGBgYGBhhhhhggAAA==
14_ledstar_63	14_ledstar	63	9	Pj+wcDAYDBwYGAwGAAGAwA==
14_ledstar_64	14_ledstar	64	12	P8QCgBj5n5sZsZsZsZv6ncgBQCP8
14_ledstar_65	14_ledstar	65	10	DAeDMYbA8DwP///wPA8DwPAw
14_ledstar_66	14_ledstar	66	10	/z/sHwPA8G/z/MGwPA8H/7/A
14_ledstar_67	14_ledstar	67	10	Hg/GGYfA8AwDAMAwNh2GPweA
14_ledstar_68	14_ledstar	68	10	/z/sHwPA8DwPA8DwPA8H/7/A
14_ledstar_69	14_ledstar	69	10	///8AwDAMA/z/MAwDAMA///w
14_ledstar_70	14_ledstar	70	10	///8AwDAMA/z/MAwDAMAwDAA
14_ledstar_71	14_ledstar	71	10	Px/uHwPAMAwDH8fwPA+Hf4/A
14_ledstar_72	14_ledstar	72	10	wPA8DwPA8D///8DwPA8DwPAw
14_ledstar_73	14_ledstar	73	6	//MMMMMMMMMM//A=
14_ledstar_74	14_ledstar	74	10	A8DwGAYBgGAYBgGAYBsGfw+A
14_ledstar_75	14_ledstar	75	10	wPBsMxjMNg8DwNgzDGMMwbAw
14_ledstar_76	14_ledstar	76	10	wDAMAwDAMAwDAMAwDAMA///w
14_ledstar_77	14_ledstar	77	12	wD4H8P2bzzxjxjwDwDwDwDwDwDwD
14_ledstar_78	14_ledstar	78	10	wPg+D8PQ9jyPM8TxvC8PwfAw
14_ledstar_79	14_ledstar	79	10	Hg/GGwPA8DwPA8DwPA2GPweA
14_ledstar_80	14_ledstar	80	10	/z/sHwPA8H/7/MAwDAMAwDAA
14_ledstar_81	14_ledstar	81	10	Hg/GGwPA8DwPA8DwPC2GP4eQ
14_ledstar_82	14_ledstar	82	10	/z/sHwPA8H/7/NgzDGMMwbAw
14_ledstar_83	14_ledstar	83	10	Px/sDwHAGAPgfAGAOA8Df4/A
14_ledstar_84	14_ledstar	84	10	///wwDAMAwDAMAwDAMAwDAMA
14_ledstar_85	14_ledstar	85	10	wPA8DwPA8DwPA8DwPA8Df4/A
14_ledstar_86	14_ledstar	86	11	wHgPAfB2DMGMYYwbA2A4BwBACAA=
14_ledstar_87	14_ledstar	87	12	wDwDwDwDwDwDwDxjxjzz2b8P4HwD
14_ledstar_88	14_ledstar	88	10	wPA8DwNhjMHgeDMYbA8DwPAw
14_ledstar_89	14_ledstar	89	10	wPA8DwNhmGMweAwDAMAwDAMA
14_ledstar_90	14_ledstar	90	10	///wDAMBgMBgMBgMBgMA///w
14_ledstar_91	14_ledstar	91	8	/v7AwMDAwMDAwMDA/v4=
14_ledstar_92	14_ledstar	92	10	gDAGAMAYAwBgDAGAMAQAAAAA
14_ledstar_93	14_ledstar	93	8	f38DAwMDAwMDAwMDf38=
14_ledstar_94	14_ledstar	94	14	/8P+D/A/gP8D/g/8O/jH8g/gH8A/AHgAwA==
14_ledstar_95	14_ledstar	95	7	AAAAAAAAAAAAAA//wA==
14_ledstar_96	14_ledstar	96	10	///////////////////////w
14_ledstar_97	14_ledstar	97	8	GDxmw8PDw///w8PDw8M=
14_ledstar_98	14_ledstar	98	8	wMDAwMDA/v/Dw8PD//4=
14_ledstar_99	14_ledstar	99	9	AAAAAAPj+4+AwGAwHHfx8A==
14_ledstar_100	14_ledstar	100	9	AwGAwGAwGf3+w2Gw2G/z7A==
14_ledstar_101	14_ledstar	101	7	//8GDBg/fsGDBg//wA==
14_ledstar_102	14_ledstar	102	7	//8GDBg/fsGDBgwYAA==
14_ledstar_103	14_ledstar	103	8	AAAAAH//w8P/fwOD/34=
14_ledstar_104	14_ledstar	104	8	wMDAwMDAwP7/w8PDw8M=
14_ledstar_105	14_ledstar	105	2	8P//8A==
14_ledstar_106	14_ledstar	106	7	BgwAAGDBgwYMHD/vgA==
14_ledstar_107	14_ledstar	107	8	wMDAwMDGzNjw8NjMxsM=
14_ledstar_108	14_ledstar	108	7	wYMGDBgwYMGDBg//wA==
14_ledstar_109	14_ledstar	109	14	AAAAAAAAALhz9+558MPDDww8MPDDww8MMA==
14_ledstar_110	14_ledstar	110	8	w8Pj4/PT28vPx8fDw8M=
14_ledstar_111	14_ledstar	111	8	AAAAADx+58PDw8Pnfjw=
14_ledstar_112	14_ledstar	112	8	AAAAAP7/w8P//sDAwMA=
14_ledstar_113	14_ledstar	113	8	AAAAAH//w8P/fwMDAwM=
14_ledstar_114	14_ledstar	114	8	AAAAAL7/4cDAwMDAwMA=
14_ledstar_115	14_ledstar	115	7	AAAAA8/w4Pj4HD/PAA==
14_ledstar_116	14_ledstar	116	8	//8YGBgYGBgYGBgYGBg=
14_ledstar_117	14_ledstar	117	8	AAAAAADDw8PDw8PH/3s=
14_ledstar_118	14_ledstar	118	10	AAAAAADA8DYZhiEMwzBIDAMA
14_ledstar_119	14_ledstar	119	11	AAAAAAAMB4DwHgPAeI8R5z2244A=
14_ledstar_120	14_ledstar	120	7	AAAADHjxtjhxtjx4wA==
14_ledstar_121	14_ledstar	121	8	AAAAAMPDw2Y8GBgYGBg=
14_ledstar_122	14_ledstar	122	8	AAAAAP//BgwYMGDA//8=
14_ledstar_123	14_ledstar	123	14	ADAB4A/Af4P7H87+P/D/g/wP4D/A/4P/AA==
14_ledstar_124	14_ledstar	124	2	////8A==
14_ledstar_125	14_ledstar	125	14	MAHgD8A/gH8E/jH9w/8H/A/wH8D/B/w/8A==
14_ledstar_126	14_ledstar	126	1	AAA=
\.

COMMIT;
