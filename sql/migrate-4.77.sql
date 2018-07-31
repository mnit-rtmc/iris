\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.76.0', '4.77.0');

CREATE TABLE iris.color_scheme (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

COPY iris.color_scheme (id, description) FROM stdin;
1	monochrome1Bit
2	monochrome8Bit
3	colorClassic
4	color24Bit
\.

ALTER TABLE iris.sign_config
	ADD COLUMN color_scheme INTEGER REFERENCES iris.color_scheme;
ALTER TABLE iris.sign_config ADD COLUMN monochrome_foreground INTEGER;
ALTER TABLE iris.sign_config ADD COLUMN monochrome_background INTEGER;

UPDATE iris.sign_config SET color_scheme = 1;
UPDATE iris.sign_config SET monochrome_foreground = 16764928; -- amber
UPDATE iris.sign_config SET monochrome_background = 0;

ALTER TABLE iris.sign_config ALTER COLUMN color_scheme SET NOT NULL;
ALTER TABLE iris.sign_config ALTER COLUMN monochrome_foreground SET NOT NULL;
ALTER TABLE iris.sign_config ALTER COLUMN monochrome_background SET NOT NULL;

DROP VIEW dms_view;
DROP VIEW sign_config_view;
CREATE VIEW sign_config_view AS
	SELECT name, dt.description AS dms_type, portable, technology,
	       sign_access, legend, beacon_type, face_width, face_height,
	       border_horiz, border_vert, pitch_horiz, pitch_vert,
	       pixel_width, pixel_height, char_width, char_height,
	       cs.description AS color_scheme,
	       monochrome_foreground, monochrome_background, default_font
	FROM iris.sign_config
	JOIN iris.dms_type dt ON sign_config.dms_type = dt.id
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, d.beacon, p.camera, p.preset_num, d.sign_config,
	       COALESCE(d.default_font, sc.default_font) AS default_font,
	       msg_sched, msg_current, deploy_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

COMMIT;
