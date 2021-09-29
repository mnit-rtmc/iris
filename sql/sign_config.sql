-- Normally, IRIS will automatically add sign_config records when communicating
-- with a new style DMS.  This script can be used to create a sign configuration
-- for testing purposes if needed before that time.  Before running it, the
-- VALUES must be adjusted manually for the specific configuration needed.
\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.sign_config (name, face_width, face_height, border_horiz,
	border_vert, pitch_horiz, pitch_vert, pixel_width, pixel_height,
	char_width, char_height, monochrome_foreground, monochrome_background,
	color_scheme, default_font)
VALUES (
	'sc_432x128_1', -- name
	9360,     -- face_width (mm)
	3048,     -- face_height (mm)
	229,      -- border_horiz (mm)
	210,      -- border_vert (mm)
	20,       -- pitch_horiz (mm)
	20,       -- pitch_vert (mm)
	432,      -- pixel_width
	128,      -- pixel_height
	0,        -- char_width
	0,        -- char_height
	0,        -- monochrome_foreground
	0,        -- monochrome_background
	4,        -- color_scheme
	'26_full' -- default_font
);

COMMIT;
