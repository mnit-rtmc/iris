\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.36.0', '5.37.0');

ALTER TABLE iris.graphic DROP CONSTRAINT graphic_height_ck;
ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_height_ck
    CHECK (height >= 1 AND height <= 144);

ALTER TABLE iris.graphic DROP CONSTRAINT graphic_width_ck;
ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_width_ck
    CHECK (width >= 1 AND width <= 240);

-- Remove exclude_font from sign_config
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW sign_config_view;

ALTER TABLE iris.sign_config DROP COLUMN exclude_font;

CREATE VIEW sign_config_view AS
	SELECT name, face_width, face_height, border_horiz, border_vert,
	       pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
	       char_height, monochrome_foreground, monochrome_background,
	       cs.description AS color_scheme, default_font,
	       module_width, module_height
	FROM iris.sign_config
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

-- Remove override_font, override_foreground and override_background from dms
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();

ALTER TABLE iris._dms DROP COLUMN override_font;
ALTER TABLE iris._dms DROP COLUMN override_foreground;
ALTER TABLE iris._dms DROP COLUMN override_background;

CREATE VIEW iris.dms AS
    SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
           purpose, hidden, beacon, preset, sign_config, sign_detail,
           msg_user, msg_sched, msg_current, expire_time, status, stuck_pixels
    FROM iris._dms d
    JOIN iris.controller_io cio ON d.name = cio.name
    JOIN iris._device_preset p ON d.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
    $dms_insert$
BEGIN
    INSERT INTO iris.controller_io (name, resource_n, controller, pin)
         VALUES (NEW.name, 'dms', NEW.controller, NEW.pin);
    INSERT INTO iris._device_preset (name, preset)
         VALUES (NEW.name, NEW.preset);
    INSERT INTO iris._dms (
        name, geo_loc, notes, gps, static_graphic, purpose, hidden, beacon,
        sign_config, sign_detail, msg_user, msg_sched, msg_current,
        expire_time, status, stuck_pixels
    ) VALUES (
        NEW.name, NEW.geo_loc, NEW.notes, NEW.gps, NEW.static_graphic,
        NEW.purpose, NEW.hidden, NEW.beacon, NEW.sign_config, NEW.sign_detail,
        NEW.msg_user, NEW.msg_sched, NEW.msg_current, NEW.expire_time,
        NEW.status, NEW.stuck_pixels
    );
    RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
    $dms_update$
BEGIN
    UPDATE iris.controller_io
       SET controller = NEW.controller,
           pin = NEW.pin
     WHERE name = OLD.name;
    UPDATE iris._device_preset
       SET preset = NEW.preset
     WHERE name = OLD.name;
    UPDATE iris._dms
       SET geo_loc = NEW.geo_loc,
           notes = NEW.notes,
           gps = NEW.gps,
           static_graphic = NEW.static_graphic,
           purpose = NEW.purpose,
           hidden = NEW.hidden,
           beacon = NEW.beacon,
           sign_config = NEW.sign_config,
           sign_detail = NEW.sign_detail,
           msg_user = NEW.msg_user,
           msg_sched = NEW.msg_sched,
           msg_current = NEW.msg_current,
           expire_time = NEW.expire_time,
           status = NEW.status,
           stuck_pixels = NEW.stuck_pixels
     WHERE name = OLD.name;
    RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.controller_io_delete();

CREATE VIEW dms_view AS
    SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
           d.static_graphic, dp.description AS purpose, d.hidden, d.beacon,
           p.camera, p.preset_num, d.sign_config, d.sign_detail,
           default_font, msg_user, msg_sched, msg_current, expire_time,
           status, stuck_pixels,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street,
           l.cross_dir, l.landmark, l.lat, l.lon, l.corridor, l.location
    FROM iris.dms d
    LEFT JOIN iris.camera_preset p ON d.preset = p.name
    LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
    LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
    LEFT JOIN iris.sign_config sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
    SELECT d.name, msg_current, cc.description AS condition,
           fail_time IS NOT NULL AS failed, multi, beacon_enabled,
           msg_priority, iris.sign_msg_sources(source) AS sources,
           duration, expire_time
    FROM iris.dms d
    LEFT JOIN iris.controller c ON d.controller = c.name
    LEFT JOIN iris.condition cc ON c.condition = cc.id
    LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Remove DMS font selection system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_font_selection_enable';

-- Remove DMS page-on selection system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_page_on_selection_enable';

-- Remove DMS default line/page justification system attributes
DELETE FROM iris.system_attribute WHERE name = 'dms_default_justification_line';
DELETE FROM iris.system_attribute WHERE name = 'dms_default_justification_page';

-- Remove DMS default page time system attributes
DELETE FROM iris.system_attribute WHERE name = 'dms_page_on_default_secs';
DELETE FROM iris.system_attribute WHERE name = 'dms_page_off_default_secs';

-- Remove quick message store system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_quickmsg_store_enable';

-- Remove DMS message min pages system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_message_min_pages';

-- Remove DMS max lines system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_max_lines';

-- Remove DMS manufacturer enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_manufacturer_enable';

-- Remove DMS duration enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_duration_enable';

-- Replace quick_message with msg_pattern
DROP VIEW quick_message_view;
DROP VIEW dms_toll_zone_view;
DROP VIEW iris.quick_message_toll_zone;
DROP VIEW iris.quick_message_priced;
DROP VIEW iris.quick_message_open;
DROP VIEW iris.quick_message_closed;
DROP VIEW dms_action_view;
DROP VIEW lane_use_multi_view;
INSERT INTO iris.resource_type (name) VALUES ('msg_pattern');

UPDATE iris.privilege SET type_n = 'msg_pattern'
    WHERE type_n = 'quick_message';

CREATE TABLE iris.msg_pattern (
    name VARCHAR(20) PRIMARY KEY,
    sign_config VARCHAR(16) REFERENCES iris.sign_config,
    sign_group VARCHAR(20) REFERENCES iris.sign_group,
    multi VARCHAR(1024) NOT NULL
);

CREATE VIEW msg_pattern_view AS
    SELECT name, sign_config, sign_group, multi
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

INSERT INTO iris.msg_pattern
    (name, sign_config, sign_group, multi)
    SELECT name, sign_config, sign_group, multi
    FROM iris.quick_message;

ALTER TABLE iris.dms_action
    ADD COLUMN msg_pattern VARCHAR(20) REFERENCES iris.msg_pattern;
UPDATE iris.dms_action SET msg_pattern = quick_message;
ALTER TABLE iris.dms_action DROP COLUMN quick_message;

CREATE VIEW dms_action_view AS
    SELECT name, action_plan, sign_group, phase, msg_pattern, beacon_enabled,
           msg_priority
    FROM iris.dms_action;
GRANT SELECT ON dms_action_view TO PUBLIC;

ALTER TABLE iris.alert_message
    ADD COLUMN msg_pattern VARCHAR(20) REFERENCES iris.msg_pattern;
UPDATE iris.alert_message SET msg_pattern = quick_message;
ALTER TABLE iris.alert_message DROP COLUMN quick_message;

ALTER TABLE iris.lane_use_multi
    ADD COLUMN msg_pattern VARCHAR(20) REFERENCES iris.msg_pattern;
UPDATE iris.lane_use_multi SET msg_pattern = quick_message;
ALTER TABLE iris.lane_use_multi DROP COLUMN quick_message;

CREATE VIEW lane_use_multi_view AS
    SELECT name, indication, msg_num, msg_pattern
    FROM iris.lane_use_multi;
GRANT SELECT ON lane_use_multi_view TO PUBLIC;

DROP TABLE iris.quick_message;

CREATE VIEW iris.msg_pattern_priced AS
    SELECT name AS msg_pattern, 'priced'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzp,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.msg_pattern WHERE multi LIKE '%tzp%';

CREATE VIEW iris.msg_pattern_open AS
    SELECT name AS msg_pattern, 'open'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzo,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.msg_pattern WHERE multi LIKE '%tzo%';

CREATE VIEW iris.msg_pattern_closed AS
    SELECT name AS msg_pattern, 'closed'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzc,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.msg_pattern WHERE multi LIKE '%tzc%';

CREATE VIEW iris.msg_pattern_toll_zone AS
    SELECT msg_pattern, state, toll_zone
        FROM iris.msg_pattern_priced UNION ALL
    SELECT msg_pattern, state, toll_zone
        FROM iris.msg_pattern_open UNION ALL
    SELECT msg_pattern, state, toll_zone
        FROM iris.msg_pattern_closed;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, tz.state, toll_zone, action_plan, da.msg_pattern
    FROM dms_action_view da
    JOIN iris.dms_sign_group dsg
    ON da.sign_group = dsg.sign_group
    JOIN iris.msg_pattern mp
    ON da.msg_pattern = mp.name
    JOIN iris.msg_pattern_toll_zone tz
    ON da.msg_pattern = tz.msg_pattern;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

DELETE FROM iris.resource_type WHERE name = 'quick_message';

-- Delete unused sign_config records
DELETE FROM iris.sign_config WHERE name NOT IN (
    SELECT sign_config FROM iris._dms WHERE sign_config IS NOT NULL
    UNION ALL
    SELECT sign_config FROM iris.msg_pattern WHERE sign_config IS NOT NULL
    UNION ALL
    SELECT sign_config FROM iris.sign_message WHERE sign_config IS NOT NULL
);

-- Delete unused sign_detail records
DELETE FROM iris.sign_detail WHERE name NOT IN (
    SELECT sign_detail FROM iris._dms WHERE sign_detail IS NOT NULL
);

-- DROP msg_combining table
DROP VIEW sign_message_view;

ALTER TABLE iris.sign_message DROP COLUMN msg_combining;

DROP TABLE iris.msg_combining;

CREATE VIEW sign_message_view AS
    SELECT name, sign_config, incident, multi, beacon_enabled, msg_priority,
           iris.sign_msg_sources(source) AS sources, owner, duration
    FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

-- Update protocols to make one for NDOT beacons
UPDATE iris.comm_protocol SET description = 'NDOT Beacon'
    WHERE id = 24;

COMMIT;
