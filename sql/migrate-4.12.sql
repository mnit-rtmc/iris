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
