\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.79.0', '4.80.0');

ALTER TABLE iris.font DROP CONSTRAINT font_height_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_width_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_line_sp_ck;
ALTER TABLE iris.font DROP CONSTRAINT font_char_sp_ck;

ALTER TABLE iris.font
	ADD CONSTRAINT font_number_ck
	CHECK (f_number > 0 AND f_number <= 255);
ALTER TABLE iris.font
	ADD CONSTRAINT font_height_ck
	CHECK (height > 0 AND height <= 30);
ALTER TABLE iris.font
	ADD CONSTRAINT font_width_ck
	CHECK (width >= 0 AND width <= 12);
ALTER TABLE iris.font
	ADD CONSTRAINT font_line_sp_ck
	CHECK (line_spacing >= 0 AND line_spacing <= 9);
ALTER TABLE iris.font
	ADD CONSTRAINT font_char_sp_ck
	CHECK (char_spacing >= 0 AND char_spacing <= 6);

ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_number_ck
	CHECK (g_number > 0 AND g_number <= 999);

-- Drop old stuff
DROP VIEW glyph_view;
DROP TRIGGER font_ck_trig ON iris.font;
DROP FUNCTION iris.font_ck();
DROP TRIGGER glyph_ck_trig ON iris.glyph;
DROP FUNCTION iris.glyph_ck();
DROP TRIGGER graphic_ck_trig ON iris.graphic;
DROP FUNCTION iris.graphic_ck();

-- Copy width from graphic to glyph
ALTER TABLE iris.glyph ADD COLUMN width INTEGER;
UPDATE iris.glyph
   SET width = g.width
  FROM iris.graphic g
 WHERE glyph.graphic = g.name;
ALTER TABLE iris.glyph ALTER COLUMN width SET NOT NULL;

-- Copy pixels from graphic to glyph
ALTER TABLE iris.glyph ADD COLUMN pixels VARCHAR(128);
UPDATE iris.glyph
   SET pixels = g.pixels
  FROM iris.graphic g
 WHERE glyph.graphic = g.name;
ALTER TABLE iris.glyph ALTER COLUMN pixels SET NOT NULL;

-- Remove graphic column from glyph
ALTER TABLE iris.glyph DROP COLUMN graphic;

-- Remove glyph graphics
DELETE FROM iris.graphic WHERE g_number IS NULL;
ALTER TABLE iris.graphic ALTER COLUMN g_number SET NOT NULL;

-- Create new glyph_ck trigger
CREATE FUNCTION iris.glyph_ck() RETURNS TRIGGER AS
	$glyph_ck$
DECLARE
	f_width INTEGER;
BEGIN
	SELECT width INTO f_width FROM iris.font WHERE name = NEW.font;
	IF f_width > 0 AND f_width != NEW.width THEN
		RAISE EXCEPTION 'width does not match font';
	END IF;
	RETURN NEW;
END;
$glyph_ck$ LANGUAGE plpgsql;

CREATE TRIGGER glyph_ck_trig
	BEFORE INSERT OR UPDATE ON iris.glyph
	FOR EACH ROW EXECUTE PROCEDURE iris.glyph_ck();

-- Create new font_ck trigger
CREATE FUNCTION iris.font_ck() RETURNS TRIGGER AS
	$font_ck$
DECLARE
	g_width INTEGER;
BEGIN
	IF NEW.width > 0 THEN
		SELECT width INTO g_width FROM iris.glyph WHERE font = NEW.name;
		IF FOUND AND NEW.width != g_width THEN
			RAISE EXCEPTION 'width does not match glyph';
		END IF;
	END IF;
	RETURN NEW;
END;
$font_ck$ LANGUAGE plpgsql;

CREATE TRIGGER font_ck_trig
	BEFORE UPDATE ON iris.font
	FOR EACH ROW EXECUTE PROCEDURE iris.font_ck();

-- Add width and pixels to glyph_view
CREATE VIEW glyph_view AS
	SELECT name, font, code_point, width, pixels
	FROM iris.glyph;
GRANT SELECT ON glyph_view TO PUBLIC;

-- Add tag_reader_sync_mode LUT
CREATE TABLE iris.tag_reader_sync_mode (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

COPY iris.tag_reader_sync_mode (id, description) FROM stdin;
0	slave
1	master
2	GPS secondary
3	GPS primary
\.

-- Add sync_mode and slave_select_count to tag_reader
ALTER TABLE iris._tag_reader ADD COLUMN sync_mode INTEGER
	REFERENCES iris.tag_reader_sync_mode;
ALTER TABLE iris._tag_reader ADD COLUMN slave_select_count INTEGER;

-- Drop old tag reader view stuff
DROP VIEW tag_reader_view;
DROP VIEW iris.tag_reader;
DROP FUNCTION iris.tag_reader_insert();
DROP FUNCTION iris.tag_reader_update();
DROP FUNCTION iris.tag_reader_delete();

CREATE VIEW iris.tag_reader AS
	SELECT t.name, geo_loc, controller, pin, notes, toll_zone,
	       downlink_freq_khz, uplink_freq_khz, sego_atten_downlink_db,
	       sego_atten_uplink_db, sego_data_detect_db, sego_seen_count,
	       sego_unique_count, iag_atten_downlink_db, iag_atten_uplink_db,
	       iag_data_detect_db, iag_seen_count, iag_unique_count,
	       line_loss_db, sync_mode, slave_select_count
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
	                              line_loss_db, sync_mode,
	                              slave_select_count)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone,
	             NEW.downlink_freq_khz, NEW.uplink_freq_khz,
	             NEW.sego_atten_downlink_db, NEW.sego_atten_uplink_db,
	             NEW.sego_data_detect_db, NEW.sego_seen_count,
	             NEW.sego_unique_count, NEW.iag_atten_downlink_db,
	             NEW.iag_atten_uplink_db, NEW.iag_data_detect_db,
	             NEW.iag_seen_count, NEW.iag_unique_count, NEW.line_loss_db,
	             NEW.sync_mode, NEW.slave_select_count);
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
	       line_loss_db = NEW.line_loss_db,
	       sync_mode = NEW.sync_mode,
	       slave_select_count = NEW.slave_select_count
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
	       iag_seen_count, iag_unique_count, line_loss_db,
	       m.description AS sync_mode, slave_select_count
	FROM iris.tag_reader t
	LEFT JOIN geo_loc_view l ON t.geo_loc = l.name
	LEFT JOIN iris.tag_reader_sync_mode m ON t.sync_mode = m.id;
GRANT SELECT ON tag_reader_view TO PUBLIC;

COMMIT;
