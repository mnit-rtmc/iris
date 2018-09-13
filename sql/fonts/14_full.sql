\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('14_full', 11, 14, 0, 6, 3, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
14_full_32	14_full	32	1	AAA=
14_full_33	14_full	33	3	22222A2A
14_full_34	14_full	34	5	3tIAAAAAAAAA
14_full_35	14_full	35	8	AABmZv//Zmb//2ZmAAA=
14_full_36	14_full	36	8	GH7/2djY/n8bG5v/fhg=
14_full_37	14_full	37	8	AOCh4wcOHDhw4MeFBwA=
14_full_38	14_full	38	8	GDxmZmY8OHjNx8bG/3s=
14_full_39	14_full	39	2	+AAAAA==
14_full_40	14_full	40	4	E2zMzMzGMQ==
14_full_41	14_full	41	4	jGMzMzM2yA==
14_full_42	14_full	42	8	AAAYmdt+PDx+25kYAAA=
14_full_43	14_full	43	6	AAAMMM//MMMAAAA=
14_full_44	14_full	44	4	AAAAAAAG6A==
14_full_45	14_full	45	5	AAAAA/8AAAAA
14_full_46	14_full	46	3	AAAAAA2A
14_full_47	14_full	47	8	AwMHBg4MHDgwcGDgwMA=
14_full_48	14_full	48	7	OPu+PHjx48ePH3fHAA==
14_full_49	14_full	49	6	Mc8MMMMMMMMM//A=
14_full_50	14_full	50	8	fP7HAwMHDhw4cODA//8=
14_full_51	14_full	51	8	fP7HAwMHHh4HAwPH/nw=
14_full_52	14_full	52	8	Bg4eNmbGxv//BgYGBgY=
14_full_53	14_full	53	8	///AwMD8fgcDAwPH/nw=
14_full_54	14_full	54	8	Pn/jwMD8/sfDw8Pnfjw=
14_full_55	14_full	55	7	//wYMGHDDhhww4YMAA==
14_full_56	14_full	56	8	PH7nw8Pnfn7nw8Pnfjw=
14_full_57	14_full	57	8	PH7nw8Pjfz8DAwPH/nw=
14_full_58	14_full	58	3	AA2A2AAA
14_full_59	14_full	59	4	AABmAG6AAA==
14_full_60	14_full	60	6	ABDGMYwwYMGDBAA=
14_full_61	14_full	61	6	AAAA//AA//AAAAA=
14_full_62	14_full	62	6	AgwYMGDDGMYwgAA=
14_full_63	14_full	63	8	fP7HAwMHDhwYGAAAGBg=
14_full_64	14_full	64	9	Pj+4+Dx+fyeTz+OwHAfx8A==
14_full_65	14_full	65	8	GDx+58PDw///w8PDw8M=
14_full_66	14_full	66	8	/P7Hw8PH/v7Hw8PH/vw=
14_full_67	14_full	67	8	PH7nw8DAwMDAwMPnfjw=
14_full_68	14_full	68	8	/P7Hw8PDw8PDw8PH/vw=
14_full_69	14_full	69	8	///AwMDA+PjAwMDA//8=
14_full_70	14_full	70	8	///AwMDA+PjAwMDAwMA=
14_full_71	14_full	71	8	Pn/jwMDAwMfHw8Pnfjw=
14_full_72	14_full	72	7	x48ePHj//8ePHjx4wA==
14_full_73	14_full	73	4	/2ZmZmZm/w==
14_full_74	14_full	74	7	BgwYMGDBgwYPH3fHAA==
14_full_75	14_full	75	8	w8PHztz48PD43M7Hw8M=
14_full_76	14_full	76	7	wYMGDBgwYMGDBg//wA==
14_full_77	14_full	77	11	wHwfx/3995zxHgPAeA8B4DwHgMA=
14_full_78	14_full	78	9	wfD4fj8ez2ebzePx+Hw+DA==
14_full_79	14_full	79	8	PH7nw8PDw8PDw8Pnfjw=
14_full_80	14_full	80	8	/P7Hw8PH/vzAwMDAwMA=
14_full_81	14_full	81	8	PH7nw8PDw8PD29/ufzs=
14_full_82	14_full	82	8	/P7Hw8PH/vzMzMbGw8M=
14_full_83	14_full	83	8	PH7nw8DgfD4HA8Pnfjw=
14_full_84	14_full	84	8	//8YGBgYGBgYGBgYGBg=
14_full_85	14_full	85	8	w8PDw8PDw8PDw8Pnfjw=
14_full_86	14_full	86	8	w8PDw8PDw8NmZjw8GBg=
14_full_87	14_full	87	11	wHgPAeA8B4DwHiPu7dn/PeMYYwA=
14_full_88	14_full	88	8	w8PDZn48GBg8fmbDw8M=
14_full_89	14_full	89	8	w8PDZmY8PBgYGBgYGBg=
14_full_90	14_full	90	8	//8DAwcOHDhw4MDA//8=
14_full_91	14_full	91	4	/8zMzMzM/w==
14_full_92	14_full	92	8	wMDgYHAwOBwMDgYHAwM=
14_full_93	14_full	93	4	/zMzMzMz/w==
14_full_94	14_full	94	6	MezhAAAAAAAAAAA=
14_full_95	14_full	95	6	AAAAAAAAAAAA//A=
\.

COMMIT;
