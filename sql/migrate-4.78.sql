\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.77.0', '4.78.0');

DROP VIEW dms_message_view;
CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition, multi,
	       beacon_enabled, prefix_page, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, deploy_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

ALTER TABLE iris._dms ALTER COLUMN msg_current DROP NOT NULL;

DROP VIEW graphic_view;
DROP FUNCTION iris.graphic_ck() CASCADE;
DROP FUNCTION iris.glyph_ck() CASCADE;
ALTER TABLE iris.graphic DROP CONSTRAINT graphic_bpp_ck;
ALTER TABLE iris.graphic
	ADD COLUMN color_scheme INTEGER REFERENCES iris.color_scheme;
UPDATE iris.graphic SET color_scheme = 1;
UPDATE iris.graphic SET color_scheme = 2 WHERE bpp = 8;
UPDATE iris.graphic SET color_scheme = 3 WHERE bpp = 4;
UPDATE iris.graphic SET color_scheme = 4 WHERE bpp = 24;
ALTER TABLE iris.graphic ALTER COLUMN color_scheme SET NOT NULL;
ALTER TABLE iris.graphic DROP COLUMN bpp;
ALTER TABLE iris.graphic ADD COLUMN transparent_color INTEGER;
UPDATE iris.graphic SET transparent_color = 0 WHERE g_number IS NOT NULL;

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
	IF NEW.color_scheme != 1 THEN
		RAISE EXCEPTION 'color_scheme must be 1 for font glyph';
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

CREATE FUNCTION iris.glyph_ck() RETURNS TRIGGER AS
	$glyph_ck$
DECLARE
	g_scheme INTEGER;
	f_height INTEGER;
	f_width INTEGER;
	g_height INTEGER;
	g_width INTEGER;
BEGIN
	SELECT color_scheme INTO g_scheme FROM iris.graphic
		WHERE name = NEW.graphic;
	IF g_scheme != 1 THEN
		RAISE EXCEPTION 'color_scheme must be 1 for font glyph';
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

CREATE VIEW graphic_view AS
	SELECT name, g_number, cs.description AS color_scheme, height, width,
	       transparent_color, pixels
	FROM iris.graphic
	JOIN iris.color_scheme cs ON graphic.color_scheme = cs.id;
GRANT SELECT ON graphic_view TO PUBLIC;

COMMIT;
