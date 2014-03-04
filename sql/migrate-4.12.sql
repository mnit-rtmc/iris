\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.12.0'
	WHERE name = 'database_version';

-- Split beacon_insert and beacon_delete out of beacon_update

DROP TRIGGER beacon_update_trig ON iris.beacon;
DROP FUNCTION iris.beacon_update();

CREATE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
	$beacon_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	    VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._beacon (name, geo_loc, notes, message, camera)
	    VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message, NEW.camera);
	RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_insert();

CREATE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
	$beacon_update$
BEGIN
	UPDATE iris._device_io SET controller = NEW.controller, pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._beacon
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       message = NEW.message,
	       camera = NEW.camera
	WHERE name = OLD.name;
	RETURN NEW;
END;
$beacon_update$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_update_trig
    INSTEAD OF UPDATE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_update();

CREATE FUNCTION iris.beacon_delete() RETURNS TRIGGER AS
	$beacon_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$beacon_delete$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_delete();

-- Update font constraint stuff to use triggers

ALTER TABLE iris.graphic DROP CONSTRAINT graphic_font_ck;
ALTER TABLE iris.glyph DROP CONSTRAINT glyph_bpp_ck;
ALTER TABLE iris.glyph DROP CONSTRAINT glyph_size_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_graphic_ck;

DROP FUNCTION graphic_bpp(VARCHAR(20));
DROP FUNCTION graphic_height(VARCHAR(20));
DROP FUNCTION graphic_width(VARCHAR(20));
DROP FUNCTION glyph_font(VARCHAR(20));
DROP FUNCTION font_height(VARCHAR(16));
DROP FUNCTION font_width(VARCHAR(16));
DROP FUNCTION font_graphic(VARCHAR(16));

ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_height_ck
	CHECK (height > 0);
ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_width_ck
	CHECK (width > 0);

CREATE FUNCTION iris.graphic_ck() RETURNS TRIGGER AS
	$graphic_ck$
DECLARE
	f_name VARCHAR(16);
	f_height INTEGER;
	f_width INTEGER;
BEGIN
	SELECT INTO f_name font FROM iris.glyph WHERE graphic = NEW.name;
	IF NOT FOUND THEN
		RETURN NEW;
	END IF;
	IF NEW.bpp != 1 THEN
		RAISE EXCEPTION 'bpp must be 1 for font glyph';
	END IF;
	SELECT height, width INTO f_height, f_width FROM iris.font
	                     WHERE name = f_name;
	IF f_height != NEW.height THEN
		RAISE EXCEPTION 'height does not match font';
	END IF;
	IF f_width > 0 AND f_width != NEW.width THEN
		RAISE EXCEPTION 'width does not match font';
	END IF;
	RETURN NEW;
END;
$graphic_ck$ LANGUAGE plpgsql;

CREATE TRIGGER graphic_ck_trig
	BEFORE INSERT OR UPDATE ON iris.graphic
	FOR EACH ROW EXECUTE PROCEDURE iris.graphic_ck();

ALTER TABLE iris.glyph
	ADD CONSTRAINT glyph_code_point_ck
	CHECK (code_point > 0);

CREATE FUNCTION iris.glyph_ck() RETURNS TRIGGER AS
	$glyph_ck$
DECLARE
	g_bpp INTEGER;
	f_height INTEGER;
	f_width INTEGER;
	g_height INTEGER;
	g_width INTEGER;
BEGIN
	SELECT bpp INTO g_bpp FROM iris.graphic WHERE name = NEW.graphic;
	IF g_bpp != 1 THEN
		RAISE EXCEPTION 'bpp must be 1 for font glyph';
	END IF;
	SELECT height, width INTO f_height, f_width FROM iris.font
	                     WHERE name = NEW.font;
	SELECT height, width INTO g_height, g_width FROM iris.graphic
	                     WHERE name = NEW.graphic;
	IF f_height != g_height THEN
		RAISE EXCEPTION 'height does not match font';
	END IF;
	IF f_width > 0 AND f_width != g_width THEN
		RAISE EXCEPTION 'width does not match font';
	END IF;
	RETURN NEW;
END;
$glyph_ck$ LANGUAGE plpgsql;

CREATE TRIGGER glyph_ck_trig
	BEFORE INSERT OR UPDATE ON iris.glyph
	FOR EACH ROW EXECUTE PROCEDURE iris.glyph_ck();

CREATE FUNCTION iris.font_ck() RETURNS TRIGGER AS
	$font_ck$
DECLARE
	f_graphic VARCHAR(20);
	g_height INTEGER;
	g_width INTEGER;
BEGIN
	SELECT graphic INTO f_graphic FROM iris.glyph WHERE font = NEW.name;
	IF NOT FOUND THEN
		RETURN NEW;
	END IF;
	SELECT height, width INTO g_height, g_width FROM iris.graphic
	                     WHERE name = f_graphic;
	IF NEW.height != g_height THEN
		RAISE EXCEPTION 'height does not match glyph';
	END IF;
	IF NEW.width > 0 AND NEW.width != g_width THEN
		RAISE EXCEPTION 'width does not match glyph';
	END IF;
	RETURN NEW;
END;
$font_ck$ LANGUAGE plpgsql;

CREATE TRIGGER font_ck_trig
	BEFORE INSERT OR UPDATE ON iris.font
	FOR EACH ROW EXECUTE PROCEDURE iris.font_ck();

-- Update r_node edge checks

ALTER TABLE iris.r_node DROP CONSTRAINT left_edge_ck;
ALTER TABLE iris.r_node DROP CONSTRAINT right_edge_ck;

DROP FUNCTION iris.r_node_left(INTEGER, INTEGER, BOOLEAN, INTEGER);
DROP FUNCTION iris.r_node_right(INTEGER, INTEGER, BOOLEAN, INTEGER);

CREATE FUNCTION iris.r_node_left(INTEGER, INTEGER, BOOLEAN, INTEGER)
	RETURNS INTEGER AS $r_node_left$
DECLARE
	node_type ALIAS FOR $1;
	lanes ALIAS FOR $2;
	attach_side ALIAS FOR $3;
	shift ALIAS FOR $4;
BEGIN
	IF attach_side = TRUE THEN
		RETURN shift;
	END IF;
	IF node_type = 0 THEN
		RETURN shift - lanes;
	END IF;
	RETURN shift;
END;
$r_node_left$ LANGUAGE plpgsql;

CREATE FUNCTION iris.r_node_right(INTEGER, INTEGER, BOOLEAN, INTEGER)
	RETURNS INTEGER AS $r_node_right$
DECLARE
	node_type ALIAS FOR $1;
	lanes ALIAS FOR $2;
	attach_side ALIAS FOR $3;
	shift ALIAS FOR $4;
BEGIN
	IF attach_side = FALSE THEN
		RETURN shift;
	END IF;
	IF node_type = 0 THEN
		RETURN shift + lanes;
	END IF;
	RETURN shift;
END;
$r_node_right$ LANGUAGE plpgsql;

ALTER TABLE iris.r_node ADD CONSTRAINT left_edge_ck
	CHECK (iris.r_node_left(node_type, lanes, attach_side, shift) >= 1);
ALTER TABLE iris.r_node ADD CONSTRAINT right_edge_ck
	CHECK (iris.r_node_right(node_type, lanes, attach_side, shift) <= 9);

-- Update detector_label function

DROP VIEW detector_event_view;
DROP VIEW detector_view;
DROP VIEW detector_label_view;

DROP FUNCTION detector_label(TEXT, VARCHAR, TEXT, VARCHAR, TEXT, SMALLINT,
	SMALLINT, BOOLEAN);

CREATE FUNCTION iris.detector_label(VARCHAR(6), VARCHAR(4), VARCHAR(6),
	VARCHAR(4), VARCHAR(2), SMALLINT, SMALLINT, BOOLEAN)
	RETURNS TEXT AS $detector_label$
DECLARE
	rd ALIAS FOR $1;
	rdir ALIAS FOR $2;
	xst ALIAS FOR $3;
	xdir ALIAS FOR $4;
	xmod ALIAS FOR $5;
	l_type ALIAS FOR $6;
	lane_number ALIAS FOR $7;
	abandoned ALIAS FOR $8;
	xmd VARCHAR(2);
	ltyp VARCHAR(2);
	lnum VARCHAR(2);
	suffix VARCHAR(5);
BEGIN
	IF rd IS NULL OR xst IS NULL THEN
		RETURN 'FUTURE';
	END IF;
	SELECT dcode INTO ltyp FROM lane_type_view WHERE id = l_type;
	lnum = '';
	IF lane_number > 0 THEN
		lnum = TO_CHAR(lane_number, 'FM9');
	END IF;
	xmd = '';
	IF xmod != '@' THEN
		xmd = xmod;
	END IF;
	suffix = '';
	IF abandoned THEN
		suffix = '-ABND';
	END IF;
	RETURN rd || '/' || xdir || xmd || xst || rdir || ltyp || lnum ||
	       suffix;
END;
$detector_label$ LANGUAGE plpgsql;

CREATE VIEW detector_label_view AS
	SELECT d.name AS det_id,
	iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id,
	d.pin, iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label,
	rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	l.cross_dir, d.lane_number, d.field_length, ln.description AS lane_type,
	d.abandoned, d.force_fail, df.fail_reason, c.active, d.fake, d.notes
	FROM (iris.detector d
	LEFT OUTER JOIN detector_fail_view df
		ON d.name = df.device_id AND force_fail = 't')
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN iris.controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

-- Add beacon actions

CREATE TABLE iris.beacon_action (
	name VARCHAR(20) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	beacon VARCHAR(10) NOT NULL REFERENCES iris._beacon,
	phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase
);

INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
                            priv_d)
       VALUES ('prv_ba1', 'plan_tab', 'beacon_action(/.*)?', true, false,
               false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
                            priv_d)
       VALUES ('prv_ba2', 'policy_admin', 'beacon_action(/.*)?', true, false,
               false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
                            priv_d)
       VALUES ('prv_ba3', 'policy_admin', 'beacon_action/.*', false, true,
               true, true);
