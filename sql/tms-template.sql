--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET check_function_bodies = false;

CREATE PROCEDURAL LANGUAGE plpgsql;

CREATE SCHEMA iris;
ALTER SCHEMA iris OWNER TO tms;

CREATE SCHEMA event;
ALTER SCHEMA event OWNER TO tms;

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
	dip integer
);

CREATE TABLE cabinet (
	name VARCHAR(20) PRIMARY KEY,
	style VARCHAR(20) REFERENCES cabinet_style(name),
	geo_loc VARCHAR(20) NOT NULL REFERENCES geo_loc(name),
	mile real
);

CREATE TABLE controller (
	name VARCHAR(20) PRIMARY KEY,
	drop_id smallint NOT NULL,
	comm_link VARCHAR(20) NOT NULL REFERENCES comm_link(name),
	cabinet VARCHAR(20) NOT NULL REFERENCES cabinet(name),
	active boolean NOT NULL,
	notes text NOT NULL
);

CREATE UNIQUE INDEX ctrl_link_drop_idx ON controller
	USING btree (comm_link, drop_id);

CREATE TABLE iris._device_io (
	name VARCHAR(10) PRIMARY KEY,
	controller VARCHAR(20) REFERENCES controller(name),
	pin integer NOT NULL
);

CREATE UNIQUE INDEX _device_io_ctrl_pin ON iris._device_io
	USING btree (controller, pin);

CREATE TABLE iris._alarm (
	name VARCHAR(10) PRIMARY KEY,
	description VARCHAR(24) NOT NULL,
	state BOOLEAN NOT NULL
);

ALTER TABLE iris._alarm ADD CONSTRAINT _alarm_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.alarm AS
	SELECT a.name, description, controller, pin, state
	FROM iris._alarm a JOIN iris._device_io d ON a.name = d.name;

CREATE RULE alarm_insert AS ON INSERT TO iris.alarm DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._alarm VALUES (NEW.name, NEW.description, NEW.state);
);

CREATE RULE alarm_update AS ON UPDATE TO iris.alarm DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._alarm SET
		description = NEW.description,
		state = NEW.state
	WHERE name = OLD.name;
);

CREATE RULE alarm_delete AS ON DELETE TO iris.alarm DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris.r_node (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) NOT NULL REFERENCES geo_loc(name),
	node_type integer NOT NULL REFERENCES r_node_type(n_type),
	pickable boolean NOT NULL,
	transition integer NOT NULL REFERENCES r_node_transition(n_transition),
	lanes integer NOT NULL,
	attach_side boolean NOT NULL,
	shift integer NOT NULL,
	station_id VARCHAR(10),
	speed_limit integer NOT NULL,
	notes text NOT NULL
);

CREATE UNIQUE INDEX r_node_station_idx ON iris.r_node USING btree (station_id);

CREATE TABLE iris._detector (
	name VARCHAR(10) PRIMARY KEY,
	r_node VARCHAR(10) REFERENCES iris.r_node(name),
	lane_type smallint NOT NULL REFERENCES lane_type(id),
	lane_number smallint NOT NULL,
	abandoned boolean NOT NULL,
	force_fail boolean NOT NULL,
	field_length real NOT NULL,
	fake VARCHAR(32),
	notes VARCHAR(32)
);

ALTER TABLE iris._detector ADD CONSTRAINT _detector_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.detector AS SELECT
	det.name, controller, pin, r_node, lane_type, lane_number, abandoned,
	force_fail, field_length, fake, notes
	FROM iris._detector det JOIN iris._device_io d ON det.name = d.name;

CREATE RULE detector_insert AS ON INSERT TO iris.detector DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._detector VALUES (NEW.name, NEW.r_node, NEW.lane_type,
		NEW.lane_number, NEW.abandoned, NEW.force_fail,
		NEW.field_length, NEW.fake, NEW.notes);
);

CREATE RULE detector_update AS ON UPDATE TO iris.detector DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._detector SET
		r_node = NEW.r_node,
		lane_type = NEW.lane_type,
		lane_number = NEW.lane_number,
		abandoned = NEW.abandoned,
		force_fail = NEW.force_fail,
		field_length = NEW.field_length,
		fake = NEW.fake,
		notes = NEW.notes
	WHERE name = OLD.name;
);

CREATE RULE detector_delete AS ON DELETE TO iris.detector DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris._camera (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES geo_loc(name),
	notes text NOT NULL,
	encoder text NOT NULL,
	encoder_channel integer NOT NULL,
	nvr text NOT NULL,
	publish boolean NOT NULL
);

ALTER TABLE iris._camera ADD CONSTRAINT _camera_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, encoder, encoder_channel, nvr,
		publish
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE RULE camera_insert AS ON INSERT TO iris.camera DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.encoder, NEW.encoder_channel, NEW.nvr, NEW.publish);
);

CREATE RULE camera_update AS ON UPDATE TO iris.camera DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._camera SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes,
		encoder = NEW.encoder,
		encoder_channel = NEW.encoder_channel,
		nvr = NEW.nvr,
		publish = NEW.publish
	WHERE name = OLD.name;
);

CREATE RULE camera_delete AS ON DELETE TO iris.camera DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris._warning_sign (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES geo_loc(name),
	notes text NOT NULL,
	message text NOT NULL,
	camera VARCHAR(10) REFERENCES iris._camera(name)
);

ALTER TABLE iris._warning_sign ADD CONSTRAINT _warning_sign_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.warning_sign AS SELECT
	w.name, geo_loc, controller, pin, notes, message, camera
	FROM iris._warning_sign w JOIN iris._device_io d ON w.name = d.name;

CREATE RULE warning_sign_insert AS ON INSERT TO iris.warning_sign DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._warning_sign VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.message, NEW.camera);
);

CREATE RULE warning_sign_update AS ON UPDATE TO iris.warning_sign DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._warning_sign SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes,
		message = NEW.message,
		camera = NEW.camera
	WHERE name = OLD.name;
);

CREATE RULE warning_sign_delete AS ON DELETE TO iris.warning_sign DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE traffic_device_attribute (
	name VARCHAR(32) PRIMARY KEY,
	id text NOT NULL,
	aname VARCHAR(32) NOT NULL,
	avalue VARCHAR(64) NOT NULL
);

CREATE TABLE system_attribute (
	name VARCHAR(32) PRIMARY KEY,
	value VARCHAR(64) NOT NULL
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

CREATE TABLE traffic_device (
    id text NOT NULL
)
INHERITS (device);

CREATE TABLE dms (
    camera VARCHAR(10) NOT NULL,
    mile real NOT NULL,
    travel text NOT NULL
)
INHERITS (traffic_device);

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

CREATE TABLE ramp_meter (
    "controlMode" integer NOT NULL,
    "singleRelease" boolean NOT NULL,
    "storage" integer NOT NULL,
    "maxWait" integer NOT NULL,
    camera VARCHAR(10) NOT NULL
)
INHERITS (traffic_device);

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
    camera VARCHAR(10) NOT NULL,
    modules integer[] NOT NULL
)
INHERITS (traffic_device);

REVOKE ALL ON TABLE lcs FROM PUBLIC;
GRANT SELECT ON TABLE lcs TO PUBLIC;

CREATE FUNCTION time_plan_log() RETURNS "trigger"
    AS '
	begin if (OLD."startTime" != NEW."startTime" or OLD."stopTime"!= NEW."stopTime" or OLD.target!=NEW.target) 
	then insert into time_plan_log(vault_oid, event_date, logged_by, start_time, stop_time, target) 
	values(OLD.vault_oid, CURRENT_TIMESTAMP, user, OLD."startTime", OLD."stopTime", OLD.target); end if; return old; 
	end; '
    LANGUAGE plpgsql;

CREATE TABLE traffic_device_timing_plan (
    traffic_device text,
    timing_plan integer
);

REVOKE ALL ON TABLE traffic_device_timing_plan FROM PUBLIC;
GRANT SELECT ON TABLE traffic_device_timing_plan TO PUBLIC;



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
	SELECT d.vault_oid, d.controller, d.geo_loc,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON device_loc_view TO PUBLIC;

CREATE VIEW r_node_view AS
	SELECT n.name, freeway, free_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, tr.name AS transition,
	n.lanes, n.attach_side, n.shift, n.station_id, n.speed_limit, n.notes
	FROM iris.r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN r_node_type nt ON n.node_type = nt.n_type
	JOIN r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

CREATE VIEW freeway_station_view AS
	SELECT station_id, freeway, free_dir, cross_mod, cross_street,
	speed_limit
	FROM iris.r_node r, geo_loc_view l
	WHERE r.geo_loc = l.name AND station_id IS NOT NULL;
GRANT SELECT ON freeway_station_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
	SELECT c.name, c.drop_id, c.comm_link, c.cabinet, c.active, c.notes,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller c
	LEFT JOIN cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.controller, a.pin, c.comm_link,
		c.drop_id
	FROM iris.alarm a LEFT JOIN controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.id, d.notes, d.camera, d.mile, d.travel, d.geo_loc,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off, d.controller
	FROM dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
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
	m."storage", m."maxWait" AS max_wait, m.camera, m.geo_loc,
	l.fwy, l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off, m.controller
	FROM ramp_meter m
	JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, c.encoder, c.encoder_channel, c.nvr, c.publish,
	c.geo_loc, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir, l.easting, l.northing, l.east_off, l.north_off,
	c.controller, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.camera c
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW warning_sign_view AS
	SELECT w.name, w.notes, w.message, w.camera, w.geo_loc,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off,
	w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.warning_sign w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller ctr ON w.controller = ctr.name;
GRANT SELECT ON warning_sign_view TO PUBLIC;

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

CREATE VIEW detector_label_view AS
	SELECT d.name AS det_id,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name AS det_id, d.controller, c.comm_link, c.drop_id, d.pin,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label,
	rnd.geo_loc, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir, d.lane_number, d.field_length, ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d.force_fail) AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN lane_type ln ON d.lane_type = ln.id
	LEFT JOIN controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW controller_view AS
	SELECT c.name, drop_id, comm_link, cabinet, active, notes, cab.geo_loc
	FROM controller c
	JOIN cabinet cab ON c.cabinet = cab.name;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW controller_device_view AS
	SELECT d.id, d.controller, d.pin, d.geo_loc,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM traffic_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.mile, cab.geo_loc,
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
38	4	0	java_util_ArrayList	java.util.ArrayList
63230	4	0	timing_plan	us.mn.state.dot.tms.TimingPlanImpl
58	4	0	dms	us.mn.state.dot.tms.DMSImpl
43415	4	0	simple_plan	us.mn.state.dot.tms.SimplePlanImpl
37	4	0	vault_list	us.mn.state.dot.vault.ListElement
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

COPY system_attribute (name, value) FROM stdin;
database_version	3.80.0
dms_page_on_secs	2.0
dms_page_off_secs	0.0
meter_green_secs	1.3
meter_yellow_secs	0.7
meter_min_red_secs	0.1
incident_ring_1_miles	2
incident_ring_2_miles	5
incident_ring_3_miles	10
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

CREATE UNIQUE INDEX ramp_meter_pkey ON ramp_meter USING btree (vault_oid);

CREATE UNIQUE INDEX timing_plan_pkey ON timing_plan USING btree (vault_oid);

CREATE UNIQUE INDEX simple_plan_pkey ON simple_plan USING btree (vault_oid);

CREATE UNIQUE INDEX stratified_plan_pkey ON stratified_plan USING btree (vault_oid);

CREATE UNIQUE INDEX lcs_module_pkey ON lcs_module USING btree (vault_oid);

CREATE UNIQUE INDEX lcs_pkey ON lcs USING btree (vault_oid);

ALTER TABLE ONLY time_plan_log
    ADD CONSTRAINT time_plan_log_pkey PRIMARY KEY (event_id);

CREATE TRIGGER time_plan_log_trig
    AFTER UPDATE ON simple_plan
    FOR EACH ROW
    EXECUTE PROCEDURE time_plan_log();

SELECT pg_catalog.setval('tms_log_seq', 8284, true);

SET search_path = event, public, pg_catalog;

CREATE SEQUENCE event.event_id_seq;

CREATE TABLE event.event_description (
	event_desc_id integer PRIMARY KEY,
	description text NOT NULL
);

CREATE TABLE event.alarm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	alarm VARCHAR(10) NOT NULL REFERENCES iris._alarm(name)
		ON DELETE CASCADE
);

CREATE TABLE event.comm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	controller VARCHAR(20) NOT NULL REFERENCES controller(name)
		ON DELETE CASCADE,
	device_id VARCHAR(20)
);

CREATE TABLE event.detector_event (
	event_id integer DEFAULT nextval('event_id_seq') NOT NULL,
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(10) REFERENCES iris._detector(name)
);

CREATE TABLE event.sign_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20),
	message text,
	iris_user VARCHAR(15) REFERENCES iris_user(name)
);

SET search_path = public, event, pg_catalog;

CREATE VIEW alarm_event_view AS
	SELECT e.event_id, e.event_date, ed.description AS event_description,
		e.alarm, a.description
	FROM event.alarm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN iris.alarm a ON e.alarm = a.name;
GRANT SELECT ON alarm_event_view TO PUBLIC;

CREATE VIEW comm_event_view AS
	SELECT e.event_id, e.event_date, ed.description,
		e.controller, c.comm_link, c.drop_id
	FROM comm_event e
	JOIN event_description ed ON e.event_desc_id = ed.event_desc_id
	LEFT JOIN controller c ON e.controller = c.name;
GRANT SELECT ON comm_event_view TO PUBLIC;

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE FUNCTION event.message_line(text, integer) RETURNS text AS
'DECLARE
	message ALIAS FOR $1;
	line ALIAS FOR $2;
	word text;
	wstop int2;
BEGIN
	word := message;

	FOR w in 1..(line-1) LOOP
		wstop := strpos(word, ''[nl]'');
		IF wstop > 0 THEN
			word := SUBSTR(word, wstop + 4);
		ELSE
			word := '''';
		END IF;
	END LOOP;
	wstop := strpos(word, ''[nl]'');
	IF wstop > 0 THEN
		word := SUBSTR(word, 0, wstop);
	END IF;
	RETURN word;
END;' LANGUAGE plpgsql;

CREATE VIEW sign_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id,
		message_line(e.message, 1) AS line1,
		message_line(e.message, 2) AS line2,
		message_line(e.message, 3) AS line3,
		e.iris_user
	FROM sign_event e
	JOIN event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
	SELECT * FROM sign_event_view
	WHERE (CURRENT_TIMESTAMP - event_date) < interval '90 days';
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

COPY event.event_description (event_desc_id, description) FROM stdin;
1	Alarm TRIGGERED
2	Alarm CLEARED
8	Comm ERROR
9	Comm RESTORED
65	Comm FAILED
89	LCS DEPLOYED
90	LCS CLEARED
91	Sign DEPLOYED
92	Sign CLEARED
94	NO HITS
95	LOCKED ON
96	CHATTER
\.
