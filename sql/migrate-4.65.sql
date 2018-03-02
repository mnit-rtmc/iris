\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.64.0', '4.65.0');

-- Drop old views
DROP VIEW controller_report;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

-- Add columns to dms table
ALTER TABLE iris._dms ADD COLUMN msg_sched VARCHAR(20)
	REFERENCES iris.sign_message;
ALTER TABLE iris._dms ADD COLUMN msg_current VARCHAR(20)
	REFERENCES iris.sign_message;
ALTER TABLE iris._dms ADD COLUMN deploy_time timestamp WITH time zone;
UPDATE iris._dms SET deploy_time = now();
ALTER TABLE iris._dms ALTER COLUMN deploy_time SET NOT NULL;

-- Create iris.dms view
CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, beacon, preset,
	       aws_allowed, aws_controlled, sign_config, default_font,
	       msg_sched, msg_current, deploy_time
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
	INSERT INTO iris._dms (name, geo_loc, notes, beacon, aws_allowed,
	                       aws_controlled, sign_config, default_font,
	                       msg_sched, msg_current, deploy_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.beacon,
	             NEW.aws_allowed, NEW.aws_controlled, NEW.sign_config,
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
	       beacon = NEW.beacon,
	       aws_allowed = NEW.aws_allowed,
	       aws_controlled = NEW.aws_controlled,
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

-- Create geo_location function
CREATE FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT)
	RETURNS TEXT AS $geo_location$
DECLARE
	roadway ALIAS FOR $1;
	road_dir ALIAS FOR $2;
	cross_mod ALIAS FOR $3;
	cross_street ALIAS FOR $4;
	cross_dir ALIAS FOR $5;
	landmark ALIAS FOR $6;
	res TEXT;
BEGIN
	res = trim(roadway || ' ' || road_dir);
	IF char_length(cross_street) > 0 THEN
		RETURN concat(res || ' ', cross_mod || ' ', cross_street,
		              ' ' || cross_dir);
	ELSIF char_length(landmark) > 0 THEN
		RETURN concat(res || ' ', '(' || landmark || ')');
	ELSE
		RETURN res;
	END IF;
END;
$geo_location$ LANGUAGE plpgsql;

-- Create sign_msg_source look-up table
CREATE TABLE iris.sign_msg_source (
	bit INTEGER PRIMARY KEY,
	source VARCHAR(16) NOT NULL
);
ALTER TABLE iris.sign_msg_source ADD CONSTRAINT msg_source_bit_ck
	CHECK (bit >= 0 AND bit < 32);

-- Create function to get sign msg sources from bit flags
CREATE FUNCTION iris.sign_msg_sources(INTEGER) RETURNS TEXT
	AS $sign_msg_sources$
DECLARE
	src ALIAS FOR $1;
	res TEXT;
	ms RECORD;
	b INTEGER;
BEGIN
	res = '';
	FOR ms IN SELECT bit, source FROM iris.sign_msg_source ORDER BY bit LOOP
		b = 1 << ms.bit;
		IF (src & b) = b THEN
			IF char_length(res) > 0 THEN
				res = res || ', ' || ms.source;
			ELSE
				res = ms.source;
			END IF;
		END IF;
	END LOOP;
	RETURN res;
END;
$sign_msg_sources$ LANGUAGE plpgsql;

-- Replace geo_loc_view
CREATE OR REPLACE VIEW geo_loc_view AS
	SELECT l.name, r.abbrev AS rd, l.roadway, r_dir.direction AS road_dir,
	       r_dir.dir AS rdir, m.modifier AS cross_mod, m.mod AS xmod,
	       c.abbrev as xst, l.cross_street, c_dir.direction AS cross_dir,
	       l.lat, l.lon, l.landmark,
	       iris.geo_location(l.roadway, r_dir.direction, m.modifier,
	       l.cross_street, c_dir.direction, l.landmark) AS location
	FROM iris.geo_loc l
	LEFT JOIN iris.road r ON l.roadway = r.name
	LEFT JOIN iris.road_modifier m ON l.cross_mod = m.id
	LEFT JOIN iris.road c ON l.cross_street = c.name
	LEFT JOIN iris.direction r_dir ON l.road_dir = r_dir.id
	LEFT JOIN iris.direction c_dir ON l.cross_dir = c_dir.id;
GRANT SELECT ON geo_loc_view TO PUBLIC;

-- Add dms_view back
CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.beacon,
	       p.camera, p.preset_num, d.aws_allowed, d.aws_controlled,
	       d.sign_config, COALESCE(d.default_font, sc.default_font)
	       AS default_font, msg_sched, msg_current, deploy_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

-- Create dms_message_view
CREATE VIEW dms_message_view AS
	SELECT d.name, multi, beacon_enabled, iris.sign_msg_sources(source)
	       AS sources, duration, deploy_time, owner
	FROM iris.dms d
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Create controller_report
CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, l.landmark, cab.geo_loc,
	       l.location, cab.style AS "type", d.name AS device, d.pin,
	       d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

-- Populate sign_msg_source table
COPY iris.sign_msg_source (bit, source) FROM stdin;
0	blank
1	operator
2	schedule
3	tolling
4	gate arm
5	lcs
6	aws
7	external
8	travel time
9	incident
10	slow warning
11	speed advisory
12	parking
\.

COMMIT;
