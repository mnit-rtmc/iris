\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('20_full', 15, 20, 0, 5, 3, 0);

COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;
20_full_48	20_full	48	9	Pj+4+DweDweDweDweDweDweDwfHfx8A=
20_full_49	20_full	49	6	Mc88MMMMMMMMMMMMMM//
20_full_50	20_full	50	10	Px/uHwMAwDAMBwOBwOBwOBwOAwDAMA///w==
20_full_51	20_full	51	10	Px/uHwMAwDAMAwHB4HgHAMAwDAPA+Hf4/A==
20_full_52	20_full	52	10	AwHA8Hw7HM4zDMMwz///AwDAMAwDAMAwDA==
20_full_53	20_full	53	10	///8AwDAMAwDAP8f4BwDAMAwDAMA8H/5/A==
20_full_54	20_full	54	10	P5/+DwDAMAwDAP8/7B8DwPA8DwPA+Hf4/A==
20_full_55	20_full	55	9	///AYDAYHAwOBgcDA4GBwMBgcDAYDAA=
20_full_56	20_full	56	10	Px/uHwPA8DwPh3+Px/uHwPA8DwPA+Hf4/A==
20_full_57	20_full	57	10	Px/uHwPA8DwPA+Df8/wDAMAwDAMA8H/5/A==
\.

COMMIT;
