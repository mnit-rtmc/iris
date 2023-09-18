\set ON_ERROR_STOP

BEGIN;

ALTER SCHEMA public OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.45.0', '5.46.0');

-- Change default_font to number (instead of name)
DROP VIEW dms_view;
DROP VIEW sign_config_view;

ALTER TABLE iris.sign_config RENAME COLUMN default_font TO default_font_name;
ALTER TABLE iris.sign_config ADD COLUMN default_font INTEGER;

UPDATE iris.sign_config SET default_font = 1;

WITH cte AS (
    SELECT name, f_number
    FROM iris.font
)
UPDATE iris.sign_config SET default_font = cte.f_number
FROM cte
WHERE iris.sign_config.default_font_name = cte.name;

ALTER TABLE iris.sign_config ALTER COLUMN default_font SET NOT NULL;

ALTER TABLE iris.sign_config DROP COLUMN default_font_name;

CREATE VIEW sign_config_view AS
    SELECT name, face_width, face_height, border_horiz, border_vert,
           pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
           char_height, monochrome_foreground, monochrome_background,
           cs.description AS color_scheme, default_font,
           module_width, module_height
    FROM iris.sign_config
    JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

CREATE VIEW dms_view AS
    SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
           d.sign_config, d.sign_detail, d.static_graphic, d.beacon,
           p.camera, p.preset_num, default_font,
           msg_user, msg_sched, msg_current, expire_time,
           status, stuck_pixels,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris.dms d
    LEFT JOIN iris.camera_preset p ON d.preset = p.name
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

-- Delete unneeded system attributes
DELETE FROM iris.system_attribute WHERE name = 'dms_high_temp_cutoff';
DELETE FROM iris.system_attribute WHERE name = 'dms_reset_enable';
DELETE FROM iris.system_attribute WHERE name = 'dms_pixel_status_enable';
DELETE FROM iris.system_attribute WHERE name = 'dms_brightness_enable';

COMMIT;
