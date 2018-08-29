\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.78.0', '4.79.0');

INSERT INTO event.tag_type (id, description) VALUES (2, 'IAG');
INSERT INTO event.tag_type (id, description) VALUES (3, 'ASTMv6');
UPDATE event.tag_read_event SET tag_type = 3 WHERE tag_type = 0;
UPDATE event.tag_type SET description = 'Unknown' WHERE id = 0;

DROP VIEW tag_read_event_view;
DROP FUNCTION event.tag_read_event_view_update();
DROP VIEW tag_reader_view;
DROP VIEW iris.tag_reader;
DROP FUNCTION iris.tag_reader_insert();
DROP FUNCTION iris.tag_reader_update();
DROP FUNCTION iris.tag_reader_delete();

-- Add tag reader configuration stuff
ALTER TABLE iris._tag_reader ADD COLUMN downlink_freq_khz INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN uplink_freq_khz INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN sego_atten_downlink_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN sego_atten_uplink_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN sego_data_detect_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN sego_seen_count INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN sego_unique_count INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN iag_atten_downlink_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN iag_atten_uplink_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN iag_data_detect_db INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN iag_seen_count INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN iag_unique_count INTEGER;
ALTER TABLE iris._tag_reader ADD COLUMN line_loss_db INTEGER;

CREATE VIEW iris.tag_reader AS
	SELECT t.name, geo_loc, controller, pin, notes, toll_zone,
	       downlink_freq_khz, uplink_freq_khz, sego_atten_downlink_db,
	       sego_atten_uplink_db, sego_data_detect_db, sego_seen_count,
	       sego_unique_count, iag_atten_downlink_db, iag_atten_uplink_db,
	       iag_data_detect_db, iag_seen_count, iag_unique_count,line_loss_db
	FROM iris._tag_reader t JOIN iris._device_io d ON t.name = d.name;

CREATE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
	$tag_reader_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._tag_reader (name, geo_loc, notes, toll_zone,
	                              downlink_freq_khz, uplink_freq_khz,
	                              sego_atten_downlink_db,
	                              sego_atten_uplink_db, sego_data_detect_db,
	                              sego_seen_count, sego_unique_count,
	                              iag_atten_downlink_db,
	                              iag_atten_uplink_db, iag_data_detect_db,
	                              iag_seen_count, iag_unique_count,
	                              line_loss_db)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone,
	             NEW.downlink_freq_khz, NEW.uplink_freq_khz,
	             NEW.sego_atten_downlink_db, NEW.sego_atten_uplink_db,
	             NEW.sego_data_detect_db, NEW.sego_seen_count,
	             NEW.sego_unique_count, NEW.iag_atten_downlink_db,
	             NEW.iag_atten_uplink_db, NEW.iag_data_detect_db,
	             NEW.iag_seen_count, NEW.iag_unique_count, NEW.line_loss_db);
	RETURN NEW;
END;
$tag_reader_insert$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_insert_trig
    INSTEAD OF INSERT ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_insert();

CREATE FUNCTION iris.tag_reader_update() RETURNS TRIGGER AS
	$tag_reader_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._tag_reader
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       toll_zone = NEW.toll_zone,
	       downlink_freq_khz = NEW.downlink_freq_khz,
	       uplink_freq_khz = NEW.uplink_freq_khz,
	       sego_atten_downlink_db = NEW.sego_atten_downlink_db,
	       sego_atten_uplink_db = NEW.sego_atten_uplink_db,
	       sego_data_detect_db = NEW.sego_data_detect_db,
	       sego_seen_count = NEW.sego_seen_count,
	       sego_unique_count = NEW.sego_unique_count,
	       iag_atten_downlink_db = NEW.iag_atten_downlink_db,
	       iag_atten_uplink_db = NEW.iag_atten_uplink_db,
	       iag_data_detect_db = NEW.iag_data_detect_db,
	       iag_seen_count = NEW.iag_seen_count,
	       iag_unique_count = NEW.iag_unique_count,
	       line_loss_db = NEW.line_loss_db
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$tag_reader_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_update_trig
    INSTEAD OF UPDATE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_update();

CREATE FUNCTION iris.tag_reader_delete() RETURNS TRIGGER AS
	$tag_reader_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$tag_reader_delete$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_delete_trig
    INSTEAD OF DELETE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.tag_reader_delete();

CREATE VIEW tag_reader_view AS
	SELECT t.name, t.geo_loc, location, controller, pin, notes, toll_zone,
	       downlink_freq_khz, uplink_freq_khz,
	       sego_atten_downlink_db, sego_atten_uplink_db, sego_data_detect_db,
	       sego_seen_count, sego_unique_count,
	       iag_atten_downlink_db, iag_atten_uplink_db, iag_data_detect_db,
	       iag_seen_count, iag_unique_count, line_loss_db
	FROM iris.tag_reader t
	LEFT JOIN geo_loc_view l ON t.geo_loc = l.name;
GRANT SELECT ON tag_reader_view TO PUBLIC;

CREATE VIEW tag_read_event_view AS
	SELECT event_id, event_date, event_description.description,
	       tag_type.description AS tag_type, agency, tag_id, tag_reader,
	       toll_zone, tollway, hov, trip_id
	FROM event.tag_read_event
	JOIN event.event_description
	ON   tag_read_event.event_desc_id = event_description.event_desc_id
	JOIN event.tag_type
	ON   tag_read_event.tag_type = tag_type.id
	JOIN iris._tag_reader
	ON   tag_read_event.tag_reader = _tag_reader.name
	LEFT JOIN iris.toll_zone
	ON        _tag_reader.toll_zone = toll_zone.name;
GRANT SELECT ON tag_read_event_view TO PUBLIC;

CREATE FUNCTION event.tag_read_event_view_update() RETURNS TRIGGER AS
	$tag_read_event_view_update$
BEGIN
	UPDATE event.tag_read_event
	   SET trip_id = NEW.trip_id
	 WHERE event_id = OLD.event_id;
	RETURN NEW;
END;
$tag_read_event_view_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_read_event_view_update_trig
    INSTEAD OF UPDATE ON tag_read_event_view
    FOR EACH ROW EXECUTE PROCEDURE event.tag_read_event_view_update();


-- Replace DMS deploy_time with expire_time
DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

ALTER TABLE iris._dms ADD COLUMN expire_time timestamp WITH time zone;
ALTER TABLE iris._dms DROP COLUMN deploy_time;

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       beacon, preset, sign_config, override_font, msg_sched,
	       msg_current, expire_time
	FROM iris._dms dms
	JOIN iris._device_io d ON dms.name = d.name
	JOIN iris._device_preset p ON dms.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
	$dms_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._device_preset (name, preset)
	     VALUES (NEW.name, NEW.preset);
	INSERT INTO iris._dms (name, geo_loc, notes, gps, static_graphic,
	                       beacon, sign_config, override_font, msg_sched,
	                       msg_current, expire_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.gps,
	             NEW.static_graphic, NEW.beacon, NEW.sign_config,
	             NEW.override_font, NEW.msg_sched, NEW.msg_current,
	             NEW.expire_time);
	RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
	$dms_update$
BEGIN
	UPDATE iris._device_io
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
	       beacon = NEW.beacon,
	       sign_config = NEW.sign_config,
	       override_font = NEW.override_font,
	       msg_sched = NEW.msg_sched,
	       msg_current = NEW.msg_current,
	       expire_time = NEW.expire_time
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE FUNCTION iris.dms_delete() RETURNS TRIGGER AS
	$dms_delete$
BEGIN
	DELETE FROM iris._device_preset WHERE name = OLD.name;
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$dms_delete$ LANGUAGE plpgsql;

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_delete();

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, d.beacon, p.camera, p.preset_num, d.sign_config,
	       default_font, override_font, msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition, multi,
	       beacon_enabled, prefix_page, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, expire_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Increase sign_group name to VARCHAR(20)
DROP VIEW sign_text_view;
DROP VIEW quick_message_view;
DROP VIEW dms_toll_zone_view;
DROP VIEW dms_action_view;

ALTER TABLE iris.sign_group ALTER COLUMN name TYPE VARCHAR(20);
ALTER TABLE iris.quick_message ALTER COLUMN sign_group TYPE VARCHAR(20);
ALTER TABLE iris.dms_sign_group ALTER COLUMN sign_group TYPE VARCHAR(20);
ALTER TABLE iris.sign_text ALTER COLUMN sign_group TYPE VARCHAR(20);
ALTER TABLE iris.dms_action ALTER COLUMN sign_group TYPE VARCHAR(20);
ALTER TABLE iris.dms_sign_group ALTER COLUMN name TYPE VARCHAR(42);

CREATE VIEW dms_action_view AS
	SELECT name, action_plan, sign_group, phase, quick_message,
	       beacon_enabled, msg_priority
	FROM iris.dms_action;
GRANT SELECT ON dms_action_view TO PUBLIC;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, state, toll_zone, action_plan, dms_action_view.quick_message
    FROM dms_action_view
    JOIN iris.dms_sign_group
    ON dms_action_view.sign_group = dms_sign_group.sign_group
    JOIN iris.quick_message
    ON dms_action_view.quick_message = quick_message.name
    JOIN iris.quick_message_toll_zone
    ON dms_action_view.quick_message = quick_message_toll_zone.quick_message;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

CREATE VIEW quick_message_view AS
	SELECT name, sign_group, sign_config, prefix_page, multi
	FROM iris.quick_message;
GRANT SELECT ON quick_message_view TO PUBLIC;

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, multi, rank
	FROM iris.dms_sign_group dsg
	JOIN iris.sign_group sg ON dsg.sign_group = sg.name
	JOIN iris.sign_text st ON sg.name = st.sign_group;
GRANT SELECT ON sign_text_view TO PUBLIC;

COMMIT;
