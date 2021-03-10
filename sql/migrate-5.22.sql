\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.21.0', '5.22.0');

ALTER TABLE iris.sign_config
    ADD COLUMN exclude_font VARCHAR(16) REFERENCES iris.font;

DROP VIEW dms_view;
DROP VIEW sign_config_view;

CREATE VIEW sign_config_view AS
	SELECT name, face_width, face_height, border_horiz, border_vert,
	       pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
	       char_height, monochrome_foreground, monochrome_background,
	       cs.description AS color_scheme, default_font, exclude_font
	FROM iris.sign_config
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, dp.description AS purpose, d.hidden, d.beacon,
	       p.camera, p.preset_num, d.sign_config, d.sign_detail,
	       default_font, exclude_font, override_font, override_foreground,
	       override_background, msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

COMMIT;
