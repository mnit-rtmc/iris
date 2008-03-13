SET SESSION AUTHORIZATION 'tms';

DROP VIEW detector_view;
DROP FUNCTION detector_label(text, varchar, text, varchar, text, smallint,
	smallint, boolean, boolean);

INSERT INTO lane_type (id, description, dcode) VALUES (14, 'HOV', 'H');
INSERT INTO lane_type (id, description, dcode) VALUES (15, 'HOT', 'HT');

UPDATE detector SET "laneType" = 14 WHERE "laneType" = 1 and hov = 't';
UPDATE detector SET "laneType" = 14 WHERE "laneType" = 2 and hov = 't';

ALTER TABLE detector DROP COLUMN hov;

CREATE FUNCTION detector_label(text, varchar, text, varchar, text, smallint,
	smallint, boolean) RETURNS text AS
'	DECLARE
		fwy ALIAS FOR $1;
		fdir ALIAS FOR $2;
		xst ALIAS FOR $3;
		cross_dir ALIAS FOR $4;
		xmod ALIAS FOR $5;
		l_type ALIAS FOR $6;
		lane_number ALIAS FOR $7;
		abandoned ALIAS FOR $8;
		xmd varchar(2);
		ltyp varchar(2);
		lnum varchar(2);
		suffix varchar(5);
	BEGIN
		IF fwy IS NULL OR xst IS NULL THEN
			RETURN ''FUTURE'';
		END IF;
		SELECT INTO ltyp dcode FROM lane_type WHERE id = l_type;
		lnum = '''';
		IF lane_number > 0 THEN
			lnum = TO_CHAR(lane_number, ''FM9'');
		END IF;
		xmd = '''';
		IF xmod != ''@'' THEN
			xmd = xmod;
		END IF;
		suffix = '''';
		IF abandoned THEN
			suffix = ''-ABND'';
		END IF;
		RETURN fwy || ''/'' || cross_dir || xmd || xst || fdir ||
			ltyp || lnum || suffix;
	END;'
LANGUAGE plpgsql;

CREATE VIEW detector_view AS
	SELECT d."index" AS det_id, ld.line, c."drop", d.pin,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d."laneType", d."laneNumber", d.abandoned) AS label,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	d."laneNumber" AS lane_number, d."fieldLength" AS field_length,
	ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d."forceFail") AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM detector d
	LEFT JOIN location_view l ON d."location" = l.vault_oid
	LEFT JOIN lane_type ln ON d."laneType" = ln.id
	LEFT JOIN controller c ON d.controller = c.vault_oid
	LEFT JOIN line_drop_view ld ON d.controller = ld.vault_oid;

GRANT SELECT ON detector_view TO PUBLIC;
