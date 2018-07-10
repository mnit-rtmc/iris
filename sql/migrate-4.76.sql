\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.75.0', '4.76.0');

CREATE VIEW graphic_view AS
	SELECT name, g_number, bpp, height, width, pixels
	FROM iris.graphic;
GRANT SELECT ON graphic_view TO PUBLIC;

CREATE VIEW font_view AS
	SELECT name, f_number, height, width, line_spacing, char_spacing,
	       version_id
	FROM iris.font;
GRANT SELECT ON font_view TO PUBLIC;

CREATE VIEW glyph_view AS
	SELECT name, font, code_point, graphic
	FROM iris.glyph;
GRANT SELECT ON glyph_view TO PUBLIC;

INSERT INTO iris.system_attribute (name, value)
	VALUES ('dms_gps_jitter_m', '100');

SELECT name, controller, pin, poll_datetime AS latest_poll,
       sample_datetime AS latest_sample, sample_lat AS lat, sample_lon AS lon
	INTO TEMP TABLE gps_temp FROM iris.gps;

DELETE FROM iris.gps;
DROP VIEW iris.gps;
DROP TABLE iris._gps;
DROP FUNCTION iris.gps_insert();
DROP FUNCTION iris.gps_update();
DROP FUNCTION iris.gps_delete();

CREATE TABLE iris._gps (
	name VARCHAR(20) PRIMARY KEY,
	notes VARCHAR(32),
	latest_poll timestamp WITH time zone,
	latest_sample timestamp WITH time zone,
	lat double precision,
	lon double precision
);

ALTER TABLE iris._gps ADD CONSTRAINT _gps_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.gps AS
	SELECT g.name, d.controller, d.pin, g.notes, g.latest_poll,
               g.latest_sample, g.lat, g.lon
	FROM iris._gps g
	JOIN iris._device_io d ON g.name = d.name;

CREATE FUNCTION iris.gps_insert() RETURNS TRIGGER AS
	$gps_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._gps (name, notes, latest_poll, latest_sample, lat,lon)
	     VALUES (NEW.name, NEW.notes, NEW.latest_poll, NEW.latest_sample,
                     NEW.lat, NEW.lon);
	RETURN NEW;
END;
$gps_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gps_insert_trig
	INSTEAD OF INSERT ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_insert();

CREATE FUNCTION iris.gps_update() RETURNS TRIGGER AS
	$gps_update$
BEGIN
        UPDATE iris._device_io
           SET controller = NEW.controller,
               pin = NEW.pin
         WHERE name = OLD.name;
	UPDATE iris._gps
	   SET notes = NEW.notes,
               latest_poll = NEW.latest_poll,
	       latest_sample = NEW.latest_sample,
	       lat = NEW.lat,
	       lon = NEW.lon
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$gps_update$ LANGUAGE plpgsql;

CREATE TRIGGER gps_update_trig
	INSTEAD OF UPDATE ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_update();

CREATE FUNCTION iris.gps_delete() RETURNS TRIGGER AS
	$gps_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$gps_delete$ LANGUAGE plpgsql;

CREATE TRIGGER gps_delete_trig
	INSTEAD OF DELETE ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_delete();

INSERT INTO iris.gps (name, controller, pin, latest_poll, latest_sample,lat,lon)
	(SELECT name, controller, pin, latest_poll, latest_sample, lat, lon
	 FROM gps_temp);

UPDATE iris.gps SET lat = null, lon = null
	WHERE lat = 0.0 OR lon = 0.0;

DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

ALTER TABLE iris._dms ADD COLUMN gps VARCHAR(20) REFERENCES iris._gps;

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       beacon, preset, sign_config, default_font, msg_sched,
	       msg_current, deploy_time
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
	                       beacon, sign_config, default_font, msg_sched,
	                       msg_current, deploy_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.gps,
	             NEW.static_graphic, NEW.beacon, NEW.sign_config,
	             NEW.default_font, NEW.msg_sched, NEW.msg_current,
	             NEW.deploy_time);
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
	       default_font = NEW.default_font,
	       msg_sched = NEW.msg_sched,
	       msg_current = NEW.msg_current,
	       deploy_time = NEW.deploy_time
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
	       COALESCE(d.default_font, sc.default_font) AS default_font,
	       msg_sched, msg_current, deploy_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
	SELECT d.name, cc.description AS condition, multi, beacon_enabled,
	       prefix_page, msg_priority, iris.sign_msg_sources(source)
	       AS sources, duration, deploy_time, owner
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

COMMIT;
