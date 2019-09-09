\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.0.0', '5.1.0');

-- Remove cleared column from view
DROP VIEW inc_descriptor_view;
CREATE VIEW inc_descriptor_view AS
	SELECT id.name, ed.description AS event_description, detail,
	       lt.description AS lane_type, multi, abbrev
	FROM iris.inc_descriptor id
	JOIN event.event_description ed ON id.event_desc_id = ed.event_desc_id
	LEFT JOIN iris.lane_type lt ON id.lane_type = lt.id;
GRANT SELECT ON inc_descriptor_view TO PUBLIC;

-- Drop cleared column
ALTER TABLE iris.inc_descriptor DROP COLUMN cleared;

-- Remove 'lanes blocked' (4) and 'lanes affected' (12) impacts
UPDATE iris.inc_impact SET description = 'both shoulders blocked' WHERE id = 4;
UPDATE iris.inc_advice SET impact = 4 WHERE impact = 5;
UPDATE iris.inc_impact SET description = 'left shoulder blocked' WHERE id = 5;
UPDATE iris.inc_advice SET impact = 5 WHERE impact = 6;
UPDATE iris.inc_impact SET description = 'right shoulder blocked' WHERE id = 6;
UPDATE iris.inc_advice SET impact = 6 WHERE impact = 7;
UPDATE iris.inc_impact SET description = 'all lanes affected' WHERE id = 7;
UPDATE iris.inc_advice SET impact = 7 WHERE impact = 8;
UPDATE iris.inc_impact SET description = 'left lanes affected' WHERE id = 8;
UPDATE iris.inc_advice SET impact = 8 WHERE impact = 9;
UPDATE iris.inc_impact SET description = 'right lanes affected' WHERE id = 9;
UPDATE iris.inc_advice SET impact = 9 WHERE impact = 10;
UPDATE iris.inc_impact SET description = 'center lanes affected' WHERE id = 10;
UPDATE iris.inc_advice SET impact = 10 WHERE impact = 11;
UPDATE iris.inc_impact SET description = 'both shoulders affected' WHERE id = 11;
UPDATE iris.inc_advice SET impact = 11 WHERE impact = 13;
UPDATE iris.inc_impact SET description = 'left shoulder affected' WHERE id = 12;
UPDATE iris.inc_advice SET impact = 12 WHERE impact = 14;
UPDATE iris.inc_impact SET description = 'right shoulder affected' WHERE id = 13;
UPDATE iris.inc_advice SET impact = 13 WHERE impact = 15;
UPDATE iris.inc_impact SET description = 'all free flowing' WHERE id = 14;
UPDATE iris.inc_advice SET impact = 14 WHERE impact = 16;
DELETE FROM iris.inc_impact WHERE id > 14;

-- Rearrange columns in incident advice view
DROP VIEW inc_advice_view;
CREATE VIEW inc_advice_view AS
	SELECT a.name, imp.description AS impact, lt.description AS lane_type,
	       rng.description AS range, impacted_lanes, open_lanes, cleared,
	       multi, abbrev
	FROM iris.inc_advice a
	LEFT JOIN iris.inc_impact imp ON a.impact = imp.id
	LEFT JOIN iris.inc_range rng ON a.range = rng.id
	LEFT JOIN iris.lane_type lt ON a.lane_type = lt.id;
GRANT SELECT ON inc_advice_view TO PUBLIC;

-- Add device purpose
CREATE TABLE iris.device_purpose (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL UNIQUE
);

COPY iris.device_purpose (id, description) FROM stdin;
0	general
1	wayfinding
2	tolling
3	parking
4	travel time
5	safety
\.

ALTER TABLE iris._dms
	ADD COLUMN purpose INTEGER REFERENCES iris.device_purpose;
UPDATE iris._dms SET purpose = 0;
ALTER TABLE iris._dms
	ALTER COLUMN purpose SET NOT NULL;

DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       purpose, beacon, preset, sign_config, sign_detail,
	       override_font, override_foreground, override_background,
	       msg_sched, msg_current, expire_time
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
	                       purpose, beacon, sign_config, sign_detail,
	                       override_font, override_foreground,
	                       override_background, msg_sched, msg_current,
	                       expire_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.gps,
	             NEW.static_graphic, NEW.purpose, NEW.beacon,
	             NEW.sign_config, NEW.sign_detail, NEW.override_font,
	             NEW.override_foreground, NEW.override_background,
	             NEW.msg_sched, NEW.msg_current, NEW.expire_time);
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
	       purpose = NEW.purpose,
	       beacon = NEW.beacon,
	       sign_config = NEW.sign_config,
	       sign_detail = NEW.sign_detail,
	       override_font = NEW.override_font,
	       override_foreground = NEW.override_foreground,
	       override_background = NEW.override_background,
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
	       d.static_graphic, dp.description AS purpose, d.beacon, p.camera,
	       p.preset_num, d.sign_config, d.sign_detail, default_font,
	       override_font, override_foreground, override_background,
	       msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
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

COMMIT;
