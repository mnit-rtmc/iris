--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET check_function_bodies = false;

CREATE PROCEDURAL LANGUAGE plpgsql;

SET SESSION AUTHORIZATION 'tms';

SET search_path = public, pg_catalog;

CREATE SEQUENCE tms_log_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1
    CYCLE;

CREATE TABLE iris_user (
	name VARCHAR(15) PRIMARY KEY,
	dn text NOT NULL,
	full_name VARCHAR(31) NOT NULL
);

CREATE TABLE role (
	name VARCHAR(15) PRIMARY KEY,
	pattern VARCHAR(31) DEFAULT ''::VARCHAR NOT NULL,
	priv_r boolean DEFAULT false NOT NULL,
	priv_w boolean DEFAULT false NOT NULL,
	priv_c boolean DEFAULT false NOT NULL,
	priv_d boolean DEFAULT false NOT NULL
);

CREATE TABLE iris_user_role (
	iris_user VARCHAR(15) NOT NULL REFERENCES iris_user(name),
	role VARCHAR(15) NOT NULL REFERENCES role(name)
);

CREATE TABLE system_policy (
	name character varying PRIMARY KEY,
	value integer NOT NULL
);

CREATE TABLE direction (
	id smallint PRIMARY KEY,
	direction VARCHAR(4) NOT NULL,
	dir VARCHAR(4) NOT NULL
);

CREATE TABLE road_class (
	id integer PRIMARY KEY,
	description VARCHAR(12) NOT NULL,
	grade CHAR NOT NULL
);

CREATE TABLE road_modifier (
	id smallint PRIMARY KEY,
	modifier text NOT NULL,
	mod VARCHAR(2) NOT NULL
);

GRANT SELECT ON TABLE road_modifier TO PUBLIC;

CREATE TABLE road (
	name VARCHAR(20) PRIMARY KEY,
	abbrev VARCHAR(6) NOT NULL,
	r_class smallint NOT NULL REFERENCES road_class(id),
	direction smallint NOT NULL REFERENCES direction(id),
	alt_dir smallint NOT NULL REFERENCES direction(id)
);

REVOKE ALL ON TABLE road FROM PUBLIC;

CREATE FUNCTION graphic_bpp(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		b INTEGER;
	BEGIN SELECT INTO b bpp FROM graphic WHERE name = n;
		RETURN b;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_height(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM font WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_width(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM font WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_height(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM graphic WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_width(TEXT) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM graphic WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_glyph(TEXT) RETURNS TEXT AS '
	DECLARE n ALIAS FOR $1;
		f TEXT;
	BEGIN SELECT INTO f font FROM glyph WHERE graphic = n;
		RETURN f;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_glyph(TEXT) RETURNS TEXT AS '
	DECLARE n ALIAS FOR $1;
		g TEXT;
	BEGIN SELECT INTO g graphic FROM glyph WHERE font = n;
		RETURN g;
	END;'
LANGUAGE PLPGSQL;

CREATE TABLE graphic (
	name TEXT PRIMARY KEY,
	bpp INTEGER NOT NULL,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	pixels TEXT NOT NULL
);

CREATE TABLE font (
	name TEXT PRIMARY KEY,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	line_spacing INTEGER NOT NULL,
	char_spacing INTEGER NOT NULL,
	version_id INTEGER NOT NULL
);

ALTER TABLE font
	ADD CONSTRAINT font_height_ck
	CHECK (height > 0 AND height < 25);
ALTER TABLE font
	ADD CONSTRAINT font_width_ck
	CHECK (width >= 0 AND width < 25);
ALTER TABLE font
	ADD CONSTRAINT font_line_sp_ck
	CHECK (line_spacing >= 0 AND line_spacing < 9);
ALTER TABLE font
	ADD CONSTRAINT font_char_sp_ck
	CHECK (char_spacing >= 0 AND char_spacing < 9);
CREATE TABLE glyph (
	name TEXT PRIMARY KEY,
	font TEXT NOT NULL,
	code_point INTEGER NOT NULL,
	graphic TEXT NOT NULL
);
ALTER TABLE glyph
	ADD CONSTRAINT fk_glyph_font FOREIGN KEY (font) REFERENCES font(name);
ALTER TABLE glyph
	ADD CONSTRAINT fk_glyph_graphic FOREIGN KEY (graphic)
	REFERENCES graphic(name);
ALTER TABLE glyph
	ADD CONSTRAINT glyph_bpp_ck
	CHECK (graphic_bpp(graphic) = 1);
ALTER TABLE glyph
	ADD CONSTRAINT glyph_size_ck
	CHECK (font_height(font) = graphic_height(graphic) AND
		(font_width(font) = 0 OR
		font_width(font) = graphic_width(graphic)));
ALTER TABLE graphic
	ADD CONSTRAINT graphic_font_ck
	CHECK (font_glyph(name) IS NULL OR
		(font_height(font_glyph(name)) = height AND
		(font_width(font_glyph(name)) = 0 OR
		font_width(font_glyph(name)) = width)));
ALTER TABLE font
	ADD CONSTRAINT font_graphic_ck
	CHECK (graphic_glyph(name) IS NULL OR
		(graphic_height(graphic_glyph(name)) = height AND
		(width = 0 OR graphic_width(graphic_glyph(name)) = width)));

GRANT SELECT ON TABLE graphic TO PUBLIC;
GRANT SELECT ON TABLE font TO PUBLIC;
GRANT SELECT ON TABLE glyph TO PUBLIC;

CREATE TABLE video_monitor (
	name TEXT PRIMARY KEY,
	description TEXT NOT NULL,
	restricted boolean NOT NULL
);

GRANT SELECT ON TABLE video_monitor TO PUBLIC;

CREATE TABLE holiday (
	name TEXT PRIMARY KEY,
	month INTEGER NOT NULL,
	day INTEGER NOT NULL,
	week INTEGER NOT NULL,
	weekday INTEGER NOT NULL,
	shift INTEGER NOT NULL,
	period INTEGER NOT NULL
);

GRANT SELECT ON TABLE holiday TO PUBLIC;

CREATE TABLE geo_loc (
	name VARCHAR(20) PRIMARY KEY,
	freeway VARCHAR(20) REFERENCES road(name),
	free_dir smallint REFERENCES direction(id),
	cross_street VARCHAR(20) REFERENCES road(name),
	cross_dir smallint REFERENCES direction(id),
	cross_mod smallint REFERENCES road_modifier(id),
	easting integer,
	east_off integer,
	northing integer,
	north_off integer
);

CREATE TABLE lane_type (
	id smallint PRIMARY KEY,
	description text NOT NULL,
	dcode VARCHAR(2) NOT NULL
);

CREATE TABLE r_node_type (
	n_type integer PRIMARY KEY,
	name text NOT NULL
);

CREATE TABLE r_node_transition (
	n_transition integer PRIMARY KEY,
	name text NOT NULL
);

CREATE TABLE comm_link (
	name VARCHAR(20) PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	url VARCHAR(64) NOT NULL,
	protocol smallint NOT NULL,
	timeout integer NOT NULL
);

CREATE TABLE cabinet_style (
	name VARCHAR(20) PRIMARY KEY,
	dip smallint
);

CREATE TABLE cabinet (
	name VARCHAR(20) PRIMARY KEY,
	style VARCHAR(20) REFERENCES cabinet_style(name),
	geo_loc VARCHAR(20) REFERENCES geo_loc(name),
	mile real
);

CREATE TABLE controller (
	name VARCHAR(20) PRIMARY KEY,
	drop_id smallint NOT NULL,
	comm_link VARCHAR(20) NOT NULL REFERENCES comm_link(name),
	cabinet VARCHAR(20) REFERENCES cabinet(name),
	active boolean NOT NULL,
	notes text NOT NULL
);



CREATE TABLE vault_object (
    vault_oid integer NOT NULL,
    vault_type integer NOT NULL,
    vault_refs smallint DEFAULT 1 NOT NULL
);

REVOKE ALL ON TABLE vault_object FROM PUBLIC;
GRANT SELECT ON TABLE vault_object TO PUBLIC;

CREATE TABLE vault_types (
    "table" text NOT NULL,
    "className" text NOT NULL
)
INHERITS (vault_object);

CREATE TABLE vault_counter (
    id integer NOT NULL,
    logging boolean NOT NULL
)
INHERITS (vault_object);

CREATE TABLE vault_log_entry (
    redo text NOT NULL,
    undo text NOT NULL
)
INHERITS (vault_object);

CREATE TABLE vault_list (
    "listId" integer NOT NULL,
    "index" integer NOT NULL,
    "elementId" integer NOT NULL
);

REVOKE ALL ON TABLE vault_list FROM PUBLIC;
GRANT SELECT ON TABLE vault_list TO PUBLIC;

CREATE TABLE "java_util_AbstractCollection" (
    dummy_24 boolean
)
INHERITS (vault_object);

CREATE TABLE "java_util_AbstractList" (
    dummy_23 boolean
)
INHERITS ("java_util_AbstractCollection");

CREATE TABLE vault_transaction (
    stamp timestamp with time zone NOT NULL,
    "user" text NOT NULL,
    entries integer NOT NULL,
    "lastId" integer NOT NULL
)
INHERITS (vault_object);

CREATE TABLE "java_lang_Number" (
    dummy_30 boolean
)
INHERITS (vault_object);

CREATE TABLE "java_lang_Integer" (
    value integer NOT NULL
)
INHERITS ("java_lang_Number");

CREATE TABLE "java_util_ArrayList" (
    size integer NOT NULL
)
INHERITS ("java_util_AbstractList");

CREATE TABLE tms_object (
    dummy_41 boolean
)
INHERITS (vault_object);

REVOKE ALL ON TABLE tms_object FROM PUBLIC;
GRANT SELECT ON TABLE tms_object TO PUBLIC;

CREATE TABLE abstract_list (
    dummy_40 boolean
)
INHERITS (tms_object);

CREATE TABLE device (
    controller VARCHAR(20) NOT NULL REFERENCES controller(name),
    pin integer NOT NULL,
    notes text NOT NULL,
    geo_loc VARCHAR(20) NOT NULL REFERENCES geo_loc(name)
)
INHERITS (tms_object);

GRANT SELECT ON device TO PUBLIC;

CREATE TABLE traffic_device (
    id text NOT NULL
)
INHERITS (device);

REVOKE ALL ON TABLE traffic_device FROM PUBLIC;
GRANT SELECT ON TABLE traffic_device TO PUBLIC;

CREATE TABLE dms (
    camera integer NOT NULL,
    mile real NOT NULL,
    travel text NOT NULL
)
INHERITS (traffic_device);

REVOKE ALL ON TABLE dms FROM PUBLIC;
GRANT SELECT ON TABLE dms TO PUBLIC;

CREATE UNIQUE INDEX dms_pkey ON dms USING btree (vault_oid);
CREATE UNIQUE INDEX dms_id_index ON dms USING btree (id);

CREATE TABLE sign_group (
	name VARCHAR(16) PRIMARY KEY,
	local BOOLEAN NOT NULL
);

CREATE TABLE dms_sign_group (
	name VARCHAR(24) PRIMARY KEY,
	dms text NOT NULL REFERENCES dms(id),
	sign_group VARCHAR(16) NOT NULL REFERENCES sign_group
);

CREATE TABLE sign_text (
	name VARCHAR(20) PRIMARY KEY,
	sign_group VARCHAR(16) NOT NULL REFERENCES sign_group,
	line smallint NOT NULL,
	message VARCHAR(24) NOT NULL,
	priority smallint NOT NULL,
	CONSTRAINT sign_text_line CHECK ((line >= 1) AND (line <= 12)),
	CONSTRAINT sign_text_priority CHECK
		((priority >= 1) AND (priority <= 99))
);


CREATE TABLE vault_map (
    "mapId" integer NOT NULL,
    "keyId" integer NOT NULL,
    "valueId" integer NOT NULL
);

CREATE TABLE "java_util_AbstractMap" (
    dummy_64 boolean
)
INHERITS (vault_object);

CREATE TABLE "java_util_TreeMap" (
    comparator integer NOT NULL
)
INHERITS ("java_util_AbstractMap");

CREATE TABLE indexed_list (
    list integer NOT NULL
)
INHERITS (abstract_list);

CREATE TABLE detector (
    "index" integer NOT NULL,
    "laneType" smallint NOT NULL,
    "laneNumber" smallint NOT NULL,
    abandoned boolean NOT NULL,
    "forceFail" boolean NOT NULL,
    "fieldLength" real NOT NULL,
    fake text NOT NULL
)
INHERITS (device);

REVOKE ALL ON TABLE detector FROM PUBLIC;
GRANT SELECT ON TABLE detector TO PUBLIC;

CREATE TABLE ramp_meter (
    "controlMode" integer NOT NULL,
    "singleRelease" boolean NOT NULL,
    "storage" integer NOT NULL,
    "maxWait" integer NOT NULL,
    camera integer NOT NULL
)
INHERITS (traffic_device);

REVOKE ALL ON TABLE ramp_meter FROM PUBLIC;
GRANT SELECT ON TABLE ramp_meter TO PUBLIC;

CREATE TABLE timing_plan (
    "startTime" integer NOT NULL,
    "stopTime" integer NOT NULL,
    active boolean NOT NULL
)
INHERITS (tms_object);

CREATE TABLE meter_plan (
    dummy_43417 boolean
)
INHERITS (timing_plan);

CREATE TABLE simple_plan (
    target integer NOT NULL
)
INHERITS (meter_plan);

REVOKE ALL ON TABLE simple_plan FROM PUBLIC;
GRANT SELECT ON TABLE simple_plan TO PUBLIC;

CREATE TABLE time_plan_log (
    event_id integer DEFAULT nextval('tms_log_seq'::text) NOT NULL,
    vault_oid integer,
    event_date timestamp with time zone NOT NULL,
    logged_by text NOT NULL,
    start_time text NOT NULL,
    stop_time text NOT NULL,
    target integer NOT NULL
);

REVOKE ALL ON TABLE time_plan_log FROM PUBLIC;
GRANT SELECT ON TABLE time_plan_log TO PUBLIC;

CREATE TABLE add_remove_device_log (
    event_id integer DEFAULT nextval('tms_log_seq'::text) NOT NULL,
    event_date timestamp with time zone NOT NULL,
    device_type text NOT NULL,
    device_id text NOT NULL,
    event_description character varying(10) NOT NULL,
    edited_by text NOT NULL
);

REVOKE ALL ON TABLE add_remove_device_log FROM PUBLIC;
GRANT SELECT ON TABLE add_remove_device_log TO PUBLIC;

CREATE TABLE camera (
    encoder text NOT NULL,
    encoder_channel integer NOT NULL,
    nvr text NOT NULL,
    publish boolean NOT NULL
)
INHERITS (traffic_device);

REVOKE ALL ON TABLE camera FROM PUBLIC;
GRANT SELECT ON TABLE camera TO PUBLIC;

CREATE TABLE stratified_plan (
    dummy_48294 boolean
)
INHERITS (meter_plan);

REVOKE ALL ON TABLE stratified_plan FROM PUBLIC;
GRANT SELECT ON TABLE stratified_plan TO PUBLIC;

CREATE TABLE lcs_module (
    "sfoRed" integer NOT NULL,
    "sfoYellow" integer NOT NULL,
    "sfoGreen" integer NOT NULL,
    "sfiRed" integer NOT NULL,
    "sfiYellow" integer NOT NULL,
    "sfiGreen" integer NOT NULL
)
INHERITS (tms_object);

CREATE TABLE lcs (
    camera integer NOT NULL,
    modules integer[] NOT NULL
)
INHERITS (traffic_device);

REVOKE ALL ON TABLE lcs FROM PUBLIC;
GRANT SELECT ON TABLE lcs TO PUBLIC;

CREATE TABLE detector_fieldlength_log (
    "index" integer,
    event_date timestamp with time zone,
    field_length real
);

REVOKE ALL ON TABLE detector_fieldlength_log FROM PUBLIC;
GRANT SELECT ON TABLE detector_fieldlength_log TO PUBLIC;

CREATE FUNCTION time_plan_log() RETURNS "trigger"
    AS '
	begin if (OLD."startTime" != NEW."startTime" or OLD."stopTime"!= NEW."stopTime" or OLD.target!=NEW.target) 
	then insert into time_plan_log(vault_oid, event_date, logged_by, start_time, stop_time, target) 
	values(OLD.vault_oid, CURRENT_TIMESTAMP, user, OLD."startTime", OLD."stopTime", OLD.target); end if; return old; 
	end; '
    LANGUAGE plpgsql;

CREATE FUNCTION detector_fieldlength_log() RETURNS "trigger"
    AS 'begin
if (OLD."fieldLength" != NEW."fieldLength") then 
insert into detector_fieldlength_log(index, event_date, field_length) 
values (OLD.index, CURRENT_TIMESTAMP, OLD."fieldLength"); 
end if; 
return old; 
end; '
    LANGUAGE plpgsql;

CREATE FUNCTION add_detector() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, NEW.index, ''add'', user); return NEW; end; '
    LANGUAGE plpgsql;

CREATE FUNCTION remove_detector() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, OLD.index, ''remove'', user); return OLD; end; '
    LANGUAGE plpgsql;

CREATE FUNCTION add_dms() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, NEW.id, ''add'', user); return NEW; end; '
    LANGUAGE plpgsql;

CREATE FUNCTION remove_dms() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, OLD.id, ''remove'', user); return OLD; end; '
    LANGUAGE plpgsql;

CREATE FUNCTION add_meter() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, NEW.id, ''add'', user); return NEW; end; '
    LANGUAGE plpgsql;

CREATE FUNCTION remove_meter() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, OLD.id, ''remove'', user); return OLD; end; '
    LANGUAGE plpgsql;

CREATE FUNCTION add_camera() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, NEW.id, ''add'', user); return NEW; end; '
    LANGUAGE plpgsql;

CREATE FUNCTION remove_camera() RETURNS "trigger"
    AS '
	begin insert into add_remove_device_log(event_date, device_type, device_id, event_description, edited_by) 
	values(CURRENT_TIMESTAMP, TG_RELNAME, OLD.id, ''remove'', user); return OLD; end; '
    LANGUAGE plpgsql;

CREATE TABLE warning_sign (
    camera integer NOT NULL,
    text text NOT NULL
)
INHERITS (traffic_device);

REVOKE ALL ON TABLE warning_sign FROM PUBLIC;
GRANT SELECT ON TABLE warning_sign TO PUBLIC;

CREATE TABLE alarm (
    controller VARCHAR(20) NOT NULL REFERENCES controller(name),
    pin integer NOT NULL,
    notes text NOT NULL
)
INHERITS (tms_object);

CREATE TABLE traffic_device_timing_plan (
    traffic_device text,
    timing_plan integer
);

REVOKE ALL ON TABLE traffic_device_timing_plan FROM PUBLIC;
GRANT SELECT ON TABLE traffic_device_timing_plan TO PUBLIC;

CREATE TABLE r_node_detector (
    r_node integer NOT NULL,
    detector integer NOT NULL
);

REVOKE ALL ON TABLE r_node_detector FROM PUBLIC;
GRANT SELECT ON TABLE r_node_detector TO PUBLIC;

CREATE TABLE r_node (
    geo_loc VARCHAR(20) NOT NULL REFERENCES geo_loc(name),
    node_type integer NOT NULL,
    pickable boolean NOT NULL,
    transition integer NOT NULL,
    lanes integer NOT NULL,
    attach_side boolean NOT NULL,
    shift integer NOT NULL,
    station_id text NOT NULL,
    speed_limit integer NOT NULL,
    notes text NOT NULL
)
INHERITS (tms_object);

REVOKE ALL ON TABLE r_node FROM PUBLIC;
GRANT SELECT ON TABLE r_node TO PUBLIC;


CREATE FUNCTION get_next_oid() RETURNS integer
    AS '
	DECLARE
		oid int;
	BEGIN
		UPDATE vault_counter SET id = id + 1;
		SELECT INTO oid id FROM vault_counter;
		RETURN oid;
	END;
'
    LANGUAGE plpgsql;

CREATE FUNCTION hour_min(integer) RETURNS text
    AS '
DECLARE
	min_of_day ALIAS FOR $1;
	hour integer;
	minute integer;
	output text;
BEGIN
	hour := min_of_day / 60;
	minute := min_of_day % 60;
	output := '''';
	IF hour < 10 THEN
		output := ''0'';
	END IF;
	output := output || hour || '':'';
	IF minute < 10 THEN
		output := output || ''0'';
	END IF;
	output := output || minute;
	RETURN output;
END;'
    LANGUAGE plpgsql;


CREATE VIEW time_plan_log_view AS
    SELECT t.event_id, rm.id AS ramp_id, t.event_date, t.logged_by, hour_min(int4(t.start_time)) AS start_time, hour_min(int4(t.stop_time)) AS stop_time, t.target FROM ((ramp_meter rm JOIN traffic_device_timing_plan tp ON ((rm.id = tp.traffic_device))) JOIN time_plan_log t ON ((t.vault_oid = tp.timing_plan)));

REVOKE ALL ON TABLE time_plan_log_view FROM PUBLIC;
GRANT SELECT ON TABLE time_plan_log_view TO PUBLIC;

CREATE VIEW "simple" AS
    SELECT rm.id, hour_min(sp."startTime") AS start_time, hour_min(sp."stopTime") AS stop_time, sp.target, sp.active FROM ((ramp_meter rm JOIN traffic_device_timing_plan tp ON ((rm.id = tp.traffic_device))) JOIN simple_plan sp ON ((tp.timing_plan = sp.vault_oid)));

REVOKE ALL ON TABLE "simple" FROM PUBLIC;
GRANT SELECT ON TABLE "simple" TO PUBLIC;

CREATE VIEW stratified AS
    SELECT rm.id, hour_min(sp."startTime") AS start_time, hour_min(sp."stopTime") AS stop_time, sp.active FROM ((ramp_meter rm JOIN traffic_device_timing_plan tp ON ((rm.id = tp.traffic_device))) JOIN stratified_plan sp ON ((tp.timing_plan = sp.vault_oid)));

GRANT SELECT ON TABLE stratified TO PUBLIC;

CREATE VIEW road_view AS
	SELECT name, abbrev, rcl.description AS r_class, dir.direction,
	adir.direction AS alt_dir
	FROM road
	LEFT JOIN road_class rcl ON road.r_class = rcl.id
	LEFT JOIN direction dir ON road.direction = dir.id
	LEFT JOIN direction adir ON road.alt_dir = adir.id;

GRANT SELECT ON road_view TO PUBLIC;

CREATE VIEW geo_loc_view AS
	SELECT l.name, f.abbrev AS fwy, l.freeway,
	f_dir.direction AS free_dir, f_dir.dir AS fdir,
	m.modifier AS cross_mod, m.mod AS xmod, c.abbrev as xst,
	l.cross_street, c_dir.direction AS cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM geo_loc l
	LEFT JOIN road f ON l.freeway = f.name
	LEFT JOIN road_modifier m ON l.cross_mod = m.id
	LEFT JOIN road c ON l.cross_street = c.name
	LEFT JOIN direction f_dir ON l.free_dir = f_dir.id
	LEFT JOIN direction c_dir ON l.cross_dir = c_dir.id;
GRANT SELECT ON geo_loc_view TO PUBLIC;

CREATE VIEW device_loc_view AS
	SELECT d.vault_oid, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir
	FROM device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON device_loc_view TO PUBLIC;

CREATE VIEW r_node_view AS
	SELECT n.vault_oid, freeway, free_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, tr.name AS transition,
	n.lanes, n.attach_side, n.shift, n.station_id, n.speed_limit, n.notes
	FROM r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN r_node_type nt ON n.node_type = nt.n_type
	JOIN r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

CREATE VIEW freeway_station_view AS
	SELECT station_id, freeway, free_dir, cross_mod, cross_street,
	speed_limit
	FROM r_node r, geo_loc_view l
	WHERE r.geo_loc = l.name AND station_id != '';
GRANT SELECT ON freeway_station_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
	SELECT c.name, c.drop_id, c.comm_link, c.cabinet, c.active, c.notes,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller c
	LEFT JOIN cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.id, d.notes, c.id AS camera, d.mile, d.travel,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN camera c ON d.camera = c.vault_oid;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, message, priority
	FROM dms_sign_group
	JOIN sign_group ON dms_sign_group.sign_group = sign_group.name
	JOIN sign_text ON sign_group.name = sign_text.sign_group;

GRANT SELECT ON sign_text_view TO PUBLIC;

CREATE VIEW ramp_meter_view AS
	SELECT m.vault_oid, m.id, m.notes,
	m."controlMode" AS control_mode, m."singleRelease" AS single_release,
	m."storage", m."maxWait" AS max_wait, c.id AS camera,
	l.fwy, l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM ramp_meter m
	JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN camera c ON m.camera = c.vault_oid;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.id, ctr.comm_link, ctr.drop_id, ctr.active, c.notes,
	c.encoder, c.encoder_channel, c.nvr, c.publish,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM camera c
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

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

CREATE FUNCTION boolean_converter(boolean) RETURNS text AS
'	DECLARE
		value ALIAS FOR $1;
	BEGIN
		IF value = ''t'' THEN
			RETURN ''Yes'';
		END IF;
		RETURN ''No'';
	END;'
LANGUAGE plpgsql;

CREATE VIEW detector_view AS
	SELECT d."index" AS det_id, c.comm_link, c.drop_id, d.pin,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d."laneType", d."laneNumber", d.abandoned) AS label,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	d."laneNumber" AS lane_number, d."fieldLength" AS field_length,
	ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d."forceFail") AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM detector d
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN lane_type ln ON d."laneType" = ln.id
	LEFT JOIN controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW controller_device_view AS
	SELECT d.id, d.controller, d.pin,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM traffic_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.comm_link, c.drop_id, cab.mile,
	trim(l.freeway || ' ' || l.free_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d1.id AS "id (meter1)",
	d1.cross_street AS "from (meter1)", d1.freeway AS "to (meter1)",
	d2.id AS meter2, d2.cross_street AS "from (meter2)",
	d2.freeway AS "to (meter2)", c.notes
	FROM controller c
	LEFT JOIN cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d1 ON
		d1.pin = 2 AND d1.controller = c.name
	LEFT JOIN controller_device_view d2 ON
		d2.pin = 3 AND d2.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

COPY vault_types (vault_oid, vault_type, vault_refs, "table", "className") FROM stdin;
3	4	0	vault_object	java.lang.Object
2	4	1	vault_counter	us.mn.state.dot.vault.Counter
24	4	0	java_util_AbstractCollection	java.util.AbstractCollection
23	4	0	java_util_AbstractList	java.util.AbstractList
1272	4	0	indexed_list	us.mn.state.dot.tms.IndexedListImpl
23834	4	0	ramp_meter	us.mn.state.dot.tms.RampMeterImpl
30	4	0	java_lang_Number	java.lang.Number
41	4	0	tms_object	us.mn.state.dot.tms.TMSObjectImpl
40	4	0	abstract_list	us.mn.state.dot.tms.AbstractListImpl
60	4	0	device	us.mn.state.dot.tms.DeviceImpl
59	4	0	traffic_device	us.mn.state.dot.tms.TrafficDeviceImpl
64	4	0	java_util_AbstractMap	java.util.AbstractMap
43417	4	0	meter_plan	us.mn.state.dot.tms.MeterPlanImpl
1395	4	0	java_util_TreeMap	java.util.TreeMap
52732	4	0	lcs_module	us.mn.state.dot.tms.LCSModuleImpl
52736	4	0	lcs	us.mn.state.dot.tms.LaneControlSignalImpl
1532	4	0	java_lang_Integer	java.lang.Integer
1536	4	0	vault_map	us.mn.state.dot.vault.MapEntry
51458	4	0	stratified_plan	us.mn.state.dot.tms.StratifiedPlanImpl
64501	4	0	warning_sign	us.mn.state.dot.tms.WarningSignImpl
38	4	0	java_util_ArrayList	java.util.ArrayList
63230	4	0	timing_plan	us.mn.state.dot.tms.TimingPlanImpl
79334	4	0	alarm	us.mn.state.dot.tms.AlarmImpl
58	4	0	dms	us.mn.state.dot.tms.DMSImpl
134	4	0	camera	us.mn.state.dot.tms.CameraImpl
43415	4	0	simple_plan	us.mn.state.dot.tms.SimplePlanImpl
84656	4	0	r_node	us.mn.state.dot.tms.R_NodeImpl
37	4	0	vault_list	us.mn.state.dot.vault.ListElement
2065	4	0	detector	us.mn.state.dot.tms.DetectorImpl
4	4	52	vault_types	us.mn.state.dot.vault.Type
\.


COPY vault_counter (vault_oid, vault_type, vault_refs, id, logging) FROM stdin;
1	2	1	92319	f
\.

COPY direction (id, direction, dir) FROM stdin;
0		
1	NB	N
2	SB	S
3	EB	E
4	WB	W
5	N-S	N-S
6	E-W	E-W
\.

COPY road_class (id, description, grade) FROM stdin;
0		
1	residential	A
2	business	B
3	collector	C
4	arterial	D
5	expressway	E
6	freeway	F
7	CD road	
\.

COPY cabinet_style (name, dip) FROM stdin;
336	0
334Z	1
334D	2
334Z-94	3
Drum	4
334DZ	5
334	6
334Z-99	7
S334Z	9
Prehistoric	10
334Z-00	11
334Z-05	13
334ZP	15
\.

COPY lane_type (id, description, dcode) FROM stdin;
0		
1	Mainline	
2	Auxiliary	A
3	CD Lane	CD
4	Reversible	R
5	Merge	M
6	Queue	Q
7	Exit	X
8	Bypass	B
9	Passage	P
10	Velocity	V
11	Omnibus	O
12	Green	G
13	Wrong Way	Y
14	HOV	H
15	HOT	HT
\.

COPY road_modifier (id, modifier, mod) FROM stdin;
0	@	
1	N of	N
2	S of	S
3	E of	E
4	W of	W
5	N Junction	Nj
6	S Junction	Sj
7	E Junction	Ej
8	W Junction	Wj
\.

COPY system_policy (name, value) FROM stdin;
meter_green_time	13
meter_yellow_time	7
meter_min_red_time	1
dms_page_on_time	20
dms_page_off_time	0
ring_radius_0	2
ring_radius_1	5
ring_radius_2	10
\.

COPY r_node_type (n_type, name) FROM stdin;
0	station
1	entrance
2	exit
3	intersection
4	access
5	interchange
\.

COPY r_node_transition (n_transition, name) FROM stdin;
0	none
1	loop
2	leg
3	slipramp
4	CD
5	HOV
6	common
7	flyover
\.

COPY role (name, pattern, priv_r, priv_w, priv_c, priv_d) FROM stdin;
admin		f	f	f	f
alert		f	f	f	f
dms	dms/.*/message	f	t	f	f
font	font/.*	f	t	t	t
glyph	glyph/.*	f	t	t	t
graphic	graphic/.*	f	t	t	t
incidents		f	f	f	f
activate	.*/.*/active	f	t	f	f
meter	meter/.*/metering	f	t	f	f
lcs	lcs/.*/signals	f	t	f	f
user_admin	user/.*	t	t	t	t
role_admin	role/.*	t	t	t	t
view	.*	t	f	f	f
\.

CREATE UNIQUE INDEX vault_object_vault_oid_key ON vault_object USING btree (vault_oid);

CREATE UNIQUE INDEX vault_object_pkey ON vault_object USING btree (vault_oid);

CREATE UNIQUE INDEX vault_types_pkey ON vault_types USING btree (vault_oid);

CREATE UNIQUE INDEX vault_counter_pkey ON vault_counter USING btree (vault_oid);

CREATE UNIQUE INDEX vault_log_entry_pkey ON vault_log_entry USING btree (vault_oid);

CREATE UNIQUE INDEX "va_util_AbstractCollection_pkey" ON "java_util_AbstractCollection" USING btree (vault_oid);

CREATE UNIQUE INDEX "java_util_AbstractList_pkey" ON "java_util_AbstractList" USING btree (vault_oid);

CREATE UNIQUE INDEX vault_transaction_pkey ON vault_transaction USING btree (vault_oid);

CREATE UNIQUE INDEX "java_lang_Number_pkey" ON "java_lang_Number" USING btree (vault_oid);

CREATE UNIQUE INDEX "java_lang_Integer_pkey" ON "java_lang_Integer" USING btree (vault_oid);

CREATE UNIQUE INDEX "java_util_ArrayList_pkey" ON "java_util_ArrayList" USING btree (vault_oid);

CREATE UNIQUE INDEX tms_object_pkey ON tms_object USING btree (vault_oid);

CREATE UNIQUE INDEX abstract_list_pkey ON abstract_list USING btree (vault_oid);

CREATE UNIQUE INDEX device_pkey ON device USING btree (vault_oid);

CREATE UNIQUE INDEX traffic_device_pkey ON traffic_device USING btree (vault_oid);

CREATE UNIQUE INDEX "java_util_AbstractMap_pkey" ON "java_util_AbstractMap" USING btree (vault_oid);

CREATE UNIQUE INDEX "java_util_TreeMap_pkey" ON "java_util_TreeMap" USING btree (vault_oid);

CREATE UNIQUE INDEX indexed_list_pkey ON indexed_list USING btree (vault_oid);

CREATE UNIQUE INDEX detector_pkey ON detector USING btree (vault_oid);

CREATE UNIQUE INDEX ramp_meter_pkey ON ramp_meter USING btree (vault_oid);

CREATE UNIQUE INDEX timing_plan_pkey ON timing_plan USING btree (vault_oid);

CREATE UNIQUE INDEX simple_plan_pkey ON simple_plan USING btree (vault_oid);

CREATE UNIQUE INDEX stratified_plan_pkey ON stratified_plan USING btree (vault_oid);

CREATE UNIQUE INDEX lcs_module_pkey ON lcs_module USING btree (vault_oid);

CREATE UNIQUE INDEX lcs_pkey ON lcs USING btree (vault_oid);

CREATE UNIQUE INDEX warning_sign_pkey ON warning_sign USING btree (vault_oid);

CREATE UNIQUE INDEX detector_index ON detector USING btree ("index");

CREATE UNIQUE INDEX r_node_pkey ON r_node USING btree (vault_oid);

ALTER TABLE ONLY time_plan_log
    ADD CONSTRAINT time_plan_log_pkey PRIMARY KEY (event_id);

ALTER TABLE ONLY r_node_detector
    ADD CONSTRAINT "$2" FOREIGN KEY (detector) REFERENCES detector("index");

ALTER TABLE ONLY r_node_detector
    ADD CONSTRAINT "$1" FOREIGN KEY (r_node) REFERENCES r_node(vault_oid);

CREATE TRIGGER time_plan_log_trig
    AFTER UPDATE ON simple_plan
    FOR EACH ROW
    EXECUTE PROCEDURE time_plan_log();

CREATE TRIGGER detector_fieldlength_log_trig
    AFTER UPDATE ON detector
    FOR EACH ROW
    EXECUTE PROCEDURE detector_fieldlength_log();

CREATE TRIGGER add_detector_trig
    AFTER INSERT ON detector
    FOR EACH ROW
    EXECUTE PROCEDURE add_detector();

CREATE TRIGGER remove_detector_trig
    AFTER DELETE ON detector
    FOR EACH ROW
    EXECUTE PROCEDURE remove_detector();

CREATE TRIGGER add_dms_trig
    AFTER INSERT ON dms
    FOR EACH ROW
    EXECUTE PROCEDURE add_dms();

CREATE TRIGGER remove_dms_trig
    AFTER DELETE ON dms
    FOR EACH ROW
    EXECUTE PROCEDURE remove_dms();

CREATE TRIGGER add_meter_trig
    AFTER INSERT ON ramp_meter
    FOR EACH ROW
    EXECUTE PROCEDURE add_meter();

CREATE TRIGGER remove_meter_trig
    AFTER DELETE ON ramp_meter
    FOR EACH ROW
    EXECUTE PROCEDURE remove_meter();

CREATE TRIGGER add_camera_trig
    AFTER INSERT ON camera
    FOR EACH ROW
    EXECUTE PROCEDURE add_camera();

CREATE TRIGGER remove_camera_trig
    AFTER DELETE ON camera
    FOR EACH ROW
    EXECUTE PROCEDURE remove_camera();

SELECT pg_catalog.setval('tms_log_seq', 8284, true);
