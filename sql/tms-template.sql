--
-- PostgreSQL database template for IRIS
--

SET client_encoding = 'UTF8';

\set ON_ERROR_STOP

CREATE SCHEMA iris;
ALTER SCHEMA iris OWNER TO tms;

CREATE SCHEMA event;
ALTER SCHEMA event OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

SET search_path = public, pg_catalog;

CREATE TABLE iris.system_attribute (
	name VARCHAR(32) PRIMARY KEY,
	value VARCHAR(64) NOT NULL
);

CREATE TABLE iris.role (
	name VARCHAR(15) PRIMARY KEY,
	enabled BOOLEAN NOT NULL
);

CREATE TABLE iris.i_user (
	name VARCHAR(15) PRIMARY KEY,
	full_name VARCHAR(31) NOT NULL,
	password VARCHAR(64) NOT NULL,
	dn VARCHAR(64) NOT NULL,
	role VARCHAR(15) REFERENCES iris.role,
	enabled BOOLEAN NOT NULL
);

CREATE TABLE iris.capability (
	name VARCHAR(16) PRIMARY KEY,
	enabled BOOLEAN NOT NULL
);

CREATE TABLE iris.sonar_type (
	name VARCHAR(16) PRIMARY KEY
);

CREATE TABLE iris.privilege (
	name VARCHAR(8) PRIMARY KEY,
	capability VARCHAR(16) NOT NULL REFERENCES iris.capability,
	type_n VARCHAR(16) NOT NULL REFERENCES iris.sonar_type,
	obj_n VARCHAR(16) DEFAULT ''::VARCHAR NOT NULL,
	group_n VARCHAR(16) DEFAULT ''::VARCHAR NOT NULL,
	attr_n VARCHAR(16) DEFAULT ''::VARCHAR NOT NULL,
	write BOOLEAN DEFAULT false NOT NULL
);

CREATE TABLE iris.role_capability (
	role VARCHAR(15) NOT NULL REFERENCES iris.role,
	capability VARCHAR(16) NOT NULL REFERENCES iris.capability
);
ALTER TABLE iris.role_capability ADD PRIMARY KEY (role, capability);

CREATE TABLE iris.direction (
	id smallint PRIMARY KEY,
	direction VARCHAR(4) NOT NULL,
	dir VARCHAR(4) NOT NULL
);

CREATE TABLE iris.road_class (
	id integer PRIMARY KEY,
	description VARCHAR(12) NOT NULL,
	grade CHAR NOT NULL
);

CREATE TABLE iris.road_modifier (
	id smallint PRIMARY KEY,
	modifier text NOT NULL,
	mod VARCHAR(2) NOT NULL
);

CREATE TABLE iris.road (
	name VARCHAR(20) PRIMARY KEY,
	abbrev VARCHAR(6) NOT NULL,
	r_class smallint NOT NULL REFERENCES iris.road_class(id),
	direction smallint NOT NULL REFERENCES iris.direction(id),
	alt_dir smallint NOT NULL REFERENCES iris.direction(id)
);

CREATE TABLE iris.graphic (
	name VARCHAR(20) PRIMARY KEY,
	g_number INTEGER UNIQUE,
	bpp INTEGER NOT NULL,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	pixels TEXT NOT NULL
);

CREATE TABLE iris.font (
	name VARCHAR(16) PRIMARY KEY,
	f_number INTEGER UNIQUE NOT NULL,
	height INTEGER NOT NULL,
	width INTEGER NOT NULL,
	line_spacing INTEGER NOT NULL,
	char_spacing INTEGER NOT NULL,
	version_id INTEGER NOT NULL
);

CREATE TABLE iris.glyph (
	name VARCHAR(20) PRIMARY KEY,
	font VARCHAR(16) NOT NULL REFERENCES iris.font(name),
	code_point INTEGER NOT NULL,
	graphic VARCHAR(20) NOT NULL REFERENCES iris.graphic(name)
);

ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_bpp_ck
	CHECK (bpp = 1 OR bpp = 8 OR bpp = 24);
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

ALTER TABLE iris.font
	ADD CONSTRAINT font_height_ck
	CHECK (height > 0 AND height < 25);
ALTER TABLE iris.font
	ADD CONSTRAINT font_width_ck
	CHECK (width >= 0 AND width < 25);
ALTER TABLE iris.font
	ADD CONSTRAINT font_line_sp_ck
	CHECK (line_spacing >= 0 AND line_spacing < 9);
ALTER TABLE iris.font
	ADD CONSTRAINT font_char_sp_ck
	CHECK (char_spacing >= 0 AND char_spacing < 9);

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

CREATE TABLE iris.word (
	name VARCHAR(24) PRIMARY KEY,
	abbr VARCHAR(12),
	allowed BOOLEAN DEFAULT false NOT NULL
);

CREATE TABLE iris.day_matcher (
	name VARCHAR(32) PRIMARY KEY,
	holiday BOOLEAN NOT NULL,
	month INTEGER NOT NULL,
	day INTEGER NOT NULL,
	week INTEGER NOT NULL,
	weekday INTEGER NOT NULL,
	shift INTEGER NOT NULL
);

CREATE TABLE iris.day_plan (
	name VARCHAR(10) PRIMARY KEY
);

CREATE TABLE iris.day_plan_day_matcher (
	day_plan VARCHAR(10) NOT NULL REFERENCES iris.day_plan,
	day_matcher VARCHAR(32) NOT NULL REFERENCES iris.day_matcher
);
ALTER TABLE iris.day_plan_day_matcher ADD PRIMARY KEY (day_plan, day_matcher);

CREATE TABLE iris.plan_phase (
	name VARCHAR(12) PRIMARY KEY,
	hold_time INTEGER NOT NULL,
	next_phase VARCHAR(12) REFERENCES iris.plan_phase
);

CREATE TABLE iris.action_plan (
	name VARCHAR(16) PRIMARY KEY,
	description VARCHAR(64) NOT NULL,
	group_n VARCHAR(16),
	sync_actions BOOLEAN NOT NULL,
	sticky BOOLEAN NOT NULL,
	active BOOLEAN NOT NULL,
	default_phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,
	phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase
);

CREATE TABLE iris.geo_loc (
	name VARCHAR(20) PRIMARY KEY,
	roadway VARCHAR(20) REFERENCES iris.road(name),
	road_dir smallint REFERENCES iris.direction(id),
	cross_street VARCHAR(20) REFERENCES iris.road(name),
	cross_dir smallint REFERENCES iris.direction(id),
	cross_mod smallint REFERENCES iris.road_modifier(id),
	lat double precision,
	lon double precision,
	landmark VARCHAR(24)
);

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
		RETURN trim(concat(res || ' ', cross_mod || ' ', cross_street),
		            ' ' || cross_dir);
	ELSIF char_length(landmark) > 0 THEN
		RETURN concat(res || ' ', '(' || landmark || ')');
	ELSE
		RETURN res;
	END IF;
END;
$geo_location$ LANGUAGE plpgsql;

CREATE TABLE iris.map_extent (
	name VARCHAR(20) PRIMARY KEY,
	lat real NOT NULL,
	lon real NOT NULL,
	zoom INTEGER NOT NULL
);

CREATE TABLE iris.lane_type (
	id smallint PRIMARY KEY,
	description VARCHAR(12) NOT NULL,
	dcode VARCHAR(2) NOT NULL
);

CREATE TABLE iris.r_node_type (
	n_type integer PRIMARY KEY,
	name VARCHAR(12) NOT NULL
);

CREATE TABLE iris.r_node_transition (
	n_transition integer PRIMARY KEY,
	name VARCHAR(12) NOT NULL
);

CREATE TABLE iris.r_node (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
	node_type integer NOT NULL REFERENCES iris.r_node_type(n_type),
	pickable boolean NOT NULL,
	above boolean NOT NULL,
	transition integer NOT NULL REFERENCES iris.r_node_transition(n_transition),
	lanes integer NOT NULL,
	attach_side boolean NOT NULL,
	shift integer NOT NULL,
	active boolean NOT NULL,
	abandoned boolean NOT NULL,
	station_id VARCHAR(10),
	speed_limit integer NOT NULL,
	notes text NOT NULL
);

CREATE UNIQUE INDEX r_node_station_idx ON iris.r_node USING btree (station_id);

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
ALTER TABLE iris.r_node ADD CONSTRAINT active_ck
	CHECK (active = FALSE OR abandoned = FALSE);

CREATE TABLE iris.toll_zone (
	name VARCHAR(20) PRIMARY KEY,
	start_id VARCHAR(10) REFERENCES iris.r_node(station_id),
	end_id VARCHAR(10) REFERENCES iris.r_node(station_id),
	tollway VARCHAR(16),
	alpha REAL,
	beta REAL,
	max_price REAL
);

CREATE TABLE iris.sign_group (
	name VARCHAR(16) PRIMARY KEY,
	local BOOLEAN NOT NULL,
	hidden BOOLEAN NOT NULL
);

CREATE TABLE iris.dms_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

CREATE TABLE iris.sign_config (
	name VARCHAR(12) PRIMARY KEY,
	dms_type INTEGER NOT NULL REFERENCES iris.dms_type,
	portable BOOLEAN NOT NULL,
	technology VARCHAR(12) NOT NULL,
	sign_access VARCHAR(12) NOT NULL,
	legend VARCHAR(12) NOT NULL,
	beacon_type VARCHAR(32) NOT NULL,
	face_width INTEGER NOT NULL,
	face_height INTEGER NOT NULL,
	border_horiz INTEGER NOT NULL,
	border_vert INTEGER NOT NULL,
	pitch_horiz INTEGER NOT NULL,
	pitch_vert INTEGER NOT NULL,
	pixel_width INTEGER NOT NULL,
	pixel_height INTEGER NOT NULL,
	char_width INTEGER NOT NULL,
	char_height INTEGER NOT NULL,
	default_font VARCHAR(16) REFERENCES iris.font
);

CREATE TABLE iris.quick_message (
	name VARCHAR(20) PRIMARY KEY,
	sign_group VARCHAR(16) REFERENCES iris.sign_group,
	sign_config VARCHAR(12) REFERENCES iris.sign_config,
	prefix_page BOOLEAN NOT NULL,
	multi VARCHAR(1024) NOT NULL
);

CREATE TABLE iris.comm_protocol (
	id smallint PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

CREATE TABLE iris.comm_link (
	name VARCHAR(20) PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	modem BOOLEAN NOT NULL,
	uri VARCHAR(64) NOT NULL,
	protocol smallint NOT NULL REFERENCES iris.comm_protocol(id),
	poll_enabled BOOLEAN NOT NULL,
	poll_period INTEGER NOT NULL,
	timeout integer NOT NULL
);

CREATE TABLE iris.modem (
	name VARCHAR(20) PRIMARY KEY,
	uri VARCHAR(64) NOT NULL,
	config VARCHAR(64) NOT NULL,
	timeout INTEGER NOT NULL,
	enabled BOOLEAN NOT NULL
);

CREATE TABLE iris.cabinet_style (
	name VARCHAR(20) PRIMARY KEY,
	dip INTEGER
);

CREATE TABLE iris.cabinet (
	name VARCHAR(20) PRIMARY KEY,
	style VARCHAR(20) REFERENCES iris.cabinet_style(name),
	geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name)
);

CREATE TABLE iris.condition (
	id INTEGER PRIMARY KEY,
	description VARCHAR(12) NOT NULL
);

CREATE TABLE iris.controller (
	name VARCHAR(20) PRIMARY KEY,
	drop_id smallint NOT NULL,
	comm_link VARCHAR(20) NOT NULL REFERENCES iris.comm_link(name),
	cabinet VARCHAR(20) NOT NULL REFERENCES iris.cabinet(name),
	condition INTEGER NOT NULL REFERENCES iris.condition,
	password VARCHAR(32),
	notes VARCHAR(128) NOT NULL,
	fail_time timestamp WITH time zone,
	version VARCHAR(64)
);

CREATE UNIQUE INDEX ctrl_link_drop_idx ON iris.controller
	USING btree (comm_link, drop_id);

CREATE TABLE iris._device_io (
	name VARCHAR(20) PRIMARY KEY,
	controller VARCHAR(20) REFERENCES iris.controller(name),
	pin INTEGER NOT NULL
);

CREATE UNIQUE INDEX _device_io_ctrl_pin ON iris._device_io
	USING btree (controller, pin);

CREATE TABLE iris._alarm (
	name VARCHAR(20) PRIMARY KEY,
	description VARCHAR(24) NOT NULL,
	state BOOLEAN NOT NULL,
	trigger_time timestamp WITH time zone
);

ALTER TABLE iris._alarm ADD CONSTRAINT _alarm_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.alarm AS
	SELECT a.name, description, controller, pin, state, trigger_time
	FROM iris._alarm a JOIN iris._device_io d ON a.name = d.name;

CREATE FUNCTION iris.alarm_insert() RETURNS TRIGGER AS
	$alarm_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._alarm (name, description, state, trigger_time)
	     VALUES (NEW.name, NEW.description, NEW.state, NEW.trigger_time);
	RETURN NEW;
END;
$alarm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_insert_trig
    INSTEAD OF INSERT ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_insert();

CREATE FUNCTION iris.alarm_update() RETURNS TRIGGER AS
	$alarm_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._alarm
	   SET description = NEW.description,
	       state = NEW.state,
	       trigger_time = NEW.trigger_time
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$alarm_update$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_update_trig
    INSTEAD OF UPDATE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_update();

CREATE FUNCTION iris.alarm_delete() RETURNS TRIGGER AS
	$alarm_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$alarm_delete$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_delete_trig
    INSTEAD OF DELETE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_delete();

CREATE TABLE iris._gps (
	name VARCHAR(24) PRIMARY KEY,
	gps_enable BOOLEAN NOT NULL DEFAULT false,
	device_name VARCHAR(20) NOT NULL REFERENCES iris.geo_loc,
	device_class VARCHAR(20) NOT NULL,
	poll_datetime timestamp with time zone,
	sample_datetime timestamp with time zone,
	sample_lat double precision DEFAULT 0.0,
	sample_lon double precision DEFAULT 0.0,
	comm_status VARCHAR(25),
	error_status VARCHAR(50),
	jitter_tolerance_meters smallint DEFAULT 0
);

CREATE VIEW iris.gps AS
	SELECT g.name, d.controller, d.pin, g.gps_enable, g.device_name,
	       g.device_class, g.poll_datetime, g.sample_datetime, g.sample_lat,
	       g.sample_lon, g.comm_status, g.error_status,
	       g.jitter_tolerance_meters
	FROM iris._gps g
	JOIN iris._device_io d ON g.name = d.name;

CREATE FUNCTION iris.gps_insert() RETURNS trigger AS
	$gps_insert$
BEGIN
	INSERT INTO iris._gps
	            (name, gps_enable, device_name, device_class,
	             sample_datetime, sample_lat, sample_lon,
	             comm_status, error_status,
	             jitter_tolerance_meters)
	     VALUES (NEW.name, NEW.gps_enable, NEW.device_name,NEW.device_class,
	             NEW.sample_datetime, NEW.sample_lat, NEW.sample_lon,
	             NEW.comm_status, NEW.error_status,
	             NEW.jitter_tolerance_meters);
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, 0);
	RETURN NEW;
END;
$gps_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gps_insert_trig
	INSTEAD OF INSERT ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_insert();

CREATE FUNCTION iris.gps_update() RETURNS trigger AS
	$gps_update$
BEGIN
        UPDATE iris._device_io
           SET controller = NEW.controller,
               pin = NEW.pin
         WHERE name = OLD.name;
	UPDATE iris._gps
	   SET gps_enable = NEW.gps_enable,
	       device_name = NEW.device_name,
	       device_class = NEW.device_class,
	       poll_datetime = NEW.poll_datetime,
	       sample_datetime = NEW.sample_datetime,
	       sample_lat = NEW.sample_lat,
	       sample_lon = NEW.sample_lon,
	       comm_status = NEW.comm_status,
	       error_status = NEW.error_status,
	       jitter_tolerance_meters = NEW.jitter_tolerance_meters
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

CREATE TABLE iris._detector (
	name VARCHAR(20) PRIMARY KEY,
	r_node VARCHAR(10) NOT NULL REFERENCES iris.r_node(name),
	lane_type smallint NOT NULL REFERENCES iris.lane_type(id),
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

CREATE FUNCTION iris.detector_insert() RETURNS TRIGGER AS
	$detector_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._detector
	            (name, r_node, lane_type, lane_number, abandoned,
	             force_fail, field_length, fake, notes)
	     VALUES (NEW.name, NEW.r_node, NEW.lane_type, NEW.lane_number,
	             NEW.abandoned, NEW.force_fail, NEW.field_length, NEW.fake,
	             NEW.notes);
	RETURN NEW;
END;
$detector_insert$ LANGUAGE plpgsql;

CREATE TRIGGER detector_insert_trig
    INSTEAD OF INSERT ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_insert();

CREATE FUNCTION iris.detector_update() RETURNS TRIGGER AS
	$detector_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._detector
	   SET r_node = NEW.r_node,
	       lane_type = NEW.lane_type,
	       lane_number = NEW.lane_number,
	       abandoned = NEW.abandoned,
	       force_fail = NEW.force_fail,
	       field_length = NEW.field_length,
	       fake = NEW.fake,
	       notes = NEW.notes
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$detector_update$ LANGUAGE plpgsql;

CREATE TRIGGER detector_update_trig
    INSTEAD OF UPDATE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_update();

CREATE FUNCTION iris.detector_delete() RETURNS TRIGGER AS
	$detector_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$detector_delete$ LANGUAGE plpgsql;

CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_delete();

CREATE TABLE iris.encoding (
	id integer PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

CREATE TABLE iris.encoder_type (
	name VARCHAR(24) PRIMARY KEY,
	encoding INTEGER NOT NULL REFERENCES iris.encoding,
	uri_scheme VARCHAR(8) NOT NULL,
	uri_path VARCHAR(64) NOT NULL,
	latency INTEGER NOT NULL
);

CREATE TABLE iris._camera (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes text NOT NULL,
	cam_num INTEGER UNIQUE,
	encoder_type VARCHAR(24) REFERENCES iris.encoder_type,
	encoder VARCHAR(64) NOT NULL,
	enc_mcast VARCHAR(64) NOT NULL,
	encoder_channel INTEGER NOT NULL,
	publish BOOLEAN NOT NULL,
	video_loss BOOLEAN NOT NULL
);

ALTER TABLE iris._camera ADD CONSTRAINT _camera_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, cam_num, encoder_type, encoder,
		enc_mcast, encoder_channel, publish, video_loss
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, cam_num, encoder_type,
	            encoder, enc_mcast, encoder_channel, publish, video_loss)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num,
	             NEW.encoder_type, NEW.encoder, NEW.enc_mcast,
	             NEW.encoder_channel, NEW.publish, NEW.video_loss);
	RETURN NEW;
END;
$camera_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_insert();

CREATE FUNCTION iris.camera_update() RETURNS TRIGGER AS
	$camera_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._camera
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       cam_num = NEW.cam_num,
	       encoder_type = NEW.encoder_type,
	       encoder = NEW.encoder,
	       enc_mcast = NEW.enc_mcast,
	       encoder_channel = NEW.encoder_channel,
	       publish = NEW.publish,
	       video_loss = NEW.video_loss
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE FUNCTION iris.camera_delete() RETURNS TRIGGER AS
	$camera_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$camera_delete$ LANGUAGE plpgsql;

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_delete();

CREATE TABLE iris._cam_sequence (
	seq_num INTEGER PRIMARY KEY
);

CREATE TABLE iris._play_list (
	name VARCHAR(20) PRIMARY KEY,
	seq_num INTEGER REFERENCES iris._cam_sequence,
	description VARCHAR(32)
);

CREATE VIEW iris.play_list AS
	SELECT name, seq_num, description
	FROM iris._play_list;

CREATE FUNCTION iris.play_list_insert() RETURNS TRIGGER AS
	$play_list_insert$
BEGIN
	IF NEW.seq_num IS NOT NULL THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	INSERT INTO iris._play_list (name, seq_num, description)
	     VALUES (NEW.name, NEW.seq_num, NEW.description);
	RETURN NEW;
END;
$play_list_insert$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_insert_trig
    INSTEAD OF INSERT ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_insert();

CREATE FUNCTION iris.play_list_update() RETURNS TRIGGER AS
	$play_list_update$
BEGIN
	IF NEW.seq_num IS NOT NULL AND (OLD.seq_num IS NULL OR
	                                NEW.seq_num != OLD.seq_num)
	THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	UPDATE iris._play_list
	   SET seq_num = NEW.seq_num,
	       description = NEW.description
	 WHERE name = OLD.name;
	IF OLD.seq_num IS NOT NULL AND (NEW.seq_num IS NULL OR
	                                NEW.seq_num != OLD.seq_num)
	THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
	END IF;
	RETURN NEW;
END;
$play_list_update$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_update_trig
    INSTEAD OF UPDATE ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_update();

CREATE FUNCTION iris.play_list_delete() RETURNS TRIGGER AS
	$play_list_delete$
BEGIN
	DELETE FROM iris._play_list WHERE name = OLD.name;
	IF FOUND THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$play_list_delete$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_delete_trig
    INSTEAD OF DELETE ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_delete();

CREATE TABLE iris.play_list_camera (
	play_list VARCHAR(20) NOT NULL REFERENCES iris._play_list,
	ordinal INTEGER NOT NULL,
	camera VARCHAR(20) NOT NULL REFERENCES iris._camera
);
ALTER TABLE iris.play_list_camera ADD PRIMARY KEY (play_list, ordinal);

CREATE TABLE iris._catalog (
	name VARCHAR(20) PRIMARY KEY,
	seq_num INTEGER NOT NULL REFERENCES iris._cam_sequence,
	description VARCHAR(32)
);

CREATE VIEW iris.catalog AS
	SELECT name, seq_num, description
	FROM iris._catalog;

CREATE FUNCTION iris.catalog_insert() RETURNS TRIGGER AS
	$catalog_insert$
BEGIN
	INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	INSERT INTO iris._catalog (name, seq_num, description)
	     VALUES (NEW.name,NEW.seq_num, NEW.catalog);
	RETURN NEW;
END;
$catalog_insert$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_insert_trig
    INSTEAD OF INSERT ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_insert();

CREATE FUNCTION iris.catalog_update() RETURNS TRIGGER AS
	$catalog_update$
BEGIN
	IF NEW.seq_num != OLD.seq_num THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	UPDATE iris._catalog
	   SET seq_num = NEW.seq_num,
	       description = NEW.description
	 WHERE name = OLD.name;
	IF NEW.seq_num != OLD.seq_num THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
	END IF;
	RETURN NEW;
END;
$catalog_update$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_update_trig
    INSTEAD OF UPDATE ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_update();

CREATE FUNCTION iris.catalog_delete() RETURNS TRIGGER AS
	$catalog_delete$
BEGIN
	DELETE FROM iris._catalog WHERE name = OLD.name;
	IF FOUND THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$catalog_delete$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_delete_trig
    INSTEAD OF DELETE ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_delete();

CREATE TABLE iris.catalog_play_list (
	catalog VARCHAR(20) NOT NULL REFERENCES iris._catalog,
	ordinal INTEGER NOT NULL,
	play_list VARCHAR(20) NOT NULL REFERENCES iris._play_list
);
ALTER TABLE iris.catalog_play_list ADD PRIMARY KEY (catalog, ordinal);

CREATE TABLE iris.monitor_style (
	name VARCHAR(24) PRIMARY KEY,
	force_aspect BOOLEAN NOT NULL,
	accent VARCHAR(8) NOT NULL,
	font_sz INTEGER NOT NULL,
	title_bar BOOLEAN NOT NULL,
	auto_expand BOOLEAN NOT NULL,
	hgap INTEGER NOT NULL,
	vgap INTEGER NOT NULL
);

CREATE TABLE iris._video_monitor (
	name VARCHAR(12) PRIMARY KEY,
	notes VARCHAR(32) NOT NULL,
	group_n VARCHAR(16),
	mon_num INTEGER NOT NULL,
	restricted BOOLEAN NOT NULL,
	monitor_style VARCHAR(24) REFERENCES iris.monitor_style,
	camera VARCHAR(20) REFERENCES iris._camera
);

ALTER TABLE iris._video_monitor ADD CONSTRAINT _video_monitor_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.video_monitor AS
	SELECT m.name, controller, pin, notes, group_n, mon_num, restricted,
	       monitor_style, camera
	FROM iris._video_monitor m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
	$video_monitor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._video_monitor (name, notes, group_n, mon_num,
	                                 restricted, monitor_style, camera)
	     VALUES (NEW.name, NEW.notes, NEW.group_n, NEW.mon_num,
	             NEW.restricted, NEW.monitor_style, NEW.camera);
	RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_insert();

CREATE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
	$video_monitor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._video_monitor
	   SET notes = NEW.notes,
	       group_n = NEW.group_n,
	       mon_num = NEW.mon_num,
	       restricted = NEW.restricted,
	       monitor_style = NEW.monitor_style,
	       camera = NEW.camera
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_update();

CREATE FUNCTION iris.video_monitor_delete() RETURNS TRIGGER AS
	$video_monitor_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$video_monitor_delete$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_delete();

CREATE TABLE iris.camera_preset (
	name VARCHAR(20) PRIMARY KEY,
	camera VARCHAR(20) NOT NULL REFERENCES iris._camera,
	preset_num INTEGER NOT NULL CHECK (preset_num > 0 AND preset_num <= 12),
	direction SMALLINT REFERENCES iris.direction(id),
	UNIQUE(camera, preset_num)
);

-- Ideally, _device_preset would be combined with _device_io,
-- but that would create a circular dependency.
CREATE TABLE iris._device_preset (
	name VARCHAR(20) PRIMARY KEY,
	preset VARCHAR(20) UNIQUE REFERENCES iris.camera_preset(name)
);

CREATE TABLE iris._beacon (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes text NOT NULL,
	message text NOT NULL,
	verify_pin INTEGER -- FIXME: make unique on _device_io_ctrl_pin
);

ALTER TABLE iris._beacon ADD CONSTRAINT _beacon_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.beacon AS
	SELECT b.name, geo_loc, controller, pin, notes, message, verify_pin,
	       preset
	FROM iris._beacon b
	JOIN iris._device_io d ON b.name = d.name
	JOIN iris._device_preset p ON b.name = p.name;

CREATE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
	$beacon_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	    VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._device_preset (name, preset)
	    VALUES (NEW.name, NEW.preset);
	INSERT INTO iris._beacon (name, geo_loc, notes, message, verify_pin)
	    VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message,
	            NEW.verify_pin);
	RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_insert();

CREATE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
	$beacon_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._device_preset
	   SET preset = NEW.preset
	 WHERE name = OLD.name;
	UPDATE iris._beacon
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       message = NEW.message,
	       verify_pin = NEW.verify_pin
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
	DELETE FROM iris._device_preset WHERE name = OLD.name;
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

CREATE TABLE iris.meter_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	lanes INTEGER NOT NULL
);

CREATE TABLE iris.meter_algorithm (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

CREATE TABLE iris.meter_lock (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE iris._ramp_meter (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes text NOT NULL,
	meter_type INTEGER NOT NULL REFERENCES iris.meter_type(id),
	storage INTEGER NOT NULL,
	max_wait INTEGER NOT NULL,
	algorithm INTEGER NOT NULL REFERENCES iris.meter_algorithm,
	am_target INTEGER NOT NULL,
	pm_target INTEGER NOT NULL,
	beacon VARCHAR(20) REFERENCES iris._beacon,
	m_lock INTEGER REFERENCES iris.meter_lock(id)
);

ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.ramp_meter AS
	SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
	       max_wait, algorithm, am_target, pm_target, beacon, preset, m_lock
	FROM iris._ramp_meter m
	JOIN iris._device_io d ON m.name = d.name
	JOIN iris._device_preset p ON m.name = p.name;

CREATE FUNCTION iris.ramp_meter_insert() RETURNS TRIGGER AS
	$ramp_meter_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._device_preset (name, preset)
	     VALUES (NEW.name, NEW.preset);
	INSERT INTO iris._ramp_meter
	            (name, geo_loc, notes, meter_type, storage, max_wait,
	             algorithm, am_target, pm_target, beacon, m_lock)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type,
	             NEW.storage, NEW.max_wait, NEW.algorithm, NEW.am_target,
	             NEW.pm_target, NEW.beacon, NEW.m_lock);
	RETURN NEW;
END;
$ramp_meter_insert$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_insert();

CREATE FUNCTION iris.ramp_meter_update() RETURNS TRIGGER AS
	$ramp_meter_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._device_preset
	   SET preset = NEW.preset
	 WHERE name = OLD.name;
	UPDATE iris._ramp_meter
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       meter_type = NEW.meter_type,
	       storage = NEW.storage,
	       max_wait = NEW.max_wait,
	       algorithm = NEW.algorithm,
	       am_target = NEW.am_target,
	       pm_target = NEW.pm_target,
	       beacon = NEW.beacon,
	       m_lock = NEW.m_lock
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$ramp_meter_update$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_update_trig
    INSTEAD OF UPDATE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_update();

CREATE FUNCTION iris.ramp_meter_delete() RETURNS TRIGGER AS
	$ramp_meter_delete$
BEGIN
	DELETE FROM iris._device_preset WHERE name = OLD.name;
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$ramp_meter_delete$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_delete();

CREATE TABLE iris.sign_msg_source (
	bit INTEGER PRIMARY KEY,
	source VARCHAR(16) NOT NULL
);
ALTER TABLE iris.sign_msg_source ADD CONSTRAINT msg_source_bit_ck
	CHECK (bit >= 0 AND bit < 32);

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

CREATE TABLE iris.sign_message (
	name VARCHAR(20) PRIMARY KEY,
	incident VARCHAR(16),
	multi VARCHAR(1024) NOT NULL,
	beacon_enabled BOOLEAN NOT NULL,
	prefix_page BOOLEAN NOT NULL,
	msg_priority INTEGER NOT NULL,
	source INTEGER NOT NULL,
	owner VARCHAR(15),
	duration INTEGER
);

CREATE TABLE iris._dms (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc,
	notes text NOT NULL,
	static_graphic VARCHAR(20) REFERENCES iris.graphic,
	beacon VARCHAR(20) REFERENCES iris._beacon,
	sign_config VARCHAR(12) REFERENCES iris.sign_config,
	default_font VARCHAR(16) REFERENCES iris.font,
	msg_sched VARCHAR(20) REFERENCES iris.sign_message,
	msg_current VARCHAR(20) REFERENCES iris.sign_message NOT NULL,
	deploy_time timestamp WITH time zone NOT NULL
);

ALTER TABLE iris._dms ADD CONSTRAINT _dms_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, static_graphic, beacon,
	       preset, sign_config, default_font, msg_sched, msg_current,
	       deploy_time
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
	INSERT INTO iris._dms (name, geo_loc, notes, static_graphic, beacon,
	                       sign_config, default_font, msg_sched, msg_current,
	                       deploy_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.static_graphic,
	             NEW.beacon, NEW.sign_config, NEW.default_font,
	             NEW.msg_sched, NEW.msg_current, NEW.deploy_time);
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

CREATE TABLE iris._lane_marking (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes VARCHAR(64) NOT NULL
);

ALTER TABLE iris._lane_marking ADD CONSTRAINT _lane_marking_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lane_marking AS SELECT
	m.name, geo_loc, controller, pin, notes
	FROM iris._lane_marking m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.lane_marking_insert() RETURNS TRIGGER AS
	$lane_marking_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lane_marking (name, geo_loc, notes)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes);
	RETURN NEW;
END;
$lane_marking_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_insert_trig
    INSTEAD OF INSERT ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_insert();

CREATE FUNCTION iris.lane_marking_update() RETURNS TRIGGER AS
	$lane_marking_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._lane_marking
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$lane_marking_update$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_update_trig
    INSTEAD OF UPDATE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_update();

CREATE FUNCTION iris.lane_marking_delete() RETURNS TRIGGER AS
	$lane_marking_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$lane_marking_delete$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_delete_trig
    INSTEAD OF DELETE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_delete();

CREATE TABLE iris._weather_sensor (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes VARCHAR(64) NOT NULL
);

ALTER TABLE iris._weather_sensor ADD CONSTRAINT _weather_sensor_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.weather_sensor AS SELECT
	m.name, geo_loc, controller, pin, notes
	FROM iris._weather_sensor m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.weather_sensor_insert() RETURNS TRIGGER AS
	$weather_sensor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._weather_sensor (name, geo_loc, notes)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes);
	RETURN NEW;
END;
$weather_sensor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_insert_trig
    INSTEAD OF INSERT ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_insert();

CREATE FUNCTION iris.weather_sensor_update() RETURNS TRIGGER AS
	$weather_sensor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._weather_sensor
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$weather_sensor_update$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_update_trig
    INSTEAD OF UPDATE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_update();

CREATE FUNCTION iris.weather_sensor_delete() RETURNS TRIGGER AS
	$weather_sensor_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$weather_sensor_delete$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_delete();

CREATE TABLE iris._tag_reader (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes VARCHAR(64) NOT NULL,
	toll_zone VARCHAR(20) REFERENCES iris.toll_zone(name)
);

ALTER TABLE iris._tag_reader ADD CONSTRAINT _tag_reader_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.tag_reader AS SELECT
	t.name, geo_loc, controller, pin, notes, toll_zone
	FROM iris._tag_reader t JOIN iris._device_io d ON t.name = d.name;

CREATE FUNCTION iris.tag_reader_insert() RETURNS TRIGGER AS
	$tag_reader_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._tag_reader (name, geo_loc, notes, toll_zone)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.toll_zone);
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
	       toll_zone = NEW.toll_zone
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

CREATE TABLE iris.tag_reader_dms (
	tag_reader VARCHAR(20) NOT NULL REFERENCES iris._tag_reader,
	dms VARCHAR(20) NOT NULL REFERENCES iris._dms
);
ALTER TABLE iris.tag_reader_dms ADD PRIMARY KEY (tag_reader, dms);

CREATE TABLE iris.lcs_lock (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE iris._lcs_array (
	name VARCHAR(20) PRIMARY KEY,
	notes text NOT NULL,
	shift INTEGER NOT NULL,
	lcs_lock INTEGER REFERENCES iris.lcs_lock(id)
);

ALTER TABLE iris._lcs_array ADD CONSTRAINT _lcs_array_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lcs_array AS SELECT
	d.name, controller, pin, notes, shift, lcs_lock
	FROM iris._lcs_array la JOIN iris._device_io d ON la.name = d.name;

CREATE FUNCTION iris.lcs_array_insert() RETURNS TRIGGER AS
	$lcs_array_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_array(name, notes, shift, lcs_lock)
	     VALUES (NEW.name, NEW.notes, NEW.shift, NEW.lcs_lock);
	RETURN NEW;
END;
$lcs_array_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_insert_trig
    INSTEAD OF INSERT ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_insert();

CREATE FUNCTION iris.lcs_array_update() RETURNS TRIGGER AS
	$lcs_array_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._lcs_array
	   SET notes = NEW.notes,
	       shift = NEW.shift,
	       lcs_lock = NEW.lcs_lock
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$lcs_array_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_update_trig
    INSTEAD OF UPDATE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_update();

CREATE FUNCTION iris.lcs_array_delete() RETURNS TRIGGER AS
	$lcs_array_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$lcs_array_delete$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_delete_trig
    INSTEAD OF DELETE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_delete();

CREATE TABLE iris.lcs (
	name VARCHAR(20) PRIMARY KEY REFERENCES iris._dms,
	lcs_array VARCHAR(20) NOT NULL REFERENCES iris._lcs_array,
	lane INTEGER NOT NULL
);

CREATE UNIQUE INDEX lcs_array_lane ON iris.lcs USING btree (lcs_array, lane);

CREATE TABLE iris.lane_use_indication (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

CREATE TABLE iris._lcs_indication (
	name VARCHAR(20) PRIMARY KEY,
	lcs VARCHAR(20) NOT NULL REFERENCES iris.lcs,
	indication INTEGER NOT NULL REFERENCES iris.lane_use_indication
);

ALTER TABLE iris._lcs_indication ADD CONSTRAINT _lcs_indication_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lcs_indication AS SELECT
	d.name, controller, pin, lcs, indication
	FROM iris._lcs_indication li JOIN iris._device_io d ON li.name = d.name;

CREATE FUNCTION iris.lcs_indication_insert() RETURNS TRIGGER AS
	$lcs_indication_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_indication(name, lcs, indication)
	     VALUES (NEW.name, NEW.lcs, NEW.indication);
	RETURN NEW;
END;
$lcs_indication_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_insert_trig
    INSTEAD OF INSERT ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_insert();

CREATE FUNCTION iris.lcs_indication_update() RETURNS TRIGGER AS
	$lcs_indication_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._lcs_indication
	   SET lcs = NEW.lcs,
	       indication = NEW.indication
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$lcs_indication_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_update_trig
    INSTEAD OF UPDATE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_update();

CREATE FUNCTION iris.lcs_indication_delete() RETURNS TRIGGER AS
	$lcs_indication_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$lcs_indication_delete$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_delete_trig
    INSTEAD OF DELETE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_delete();

CREATE TABLE iris._gate_arm_array (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc,
	notes VARCHAR(64) NOT NULL,
	prereq VARCHAR(20) REFERENCES iris._gate_arm_array,
	camera VARCHAR(20) REFERENCES iris._camera,
	approach VARCHAR(20) REFERENCES iris._camera,
	action_plan VARCHAR(16) REFERENCES iris.action_plan,
	open_phase VARCHAR(12) REFERENCES iris.plan_phase,
	closed_phase VARCHAR(12) REFERENCES iris.plan_phase
);

ALTER TABLE iris._gate_arm_array ADD CONSTRAINT _gate_arm_array_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.gate_arm_array AS
	SELECT _gate_arm_array.name, geo_loc, controller, pin, notes, prereq,
	       camera, approach, action_plan, open_phase, closed_phase
	FROM iris._gate_arm_array JOIN iris._device_io
	ON _gate_arm_array.name = _device_io.name;

CREATE FUNCTION iris.gate_arm_array_insert() RETURNS TRIGGER AS
	$gate_arm_array_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._gate_arm_array (name, geo_loc, notes, prereq, camera,
	                                  approach, action_plan, open_phase,
	                                  closed_phase)
	    VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.prereq, NEW.camera,
	            NEW.approach, NEW.action_plan, NEW.open_phase,
	            NEW.closed_phase);
	RETURN NEW;
END;
$gate_arm_array_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_insert();

CREATE FUNCTION iris.gate_arm_array_update() RETURNS TRIGGER AS
	$gate_arm_array_update$
BEGIN
	UPDATE iris._device_io SET controller = NEW.controller, pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._gate_arm_array
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       prereq = NEW.prereq,
	       camera = NEW.camera,
	       approach = NEW.approach,
	       action_plan = NEW.action_plan,
	       open_phase = NEW.open_phase,
	       closed_phase = NEW.closed_phase
	WHERE name = OLD.name;
	RETURN NEW;
END;
$gate_arm_array_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_update();

CREATE FUNCTION iris.gate_arm_array_delete() RETURNS TRIGGER AS
	$gate_arm_array_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$gate_arm_array_delete$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_array_delete();

CREATE TABLE iris._gate_arm (
	name VARCHAR(20) PRIMARY KEY,
	ga_array VARCHAR(20) NOT NULL REFERENCES iris._gate_arm_array,
	idx INTEGER NOT NULL,
	notes VARCHAR(32) NOT NULL
);

ALTER TABLE iris._gate_arm ADD CONSTRAINT _gate_arm_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE UNIQUE INDEX gate_arm_array_idx ON iris._gate_arm
	USING btree (ga_array, idx);

CREATE VIEW iris.gate_arm AS
	SELECT _gate_arm.name, ga_array, idx, controller, pin, notes
	FROM iris._gate_arm JOIN iris._device_io
	ON _gate_arm.name = _device_io.name;

CREATE FUNCTION iris.gate_arm_update() RETURNS TRIGGER AS $gate_arm_update$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO iris._device_io (name, controller, pin)
            VALUES (NEW.name, NEW.controller, NEW.pin);
        INSERT INTO iris._gate_arm (name, ga_array, idx, notes)
            VALUES (NEW.name, NEW.ga_array, NEW.idx, NEW.notes);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
	UPDATE iris._device_io SET controller = NEW.controller, pin = NEW.pin
	WHERE name = OLD.name;
        UPDATE iris._gate_arm SET ga_array = NEW.ga_array, idx = NEW.idx,
            notes = NEW.notes
	WHERE name = OLD.name;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM iris._device_io WHERE name = OLD.name;
        IF FOUND THEN
            RETURN OLD;
        ELSE
            RETURN NULL;
	END IF;
    END IF;
    RETURN NEW;
END;
$gate_arm_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_update_trig
    INSTEAD OF INSERT OR UPDATE OR DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_update();

CREATE TABLE iris.parking_area (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
	preset_1 VARCHAR(20) REFERENCES iris.camera_preset(name),
	preset_2 VARCHAR(20) REFERENCES iris.camera_preset(name),
	preset_3 VARCHAR(20) REFERENCES iris.camera_preset(name),
	-- static site data
	site_id VARCHAR(25) UNIQUE,
	time_stamp_static timestamp WITH time zone,
	relevant_highway VARCHAR(10),
	reference_post VARCHAR(10),
	exit_id VARCHAR(10),
	facility_name VARCHAR(30),
	street_adr VARCHAR(30),
	city VARCHAR(30),
	state VARCHAR(2),
	zip VARCHAR(10),
	time_zone VARCHAR(10),
	ownership VARCHAR(2),
	capacity INTEGER,
	low_threshold INTEGER,
	amenities INTEGER,
	-- dynamic site data
	time_stamp timestamp WITH time zone,
	reported_available VARCHAR(8),
	true_available INTEGER,
	trend VARCHAR(8),
	open BOOLEAN,
	trust_data BOOLEAN,
	last_verification_check timestamp WITH time zone,
	verification_check_amplitude INTEGER
);

CREATE TABLE iris.parking_area_amenities (
	bit INTEGER PRIMARY KEY,
	amenity VARCHAR(32) NOT NULL
);
ALTER TABLE iris.parking_area_amenities ADD CONSTRAINT amenity_bit_ck
	CHECK (bit >= 0 AND bit < 32);

CREATE FUNCTION iris.parking_area_amenities(INTEGER)
	RETURNS SETOF iris.parking_area_amenities AS $parking_area_amenities$
DECLARE
	ms RECORD;
	b INTEGER;
BEGIN
	FOR ms IN SELECT bit, amenity FROM iris.parking_area_amenities LOOP
		b = 1 << ms.bit;
		IF ($1 & b) = b THEN
			RETURN NEXT ms;
		END IF;
	END LOOP;
END;
$parking_area_amenities$ LANGUAGE plpgsql;

CREATE TABLE iris.dms_sign_group (
	name VARCHAR(28) PRIMARY KEY,
	dms VARCHAR(20) NOT NULL REFERENCES iris._dms,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group
);

CREATE TABLE iris.sign_text (
	name VARCHAR(20) PRIMARY KEY,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	line smallint NOT NULL,
	multi VARCHAR(64) NOT NULL,
	rank smallint NOT NULL,
	CONSTRAINT sign_text_line CHECK ((line >= 1) AND (line <= 12)),
	CONSTRAINT sign_text_rank CHECK ((rank >= 1) AND (rank <= 99))
);

CREATE TABLE iris.lane_use_multi (
	name VARCHAR(10) PRIMARY KEY,
	indication INTEGER NOT NULL REFERENCES iris.lane_use_indication,
	msg_num INTEGER,
	width INTEGER NOT NULL,
	height INTEGER NOT NULL,
	quick_message VARCHAR(20) REFERENCES iris.quick_message
);

CREATE UNIQUE INDEX lane_use_multi_indication_idx ON iris.lane_use_multi
	USING btree (indication, width, height);

CREATE UNIQUE INDEX lane_use_multi_msg_num_idx ON iris.lane_use_multi
	USING btree (msg_num, width, height);

CREATE TABLE iris.time_action (
	name VARCHAR(30) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	day_plan VARCHAR(10) REFERENCES iris.day_plan,
	sched_date DATE,
	time_of_day TIME WITHOUT TIME ZONE NOT NULL,
	phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,
	CONSTRAINT time_action_date CHECK (
		((day_plan IS NULL) OR (sched_date IS NULL)) AND
		((day_plan IS NOT NULL) OR (sched_date IS NOT NULL))
	)
);

CREATE TABLE iris.dms_action (
	name VARCHAR(30) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,
	quick_message VARCHAR(20) REFERENCES iris.quick_message,
	beacon_enabled BOOLEAN NOT NULL,
	msg_priority INTEGER NOT NULL
);

CREATE TABLE iris.beacon_action (
	name VARCHAR(30) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	beacon VARCHAR(20) NOT NULL REFERENCES iris._beacon,
	phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase
);

CREATE TABLE iris.lane_action (
	name VARCHAR(30) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	lane_marking VARCHAR(20) NOT NULL REFERENCES iris._lane_marking,
	phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase
);

CREATE TABLE iris.meter_action (
	name VARCHAR(30) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	ramp_meter VARCHAR(20) NOT NULL REFERENCES iris._ramp_meter,
	phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase
);

-- Helper function to check and update database version from migrate scripts
CREATE FUNCTION iris.update_version(TEXT, TEXT) RETURNS TEXT AS
	$update_version$
DECLARE
	ver_prev ALIAS FOR $1;
	ver_new ALIAS FOR $2;
	ver_db TEXT;
BEGIN
	SELECT value INTO ver_db FROM iris.system_attribute
		WHERE name = 'database_version';
	IF ver_db != ver_prev THEN
		RAISE EXCEPTION 'Cannot migrate database -- wrong version: %',
			ver_db;
	END IF;
	UPDATE iris.system_attribute SET value = ver_new
		WHERE name = 'database_version';
	RETURN ver_new;
END;
$update_version$ language plpgsql;

CREATE SEQUENCE event.event_id_seq;

CREATE TABLE event.event_description (
	event_desc_id INTEGER PRIMARY KEY,
	description text NOT NULL
);

CREATE TABLE event.action_plan_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	action_plan VARCHAR(16) NOT NULL,
	detail VARCHAR(15) NOT NULL
);

CREATE TABLE event.alarm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	alarm VARCHAR(20) NOT NULL REFERENCES iris._alarm(name)
		ON DELETE CASCADE
);

CREATE TABLE event.brightness_sample (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	dms VARCHAR(20) NOT NULL REFERENCES iris._dms(name)
		ON DELETE CASCADE,
	photocell integer NOT NULL,
	output integer NOT NULL
);

CREATE TABLE event.comm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	controller VARCHAR(20) NOT NULL REFERENCES iris.controller(name)
		ON DELETE CASCADE,
	device_id VARCHAR(20)
);

CREATE TABLE event.detector_event (
	event_id integer DEFAULT nextval('event.event_id_seq') NOT NULL,
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20) REFERENCES iris._detector(name)
		ON DELETE CASCADE
);

CREATE TABLE event.sign_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20),
	message text,
	iris_user VARCHAR(15)
);

CREATE INDEX ON event.sign_event(event_date);

CREATE TABLE event.client_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	host_port VARCHAR(64) NOT NULL,
	iris_user VARCHAR(15)
);

CREATE TABLE event.gate_arm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20),
	iris_user VARCHAR(15)
);

CREATE TABLE event.meter_phase (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE event.meter_queue_state (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE event.meter_limit_control (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE event.meter_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	ramp_meter VARCHAR(20) NOT NULL REFERENCES iris._ramp_meter
		ON DELETE CASCADE,
	phase INTEGER NOT NULL REFERENCES event.meter_phase,
	q_state INTEGER NOT NULL REFERENCES event.meter_queue_state,
	q_len REAL NOT NULL,
	dem_adj REAL NOT NULL,
	wait_secs INTEGER NOT NULL,
	limit_ctrl INTEGER NOT NULL REFERENCES event.meter_limit_control,
	min_rate INTEGER NOT NULL,
	rel_rate INTEGER NOT NULL,
	max_rate INTEGER NOT NULL,
	d_node VARCHAR(10),
	seg_density REAL NOT NULL
);

CREATE VIEW meter_event_view AS
	SELECT event_id, event_date, event_description.description,
	       ramp_meter, meter_phase.description AS phase,
	       meter_queue_state.description AS q_state, q_len, dem_adj,
	       wait_secs, meter_limit_control.description AS limit_ctrl,
	       min_rate, rel_rate, max_rate, d_node, seg_density
	FROM event.meter_event
	JOIN event.event_description
	ON meter_event.event_desc_id = event_description.event_desc_id
	JOIN event.meter_phase ON phase = meter_phase.id
	JOIN event.meter_queue_state ON q_state = meter_queue_state.id
	JOIN event.meter_limit_control ON limit_ctrl = meter_limit_control.id;
GRANT SELECT ON meter_event_view TO PUBLIC;

CREATE TABLE event.beacon_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	beacon VARCHAR(20) NOT NULL REFERENCES iris._beacon
		ON DELETE CASCADE
);

CREATE VIEW beacon_event_view AS
	SELECT event_id, event_date, event_description.description, beacon
	FROM event.beacon_event
	JOIN event.event_description
	ON beacon_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON beacon_event_view TO PUBLIC;

CREATE TABLE event.travel_time_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20)
);

CREATE VIEW travel_time_event_view AS
	SELECT event_id, event_date, event_description.description, device_id
	FROM event.travel_time_event
	JOIN event.event_description
	ON travel_time_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON travel_time_event_view TO PUBLIC;

CREATE TABLE event.camera_switch_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	monitor_id VARCHAR(12),
	camera_id VARCHAR(20),
	source VARCHAR(20)
);

CREATE VIEW camera_switch_event_view AS
	SELECT event_id, event_date, event_description.description, monitor_id,
	       camera_id, source
	FROM event.camera_switch_event
	JOIN event.event_description
	ON camera_switch_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON camera_switch_event_view TO PUBLIC;

CREATE TABLE event.tag_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE event.tag_read_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	tag_type INTEGER NOT NULL REFERENCES event.tag_type,
	agency INTEGER,
	tag_id INTEGER NOT NULL,
	tag_reader VARCHAR(20) NOT NULL,
	hov BOOLEAN NOT NULL,
	trip_id INTEGER
);

CREATE INDEX ON event.tag_read_event(tag_id);

CREATE VIEW tag_read_event_view AS
	SELECT event_id, event_date, event_description.description,
	       tag_type.description AS tag_type, agency, tag_id, tag_reader,
	       toll_zone, tollway, hov, trip_id
	FROM event.tag_read_event
	JOIN event.event_description
	ON   tag_read_event.event_desc_id = event_description.event_desc_id
	JOIN event.tag_type
	ON   tag_read_event.tag_type = tag_type.id
	JOIN iris.tag_reader
	ON   tag_read_event.tag_reader = tag_reader.name
	LEFT JOIN iris.toll_zone
	ON        tag_reader.toll_zone = toll_zone.name;
GRANT SELECT ON tag_read_event_view TO PUBLIC;

-- Allow trip_id column to be updated by roles which have been granted
-- update permission on tag_read_event_view
CREATE FUNCTION event.tag_read_event_view_update() RETURNS TRIGGER AS
	$tag_read_event_view_update$
BEGIN
	UPDATE event.tag_read_event
	   SET trip_id = NEW.trip_id
	 WHERE event_id = OLD.event_id;
	RETURN NEW;
END;
$tag_read_event_view_update$ LANGUAGE plpgsql;

CREATE TRIGGER tag_read_event_view_update_trig
    INSTEAD OF UPDATE ON tag_read_event_view
    FOR EACH ROW EXECUTE PROCEDURE event.tag_read_event_view_update();

CREATE TABLE event.price_message_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20) NOT NULL,
	toll_zone VARCHAR(20) NOT NULL,
	price NUMERIC(4,2) NOT NULL
);

CREATE INDEX ON event.price_message_event(event_date);
CREATE INDEX ON event.price_message_event(device_id);

CREATE VIEW price_message_event_view AS
	SELECT event_id, event_date, event_description.description,
	       device_id, toll_zone, price
	FROM event.price_message_event
	JOIN event.event_description
	ON price_message_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON price_message_event_view TO PUBLIC;

CREATE TABLE event.incident_detail (
	name VARCHAR(8) PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

CREATE TABLE event.incident (
	event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	name VARCHAR(16) NOT NULL UNIQUE,
	replaces VARCHAR(16) REFERENCES event.incident(name),
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	detail VARCHAR(8) REFERENCES event.incident_detail(name),
	lane_type smallint NOT NULL REFERENCES iris.lane_type(id),
	road VARCHAR(20) NOT NULL,
	dir SMALLINT NOT NULL REFERENCES iris.direction(id),
	lat double precision NOT NULL,
	lon double precision NOT NULL,
	camera VARCHAR(20),
	impact VARCHAR(20) NOT NULL,
	cleared BOOLEAN NOT NULL,
	confirmed BOOLEAN NOT NULL
);

CREATE TABLE event.incident_update (
	event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	incident VARCHAR(16) NOT NULL REFERENCES event.incident(name),
	event_date timestamp WITH time zone NOT NULL,
	impact VARCHAR(20) NOT NULL,
	cleared BOOLEAN NOT NULL,
	confirmed BOOLEAN NOT NULL
);

CREATE FUNCTION event.incident_update_trig() RETURNS TRIGGER AS
$incident_update_trig$
BEGIN
    INSERT INTO event.incident_update
               (incident, event_date, impact, cleared, confirmed)
        VALUES (NEW.name, now(), NEW.impact, NEW.cleared, NEW.confirmed);
    RETURN NEW;
END;
$incident_update_trig$ LANGUAGE plpgsql;

CREATE TRIGGER incident_update_trigger
	AFTER INSERT OR UPDATE ON event.incident
	FOR EACH ROW EXECUTE PROCEDURE event.incident_update_trig();

CREATE TABLE iris.inc_descriptor (
	name VARCHAR(10) PRIMARY KEY,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	lane_type SMALLINT NOT NULL REFERENCES iris.lane_type(id),
	detail VARCHAR(8) REFERENCES event.incident_detail(name),
	cleared BOOLEAN NOT NULL,
	multi VARCHAR(64) NOT NULL,
	abbrev VARCHAR(32)
);

CREATE FUNCTION iris.inc_descriptor_ck() RETURNS TRIGGER AS
	$inc_descriptor_ck$
BEGIN
	-- Only incident event IDs are allowed
	IF NEW.event_desc_id < 21 OR NEW.event_desc_id > 24 THEN
		RAISE EXCEPTION 'invalid incident event_desc_id';
	END IF;
	-- Only mainline, cd road, merge and exit lane types are allowed
	IF NEW.lane_type != 1 AND NEW.lane_type != 3 AND
	   NEW.lane_type != 5 AND NEW.lane_type != 7 THEN
		RAISE EXCEPTION 'invalid incident lane_type';
	END IF;
	RETURN NEW;
END;
$inc_descriptor_ck$ LANGUAGE plpgsql;

CREATE TRIGGER inc_descriptor_ck_trig
	BEFORE INSERT OR UPDATE ON iris.inc_descriptor
	FOR EACH ROW EXECUTE PROCEDURE iris.inc_descriptor_ck();

CREATE TABLE iris.inc_range (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

CREATE TABLE iris.inc_locator (
	name VARCHAR(10) PRIMARY KEY,
	range INTEGER NOT NULL REFERENCES iris.inc_range(id),
	branched BOOLEAN NOT NULL,
	pickable BOOLEAN NOT NULL,
	multi VARCHAR(64) NOT NULL,
	abbrev VARCHAR(32)
);

CREATE TABLE iris.inc_advice (
	name VARCHAR(10) PRIMARY KEY,
	range INTEGER NOT NULL REFERENCES iris.inc_range(id),
	lane_type SMALLINT NOT NULL REFERENCES iris.lane_type(id),
	impact VARCHAR(20) NOT NULL,
	cleared BOOLEAN NOT NULL,
	multi VARCHAR(64) NOT NULL,
	abbrev VARCHAR(32)
);

CREATE FUNCTION iris.inc_advice_ck() RETURNS TRIGGER AS
	$inc_advice_ck$
BEGIN
	-- Only mainline, cd road, merge and exit lane types are allowed
	IF NEW.lane_type != 1 AND NEW.lane_type != 3 AND
	   NEW.lane_type != 5 AND NEW.lane_type != 7 THEN
		RAISE EXCEPTION 'invalid incident lane_type';
	END IF;
	RETURN NEW;
END;
$inc_advice_ck$ LANGUAGE plpgsql;

CREATE TRIGGER inc_advice_ck_trig
	BEFORE INSERT OR UPDATE ON iris.inc_advice
	FOR EACH ROW EXECUTE PROCEDURE iris.inc_advice_ck();

--- Views

CREATE VIEW role_privilege_view AS
	SELECT role, role_capability.capability, type_n, obj_n, group_n, attr_n,
	       write
	FROM iris.role
	JOIN iris.role_capability ON role.name = role_capability.role
	JOIN iris.capability ON role_capability.capability = capability.name
	JOIN iris.privilege ON privilege.capability = capability.name
	WHERE role.enabled = 't' AND capability.enabled = 't';
GRANT SELECT ON role_privilege_view TO PUBLIC;

CREATE VIEW i_user_view AS
	SELECT name, full_name, dn, role, enabled
	FROM iris.i_user;
GRANT SELECT ON i_user_view TO PUBLIC;

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

CREATE VIEW action_plan_view AS
	SELECT name, description, group_n, sync_actions, sticky, active,
	       default_phase, phase
	FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

CREATE VIEW time_action_view AS
	SELECT name, action_plan, day_plan, sched_date, time_of_day, phase
	FROM iris.time_action;
GRANT SELECT ON time_action_view TO PUBLIC;

CREATE VIEW dms_action_view AS
	SELECT name, action_plan, sign_group, phase, quick_message,
	       beacon_enabled, msg_priority
	FROM iris.dms_action;
GRANT SELECT ON dms_action_view TO PUBLIC;

CREATE VIEW meter_action_view AS
	SELECT ramp_meter, ta.phase, time_of_day, day_plan, sched_date
	FROM iris.meter_action ma, iris.action_plan ap, iris.time_action ta
	WHERE ma.action_plan = ap.name
	AND ap.name = ta.action_plan
	AND active = true
	ORDER BY ramp_meter, time_of_day;
GRANT SELECT ON meter_action_view TO PUBLIC;

CREATE VIEW road_view AS
	SELECT name, abbrev, rcl.description AS r_class, dir.direction,
	adir.direction AS alt_dir
	FROM iris.road r
	LEFT JOIN iris.road_class rcl ON r.r_class = rcl.id
	LEFT JOIN iris.direction dir ON r.direction = dir.id
	LEFT JOIN iris.direction adir ON r.alt_dir = adir.id;
GRANT SELECT ON road_view TO PUBLIC;

CREATE VIEW geo_loc_view AS
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

CREATE VIEW r_node_view AS
	SELECT n.name, roadway, road_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, n.above,
	tr.name AS transition, n.lanes, n.attach_side, n.shift, n.active,
	n.abandoned, n.station_id, n.speed_limit, n.notes
	FROM iris.r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN iris.r_node_type nt ON n.node_type = nt.n_type
	JOIN iris.r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

CREATE VIEW roadway_station_view AS
	SELECT station_id, roadway, road_dir, cross_mod, cross_street, active,
	speed_limit
	FROM iris.r_node r, geo_loc_view l
	WHERE r.geo_loc = l.name AND station_id IS NOT NULL;
GRANT SELECT ON roadway_station_view TO PUBLIC;

CREATE VIEW toll_zone_view AS
	SELECT name, start_id, end_id, tollway, alpha, beta, max_price
	FROM iris.toll_zone;
GRANT SELECT ON toll_zone_view TO PUBLIC;

CREATE VIEW iris.quick_message_priced AS
    SELECT name AS quick_message, 'priced'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzp,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzp%';

CREATE VIEW iris.quick_message_open AS
    SELECT name AS quick_message, 'open'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzo,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzo%';

CREATE VIEW iris.quick_message_closed AS
    SELECT name AS quick_message, 'closed'::VARCHAR(6) AS state,
        unnest(string_to_array(substring(multi FROM '%tzc,#"[^]]*#"]%' FOR '#'),
        ',')) AS toll_zone
    FROM iris.quick_message WHERE multi LIKE '%tzc%';

CREATE VIEW iris.quick_message_toll_zone AS
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_priced UNION ALL
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_open UNION ALL
    SELECT quick_message, state, toll_zone
        FROM iris.quick_message_closed;

CREATE VIEW dms_toll_zone_view AS
    SELECT dms, state, toll_zone, action_plan, dms_action_view.quick_message
    FROM dms_action_view
    JOIN iris.dms_sign_group
    ON dms_action_view.sign_group = dms_sign_group.sign_group
    JOIN iris.quick_message
    ON dms_action_view.quick_message = quick_message.name
    JOIN iris.quick_message_toll_zone
    ON dms_action_view.quick_message = quick_message_toll_zone.quick_message;
GRANT SELECT ON dms_toll_zone_view TO PUBLIC;

CREATE VIEW controller_view AS
	SELECT c.name, drop_id, comm_link, cabinet,
	       cnd.description AS condition, notes, cab.geo_loc, fail_time,
	       version
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN iris.condition cnd ON c.condition = cnd.id;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW controller_loc_view AS
	SELECT c.name, drop_id, comm_link, cabinet, condition, c.notes,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM controller_view c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW quick_message_view AS
	SELECT name, sign_group, sign_config, prefix_page, multi
	FROM iris.quick_message;
GRANT SELECT ON quick_message_view TO PUBLIC;

CREATE VIEW sign_config_view AS
	SELECT name, description AS dms_type, portable, technology, sign_access,
	       legend, beacon_type, face_width, face_height, border_horiz,
	       border_vert, pitch_horiz, pitch_vert, pixel_width, pixel_height,
	       char_width, char_height, default_font
	FROM iris.sign_config
	JOIN iris.dms_type ON sign_config.dms_type = dms_type.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.static_graphic,
	       d.beacon, p.camera, p.preset_num, d.sign_config,
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

CREATE VIEW lcs_array_view AS
	SELECT name, shift, notes, lcs_lock
	FROM iris.lcs_array;
GRANT SELECT ON lcs_array_view TO PUBLIC;

CREATE VIEW lcs_view AS
	SELECT name, lcs_array, lane
	FROM iris.lcs;
GRANT SELECT ON lcs_view TO PUBLIC;

CREATE VIEW lcs_indication_view AS
	SELECT name, controller, pin, lcs, description AS indication
	FROM iris.lcs_indication
	JOIN iris.lane_use_indication ON indication = id;
GRANT SELECT ON lcs_indication_view TO PUBLIC;

CREATE VIEW ramp_meter_view AS
	SELECT m.name, geo_loc, controller, pin, notes,
	       mt.description AS meter_type, storage, max_wait,
	       alg.description AS algorithm, am_target, pm_target, beacon,
	       camera, preset_num, ml.description AS meter_lock,
	       l.rd, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, l.lat, l.lon
	FROM iris.ramp_meter m
	LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
	LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
	LEFT JOIN iris.camera_preset p ON m.preset = p.name
	LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW monitor_style_view AS
	SELECT name, force_aspect, accent, font_sz, title_bar, auto_expand,
	       hgap, vgap
	FROM iris.monitor_style;
GRANT SELECT ON monitor_style_view TO PUBLIC;

CREATE VIEW video_monitor_view AS
	SELECT m.name, m.notes, group_n, mon_num, restricted, monitor_style,
	       m.controller, m.pin, ctr.condition, ctr.comm_link, camera
	FROM iris.video_monitor m
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

CREATE VIEW encoder_type_view AS
	SELECT name, enc.description AS encoding, uri_scheme, uri_path, latency
	FROM iris.encoder_type et
	LEFT JOIN iris.encoding enc ON et.encoding = enc.id;
GRANT SELECT ON encoder_type_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, cam_num, encoder_type, c.encoder, c.enc_mcast,
	       c.encoder_channel, c.publish, c.video_loss, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.location, l.lat, l.lon,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.camera c
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW play_list_view AS
	SELECT play_list, ordinal, seq_num, camera
	FROM iris.play_list_camera
	JOIN iris.play_list ON play_list_camera.play_list = play_list.name;
GRANT SELECT ON play_list_view TO PUBLIC;

CREATE VIEW camera_preset_view AS
	SELECT cp.name, camera, preset_num, direction, dp.name AS device
	FROM iris.camera_preset cp
	JOIN iris._device_preset dp ON cp.name = dp.preset;
GRANT SELECT ON camera_preset_view TO PUBLIC;

CREATE VIEW beacon_view AS
	SELECT b.name, b.notes, b.message, p.camera, p.preset_num, b.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon,
	       b.controller, b.pin, b.verify_pin, ctr.comm_link, ctr.drop_id,
	       ctr.condition
	FROM iris.beacon b
	LEFT JOIN iris.camera_preset p ON b.preset = p.name
	LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
	LEFT JOIN controller_view ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

CREATE VIEW lane_marking_view AS
	SELECT m.name, m.notes, m.geo_loc, l.roadway, l.road_dir, l.cross_mod,
	       l.cross_street, l.cross_dir, l.lat, l.lon,
	       m.controller, m.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.lane_marking m
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
	SELECT w.name, w.notes, w.geo_loc, l.roadway, l.road_dir, l.cross_mod,
	       l.cross_street, l.cross_dir, l.lat, l.lon,
	       w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller_view ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

CREATE VIEW tag_reader_view AS
	SELECT t.name, t.notes, t.toll_zone, t.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       t.controller, t.pin, ctr.comm_link, ctr.drop_id, ctr.condition
	FROM iris.tag_reader t
	LEFT JOIN geo_loc_view l ON t.geo_loc = l.name
	LEFT JOIN controller_view ctr ON t.controller = ctr.name;
GRANT SELECT ON tag_reader_view TO PUBLIC;

CREATE VIEW tag_reader_dms_view AS
	SELECT tag_reader, dms
	FROM iris.tag_reader_dms;
GRANT SELECT ON tag_reader_dms_view TO PUBLIC;

CREATE VIEW gate_arm_array_view AS
	SELECT ga.name, ga.notes, ga.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       ga.controller, ga.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach, ga.action_plan, ga.open_phase,
	       ga.closed_phase
	FROM iris.gate_arm_array ga
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON ga.controller = ctr.name;
GRANT SELECT ON gate_arm_array_view TO PUBLIC;

CREATE VIEW gate_arm_view AS
	SELECT g.name, g.ga_array, g.notes, ga.geo_loc, l.roadway, l.road_dir,
	       l.cross_mod, l.cross_street, l.cross_dir, l.lat, l.lon,
	       g.controller, g.pin, ctr.comm_link, ctr.drop_id, ctr.condition,
	       ga.prereq, ga.camera, ga.approach
	FROM iris.gate_arm g
	JOIN iris.gate_arm_array ga ON g.ga_array = ga.name
	LEFT JOIN geo_loc_view l ON ga.geo_loc = l.name
	LEFT JOIN controller_view ctr ON g.controller = ctr.name;
GRANT SELECT ON gate_arm_view TO PUBLIC;

CREATE VIEW parking_area_view AS
	SELECT pa.name, site_id, time_stamp_static, relevant_highway,
	       reference_post, exit_id, facility_name, street_adr, city, state,
	       zip, time_zone, ownership, capacity, low_threshold,
	       (SELECT string_agg(a.amenity, ', ') FROM
	        (SELECT bit, amenity FROM iris.parking_area_amenities(amenities)
	         ORDER BY bit) AS a) AS amenities,
	       time_stamp, reported_available, true_available, trend, open,
	       trust_data, last_verification_check, verification_check_amplitude,
	       p1.camera AS camera_1, p2.camera AS camera_2,
	       p3.camera AS camera_3,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon, sa.value AS camera_image_base_url
	FROM iris.parking_area pa
	LEFT JOIN iris.camera_preset p1 ON preset_1 = p1.name
	LEFT JOIN iris.camera_preset p2 ON preset_2 = p2.name
	LEFT JOIN iris.camera_preset p3 ON preset_3 = p3.name
	LEFT JOIN geo_loc_view l ON pa.geo_loc = l.name
	LEFT JOIN iris.system_attribute sa ON sa.name = 'camera_image_base_url';
GRANT SELECT ON parking_area_view TO PUBLIC;

CREATE VIEW lane_type_view AS
	SELECT id, description, dcode FROM iris.lane_type;
GRANT SELECT ON lane_type_view TO PUBLIC;

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

CREATE VIEW detector_fail_view AS SELECT DISTINCT ON (device_id)
	device_id, description AS fail_reason, event_date AS fail_date
	FROM event.detector_event de
	JOIN event.event_description ed ON de.event_desc_id = ed.event_desc_id
	ORDER BY device_id, event_id DESC;
GRANT SELECT ON detector_fail_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
	       iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
	       d.lane_type, d.lane_number, d.abandoned) AS label,
	       rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, d.lane_number, d.field_length,
	       ln.description AS lane_type, d.abandoned, d.force_fail,
	       df.fail_reason, df.fail_date, c.condition, d.fake, d.notes
	FROM (iris.detector d
	LEFT OUTER JOIN detector_fail_view df
		ON d.name = df.device_id AND force_fail = 't')
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN controller_view c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.trigger_time, a.controller,
		a.pin, c.comm_link, c.drop_id
	FROM iris.alarm a LEFT JOIN iris.controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, multi, rank
	FROM iris.dms_sign_group dsg
	JOIN iris.sign_group sg ON dsg.sign_group = sg.name
	JOIN iris.sign_text st ON sg.name = st.sign_group;
GRANT SELECT ON sign_text_view TO PUBLIC;

CREATE VIEW cabinet_view AS
	SELECT name, style, geo_loc
	FROM iris.cabinet;
GRANT SELECT ON cabinet_view TO PUBLIC;

CREATE VIEW device_controller_view AS
	SELECT name, controller, pin
	FROM iris._device_io;
GRANT SELECT ON device_controller_view TO PUBLIC;

CREATE VIEW iris.device_geo_loc_view AS
	SELECT name, geo_loc FROM iris._lane_marking UNION ALL
	SELECT name, geo_loc FROM iris._beacon UNION ALL
	SELECT name, geo_loc FROM iris._weather_sensor UNION ALL
	SELECT name, geo_loc FROM iris._tag_reader UNION ALL
	SELECT name, geo_loc FROM iris._camera UNION ALL
	SELECT name, geo_loc FROM iris._dms UNION ALL
	SELECT name, geo_loc FROM iris._ramp_meter UNION ALL
	SELECT g.name, geo_loc FROM iris._gate_arm g
	JOIN iris._gate_arm_array ga ON g.ga_array = ga.name UNION ALL
	SELECT d.name, geo_loc FROM iris._detector d
	JOIN iris.r_node rn ON d.r_node = rn.name;

CREATE VIEW controller_device_view AS
	SELECT d.name, d.controller, d.pin, g.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) AS corridor,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_loc
	FROM iris._device_io d
	JOIN iris.device_geo_loc_view g ON d.name = g.name
	JOIN geo_loc_view l ON g.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, l.landmark, cab.geo_loc,
	       l.location, cab.style AS "type", d.name AS device, d.pin,
	       d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

CREATE VIEW comm_link_view AS
	SELECT cl.name, cl.description, modem, uri, cp.description AS protocol,
	       poll_enabled, poll_period, timeout
	FROM iris.comm_link cl
	JOIN iris.comm_protocol cp ON cl.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;

CREATE VIEW modem_view AS
	SELECT name, uri, config, timeout, enabled
	FROM iris.modem;
GRANT SELECT ON modem_view TO PUBLIC;

CREATE VIEW action_plan_event_view AS
	SELECT e.event_id, e.event_date, ed.description AS event_description,
		e.action_plan, e.detail
	FROM event.action_plan_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON action_plan_event_view TO PUBLIC;

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
	FROM event.comm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	LEFT JOIN iris.controller c ON e.controller = c.name;
GRANT SELECT ON comm_event_view TO PUBLIC;

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE VIEW sign_event_view AS
	SELECT event_id, event_date, description, device_id,
	       regexp_replace(replace(replace(message, '[nl]', E'\n'), '[np]',
	                      E'\n'), '\[.+?\]', ' ', 'g') AS message,
	       message AS multi, iris_user
	FROM event.sign_event JOIN event.event_description
	ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
	SELECT event_id, event_date, description, device_id, message, multi,
	       iris_user
	FROM sign_event_view
	WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

CREATE VIEW client_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.host_port,
		e.iris_user
	FROM event.client_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON client_event_view TO PUBLIC;

CREATE VIEW gate_arm_event_view AS
	SELECT e.event_id, e.event_date, ed.description, device_id, e.iris_user
	FROM event.gate_arm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON gate_arm_event_view TO PUBLIC;

CREATE VIEW incident_view AS
    SELECT event_id, name, event_date, ed.description, road, d.direction,
           impact, cleared, confirmed, camera, ln.description AS lane_type,
           detail, replaces, lat, lon
    FROM event.incident i
    LEFT JOIN event.event_description ed ON i.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_type ln ON i.lane_type = ln.id;
GRANT SELECT ON incident_view TO PUBLIC;

CREATE VIEW incident_update_view AS
    SELECT iu.event_id, name, iu.event_date, ed.description, road,
           d.direction, iu.impact, iu.cleared, iu.confirmed, camera,
           ln.description AS lane_type, detail, replaces, lat, lon
    FROM event.incident i
    JOIN event.incident_update iu ON i.name = iu.incident
    LEFT JOIN event.event_description ed ON i.event_desc_id = ed.event_desc_id
    LEFT JOIN iris.direction d ON i.dir = d.id
    LEFT JOIN iris.lane_type ln ON i.lane_type = ln.id;
GRANT SELECT ON incident_update_view TO PUBLIC;

--- Data

COPY iris.direction (id, direction, dir) FROM stdin;
0		
1	NB	N
2	SB	S
3	EB	E
4	WB	W
5	N-S	N-S
6	E-W	E-W
7	IN	IN
8	OUT	OUT
\.

COPY iris.road_class (id, description, grade) FROM stdin;
0		
1	residential	A
2	business	B
3	collector	C
4	arterial	D
5	expressway	E
6	freeway	F
7	CD road	
\.

COPY iris.road_modifier (id, modifier, mod) FROM stdin;
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

COPY iris.comm_protocol (id, description) FROM stdin;
0	NTCIP Class B
1	MnDOT 170 (4-bit)
2	MnDOT 170 (5-bit)
3	SmartSensor 105
4	Canoga
5	Pelco P
6	Pelco D PTZ
7	NTCIP Class C
8	Manchester PTZ
9	DMS XML
10	MSG_FEED
11	NTCIP Class A
12	Banner DXM
13	Vicon PTZ
14	SmartSensor 125 HD
15	OSi ORG-815
16	Infinova D PTZ
17	RTMS G4
18	RTMS
19	Infotek Wizard
20	Sensys
21	PeMS
22	SSI
23	CHP Incidents
24	URMS
25	DLI DIN Relay
26	Axis 292
27	Axis PTZ
28	HySecurity STC
29	Cohu PTZ
30	DR-500
31	ADDCO
32	TransCore E6
33	CBW
34	Incident Feed
35	MonStream
36	Gate NDORv5
37	GPS TAIP
38	GPS NMEA
39	GPS RedLion
\.

COPY iris.cabinet_style (name, dip) FROM stdin;
\.

COPY iris.condition (id, description) FROM stdin;
0	Planned
1	Active
2	Construction
3	Removed
4	Testing
\.

COPY iris.lane_type (id, description, dcode) FROM stdin;
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
16	Shoulder	D
17	Parking	PK
\.

COPY iris.dms_type (id, description) FROM stdin;
0	Unknown
1	Other
2	BOS (blank-out sign)
3	CMS (changeable message sign)
4	VMS Character-matrix
5	VMS Line-matrix
6	VMS Full-matrix
\.

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

COPY iris.lane_use_indication (id, description) FROM stdin;
0	Dark
1	Lane open
2	Use caution
3	Lane closed ahead
4	Lane closed
5	HOV / HOT
6	Merge right
7	Merge left
8	Merge left or right
9	Must exit right
10	Must exit left
11	Advisory variable speed limit
12	Variable speed limit
13	Low visibility
14	HOV / HOT begins
\.

COPY iris.lcs_lock (id, description) FROM stdin;
1	Incident
2	Maintenance
3	Testing
4	Other reason
\.

COPY iris.meter_type (id, description, lanes) FROM stdin;
0	One Lane	1
1	Two Lane, Alternate Release	2
2	Two Lane, Simultaneous Release	2
\.

COPY iris.meter_algorithm (id, description) FROM stdin;
0	No Metering
1	Simple Metering
2	SZM (obsolete)
3	K Adaptive Metering
\.

COPY iris.meter_lock (id, description) FROM stdin;
1	Maintenance
2	Incident
3	Construction
4	Testing
5	Police panel
6	Manual mode
\.

COPY iris.encoding (id, description) FROM stdin;
0	UNKNOWN
1	MJPEG
2	MPEG2
3	MPEG4
4	H264
5	H265
\.

COPY iris.system_attribute (name, value) FROM stdin;
action_plan_alert_list	
action_plan_event_purge_days	90
camera_autoplay	true
camera_construction_url	
camera_image_base_url	
camera_kbd_panasonic_enable	false
camera_num_blank	999
camera_out_of_service_url	
camera_sequence_dwell_sec	5
camera_preset_store_enable	false
camera_ptz_blind	true
camera_stream_controls_enable	false
camera_switch_event_purge_days	30
camera_wiper_precip_mm_hr	8
client_units_si	true
comm_event_purge_days	14
comm_idle_disconnect_dms_sec	-1
comm_idle_disconnect_gps_sec	5
comm_idle_disconnect_modem_sec	20
database_version	4.76.0
detector_auto_fail_enable	true
dict_allowed_scheme	0
dict_banned_scheme	0
dms_brightness_enable	true
dms_comm_loss_enable	true
dms_composer_edit_mode	1
dms_default_justification_line	3
dms_default_justification_page	2
dms_duration_enable	true
dms_font_selection_enable	false
dms_high_temp_cutoff	60
dms_lamp_test_timeout_secs	30
dms_manufacturer_enable	true
dms_max_lines	3
dms_message_min_pages	1
dms_page_off_default_secs	0.0
dms_page_on_default_secs	2.0
dms_page_on_max_secs	10.0
dms_page_on_min_secs	0.5
dms_page_on_selection_enable	false
dms_pixel_off_limit	2
dms_pixel_on_limit	1
dms_pixel_maint_threshold	35
dms_pixel_status_enable	true
dms_pixel_test_timeout_secs	30
dms_querymsg_enable	false
dms_quickmsg_store_enable	false
dms_reset_enable	false
dms_send_confirmation_enable	false
dms_update_font_table	true
dmsxml_modem_op_timeout_secs	305
dmsxml_op_timeout_secs	65
dmsxml_reinit_detect	false
email_sender_server	
email_smtp_host	
email_recipient_action_plan	
email_recipient_aws	
email_recipient_dmsxml_reinit	
email_recipient_gate_arm	
gate_arm_alert_timeout_secs	90
help_trouble_ticket_enable	false
help_trouble_ticket_url	
incident_clear_secs	600
map_extent_name_initial	Home
map_icon_size_scale_max	30
map_segment_max_meters	2000
meter_event_purge_days	14
meter_green_secs	1.3
meter_max_red_secs	13.0
meter_min_red_secs	0.1
meter_yellow_secs	0.7
msg_feed_verify	true
operation_retry_threshold	3
route_max_legs	8
route_max_miles	16
rwis_high_wind_speed_kph	40
rwis_low_visibility_distance_m	152
rwis_obs_age_limit_secs	240
rwis_max_valid_wind_speed_kph	282
sample_archive_enable	true
speed_limit_min_mph	45
speed_limit_default_mph	55
speed_limit_max_mph	75
toll_density_alpha	0.045
toll_density_beta	1.1
toll_min_price	0.25
toll_max_price	8
travel_time_min_mph	15
uptime_log_enable	false
vsa_bottleneck_id_mph	55
vsa_control_threshold	-1000
vsa_downstream_miles	0.2
vsa_max_display_mph	60
vsa_min_display_mph	30
vsa_min_station_miles	0.1
vsa_start_intervals	3
vsa_start_threshold	-1500
vsa_stop_threshold	-750
window_title	IRIS: 
work_request_url	
\.

COPY iris.r_node_type (n_type, name) FROM stdin;
0	station
1	entrance
2	exit
3	intersection
4	access
5	interchange
\.

COPY iris.r_node_transition (n_transition, name) FROM stdin;
0	none
1	loop
2	leg
3	slipramp
4	CD
5	HOV
6	common
7	flyover
\.

COPY iris.parking_area_amenities (bit, amenity) FROM stdin;
0	Flush toilet
1	Assisted restroom
2	Drinking fountain
3	Shower
4	Picnic table
5	Picnic shelter
6	Pay phone
7	TTY pay phone
8	Wireless internet
9	ATM
10	Vending machine
11	Shop
12	Play area
13	Pet excercise area
14	Interpretive information
\.

COPY iris.capability (name, enabled) FROM stdin;
base	t
base_admin	t
base_policy	t
beacon_admin	t
beacon_control	t
beacon_tab	t
camera_admin	t
camera_control	t
camera_policy	t
camera_tab	t
comm_admin	t
comm_control	t
comm_tab	t
dms_admin	t
dms_control	t
dms_policy	t
dms_tab	t
gate_arm_admin	t
gate_arm_control	t
gate_arm_tab	t
incident_admin	t
incident_control	t
incident_tab	t
lcs_admin	t
lcs_control	t
lcs_tab	t
meter_admin	t
meter_control	t
meter_tab	t
plan_admin	t
plan_control	t
plan_tab	t
sensor_admin	t
sensor_control	t
sensor_tab	t
toll_admin	t
toll_tab	t
parking_admin	t
parking_tab	t
\.

COPY iris.sonar_type (name) FROM stdin;
action_plan
alarm
beacon
beacon_action
cabinet
cabinet_style
camera
camera_preset
capability
catalog
comm_link
connection
controller
day_matcher
day_plan
detector
dms
dms_action
dms_sign_group
encoder_type
font
gate_arm
gate_arm_array
geo_loc
glyph
gps
graphic
inc_advice
inc_descriptor
incident
incident_detail
inc_locator
lane_action
lane_marking
lane_use_multi
lcs
lcs_array
lcs_indication
map_extent
meter_action
modem
monitor_style
parking_area
plan_phase
play_list
privilege
quick_message
ramp_meter
r_node
road
role
sign_config
sign_group
sign_message
sign_text
station
system_attribute
tag_reader
time_action
toll_zone
user
video_monitor
weather_sensor
word
\.

COPY iris.privilege (name, capability, type_n, attr_n, write) FROM stdin;
PRV_0001	base	user		f
PRV_0002	base	role		f
PRV_0003	base	capability		f
PRV_0004	base	privilege		f
PRV_0005	base	connection		f
PRV_0006	base	system_attribute		f
PRV_0007	base	map_extent		f
PRV_0008	base	road		f
PRV_0009	base	geo_loc		f
PRV_0010	base	cabinet		f
PRV_0011	base	controller		f
PRV_0012	base_admin	user		t
PRV_0013	base_admin	role		t
PRV_0014	base_admin	privilege		t
PRV_0015	base_admin	capability		t
PRV_0016	base_admin	connection		t
PRV_0017	base_policy	geo_loc		t
PRV_0018	base_policy	map_extent		t
PRV_0019	base_policy	road		t
PRV_0020	base_policy	system_attribute		t
PRV_0021	beacon_admin	beacon		t
PRV_0022	beacon_control	beacon	flashing	t
PRV_0023	beacon_tab	beacon		f
PRV_0024	camera_admin	camera		t
PRV_0025	camera_admin	encoder_type		t
PRV_0026	camera_admin	camera_preset		t
PRV_0027	camera_admin	video_monitor		t
PRV_0028	camera_admin	monitor_style		t
PRV_002C	camera_admin	play_list		t
PRV_002D	camera_admin	catalog		t
PRV_0029	camera_control	camera	ptz	t
PRV_0030	camera_control	camera	recallPreset	t
PRV_0031	camera_control	camera	deviceRequest	t
PRV_0032	camera_policy	camera	publish	t
PRV_0033	camera_policy	camera	storePreset	t
PRV_0034	camera_tab	encoder_type		f
PRV_0035	camera_tab	camera		f
PRV_0036	camera_tab	camera_preset		f
PRV_0037	camera_tab	video_monitor		f
PRV_0038	camera_tab	monitor_style		f
PRV_003C	camera_tab	play_list		f
PRV_003E	camera_tab	catalog		f
PRV_0039	comm_admin	comm_link		t
PRV_0040	comm_admin	modem		t
PRV_0041	comm_admin	cabinet_style		t
PRV_0042	comm_admin	cabinet		t
PRV_0043	comm_admin	controller		t
PRV_0044	comm_admin	alarm		t
PRV_0045	comm_control	controller	condition	t
PRV_0046	comm_control	controller	download	t
PRV_0047	comm_control	controller	counters	t
PRV_0048	comm_tab	comm_link		f
PRV_0049	comm_tab	modem		f
PRV_0050	comm_tab	alarm		f
PRV_0051	comm_tab	cabinet_style		f
PRV_0052	dms_admin	dms		t
PRV_0053	dms_admin	font		t
PRV_0054	dms_admin	glyph		t
PRV_0055	dms_admin	gps		t
PRV_0056	dms_admin	graphic		t
PRV_0057	dms_admin	sign_config		t
PRV_0058	dms_control	dms	msgUser	t
PRV_0059	dms_control	dms	deviceRequest	t
PRV_0060	dms_control	sign_message		t
PRV_0061	dms_policy	dms_sign_group		t
PRV_0062	dms_policy	quick_message		t
PRV_0063	dms_policy	sign_group		t
PRV_0064	dms_policy	sign_text		t
PRV_0065	dms_policy	word		t
PRV_0066	dms_tab	dms		f
PRV_0067	dms_tab	dms_sign_group		f
PRV_0068	dms_tab	font		f
PRV_0069	dms_tab	glyph		f
PRV_0070	dms_tab	gps		f
PRV_0071	dms_tab	graphic		f
PRV_0072	dms_tab	quick_message		f
PRV_0073	dms_tab	sign_config		f
PRV_0074	dms_tab	sign_group		f
PRV_0075	dms_tab	sign_message		f
PRV_0076	dms_tab	sign_text		f
PRV_0077	dms_tab	word		f
PRV_0078	gate_arm_admin	gate_arm		t
PRV_0079	gate_arm_admin	gate_arm_array		t
PRV_0080	gate_arm_control	gate_arm_array	armStateNext	t
PRV_0081	gate_arm_control	gate_arm_array	ownerNext	t
PRV_0082	gate_arm_control	gate_arm_array	deviceRequest	t
PRV_0083	gate_arm_tab	gate_arm		f
PRV_0084	gate_arm_tab	gate_arm_array		f
PRV_0085	gate_arm_tab	camera		f
PRV_0086	gate_arm_tab	encoder_type		f
PRV_0087	incident_admin	incident_detail		t
PRV_0088	incident_admin	inc_descriptor		t
PRV_0089	incident_admin	inc_locator		t
PRV_0090	incident_admin	inc_advice		t
PRV_0091	incident_control	incident		t
PRV_0092	incident_tab	incident		f
PRV_0093	incident_tab	incident_detail		f
PRV_0094	incident_tab	inc_descriptor		f
PRV_0095	incident_tab	inc_locator		f
PRV_0096	incident_tab	inc_advice		f
PRV_0097	lcs_admin	lane_use_multi		t
PRV_0098	lcs_admin	lcs		t
PRV_0099	lcs_admin	lcs_array		t
PRV_0100	lcs_admin	lcs_indication		t
PRV_0101	lcs_admin	lane_marking		t
PRV_0102	lcs_control	lcs_array	indicationsNext	t
PRV_0103	lcs_control	lcs_array	ownerNext	t
PRV_0104	lcs_control	lcs_array	lcsLock	t
PRV_0105	lcs_control	lcs_array	deviceRequest	t
PRV_0106	lcs_tab	dms		f
PRV_0107	lcs_tab	lane_use_multi		f
PRV_0108	lcs_tab	lcs		f
PRV_0109	lcs_tab	lcs_array		f
PRV_0110	lcs_tab	lcs_indication		f
PRV_0111	lcs_tab	quick_message		f
PRV_0112	lcs_tab	lane_marking		f
PRV_0113	meter_admin	ramp_meter		t
PRV_0114	meter_control	ramp_meter	mLock	t
PRV_0115	meter_control	ramp_meter	rateNext	t
PRV_0116	meter_control	ramp_meter	deviceRequest	t
PRV_0117	meter_tab	ramp_meter		f
PRV_0118	plan_admin	action_plan		t
PRV_0119	plan_admin	day_plan		t
PRV_0120	plan_admin	day_matcher		t
PRV_0121	plan_admin	plan_phase		t
PRV_0122	plan_admin	time_action		t
PRV_0123	plan_admin	dms_action		t
PRV_0124	plan_admin	beacon_action		t
PRV_0125	plan_admin	lane_action		t
PRV_0126	plan_admin	meter_action		t
PRV_0127	plan_control	action_plan	phase	t
PRV_0128	plan_tab	action_plan		f
PRV_0129	plan_tab	day_plan		f
PRV_0130	plan_tab	day_matcher		f
PRV_0131	plan_tab	plan_phase		f
PRV_0132	plan_tab	time_action		f
PRV_0133	plan_tab	dms_action		f
PRV_0134	plan_tab	beacon_action		f
PRV_0135	plan_tab	lane_action		f
PRV_0136	plan_tab	meter_action		f
PRV_0137	sensor_admin	detector		t
PRV_0138	sensor_admin	r_node		t
PRV_0139	sensor_admin	weather_sensor		t
PRV_0140	sensor_control	detector	fieldLength	t
PRV_0141	sensor_control	detector	forceFail	t
PRV_0142	sensor_tab	r_node		f
PRV_0143	sensor_tab	detector		f
PRV_0144	sensor_tab	station		f
PRV_0145	sensor_tab	weather_sensor		f
PRV_0146	toll_admin	tag_reader		t
PRV_0147	toll_admin	toll_zone		t
PRV_0148	toll_tab	tag_reader		f
PRV_0149	toll_tab	toll_zone		f
PRV_0150	parking_admin	parking_area		t
PRV_0151	parking_tab	parking_area		f
\.

COPY iris.privilege (name, capability, type_n, group_n, write) FROM stdin;
PRV_003D	camera_tab	play_list	user	t
\.

COPY iris.role (name, enabled) FROM stdin;
administrator	t
operator	t
\.

COPY iris.role_capability (role, capability) FROM stdin;
administrator	base
administrator	base_admin
administrator	base_policy
administrator	beacon_admin
administrator	beacon_control
administrator	beacon_tab
administrator	camera_admin
administrator	camera_control
administrator	camera_policy
administrator	camera_tab
administrator	comm_admin
administrator	comm_control
administrator	comm_tab
administrator	dms_admin
administrator	dms_control
administrator	dms_policy
administrator	dms_tab
administrator	gate_arm_admin
administrator	gate_arm_control
administrator	gate_arm_tab
administrator	incident_admin
administrator	incident_control
administrator	incident_tab
administrator	lcs_admin
administrator	lcs_control
administrator	lcs_tab
administrator	meter_admin
administrator	meter_control
administrator	meter_tab
administrator	plan_admin
administrator	plan_control
administrator	plan_tab
administrator	sensor_admin
administrator	sensor_control
administrator	sensor_tab
administrator	toll_admin
administrator	toll_tab
administrator	parking_admin
administrator	parking_tab
operator	base
operator	beacon_control
operator	beacon_tab
operator	camera_control
operator	camera_tab
operator	dms_control
operator	dms_tab
operator	gate_arm_tab
operator	incident_control
operator	incident_tab
operator	lcs_control
operator	lcs_tab
operator	meter_control
operator	meter_tab
operator	plan_control
operator	plan_tab
operator	sensor_tab
operator	toll_tab
\.

COPY iris.i_user (name, full_name, password, dn, role, enabled) FROM stdin;
admin	IRIS Administrator	+vAwDtk/0KGx9k+kIoKFgWWbd3Ku8e/FOHoZoHB65PAuNEiN2muHVavP0fztOi4=		administrator	t
\.

COPY iris.inc_range (id, description) FROM stdin;
0	near
1	middle
2	far
\.

COPY event.event_description (event_desc_id, description) FROM stdin;
1	Alarm TRIGGERED
2	Alarm CLEARED
8	Comm ERROR
9	Comm RESTORED
10	Comm QUEUE DRAINED
11	Comm POLL TIMEOUT
12	Comm PARSING ERROR
13	Comm CHECKSUM ERROR
14	Comm CONTROLLER ERROR
20	Incident CLEARED
21	Incident CRASH
22	Incident STALL
23	Incident HAZARD
24	Incident ROADWORK
29	Incident IMPACT
65	Comm FAILED
89	LCS DEPLOYED
90	LCS CLEARED
91	Sign DEPLOYED
92	Sign CLEARED
94	NO HITS
95	LOCKED ON
96	CHATTER
101	Sign BRIGHTNESS LOW
102	Sign BRIGHTNESS GOOD
103	Sign BRIGHTNESS HIGH
201	Client CONNECT
202	Client AUTHENTICATE
203	Client FAIL AUTHENTICATION
204	Client DISCONNECT
301	Gate Arm UNKNOWN
302	Gate Arm FAULT
303	Gate Arm OPENING
304	Gate Arm OPEN
305	Gate Arm WARN CLOSE
306	Gate Arm CLOSING
307	Gate Arm CLOSED
308	Gate Arm TIMEOUT
401	Meter event
501	Beacon ON
502	Beacon OFF
601	Tag Read
651	Price DEPLOYED
652	Price VERIFIED
701	TT Link too long
702	TT No data
703	TT No destination data
704	TT No origin data
705	TT No route
801	Camera SWITCHED
900	Action Plan ACTIVATED
901	Action Plan DEACTIVATED
902	Action Plan Phase CHANGED
\.

COPY event.incident_detail (name, description) FROM stdin;
animal	Animal on Road
debris	Debris
detour	Detour
emrg_veh	Emergency Vehicles
event	Event Congestion
flooding	Flash Flooding
gr_fire	Grass Fire
ice	Ice
jacknife	Jacknifed Trailer
pavement	Pavement Failure
ped	Pedestrian
rollover	Rollover
sgnl_out	Traffic Lights Out
snow_rmv	Snow Removal
spill	Spilled Load
test	Test Incident
veh_fire	Vehicle Fire
\.

COPY event.meter_phase (id, description) FROM stdin;
0	not started
1	metering
2	flushing
3	stopped
\.

COPY event.meter_queue_state (id, description) FROM stdin;
0	unknown
1	empty
2	exists
3	full
\.

COPY event.meter_limit_control (id, description) FROM stdin;
0	passage fail
1	storage limit
2	wait limit
3	target minimum
4	backup limit
\.

COPY event.tag_type (id, description) FROM stdin;
0	ASTMv6
1	SeGo
\.

-- Fonts

COPY iris.font (name, f_number, height, width, line_spacing, char_spacing, version_id) FROM stdin;
07_char	1	7	5	0	0	7314
07_line	2	7	0	0	2	40473
08_full	3	8	0	2	2	0
09_full	4	9	0	2	2	0
10_full	5	10	0	3	2	0
11_full	6	11	0	3	2	0
12_full	7	12	0	3	2	0
12_full_bold	8	12	0	4	3	0
13_full	9	13	0	4	3	0
14_full	11	14	0	4	3	0
16_full	13	16	0	4	3	0
18_full	14	18	0	5	3	0
20_full	15	20	0	5	3	0
24_full	16	24	0	5	4	0
_09_full_12	17	12	0	2	2	0
_7_full	18	7	0	3	2	0
\.

COPY iris.graphic (name, g_number, bpp, height, width, pixels) FROM stdin;
07_char_32	\N	1	7	5	AAAAAAA=
07_char_33	\N	1	7	5	IQhCAIA=
07_char_34	\N	1	7	5	UoAAAAA=
07_char_35	\N	1	7	5	Ur6vqUA=
07_char_36	\N	1	7	5	I6jiuIA=
07_char_37	\N	1	7	5	xkRETGA=
07_char_38	\N	1	7	5	RSiKyaA=
07_char_39	\N	1	7	5	IQAAAAA=
07_char_40	\N	1	7	5	ERCEEEA=
07_char_41	\N	1	7	5	QQQhEQA=
07_char_42	\N	1	7	5	JVxHVIA=
07_char_43	\N	1	7	5	AQnyEAA=
07_char_44	\N	1	7	5	AAAAEQA=
07_char_45	\N	1	7	5	AADgAAA=
07_char_46	\N	1	7	5	AAAAAIA=
07_char_47	\N	1	7	5	AEREQAA=
07_char_48	\N	1	7	5	MlKUpMA=
07_char_49	\N	1	7	5	IwhCEcA=
07_char_50	\N	1	7	5	dEJkQ+A=
07_char_51	\N	1	7	5	dEJgxcA=
07_char_52	\N	1	7	5	EZUviEA=
07_char_53	\N	1	7	5	/CDgxcA=
07_char_54	\N	1	7	5	dGHoxcA=
07_char_55	\N	1	7	5	+EQiEQA=
07_char_56	\N	1	7	5	dGLoxcA=
07_char_57	\N	1	7	5	dGLwxcA=
07_char_58	\N	1	7	5	ABAEAAA=
07_char_59	\N	1	7	5	AAgCIAA=
07_char_60	\N	1	7	5	EREEEEA=
07_char_61	\N	1	7	5	AD4PgAA=
07_char_62	\N	1	7	5	QQQREQA=
07_char_63	\N	1	7	5	ZIRCAIA=
07_char_64	\N	1	7	5	dGdZwcA=
07_char_65	\N	1	7	5	dGP4xiA=
07_char_66	\N	1	7	5	9GPox8A=
07_char_67	\N	1	7	5	dGEIRcA=
07_char_68	\N	1	7	5	9GMYx8A=
07_char_69	\N	1	7	5	/CHoQ+A=
07_char_70	\N	1	7	5	/CHoQgA=
07_char_71	\N	1	7	5	dGF4xeA=
07_char_72	\N	1	7	5	jGP4xiA=
07_char_73	\N	1	7	5	cQhCEcA=
07_char_74	\N	1	7	5	EIQhSYA=
07_char_75	\N	1	7	5	jKmKSiA=
07_char_76	\N	1	7	5	hCEIQ+A=
07_char_77	\N	1	7	5	jusYxiA=
07_char_78	\N	1	7	5	jnNZziA=
07_char_79	\N	1	7	5	dGMYxcA=
07_char_80	\N	1	7	5	9GPoQgA=
07_char_81	\N	1	7	5	dGMayaA=
07_char_82	\N	1	7	5	9GPqSiA=
07_char_83	\N	1	7	5	dGDgxcA=
07_char_84	\N	1	7	5	+QhCEIA=
07_char_85	\N	1	7	5	jGMYxcA=
07_char_86	\N	1	7	5	jGMVKIA=
07_char_87	\N	1	7	5	jGMa1UA=
07_char_88	\N	1	7	5	jFRFRiA=
07_char_89	\N	1	7	5	jFRCEIA=
07_char_90	\N	1	7	5	+EREQ+A=
07_char_91	\N	1	7	5	chCEIcA=
07_char_92	\N	1	7	5	BBBBBAA=
07_char_93	\N	1	7	5	cIQhCcA=
07_char_94	\N	1	7	5	IqIAAAA=
07_char_95	\N	1	7	5	AAAAA+A=
07_line_32	\N	1	7	1	AA==
07_line_33	\N	1	7	2	qog=
07_line_34	\N	1	7	3	tAAA
07_line_35	\N	1	7	5	Ur6vqUA=
07_line_36	\N	1	7	5	I6jiuIA=
07_line_37	\N	1	7	5	xkRETGA=
07_line_38	\N	1	7	5	RSiKyaA=
07_line_39	\N	1	7	1	wA==
07_line_40	\N	1	7	3	KkiI
07_line_41	\N	1	7	3	iJKg
07_line_42	\N	1	7	7	EFEUFEUEAA==
07_line_43	\N	1	7	5	AQnyEAA=
07_line_44	\N	1	7	3	AACg
07_line_45	\N	1	7	4	AA8AAA==
07_line_46	\N	1	7	2	AAg=
07_line_47	\N	1	7	5	AEREQAA=
07_line_48	\N	1	7	4	aZmZYA==
07_line_49	\N	1	7	3	WSS4
07_line_50	\N	1	7	4	aRJI8A==
07_line_51	\N	1	7	4	aRYZYA==
07_line_52	\N	1	7	5	EZUviEA=
07_line_53	\N	1	7	4	+IYZYA==
07_line_54	\N	1	7	4	aY6ZYA==
07_line_55	\N	1	7	4	8RIkQA==
07_line_56	\N	1	7	4	aZaZYA==
07_line_57	\N	1	7	4	aZcZYA==
07_line_58	\N	1	7	2	CIA=
07_line_59	\N	1	7	3	AQUA
07_line_60	\N	1	7	4	EkhCEA==
07_line_61	\N	1	7	4	APDwAA==
07_line_62	\N	1	7	4	hCEkgA==
07_line_63	\N	1	7	4	aRIgIA==
07_line_64	\N	1	7	5	dGdZwcA=
07_line_65	\N	1	7	4	aZ+ZkA==
07_line_66	\N	1	7	4	6Z6Z4A==
07_line_67	\N	1	7	4	aYiJYA==
07_line_68	\N	1	7	4	6ZmZ4A==
07_line_69	\N	1	7	4	+I6I8A==
07_line_70	\N	1	7	4	+I6IgA==
07_line_71	\N	1	7	4	aYuZcA==
07_line_72	\N	1	7	4	mZ+ZkA==
07_line_73	\N	1	7	3	6SS4
07_line_74	\N	1	7	4	EREZYA==
07_line_75	\N	1	7	4	maypkA==
07_line_76	\N	1	7	4	iIiI8A==
07_line_77	\N	1	7	7	g46smTBggA==
07_line_78	\N	1	7	5	jnNZziA=
07_line_79	\N	1	7	4	aZmZYA==
07_line_80	\N	1	7	4	6Z6IgA==
07_line_81	\N	1	7	5	dGMayaA=
07_line_82	\N	1	7	4	6Z6pkA==
07_line_83	\N	1	7	4	aYYZYA==
07_line_84	\N	1	7	5	+QhCEIA=
07_line_85	\N	1	7	4	mZmZYA==
07_line_86	\N	1	7	5	jGMVKIA=
07_line_87	\N	1	7	7	gwYMmrVRAA==
07_line_88	\N	1	7	5	jFRFRiA=
07_line_89	\N	1	7	5	jFRCEIA=
07_line_90	\N	1	7	5	+EREQ+A=
07_line_91	\N	1	7	3	8kk4
07_line_92	\N	1	7	5	BBBBBAA=
07_line_93	\N	1	7	3	5JJ4
07_line_94	\N	1	7	5	IqIAAAA=
07_line_95	\N	1	7	5	AAAAA+A=
08_full_32	\N	1	8	1	AA==
08_full_33	\N	1	8	2	qqI=
08_full_34	\N	1	8	3	tAAA
08_full_35	\N	1	8	5	Ur6vqUA=
08_full_36	\N	1	8	5	I6jiuIA=
08_full_37	\N	1	8	5	xkRETGA=
08_full_38	\N	1	8	5	RSiKym0=
08_full_39	\N	1	8	1	wA==
08_full_40	\N	1	8	3	KkkR
08_full_41	\N	1	8	3	iJJU
08_full_42	\N	1	8	5	ASrnVIA=
08_full_43	\N	1	8	5	AQnyEAA=
08_full_44	\N	1	8	3	AAAU
08_full_45	\N	1	8	4	AA8AAA==
08_full_46	\N	1	8	2	AAI=
08_full_47	\N	1	8	5	CERCIhA=
08_full_48	\N	1	8	4	aZmZlg==
08_full_49	\N	1	8	3	WSSX
08_full_50	\N	1	8	5	dEImQh8=
08_full_51	\N	1	8	5	dEJghi4=
08_full_52	\N	1	8	5	EZUviEI=
08_full_53	\N	1	8	5	/CDghi4=
08_full_54	\N	1	8	5	dGHoxi4=
08_full_55	\N	1	8	5	+EQiEQg=
08_full_56	\N	1	8	5	dGLoxi4=
08_full_57	\N	1	8	5	dGMXhi4=
08_full_58	\N	1	8	2	CIA=
08_full_59	\N	1	8	3	AQUA
08_full_60	\N	1	8	4	EkhCEA==
08_full_61	\N	1	8	4	APDwAA==
08_full_62	\N	1	8	4	hCEkgA==
08_full_63	\N	1	8	5	dEIiEAQ=
08_full_64	\N	1	8	5	dGdazg4=
08_full_65	\N	1	8	5	dGMfxjE=
08_full_66	\N	1	8	5	9GPoxj4=
08_full_67	\N	1	8	5	dGEIQi4=
08_full_68	\N	1	8	5	9GMYxj4=
08_full_69	\N	1	8	5	/CHIQh8=
08_full_70	\N	1	8	5	/CHIQhA=
08_full_71	\N	1	8	5	dGEJxi4=
08_full_72	\N	1	8	5	jGP4xjE=
08_full_73	\N	1	8	3	6SSX
08_full_74	\N	1	8	4	ERERlg==
08_full_75	\N	1	8	5	jKmKSjE=
08_full_76	\N	1	8	4	iIiIjw==
08_full_77	\N	1	8	7	g46smTBgwQ==
08_full_78	\N	1	8	5	jnNaznE=
08_full_79	\N	1	8	5	dGMYxi4=
08_full_80	\N	1	8	5	9GPoQhA=
08_full_81	\N	1	8	5	dGMY1k0=
08_full_82	\N	1	8	5	9GPqSjE=
08_full_83	\N	1	8	5	dGDghi4=
08_full_84	\N	1	8	5	+QhCEIQ=
08_full_85	\N	1	8	5	jGMYxi4=
08_full_86	\N	1	8	5	jGMYqUQ=
08_full_87	\N	1	8	7	gwYMGTVqog==
08_full_88	\N	1	8	5	jFRCKjE=
08_full_89	\N	1	8	5	jFSiEIQ=
08_full_90	\N	1	8	5	+ERCIh8=
08_full_91	\N	1	8	3	8kkn
08_full_92	\N	1	8	5	hBBCCCE=
08_full_93	\N	1	8	3	5JJP
08_full_94	\N	1	8	5	IqIAAAA=
08_full_95	\N	1	8	5	AAAAAB8=
09_full_32	\N	1	9	1	AAA=
09_full_33	\N	1	9	2	qqiA
09_full_34	\N	1	9	3	tAAAAA==
09_full_35	\N	1	9	5	Ur6lfUoA
09_full_36	\N	1	9	5	I6lHFK4g
09_full_37	\N	1	9	5	BjIiImMA
09_full_38	\N	1	9	6	IUUIYliidA==
09_full_39	\N	1	9	1	wAA=
09_full_40	\N	1	9	3	KkkiIA==
09_full_41	\N	1	9	3	iJJKgA==
09_full_42	\N	1	9	5	ASriOqQA
09_full_43	\N	1	9	5	AAhPkIAA
09_full_44	\N	1	9	3	AAACgA==
09_full_45	\N	1	9	4	AADwAAA=
09_full_46	\N	1	9	2	AACA
09_full_47	\N	1	9	5	CEQiIRCA
09_full_48	\N	1	9	5	dGMYxjFw
09_full_49	\N	1	9	3	WSSS4A==
09_full_50	\N	1	9	5	dEIiIhD4
09_full_51	\N	1	9	5	dEITBDFw
09_full_52	\N	1	9	5	EZUpfEIQ
09_full_53	\N	1	9	5	/CEPBDFw
09_full_54	\N	1	9	5	dGEPRjFw
09_full_55	\N	1	9	5	+EIhEIhA
09_full_56	\N	1	9	5	dGMXRjFw
09_full_57	\N	1	9	5	dGMXhDFw
09_full_58	\N	1	9	2	AiAA
09_full_59	\N	1	9	3	ACCgAA==
09_full_60	\N	1	9	4	ASSEIQA=
09_full_61	\N	1	9	4	AA8PAAA=
09_full_62	\N	1	9	4	CEISSAA=
09_full_63	\N	1	9	5	dEIREIAg
09_full_64	\N	1	9	6	ehlrppngeA==
09_full_65	\N	1	9	5	IqMfxjGI
09_full_66	\N	1	9	5	9GMfRjHw
09_full_67	\N	1	9	5	dGEIQhFw
09_full_68	\N	1	9	5	9GMYxjHw
09_full_69	\N	1	9	5	/CEOQhD4
09_full_70	\N	1	9	5	/CEOQhCA
09_full_71	\N	1	9	5	dGEJxjFw
09_full_72	\N	1	9	5	jGMfxjGI
09_full_73	\N	1	9	3	6SSS4A==
09_full_74	\N	1	9	4	ERERGWA=
09_full_75	\N	1	9	5	jGVMUlGI
09_full_76	\N	1	9	4	iIiIiPA=
09_full_77	\N	1	9	7	g46smTBgwYI=
09_full_78	\N	1	9	6	xxpplljjhA==
09_full_79	\N	1	9	5	dGMYxjFw
09_full_80	\N	1	9	5	9GMfQhCA
09_full_81	\N	1	9	5	dGMYxrJo
09_full_82	\N	1	9	5	9GMfUlGI
09_full_83	\N	1	9	5	dGEHBDFw
09_full_84	\N	1	9	5	+QhCEIQg
09_full_85	\N	1	9	5	jGMYxjFw
09_full_86	\N	1	9	5	jGMYqUQg
09_full_87	\N	1	9	7	gwYMGTJq1UQ=
09_full_88	\N	1	9	5	jGKiKjGI
09_full_89	\N	1	9	5	jGKiEIQg
09_full_90	\N	1	9	5	+EQiIRD4
09_full_91	\N	1	9	3	8kkk4A==
09_full_92	\N	1	9	5	hBCCCEEI
09_full_93	\N	1	9	3	5JJJ4A==
09_full_94	\N	1	9	5	IqIAAAAA
09_full_95	\N	1	9	5	AAAAAAD4
10_full_32	\N	1	10	1	AAA=
10_full_33	\N	1	10	2	qqgg
10_full_34	\N	1	10	3	tAAAAA==
10_full_35	\N	1	10	5	ApX1K+pQAA==
10_full_36	\N	1	10	5	I6lHFK4gAA==
10_full_37	\N	1	10	5	BjIiERMYAA==
10_full_38	\N	1	10	6	IUUIYliilZA=
10_full_39	\N	1	10	1	wAA=
10_full_40	\N	1	10	3	KkkkRA==
10_full_41	\N	1	10	3	iJJJUA==
10_full_42	\N	1	10	5	ASriOqQAAA==
10_full_43	\N	1	10	5	AAhPkIAAAA==
10_full_44	\N	1	10	3	AAACUA==
10_full_45	\N	1	10	4	AADwAAA=
10_full_46	\N	1	10	2	AACg
10_full_47	\N	1	10	5	CEQiEQiEAA==
10_full_48	\N	1	10	5	dGMYxjGLgA==
10_full_49	\N	1	10	3	WSSSXA==
10_full_50	\N	1	10	5	dGIRERCHwA==
10_full_51	\N	1	10	5	dEITBCGLgA==
10_full_52	\N	1	10	5	EZUpfEIQgA==
10_full_53	\N	1	10	5	/CEPBCGLgA==
10_full_54	\N	1	10	5	dGEPRjGLgA==
10_full_55	\N	1	10	5	+EIhEIhCAA==
10_full_56	\N	1	10	5	dGMXRjGLgA==
10_full_57	\N	1	10	5	dGMYvCGLgA==
10_full_58	\N	1	10	2	CgoA
10_full_59	\N	1	10	3	ASASgA==
10_full_60	\N	1	10	4	ASSEIQA=
10_full_61	\N	1	10	4	AA8PAAA=
10_full_62	\N	1	10	4	CEISSAA=
10_full_63	\N	1	10	5	dEIREIQBAA==
10_full_64	\N	1	10	6	ehlrpppngeA=
10_full_65	\N	1	10	5	IqMY/jGMQA==
10_full_66	\N	1	10	5	9GMfRjGPgA==
10_full_67	\N	1	10	5	dGEIQhCLgA==
10_full_68	\N	1	10	5	9GMYxjGPgA==
10_full_69	\N	1	10	5	/CEOQhCHwA==
10_full_70	\N	1	10	5	/CEOQhCEAA==
10_full_71	\N	1	10	5	dGEITjGLgA==
10_full_72	\N	1	10	5	jGMfxjGMQA==
10_full_73	\N	1	10	3	6SSSXA==
10_full_74	\N	1	10	4	EREREZY=
10_full_75	\N	1	10	5	jGVMUlGMQA==
10_full_76	\N	1	10	4	iIiIiI8=
10_full_77	\N	1	10	7	g46smTBgwYME
10_full_78	\N	1	10	6	hxxpplljjhA=
10_full_79	\N	1	10	5	dGMYxjGLgA==
10_full_80	\N	1	10	5	9GMfQhCEAA==
10_full_81	\N	1	10	5	dGMYxjWTQA==
10_full_82	\N	1	10	5	9GMfUlKMQA==
10_full_83	\N	1	10	5	dGEHBCGLgA==
10_full_84	\N	1	10	5	+QhCEIQhAA==
10_full_85	\N	1	10	5	jGMYxjGLgA==
10_full_86	\N	1	10	5	jGMYxUohAA==
10_full_87	\N	1	10	7	gwYMGDJk1aqI
10_full_88	\N	1	10	5	jGKiKjGMQA==
10_full_89	\N	1	10	5	jGMVEIQhAA==
10_full_90	\N	1	10	5	+EQiEQiHwA==
10_full_91	\N	1	10	3	8kkknA==
10_full_92	\N	1	10	5	hBCCEEIIQA==
10_full_93	\N	1	10	3	5JJJPA==
10_full_94	\N	1	10	5	IqIAAAAAAA==
10_full_95	\N	1	10	5	AAAAAAAHwA==
11_full_32	\N	1	11	1	AAA=
11_full_33	\N	1	11	2	qqoI
11_full_34	\N	1	11	3	toAAAAA=
11_full_35	\N	1	11	5	ApX1K+pQAA==
11_full_36	\N	1	11	5	I6lKOKUriA==
11_full_37	\N	1	11	5	BjYjEYjYwA==
11_full_38	\N	1	11	6	IUUUIYliilZA
11_full_39	\N	1	11	1	4AA=
11_full_40	\N	1	11	3	L0kkzIA=
11_full_41	\N	1	11	3	mZJJegA=
11_full_42	\N	1	11	5	AAlXEdUgAA==
11_full_43	\N	1	11	5	AAhCfIQgAA==
11_full_44	\N	1	11	3	AAAAWgA=
11_full_45	\N	1	11	5	AAAAfAAAAA==
11_full_46	\N	1	11	2	AAAo
11_full_47	\N	1	11	5	CEYjEYjEIA==
11_full_48	\N	1	11	5	duMYxjGO3A==
11_full_49	\N	1	11	3	WSSSS4A=
11_full_50	\N	1	11	6	ezhBDGcwgg/A
11_full_51	\N	1	11	6	ezBBDODBBzeA
11_full_52	\N	1	11	6	GOayii/CCCCA
11_full_53	\N	1	11	6	/ggg+DBBBzeA
11_full_54	\N	1	11	6	ezggg+jhhzeA
11_full_55	\N	1	11	5	+EIxGIxCEA==
11_full_56	\N	1	11	6	ezhhzezhhzeA
11_full_57	\N	1	11	6	ezhhxfBBBzeA
11_full_58	\N	1	11	2	AooA
11_full_59	\N	1	11	3	ACQWgAA=
11_full_60	\N	1	11	5	AEZmYYYYQA==
11_full_61	\N	1	11	5	AAAPg+AAAA==
11_full_62	\N	1	11	5	BDDDDMzEAA==
11_full_63	\N	1	11	6	ezhBDGMIIAIA
11_full_64	\N	1	11	6	ezhlrppngweA
11_full_65	\N	1	11	6	Mezhh/hhhhhA
11_full_66	\N	1	11	6	+jhhj+jhhj+A
11_full_67	\N	1	11	6	ezgggggggzeA
11_full_68	\N	1	11	6	+jhhhhhhhj+A
11_full_69	\N	1	11	5	/CEIchCEPg==
11_full_70	\N	1	11	5	/CEIchCEIA==
11_full_71	\N	1	11	6	ezgggnhhhzeA
11_full_72	\N	1	11	5	jGMY/jGMYg==
11_full_73	\N	1	11	3	6SSSS4A=
11_full_74	\N	1	11	5	CEIQhCGO3A==
11_full_75	\N	1	11	6	hjms44smijhA
11_full_76	\N	1	11	5	hCEIQhCEPg==
11_full_77	\N	1	11	7	g4+92TJgwYMGCA==
11_full_78	\N	1	11	6	hxx5ptlnjjhA
11_full_79	\N	1	11	6	ezhhhhhhhzeA
11_full_80	\N	1	11	6	+jhhj+gggggA
11_full_81	\N	1	11	6	ezhhhhhhlydA
11_full_82	\N	1	11	6	+jhhj+kmijhA
11_full_83	\N	1	11	6	ezggweDBBzeA
11_full_84	\N	1	11	5	+QhCEIQhCA==
11_full_85	\N	1	11	6	hhhhhhhhhzeA
11_full_86	\N	1	11	6	hhhhzSSSeMMA
11_full_87	\N	1	11	7	gwYMGDJk3avdEA==
11_full_88	\N	1	11	6	hhzSeMeSzhhA
11_full_89	\N	1	11	5	jGO1OIQhCA==
11_full_90	\N	1	11	6	/BDGEMIYwg/A
11_full_91	\N	1	11	3	8kkkk4A=
11_full_92	\N	1	11	5	hDCGEMIYQg==
11_full_93	\N	1	11	3	5JJJJ4A=
11_full_94	\N	1	11	5	I7cQAAAAAA==
11_full_95	\N	1	11	5	AAAAAAAAPg==
12_full_32	\N	1	12	2	AAAA
12_full_33	\N	1	12	2	qqqC
12_full_34	\N	1	12	3	toAAAAA=
12_full_35	\N	1	12	5	ABSvqV9SgAA=
12_full_36	\N	1	12	5	I6lKOKUriAA=
12_full_37	\N	1	12	6	AwzCGMMYQzDA
12_full_38	\N	1	12	6	IcUUcId1ni3d
12_full_39	\N	1	12	1	4AA=
12_full_40	\N	1	12	3	LWkkyZA=
12_full_41	\N	1	12	3	mTJJa0A=
12_full_42	\N	1	12	5	AQlXEdUhAAA=
12_full_43	\N	1	12	5	AQhCfIQhAAA=
12_full_44	\N	1	12	3	AAAAC0A=
12_full_45	\N	1	12	5	AAAAfAAAAAA=
12_full_46	\N	1	12	3	AAAAA2A=
12_full_47	\N	1	12	5	CEYjEIxGIQA=
12_full_48	\N	1	12	5	duMYxjGMduA=
12_full_49	\N	1	12	3	WSSSSXA=
12_full_50	\N	1	12	6	ezhBDGMYwgg/
12_full_51	\N	1	12	6	ezBBDODBBBze
12_full_52	\N	1	12	6	GOayii/CCCCC
12_full_53	\N	1	12	6	/gggg+DBBBze
12_full_54	\N	1	12	6	ezggg+jhhhze
12_full_55	\N	1	12	5	+EIxCMQjEIA=
12_full_56	\N	1	12	6	ezhhzezhhhze
12_full_57	\N	1	12	6	ezhhhxfBBBze
12_full_58	\N	1	12	2	AoKA
12_full_59	\N	1	12	3	ACQC0AA=
12_full_60	\N	1	12	5	AEZmYYYYQAA=
12_full_61	\N	1	12	5	AAAPgB8AAAA=
12_full_62	\N	1	12	5	BDDDDMzEAAA=
12_full_63	\N	1	12	6	ezhBBDGMIIAI
12_full_64	\N	1	12	7	fY4M2nRo2Z8DA8A=
12_full_65	\N	1	12	6	Mezhhh/hhhhh
12_full_66	\N	1	12	6	+jhhj+jhhhj+
12_full_67	\N	1	12	6	ezggggggggze
12_full_68	\N	1	12	6	+jhhhhhhhhj+
12_full_69	\N	1	12	6	/gggg8ggggg/
12_full_70	\N	1	12	6	/gggg8gggggg
12_full_71	\N	1	12	6	ezggggnhhhze
12_full_72	\N	1	12	5	jGMY/jGMYxA=
12_full_73	\N	1	12	3	6SSSSXA=
12_full_74	\N	1	12	5	CEIQhCEMduA=
12_full_75	\N	1	12	6	hjims44smijh
12_full_76	\N	1	12	5	hCEIQhCEIfA=
12_full_77	\N	1	12	9	gOD49tnMRiMBgMBgMBA=
12_full_78	\N	1	12	7	w4eNGzJmxY8OHBA=
12_full_79	\N	1	12	6	ezhhhhhhhhze
12_full_80	\N	1	12	6	+jhhhj+ggggg
12_full_81	\N	1	12	6	ezhhhhhhhlyd
12_full_82	\N	1	12	6	+jhhhj+kmijh
12_full_83	\N	1	12	6	ezggweDBBBze
12_full_84	\N	1	12	5	+QhCEIQhCEA=
12_full_85	\N	1	12	6	hhhhhhhhhhze
12_full_86	\N	1	12	6	hhhhhzSSSeMM
12_full_87	\N	1	12	9	gMBgMBgMByaSXSqdxEA=
12_full_88	\N	1	12	6	hhzSeMMeSzhh
12_full_89	\N	1	12	7	gwcaJscECBAgQIA=
12_full_90	\N	1	12	6	/BDCGMIYQwg/
12_full_91	\N	1	12	3	8kkkknA=
12_full_92	\N	1	12	5	hDCGEIYQwhA=
12_full_93	\N	1	12	3	5JJJJPA=
12_full_94	\N	1	12	5	I7cQAAAAAAA=
12_full_95	\N	1	12	5	AAAAAAAAAfA=
12_full_bold_32	\N	1	12	1	AAA=
12_full_bold_33	\N	1	12	3	2222w2A=
12_full_bold_34	\N	1	12	5	3tIAAAAAAAA=
12_full_bold_35	\N	1	12	8	AABm//9mZv//ZgAA
12_full_bold_36	\N	1	12	8	GH7/2Nj+fxsb/34Y
12_full_bold_37	\N	1	12	7	AYMYccMMOOGMGAA=
12_full_bold_38	\N	1	12	7	cfNmxx42782b+9A=
12_full_bold_39	\N	1	12	2	+AAA
12_full_bold_40	\N	1	12	4	NszMzMxj
12_full_bold_41	\N	1	12	4	xjMzMzNs
12_full_bold_42	\N	1	12	8	ABjbfjwYPH7bGAAA
12_full_bold_43	\N	1	12	6	AAMMM//MMMAA
12_full_bold_44	\N	1	12	4	AAAAAAZs
12_full_bold_45	\N	1	12	5	AAAAf+AAAAA=
12_full_bold_46	\N	1	12	3	AAAAA2A=
12_full_bold_47	\N	1	12	7	BgwwYYMMGGDDBgA=
12_full_bold_48	\N	1	12	7	ff8ePHjx48eP++A=
12_full_bold_49	\N	1	12	4	JuZmZmb/
12_full_bold_50	\N	1	12	7	ff4YMGOOMMGD//A=
12_full_bold_51	\N	1	12	7	ff4YMGOHAwcP++A=
12_full_bold_52	\N	1	12	7	HHn3bNm//wwYMGA=
12_full_bold_53	\N	1	12	7	//8GDB+fgwcP++A=
12_full_bold_54	\N	1	12	7	ff8ODB+/48eP++A=
12_full_bold_55	\N	1	12	6	//DDGGGMMMYY
12_full_bold_56	\N	1	12	7	ff8ePG+fY8eP++A=
12_full_bold_57	\N	1	12	7	ff8ePH/fgwcP++A=
12_full_bold_58	\N	1	12	3	AGwGwAA=
12_full_bold_59	\N	1	12	4	AAZgBugA
12_full_bold_60	\N	1	12	6	ADGMYwwYMGDA
12_full_bold_61	\N	1	12	5	AAH/gB/4AAA=
12_full_bold_62	\N	1	12	6	AwYMGDDGMYwA
12_full_bold_63	\N	1	12	6	e/jDDHOMMAMM
12_full_bold_64	\N	1	12	8	fv/Dz9/T09/OwP58
12_full_bold_65	\N	1	12	8	GDx+58PD///Dw8PD
12_full_bold_66	\N	1	12	8	/P7Hw8b8/MbDx/78
12_full_bold_67	\N	1	12	7	PP+eDBgwYMHN+eA=
12_full_bold_68	\N	1	12	7	/f8ePHjx48eP/+A=
12_full_bold_69	\N	1	12	7	//8GDB8+YMGD//A=
12_full_bold_70	\N	1	12	7	//8GDB8+YMGDBgA=
12_full_bold_71	\N	1	12	7	ff8eDBnz48eP++A=
12_full_bold_72	\N	1	12	6	zzzzz//zzzzz
12_full_bold_73	\N	1	12	4	/2ZmZmb/
12_full_bold_74	\N	1	12	6	DDDDDDDDDz/e
12_full_bold_75	\N	1	12	7	x5s2zZ48bNmbNjA=
12_full_bold_76	\N	1	12	6	wwwwwwwwww//
12_full_bold_77	\N	1	12	10	wPh/P//e8zwPA8DwPA8D
12_full_bold_78	\N	1	12	8	w+Pj8/Pb28/Px8fD
12_full_bold_79	\N	1	12	7	ff8ePHjx48eP++A=
12_full_bold_80	\N	1	12	7	/f8ePH//YMGDBgA=
12_full_bold_81	\N	1	12	8	fP7GxsbGxsbezv57
12_full_bold_82	\N	1	12	7	/f8ePH//bM2bHjA=
12_full_bold_83	\N	1	12	7	ff8eDB+fgweP++A=
12_full_bold_84	\N	1	12	6	//MMMMMMMMMM
12_full_bold_85	\N	1	12	7	x48ePHjx48eP++A=
12_full_bold_86	\N	1	12	7	x48ePHjbNmxw4IA=
12_full_bold_87	\N	1	12	10	wPA8DwPA8DzPt3+f4zDM
12_full_bold_88	\N	1	12	7	x48bZscONm2PHjA=
12_full_bold_89	\N	1	12	8	w8PDZmY8PBgYGBgY
12_full_bold_90	\N	1	12	7	//wYMMMMMMGD//A=
12_full_bold_91	\N	1	12	4	/8zMzMz/
12_full_bold_92	\N	1	12	7	wYGDAwYGDAwYGDA=
12_full_bold_93	\N	1	12	4	/zMzMzP/
12_full_bold_94	\N	1	12	6	MezhAAAAAAAA
12_full_bold_95	\N	1	12	5	AAAAAAAAP/A=
13_full_32	\N	1	13	1	AAA=
13_full_33	\N	1	13	1	/8g=
13_full_34	\N	1	13	3	toAAAAA=
13_full_35	\N	1	13	6	AASS/SS/SSAAAA==
13_full_36	\N	1	13	7	EPtciRofCxInW+EA
13_full_37	\N	1	13	7	AYMIMMMEGGGCGDAA
13_full_38	\N	1	13	7	MPEiR4YcbIseFneg
13_full_39	\N	1	13	1	4AA=
13_full_40	\N	1	13	4	E2yIiIxjEA==
13_full_41	\N	1	13	4	jGMRERNsgA==
13_full_42	\N	1	13	7	AABGt8cEHH2sQAAA
13_full_43	\N	1	13	5	AABCE+QhAAAA
13_full_44	\N	1	13	3	AAAAAWg=
13_full_45	\N	1	13	5	AAAAA+AAAAAA
13_full_46	\N	1	13	3	AAAAAGw=
13_full_47	\N	1	13	6	BBDCGEMIYQwggA==
13_full_48	\N	1	13	6	ezhhhhhhhhhzeA==
13_full_49	\N	1	13	3	WSSSSS4=
13_full_50	\N	1	13	7	fY4IEGGGGGGCBA/g
13_full_51	\N	1	13	7	fYwIECDHAwIEDjfA
13_full_52	\N	1	13	7	DDjTLFC/ggQIECBA
13_full_53	\N	1	13	7	/wIECB+BgQIEDjfA
13_full_54	\N	1	13	7	fY4MCBA/Q4MGDjfA
13_full_55	\N	1	13	6	/BBBDCGEMIYQQA==
13_full_56	\N	1	13	7	fY4MGDjfY4MGDjfA
13_full_57	\N	1	13	7	fY4MGDhfgQIGDjfA
13_full_58	\N	1	13	2	AKKAAA==
13_full_59	\N	1	13	3	AASC0AA=
13_full_60	\N	1	13	6	ABDGMYwYMGDBAA==
13_full_61	\N	1	13	5	AAAAfB8AAAAA
13_full_62	\N	1	13	6	AgwYMGDGMYwgAA==
13_full_63	\N	1	13	7	fY4IECDDDBAgAAEA
13_full_64	\N	1	13	7	fY4M23Ro0bM+BgeA
13_full_65	\N	1	13	7	EHG2ODB/wYMGDBgg
13_full_66	\N	1	13	7	/Q4MGDD/Q4MGDD/A
13_full_67	\N	1	13	7	fY4MCBAgQIECDjfA
13_full_68	\N	1	13	7	+RocGDBgwYMGHG+A
13_full_69	\N	1	13	7	/wIECBA+QIECBA/g
13_full_70	\N	1	13	7	/wIECBA+QIECBAgA
13_full_71	\N	1	13	7	fY4MCBAjwYMGDjfA
13_full_72	\N	1	13	6	hhhhhh/hhhhhhA==
13_full_73	\N	1	13	3	6SSSSS4=
13_full_74	\N	1	13	6	BBBBBBBBBBhzeA==
13_full_75	\N	1	13	7	gw40yxwwcLEyNDgg
13_full_76	\N	1	13	6	gggggggggggg/A==
13_full_77	\N	1	13	9	gOD49tnMRiMBgMBgMBgI
13_full_78	\N	1	13	8	wcHhobGRmYmNhYeDgw==
13_full_79	\N	1	13	7	fY4MGDBgwYMGDjfA
13_full_80	\N	1	13	7	/Q4MGDD/QIECBAgA
13_full_81	\N	1	13	7	fY4MGDBgwYMmLieg
13_full_82	\N	1	13	7	/Q4MGDD/TI0KHBgg
13_full_83	\N	1	13	7	fY4MCBgfAwIGDjfA
13_full_84	\N	1	13	7	/iBAgQIECBAgQIEA
13_full_85	\N	1	13	7	gwYMGDBgwYMGDjfA
13_full_86	\N	1	13	7	gwYMGDBxokTYocEA
13_full_87	\N	1	13	9	gMBgMBgMBgOTSS6VTuIg
13_full_88	\N	1	13	7	gwcaJscEHGyLHBgg
13_full_89	\N	1	13	7	gwcaJsUOCBAgQIEA
13_full_90	\N	1	13	8	/wEBAwYMGDBgwICA/w==
13_full_91	\N	1	13	4	+IiIiIiI8A==
13_full_92	\N	1	13	6	ggwQYIMEGCDBBA==
13_full_93	\N	1	13	4	8RERERER8A==
13_full_94	\N	1	13	6	MezhAAAAAAAAAA==
13_full_95	\N	1	13	5	AAAAAAAAAA+A
14_full_32	\N	1	14	1	AAA=
14_full_33	\N	1	14	3	22222A2A
14_full_34	\N	1	14	5	3tIAAAAAAAAA
14_full_35	\N	1	14	8	AABmZv//Zmb//2ZmAAA=
14_full_36	\N	1	14	8	GH7/2djY/n8bG5v/fhg=
14_full_37	\N	1	14	8	AOCh4wcOHDhw4MeFBwA=
14_full_38	\N	1	14	8	GDxmZmY8OHjNx8bG/3s=
14_full_39	\N	1	14	2	+AAAAA==
14_full_40	\N	1	14	4	E2zMzMzGMQ==
14_full_41	\N	1	14	4	jGMzMzM2yA==
14_full_42	\N	1	14	8	AAAYmdt+PDx+25kYAAA=
14_full_43	\N	1	14	6	AAAMMM//MMMAAAA=
14_full_44	\N	1	14	4	AAAAAAAG6A==
14_full_45	\N	1	14	5	AAAAA/8AAAAA
14_full_46	\N	1	14	3	AAAAAA2A
14_full_47	\N	1	14	8	AwMHBg4MHDgwcGDgwMA=
14_full_48	\N	1	14	7	OPu+PHjx48ePH3fHAA==
14_full_49	\N	1	14	6	Mc8MMMMMMMMM//A=
14_full_50	\N	1	14	8	fP7HAwMHDhw4cODA//8=
14_full_51	\N	1	14	8	fP7HAwMHHh4HAwPH/nw=
14_full_52	\N	1	14	8	Bg4eNmbGxv//BgYGBgY=
14_full_53	\N	1	14	8	///AwMD8fgcDAwPH/nw=
14_full_54	\N	1	14	8	Pn/jwMD8/sfDw8Pnfjw=
14_full_55	\N	1	14	7	//wYMGHDDhhww4YMAA==
14_full_56	\N	1	14	8	PH7nw8Pnfn7nw8Pnfjw=
14_full_57	\N	1	14	8	PH7nw8Pjfz8DAwPH/nw=
14_full_58	\N	1	14	3	AA2A2AAA
14_full_59	\N	1	14	4	AABmAG6AAA==
14_full_60	\N	1	14	6	ABDGMYwwYMGDBAA=
14_full_61	\N	1	14	6	AAAA//AA//AAAAA=
14_full_62	\N	1	14	6	AgwYMGDDGMYwgAA=
14_full_63	\N	1	14	8	fP7HAwMHDhwYGAAAGBg=
14_full_64	\N	1	14	9	Pj+4+Dx+fyeTz+OwHAfx8A==
14_full_65	\N	1	14	8	GDx+58PDw///w8PDw8M=
14_full_66	\N	1	14	8	/P7Hw8PH/v7Hw8PH/vw=
14_full_67	\N	1	14	8	PH7nw8DAwMDAwMPnfjw=
14_full_68	\N	1	14	8	/P7Hw8PDw8PDw8PH/vw=
14_full_69	\N	1	14	8	///AwMDA+PjAwMDA//8=
14_full_70	\N	1	14	8	///AwMDA+PjAwMDAwMA=
14_full_71	\N	1	14	8	Pn/jwMDAwMfHw8Pnfjw=
14_full_72	\N	1	14	7	x48ePHj//8ePHjx4wA==
14_full_73	\N	1	14	4	/2ZmZmZm/w==
14_full_74	\N	1	14	7	BgwYMGDBgwYPH3fHAA==
14_full_75	\N	1	14	8	w8PHztz48PD43M7Hw8M=
14_full_76	\N	1	14	7	wYMGDBgwYMGDBg//wA==
14_full_77	\N	1	14	11	wHwfx/3995zxHgPAeA8B4DwHgMA=
14_full_78	\N	1	14	9	wfD4fj8ez2ebzePx+Hw+DA==
14_full_79	\N	1	14	8	PH7nw8PDw8PDw8Pnfjw=
14_full_80	\N	1	14	8	/P7Hw8PH/vzAwMDAwMA=
14_full_81	\N	1	14	8	PH7nw8PDw8PD29/ufzs=
14_full_82	\N	1	14	8	/P7Hw8PH/vzMzMbGw8M=
14_full_83	\N	1	14	8	PH7nw8DgfD4HA8Pnfjw=
14_full_84	\N	1	14	8	//8YGBgYGBgYGBgYGBg=
14_full_85	\N	1	14	8	w8PDw8PDw8PDw8Pnfjw=
14_full_86	\N	1	14	8	w8PDw8PDw8NmZjw8GBg=
14_full_87	\N	1	14	11	wHgPAeA8B4DwHiPu7dn/PeMYYwA=
14_full_88	\N	1	14	8	w8PDZn48GBg8fmbDw8M=
14_full_89	\N	1	14	8	w8PDZmY8PBgYGBgYGBg=
14_full_90	\N	1	14	8	//8DAwcOHDhw4MDA//8=
14_full_91	\N	1	14	4	/8zMzMzM/w==
14_full_92	\N	1	14	8	wMDgYHAwOBwMDgYHAwM=
14_full_93	\N	1	14	4	/zMzMzMz/w==
14_full_94	\N	1	14	6	MezhAAAAAAAAAAA=
14_full_95	\N	1	14	6	AAAAAAAAAAAA//A=
16_full_32	\N	1	16	1	AAA=
16_full_33	\N	1	16	3	222222A2
16_full_34	\N	1	16	6	zzRAAAAAAAAAAAAA
16_full_35	\N	1	16	8	AAAAZmb//2Zm//9mZgAAAA==
16_full_36	\N	1	16	8	GBh+/9nY2P5/Gxub/34YGA==
16_full_37	\N	1	16	8	AOCh4wcGDBw4MGDgx4UHAA==
16_full_38	\N	1	16	8	OHzuxu58OHj8z8/Gxu9/OQ==
16_full_39	\N	1	16	2	+AAAAA==
16_full_40	\N	1	16	4	N+zMzMzMznM=
16_full_41	\N	1	16	4	znMzMzMzN+w=
16_full_42	\N	1	16	8	AAAY2/9+PBg8fv/bGAAAAA==
16_full_43	\N	1	16	6	AAAAMMM//MMMAAAA
16_full_44	\N	1	16	4	AAAAAAAABuw=
16_full_45	\N	1	16	6	AAAAAAA//AAAAAAA
16_full_46	\N	1	16	3	AAAAAAA2
16_full_47	\N	1	16	8	AAMDBgYMDBgYMDBgYMDAAA==
16_full_48	\N	1	16	8	PH7nw8PDw8PDw8PDw+d+PA==
16_full_49	\N	1	16	6	Mc8MMMMMMMMMMM//
16_full_50	\N	1	16	8	PH7nwwMDBw4cOHDgwMD//w==
16_full_51	\N	1	16	8	fP7HAwMDBx4eBwMDA8f+fA==
16_full_52	\N	1	16	8	Bg4ePnbmxsb//wYGBgYGBg==
16_full_53	\N	1	16	8	///AwMDA/H4HAwMDA8f+fA==
16_full_54	\N	1	16	8	Pn/jwMDA/P7Hw8PDw+d+PA==
16_full_55	\N	1	16	8	//8DAwMHBg4MHBg4MHBgYA==
16_full_56	\N	1	16	8	PH7nw8PD535+58PDw+d+PA==
16_full_57	\N	1	16	8	PH7nw8PD438/AwMDA8f+fA==
16_full_58	\N	1	16	3	AAGwGwAA
16_full_59	\N	1	16	4	AAAGYAboAAA=
16_full_60	\N	1	16	7	AAQYYYYYYMDAwMDAwIA=
16_full_61	\N	1	16	6	AAAAA//AA//AAAAA
16_full_62	\N	1	16	7	AQMDAwMDAwYYYYYYIAA=
16_full_63	\N	1	16	8	fP7HAwMDBw4cGBgYAAAYGA==
16_full_64	\N	1	16	10	Px/uHwPH8/zPM8zzPP8ewDgH+Pw=
16_full_65	\N	1	16	8	GDx+58PDw8P//8PDw8PDww==
16_full_66	\N	1	16	8	/P7Hw8PDx/7+x8PDw8f+/A==
16_full_67	\N	1	16	8	PH7nw8DAwMDAwMDAw+d+PA==
16_full_68	\N	1	16	8	/P7Hw8PDw8PDw8PDw8f+/A==
16_full_69	\N	1	16	8	///AwMDAwPj4wMDAwMD//w==
16_full_70	\N	1	16	8	///AwMDAwPj4wMDAwMDAwA==
16_full_71	\N	1	16	8	PH7nw8DAwMDHx8PDw+d+PA==
16_full_72	\N	1	16	8	w8PDw8PDw///w8PDw8PDww==
16_full_73	\N	1	16	4	/2ZmZmZmZv8=
16_full_74	\N	1	16	7	BgwYMGDBgwYMGDx93xw=
16_full_75	\N	1	16	8	w8PHxs7c+PD43MzOxsfDww==
16_full_76	\N	1	16	7	wYMGDBgwYMGDBgwYP/8=
16_full_77	\N	1	16	12	wD4H8P+f37zzxjxjwDwDwDwDwDwDwDwD
16_full_78	\N	1	16	9	weD4fD8fj2ezzebx+Pw+HweD
16_full_79	\N	1	16	8	PH7nw8PDw8PDw8PDw+d+PA==
16_full_80	\N	1	16	8	/P7Hw8PDx/78wMDAwMDAwA==
16_full_81	\N	1	16	8	PH7nw8PDw8PDw8Pb3+5/Ow==
16_full_82	\N	1	16	8	/P7Hw8PDx/78zMzGxsPDww==
16_full_83	\N	1	16	8	PH7nw8DA4Hw+BwMDw+d+PA==
16_full_84	\N	1	16	8	//8YGBgYGBgYGBgYGBgYGA==
16_full_85	\N	1	16	8	w8PDw8PDw8PDw8PDw+d+PA==
16_full_86	\N	1	16	8	w8PDw8PDw8PDZmZmPDwYGA==
16_full_87	\N	1	16	12	wDwDwDwDwDwDwDwDxjxjzz73f+eeeeMM
16_full_88	\N	1	16	8	w8PDZmY8PBgYPDxmZsPDww==
16_full_89	\N	1	16	8	w8PDZmZmPDw8GBgYGBgYGA==
16_full_90	\N	1	16	8	//8DBgYMDBgYMDBgYMD//w==
16_full_91	\N	1	16	4	/8zMzMzMzP8=
16_full_92	\N	1	16	8	AMDAYGAwMBgYDAwGBgMDAA==
16_full_93	\N	1	16	4	/zMzMzMzM/8=
16_full_94	\N	1	16	6	MezhAAAAAAAAAAAA
16_full_95	\N	1	16	6	AAAAAAAAAAAAAA//
18_full_32	\N	1	18	1	AAAA
18_full_33	\N	1	18	3	2222222A2A==
18_full_34	\N	1	18	6	zzRAAAAAAAAAAAAAAAA=
18_full_35	\N	1	18	8	AAAAZmb//2ZmZv//ZmYAAAAA
18_full_36	\N	1	18	8	GBh+/9vY2Pj+fx8bG9v/fhgY
18_full_37	\N	1	18	8	AADgoeMHBgwcODBg4MeFBwAA
18_full_38	\N	1	18	9	OD47mMzj4eBgeH7z+exmMx3ffZxA
18_full_39	\N	1	18	2	+AAAAAA=
18_full_40	\N	1	18	4	N27MzMzMzOZz
18_full_41	\N	1	18	4	zmczMzMzM3bs
18_full_42	\N	1	18	8	AAAYGJnb/348PH7/25kYGAAA
18_full_43	\N	1	18	6	AAAAAMMM//MMMAAAAAA=
18_full_44	\N	1	18	4	AAAAAAAAAAbs
18_full_45	\N	1	18	6	AAAAAAAA//AAAAAAAAA=
18_full_46	\N	1	18	3	AAAAAAAA2A==
18_full_47	\N	1	18	8	AwMHBgYODBwYGDgwcGBg4MDA
18_full_48	\N	1	18	8	PH7nw8PDw8PDw8PDw8PD5348
18_full_49	\N	1	18	6	Mc88MMMMMMMMMMMM//A=
18_full_50	\N	1	18	9	Pj+4+DAYDAYHBwcHBwcHAwGA///A
18_full_51	\N	1	18	9	fn+w4DAYDAYHDweA4DAYDAeH/z8A
18_full_52	\N	1	18	9	BgcHh8dnMxmMxn//4MBgMBgMBgMA
18_full_53	\N	1	18	9	///wGAwGAwH8fwHAYDAYDAeH/z8A
18_full_54	\N	1	18	9	Pz/4eAwGAwH8/2HweDweDwfHfx8A
18_full_55	\N	1	18	8	//8DBwYGDgwMHBgYODAwcGBg
18_full_56	\N	1	18	9	Pj+4+DweD47+Pj+4+DweDwfHfx8A
18_full_57	\N	1	18	9	Pj+4+DweDwfDf5/AYDAYDAeH/z8A
18_full_58	\N	1	18	3	AAA2A2AAAA==
18_full_59	\N	1	18	4	AAAAZgBugAAA
18_full_60	\N	1	18	8	AAEDBw4cOHDg4HA4HA4HAwEA
18_full_61	\N	1	18	6	AAAAAA//AA//AAAAAAA=
18_full_62	\N	1	18	8	AIDA4HA4HA4HBw4cOHDgwIAA
18_full_63	\N	1	18	9	fn+w4DAYDAYHBwcHAwGAwAAAGAwA
18_full_64	\N	1	18	10	Px/uHwPA8fz/M8zzPM8/x7AMA4B/j8A=
18_full_65	\N	1	18	9	CA4Pju4+DweDwf//+DweDweDweDA
18_full_66	\N	1	18	9	/n+w+DweDw/+/2HweDweDweH/38A
18_full_67	\N	1	18	9	Pj+4+DwGAwGAwGAwGAwGAwfHfx8A
18_full_68	\N	1	18	9	/n+w+DweDweDweDweDweDweH/38A
18_full_69	\N	1	18	9	///wGAwGAwH4/GAwGAwGAwGA///A
18_full_70	\N	1	18	9	///wGAwGAwH4/GAwGAwGAwGAwGAA
18_full_71	\N	1	18	9	Pj+4+DwGAwGAwGHw+DweDwfHfx8A
18_full_72	\N	1	18	8	w8PDw8PDw8P//8PDw8PDw8PD
18_full_73	\N	1	18	4	/2ZmZmZmZmb/
18_full_74	\N	1	18	7	BgwYMGDBgwYMGDBg8fd8cA==
18_full_75	\N	1	18	9	weDw+Gx2c3Hw8Hw3GcxmOw2HweDA
18_full_76	\N	1	18	8	wMDAwMDAwMDAwMDAwMDAwP//
18_full_77	\N	1	18	12	wD4H8P8P+f37zzxjxjwDwDwDwDwDwDwDwDwD
18_full_78	\N	1	18	10	wPA+D4Pw/D2PY8zzPG8bw/D8HwfA8DA=
18_full_79	\N	1	18	9	Pj+4+DweDweDweDweDweDwfHfx8A
18_full_80	\N	1	18	9	/n+w+DweDw/+/mAwGAwGAwGAwGAA
18_full_81	\N	1	18	9	Pj+4+DweDweDweDweDwebz/Of57A
18_full_82	\N	1	18	9	/n+w+DweDw/+/mYzGcxmOw2HweDA
18_full_83	\N	1	18	9	Pz/4eAwGAwHAfh+A4DAYDAeH/z8A
18_full_84	\N	1	18	8	//8YGBgYGBgYGBgYGBgYGBgY
18_full_85	\N	1	18	9	weDweDweDweDweDweDweDwfHfx8A
18_full_86	\N	1	18	9	weDweDweDweDwfHYzuNh8HA4CAQA
18_full_87	\N	1	18	12	wDwDwDwDwDwDwDwDwDxjxjzz73//f+eeMMMM
18_full_88	\N	1	18	9	weDwfHYzGdx8HA4PjuYzG4+DweDA
18_full_89	\N	1	18	8	w8PD52Zmfjw8PBgYGBgYGBgY
18_full_90	\N	1	18	9	///AYDA4GBwcHA4ODgYHAwGA///A
18_full_91	\N	1	18	5	//GMYxjGMYxjGP/A
18_full_92	\N	1	18	8	wMDgYGBwMDgYGBwMDgYGBwMD
18_full_93	\N	1	18	5	/8YxjGMYxjGMY//A
18_full_94	\N	1	18	7	EHH3fHBAAAAAAAAAAAAAAA==
18_full_95	\N	1	18	7	AAAAAAAAAAAAAAAAAAD//A==
20_full_48	\N	1	20	9	Pj+4+DweDweDweDweDweDweDwfHfx8A=
20_full_49	\N	1	20	6	Mc88MMMMMMMMMMMMMM//
20_full_50	\N	1	20	10	Px/uHwMAwDAMBwOBwOBwOBwOAwDAMA///w==
20_full_51	\N	1	20	10	Px/uHwMAwDAMAwHB4HgHAMAwDAPA+Hf4/A==
20_full_52	\N	1	20	10	AwHA8Hw7HM4zDMMwz///AwDAMAwDAMAwDA==
20_full_53	\N	1	20	10	///8AwDAMAwDAP8f4BwDAMAwDAMA8H/5/A==
20_full_54	\N	1	20	10	P5/+DwDAMAwDAP8/7B8DwPA8DwPA+Hf4/A==
20_full_55	\N	1	20	9	///AYDAYHAwOBgcDA4GBwMBgcDAYDAA=
20_full_56	\N	1	20	10	Px/uHwPA8DwPh3+Px/uHwPA8DwPA+Hf4/A==
20_full_57	\N	1	20	10	Px/uHwPA8DwPA+Df8/wDAMAwDAMA8H/5/A==
24_full_48	\N	1	24	12	H4P8cO4HwDwDwDwDwDwDwDwDwDwDwDwDwDwDwDwD4HcOP8H4
24_full_49	\N	1	24	6	EMc88MMMMMMMMMMMMMMMMM//
24_full_50	\N	1	24	12	P4f84OwHADADADADAHAOAcA4BwDgHAOAcA4AwAwAwAwA////
24_full_51	\N	1	24	12	P4f84OwHADADADADADAHAOA8A8AOAHADADADADADwH4Of8P4
24_full_52	\N	1	24	12	AMAcA8B8DsHMOMcM4MwMwMwM////AMAMAMAMAMAMAMAMAMAM
24_full_53	\N	1	24	12	////wAwAwAwAwAwAwA4A/8f+AHADADADADADADADAH4O/8P4
24_full_54	\N	1	24	12	H+P/cD4AwAwAwAwAwAwA/4/84OwHwDwDwDwDwDwD4HcOP8H4
24_full_55	\N	1	24	11	///8AYAwBgDAGAcAwDgGAcAwDgGAcAwDgGAcAwDgGAMA
24_full_56	\N	1	24	12	H4P8cO4HwDwDwDwD4HcOP8P8cO4HwDwDwDwDwDwD4HcOP8H4
24_full_57	\N	1	24	12	H4P8cO4HwDwDwDwDwDwD4Hf/P/ADADADADADADADAHwO/8f4
_09_full_12_32	\N	1	12	1	AAA=
_09_full_12_33	\N	1	12	2	qqiA
_09_full_12_34	\N	1	12	3	tAAAAAA=
_09_full_12_35	\N	1	12	5	Ur6lfUoAAAA=
_09_full_12_36	\N	1	12	5	I6lHFK4gAAA=
_09_full_12_37	\N	1	12	5	BnQiIXMAAAA=
_09_full_12_38	\N	1	12	6	IUUIYliidBAA
_09_full_12_39	\N	1	12	1	wAA=
_09_full_12_40	\N	1	12	3	KkkiIAA=
_09_full_12_41	\N	1	12	3	iJJKgAA=
_09_full_12_42	\N	1	12	5	ASriOqQAAAA=
_09_full_12_43	\N	1	12	5	AAhPkIAAAAA=
_09_full_12_44	\N	1	12	3	AAACUAA=
_09_full_12_45	\N	1	12	4	AADwAAAA
_09_full_12_46	\N	1	12	2	AACA
_09_full_12_47	\N	1	12	5	CEQiIRCAAAA=
_09_full_12_48	\N	1	12	5	dGMYxjFwAAA=
_09_full_12_49	\N	1	12	3	WSSS4AA=
_09_full_12_50	\N	1	12	5	dEIiIhD4AAA=
_09_full_12_51	\N	1	12	5	dEITBDFwAAA=
_09_full_12_52	\N	1	12	5	EZUpfEIQAAA=
_09_full_12_53	\N	1	12	5	/CEPBDFwAAA=
_09_full_12_54	\N	1	12	5	dGEPRjFwAAA=
_09_full_12_55	\N	1	12	5	+EIhEIhAAAA=
_09_full_12_56	\N	1	12	5	dGMXRjFwAAA=
_09_full_12_57	\N	1	12	5	dGMXhDFwAAA=
_09_full_12_58	\N	1	12	2	AiAA
_09_full_12_59	\N	1	12	3	ACCgAAA=
_09_full_12_60	\N	1	12	4	ASSEIQAA
_09_full_12_61	\N	1	12	4	AA8PAAAA
_09_full_12_62	\N	1	12	4	CEISSAAA
_09_full_12_63	\N	1	12	5	dEIREIAgAAA=
_09_full_12_64	\N	1	12	6	ehlrppngeAAA
_09_full_12_65	\N	1	12	5	IqMfxjGIAAA=
_09_full_12_66	\N	1	12	5	9GMfRjHwAAA=
_09_full_12_67	\N	1	12	5	dGEIQhFwAAA=
_09_full_12_68	\N	1	12	5	9GMYxjHwAAA=
_09_full_12_69	\N	1	12	5	/CEOQhD4AAA=
_09_full_12_70	\N	1	12	5	/CEOQhCAAAA=
_09_full_12_71	\N	1	12	5	dGEJxjFwAAA=
_09_full_12_72	\N	1	12	5	jGMfxjGIAAA=
_09_full_12_73	\N	1	12	3	6SSS4AA=
_09_full_12_74	\N	1	12	4	ERERGWAA
_09_full_12_75	\N	1	12	5	jGVMUlGIAAA=
_09_full_12_76	\N	1	12	4	iIiIiPAA
_09_full_12_77	\N	1	12	7	g46smTBgwYIAAAA=
_09_full_12_78	\N	1	12	6	xxpplljjhAAA
_09_full_12_79	\N	1	12	5	dGMYxjFwAAA=
_09_full_12_80	\N	1	12	5	9GMfQhCAAAA=
_09_full_12_81	\N	1	12	5	dGMYxrJoQAA=
_09_full_12_82	\N	1	12	5	9GMfUlGIAAA=
_09_full_12_83	\N	1	12	5	dGEHBDFwAAA=
_09_full_12_84	\N	1	12	5	+QhCEIQgAAA=
_09_full_12_85	\N	1	12	5	jGMYxjFwAAA=
_09_full_12_86	\N	1	12	5	jGMYqUQgAAA=
_09_full_12_87	\N	1	12	7	gwYMGTJq1UQAAAA=
_09_full_12_88	\N	1	12	5	jGKiKjGIAAA=
_09_full_12_89	\N	1	12	5	jGKiEIQgAAA=
_09_full_12_90	\N	1	12	5	+EQiIRD4AAA=
_09_full_12_91	\N	1	12	3	8kkk4AA=
_09_full_12_92	\N	1	12	5	hBCCCEEIAAA=
_09_full_12_93	\N	1	12	3	5JJJ4AA=
_09_full_12_94	\N	1	12	5	IqIAAAAAAAA=
_09_full_12_95	\N	1	12	4	AAAAAA8A
_09_full_12_96	\N	1	12	2	pAAA
_09_full_12_97	\N	1	12	5	AADgvjNoAAA=
_09_full_12_98	\N	1	12	5	hCEPRjmwAAA=
_09_full_12_99	\N	1	12	4	AAB4iHAA
_09_full_12_100	\N	1	12	5	CEIXxjNoAAA=
_09_full_12_101	\N	1	12	5	AADox9BwAAA=
_09_full_12_102	\N	1	12	4	NET0REAA
_09_full_12_103	\N	1	12	5	AAAGzjF4YuA=
_09_full_12_104	\N	1	12	5	hCELZjGIAAA=
_09_full_12_105	\N	1	12	3	AQSSQAA=
_09_full_12_106	\N	1	12	4	ABARERGW
_09_full_12_107	\N	1	12	5	hCMqYpKIAAA=
_09_full_12_108	\N	1	12	3	SSSSQAA=
_09_full_12_109	\N	1	12	7	AAAACtpkyZIAAAA=
_09_full_12_110	\N	1	12	5	AAALZjGIAAA=
_09_full_12_111	\N	1	12	5	AAAHRjFwAAA=
_09_full_12_112	\N	1	12	5	AAALZjH0IQA=
_09_full_12_113	\N	1	12	5	AAAGzjF4QhA=
_09_full_12_114	\N	1	12	4	AAC8iIAA
_09_full_12_115	\N	1	12	5	AADosFFwAAA=
_09_full_12_116	\N	1	12	4	AETkRDAA
_09_full_12_117	\N	1	12	5	AAAIxjNoAAA=
_09_full_12_118	\N	1	12	5	AAAIxiogAAA=
_09_full_12_119	\N	1	12	7	AAAACDBk1UQAAAA=
_09_full_12_120	\N	1	12	5	AAAIqIqIAAA=
_09_full_12_121	\N	1	12	5	AAAIxjNoQuA=
_09_full_12_122	\N	1	12	5	AAHxERD4AAA=
_09_full_12_123	\N	1	12	3	aSiSYAA=
_09_full_12_124	\N	1	12	1	94A=
_09_full_12_125	\N	1	12	3	ySKSwAA=
_7_full_32	\N	1	7	1	AA==
_7_full_33	\N	1	7	2	qog=
_7_full_34	\N	1	7	3	tAAA
_7_full_35	\N	1	7	5	Ur6vqUA=
_7_full_36	\N	1	7	5	I6jiuIA=
_7_full_37	\N	1	7	5	xkRETGA=
_7_full_38	\N	1	7	5	RSiKyaA=
_7_full_39	\N	1	7	1	wA==
_7_full_40	\N	1	7	3	KkiI
_7_full_41	\N	1	7	3	iJKg
_7_full_42	\N	1	7	7	EFEUFEUEAA==
_7_full_43	\N	1	7	5	AQnyEAA=
_7_full_44	\N	1	7	3	AACg
_7_full_45	\N	1	7	4	AA8AAA==
_7_full_46	\N	1	7	2	AAg=
_7_full_47	\N	1	7	5	AEREQAA=
_7_full_48	\N	1	7	4	aZmZYA==
_7_full_49	\N	1	7	3	WSS4
_7_full_50	\N	1	7	4	aRJI8A==
_7_full_51	\N	1	7	4	aRYZYA==
_7_full_52	\N	1	7	5	EZUviEA=
_7_full_53	\N	1	7	4	+IYZYA==
_7_full_54	\N	1	7	4	aY6ZYA==
_7_full_55	\N	1	7	4	8RIkQA==
_7_full_56	\N	1	7	4	aZaZYA==
_7_full_57	\N	1	7	4	aZcZYA==
_7_full_58	\N	1	7	2	CIA=
_7_full_59	\N	1	7	3	AQUA
_7_full_60	\N	1	7	4	EkhCEA==
_7_full_61	\N	1	7	4	APDwAA==
_7_full_62	\N	1	7	4	hCEkgA==
_7_full_63	\N	1	7	4	aRIgIA==
_7_full_64	\N	1	7	5	dGdZwcA=
_7_full_65	\N	1	7	4	aZ+ZkA==
_7_full_66	\N	1	7	4	6Z6Z4A==
_7_full_67	\N	1	7	4	aYiJYA==
_7_full_68	\N	1	7	4	6ZmZ4A==
_7_full_69	\N	1	7	4	+I6I8A==
_7_full_70	\N	1	7	4	+I6IgA==
_7_full_71	\N	1	7	4	aYuZcA==
_7_full_72	\N	1	7	4	mZ+ZkA==
_7_full_73	\N	1	7	3	6SS4
_7_full_74	\N	1	7	4	EREZYA==
_7_full_75	\N	1	7	4	maypkA==
_7_full_76	\N	1	7	4	iIiI8A==
_7_full_77	\N	1	7	7	g46smTBggA==
_7_full_78	\N	1	7	5	jnNZziA=
_7_full_79	\N	1	7	4	aZmZYA==
_7_full_80	\N	1	7	4	6Z6IgA==
_7_full_81	\N	1	7	5	dGMayaA=
_7_full_82	\N	1	7	4	6Z6pkA==
_7_full_83	\N	1	7	4	aYYZYA==
_7_full_84	\N	1	7	5	+QhCEIA=
_7_full_85	\N	1	7	4	mZmZYA==
_7_full_86	\N	1	7	5	jGMVKIA=
_7_full_87	\N	1	7	7	gwYMmrVRAA==
_7_full_88	\N	1	7	5	jFRFRiA=
_7_full_89	\N	1	7	5	jFRCEIA=
_7_full_90	\N	1	7	5	+EREQ+A=
_7_full_91	\N	1	7	3	8kk4
_7_full_92	\N	1	7	5	BBBBBAA=
_7_full_93	\N	1	7	3	5JJ4
_7_full_94	\N	1	7	5	IqIAAAA=
_7_full_95	\N	1	7	5	AAAAA+A=
\.

COPY iris.glyph (name, font, code_point, graphic) FROM stdin;
07_char_32	07_char	32	07_char_32
07_char_33	07_char	33	07_char_33
07_char_34	07_char	34	07_char_34
07_char_35	07_char	35	07_char_35
07_char_36	07_char	36	07_char_36
07_char_37	07_char	37	07_char_37
07_char_38	07_char	38	07_char_38
07_char_39	07_char	39	07_char_39
07_char_40	07_char	40	07_char_40
07_char_41	07_char	41	07_char_41
07_char_42	07_char	42	07_char_42
07_char_43	07_char	43	07_char_43
07_char_44	07_char	44	07_char_44
07_char_45	07_char	45	07_char_45
07_char_46	07_char	46	07_char_46
07_char_47	07_char	47	07_char_47
07_char_48	07_char	48	07_char_48
07_char_49	07_char	49	07_char_49
07_char_50	07_char	50	07_char_50
07_char_51	07_char	51	07_char_51
07_char_52	07_char	52	07_char_52
07_char_53	07_char	53	07_char_53
07_char_54	07_char	54	07_char_54
07_char_55	07_char	55	07_char_55
07_char_56	07_char	56	07_char_56
07_char_57	07_char	57	07_char_57
07_char_58	07_char	58	07_char_58
07_char_59	07_char	59	07_char_59
07_char_60	07_char	60	07_char_60
07_char_61	07_char	61	07_char_61
07_char_62	07_char	62	07_char_62
07_char_63	07_char	63	07_char_63
07_char_64	07_char	64	07_char_64
07_char_65	07_char	65	07_char_65
07_char_66	07_char	66	07_char_66
07_char_67	07_char	67	07_char_67
07_char_68	07_char	68	07_char_68
07_char_69	07_char	69	07_char_69
07_char_70	07_char	70	07_char_70
07_char_71	07_char	71	07_char_71
07_char_72	07_char	72	07_char_72
07_char_73	07_char	73	07_char_73
07_char_74	07_char	74	07_char_74
07_char_75	07_char	75	07_char_75
07_char_76	07_char	76	07_char_76
07_char_77	07_char	77	07_char_77
07_char_78	07_char	78	07_char_78
07_char_79	07_char	79	07_char_79
07_char_80	07_char	80	07_char_80
07_char_81	07_char	81	07_char_81
07_char_82	07_char	82	07_char_82
07_char_83	07_char	83	07_char_83
07_char_84	07_char	84	07_char_84
07_char_85	07_char	85	07_char_85
07_char_86	07_char	86	07_char_86
07_char_87	07_char	87	07_char_87
07_char_88	07_char	88	07_char_88
07_char_89	07_char	89	07_char_89
07_char_90	07_char	90	07_char_90
07_char_91	07_char	91	07_char_91
07_char_92	07_char	92	07_char_92
07_char_93	07_char	93	07_char_93
07_char_94	07_char	94	07_char_94
07_char_95	07_char	95	07_char_95
07_line_32	07_line	32	07_line_32
07_line_33	07_line	33	07_line_33
07_line_34	07_line	34	07_line_34
07_line_35	07_line	35	07_line_35
07_line_36	07_line	36	07_line_36
07_line_37	07_line	37	07_line_37
07_line_38	07_line	38	07_line_38
07_line_39	07_line	39	07_line_39
07_line_40	07_line	40	07_line_40
07_line_41	07_line	41	07_line_41
07_line_42	07_line	42	07_line_42
07_line_43	07_line	43	07_line_43
07_line_44	07_line	44	07_line_44
07_line_45	07_line	45	07_line_45
07_line_46	07_line	46	07_line_46
07_line_47	07_line	47	07_line_47
07_line_48	07_line	48	07_line_48
07_line_49	07_line	49	07_line_49
07_line_50	07_line	50	07_line_50
07_line_51	07_line	51	07_line_51
07_line_52	07_line	52	07_line_52
07_line_53	07_line	53	07_line_53
07_line_54	07_line	54	07_line_54
07_line_55	07_line	55	07_line_55
07_line_56	07_line	56	07_line_56
07_line_57	07_line	57	07_line_57
07_line_58	07_line	58	07_line_58
07_line_59	07_line	59	07_line_59
07_line_60	07_line	60	07_line_60
07_line_61	07_line	61	07_line_61
07_line_62	07_line	62	07_line_62
07_line_63	07_line	63	07_line_63
07_line_64	07_line	64	07_line_64
07_line_65	07_line	65	07_line_65
07_line_66	07_line	66	07_line_66
07_line_67	07_line	67	07_line_67
07_line_68	07_line	68	07_line_68
07_line_69	07_line	69	07_line_69
07_line_70	07_line	70	07_line_70
07_line_71	07_line	71	07_line_71
07_line_72	07_line	72	07_line_72
07_line_73	07_line	73	07_line_73
07_line_74	07_line	74	07_line_74
07_line_75	07_line	75	07_line_75
07_line_76	07_line	76	07_line_76
07_line_77	07_line	77	07_line_77
07_line_78	07_line	78	07_line_78
07_line_79	07_line	79	07_line_79
07_line_80	07_line	80	07_line_80
07_line_81	07_line	81	07_line_81
07_line_82	07_line	82	07_line_82
07_line_83	07_line	83	07_line_83
07_line_84	07_line	84	07_line_84
07_line_85	07_line	85	07_line_85
07_line_86	07_line	86	07_line_86
07_line_87	07_line	87	07_line_87
07_line_88	07_line	88	07_line_88
07_line_89	07_line	89	07_line_89
07_line_90	07_line	90	07_line_90
07_line_91	07_line	91	07_line_91
07_line_92	07_line	92	07_line_92
07_line_93	07_line	93	07_line_93
07_line_94	07_line	94	07_line_94
07_line_95	07_line	95	07_line_95
08_full_32	08_full	32	08_full_32
08_full_33	08_full	33	08_full_33
08_full_34	08_full	34	08_full_34
08_full_35	08_full	35	08_full_35
08_full_36	08_full	36	08_full_36
08_full_37	08_full	37	08_full_37
08_full_38	08_full	38	08_full_38
08_full_39	08_full	39	08_full_39
08_full_40	08_full	40	08_full_40
08_full_41	08_full	41	08_full_41
08_full_42	08_full	42	08_full_42
08_full_43	08_full	43	08_full_43
08_full_44	08_full	44	08_full_44
08_full_45	08_full	45	08_full_45
08_full_46	08_full	46	08_full_46
08_full_47	08_full	47	08_full_47
08_full_48	08_full	48	08_full_48
08_full_49	08_full	49	08_full_49
08_full_50	08_full	50	08_full_50
08_full_51	08_full	51	08_full_51
08_full_52	08_full	52	08_full_52
08_full_53	08_full	53	08_full_53
08_full_54	08_full	54	08_full_54
08_full_55	08_full	55	08_full_55
08_full_56	08_full	56	08_full_56
08_full_57	08_full	57	08_full_57
08_full_58	08_full	58	08_full_58
08_full_59	08_full	59	08_full_59
08_full_60	08_full	60	08_full_60
08_full_61	08_full	61	08_full_61
08_full_62	08_full	62	08_full_62
08_full_63	08_full	63	08_full_63
08_full_64	08_full	64	08_full_64
08_full_65	08_full	65	08_full_65
08_full_66	08_full	66	08_full_66
08_full_67	08_full	67	08_full_67
08_full_68	08_full	68	08_full_68
08_full_69	08_full	69	08_full_69
08_full_70	08_full	70	08_full_70
08_full_71	08_full	71	08_full_71
08_full_72	08_full	72	08_full_72
08_full_73	08_full	73	08_full_73
08_full_74	08_full	74	08_full_74
08_full_75	08_full	75	08_full_75
08_full_76	08_full	76	08_full_76
08_full_77	08_full	77	08_full_77
08_full_78	08_full	78	08_full_78
08_full_79	08_full	79	08_full_79
08_full_80	08_full	80	08_full_80
08_full_81	08_full	81	08_full_81
08_full_82	08_full	82	08_full_82
08_full_83	08_full	83	08_full_83
08_full_84	08_full	84	08_full_84
08_full_85	08_full	85	08_full_85
08_full_86	08_full	86	08_full_86
08_full_87	08_full	87	08_full_87
08_full_88	08_full	88	08_full_88
08_full_89	08_full	89	08_full_89
08_full_90	08_full	90	08_full_90
08_full_91	08_full	91	08_full_91
08_full_92	08_full	92	08_full_92
08_full_93	08_full	93	08_full_93
08_full_94	08_full	94	08_full_94
08_full_95	08_full	95	08_full_95
09_full_32	09_full	32	09_full_32
09_full_33	09_full	33	09_full_33
09_full_34	09_full	34	09_full_34
09_full_35	09_full	35	09_full_35
09_full_36	09_full	36	09_full_36
09_full_37	09_full	37	09_full_37
09_full_38	09_full	38	09_full_38
09_full_39	09_full	39	09_full_39
09_full_40	09_full	40	09_full_40
09_full_41	09_full	41	09_full_41
09_full_42	09_full	42	09_full_42
09_full_43	09_full	43	09_full_43
09_full_44	09_full	44	09_full_44
09_full_45	09_full	45	09_full_45
09_full_46	09_full	46	09_full_46
09_full_47	09_full	47	09_full_47
09_full_48	09_full	48	09_full_48
09_full_49	09_full	49	09_full_49
09_full_50	09_full	50	09_full_50
09_full_51	09_full	51	09_full_51
09_full_52	09_full	52	09_full_52
09_full_53	09_full	53	09_full_53
09_full_54	09_full	54	09_full_54
09_full_55	09_full	55	09_full_55
09_full_56	09_full	56	09_full_56
09_full_57	09_full	57	09_full_57
09_full_58	09_full	58	09_full_58
09_full_59	09_full	59	09_full_59
09_full_60	09_full	60	09_full_60
09_full_61	09_full	61	09_full_61
09_full_62	09_full	62	09_full_62
09_full_63	09_full	63	09_full_63
09_full_64	09_full	64	09_full_64
09_full_65	09_full	65	09_full_65
09_full_66	09_full	66	09_full_66
09_full_67	09_full	67	09_full_67
09_full_68	09_full	68	09_full_68
09_full_69	09_full	69	09_full_69
09_full_70	09_full	70	09_full_70
09_full_71	09_full	71	09_full_71
09_full_72	09_full	72	09_full_72
09_full_73	09_full	73	09_full_73
09_full_74	09_full	74	09_full_74
09_full_75	09_full	75	09_full_75
09_full_76	09_full	76	09_full_76
09_full_77	09_full	77	09_full_77
09_full_78	09_full	78	09_full_78
09_full_79	09_full	79	09_full_79
09_full_80	09_full	80	09_full_80
09_full_81	09_full	81	09_full_81
09_full_82	09_full	82	09_full_82
09_full_83	09_full	83	09_full_83
09_full_84	09_full	84	09_full_84
09_full_85	09_full	85	09_full_85
09_full_86	09_full	86	09_full_86
09_full_87	09_full	87	09_full_87
09_full_88	09_full	88	09_full_88
09_full_89	09_full	89	09_full_89
09_full_90	09_full	90	09_full_90
09_full_91	09_full	91	09_full_91
09_full_92	09_full	92	09_full_92
09_full_93	09_full	93	09_full_93
09_full_94	09_full	94	09_full_94
09_full_95	09_full	95	09_full_95
10_full_32	10_full	32	10_full_32
10_full_33	10_full	33	10_full_33
10_full_34	10_full	34	10_full_34
10_full_35	10_full	35	10_full_35
10_full_36	10_full	36	10_full_36
10_full_37	10_full	37	10_full_37
10_full_38	10_full	38	10_full_38
10_full_39	10_full	39	10_full_39
10_full_40	10_full	40	10_full_40
10_full_41	10_full	41	10_full_41
10_full_42	10_full	42	10_full_42
10_full_43	10_full	43	10_full_43
10_full_44	10_full	44	10_full_44
10_full_45	10_full	45	10_full_45
10_full_46	10_full	46	10_full_46
10_full_47	10_full	47	10_full_47
10_full_48	10_full	48	10_full_48
10_full_49	10_full	49	10_full_49
10_full_50	10_full	50	10_full_50
10_full_51	10_full	51	10_full_51
10_full_52	10_full	52	10_full_52
10_full_53	10_full	53	10_full_53
10_full_54	10_full	54	10_full_54
10_full_55	10_full	55	10_full_55
10_full_56	10_full	56	10_full_56
10_full_57	10_full	57	10_full_57
10_full_58	10_full	58	10_full_58
10_full_59	10_full	59	10_full_59
10_full_60	10_full	60	10_full_60
10_full_61	10_full	61	10_full_61
10_full_62	10_full	62	10_full_62
10_full_63	10_full	63	10_full_63
10_full_64	10_full	64	10_full_64
10_full_65	10_full	65	10_full_65
10_full_66	10_full	66	10_full_66
10_full_67	10_full	67	10_full_67
10_full_68	10_full	68	10_full_68
10_full_69	10_full	69	10_full_69
10_full_70	10_full	70	10_full_70
10_full_71	10_full	71	10_full_71
10_full_72	10_full	72	10_full_72
10_full_73	10_full	73	10_full_73
10_full_74	10_full	74	10_full_74
10_full_75	10_full	75	10_full_75
10_full_76	10_full	76	10_full_76
10_full_77	10_full	77	10_full_77
10_full_78	10_full	78	10_full_78
10_full_79	10_full	79	10_full_79
10_full_80	10_full	80	10_full_80
10_full_81	10_full	81	10_full_81
10_full_82	10_full	82	10_full_82
10_full_83	10_full	83	10_full_83
10_full_84	10_full	84	10_full_84
10_full_85	10_full	85	10_full_85
10_full_86	10_full	86	10_full_86
10_full_87	10_full	87	10_full_87
10_full_88	10_full	88	10_full_88
10_full_89	10_full	89	10_full_89
10_full_90	10_full	90	10_full_90
10_full_91	10_full	91	10_full_91
10_full_92	10_full	92	10_full_92
10_full_93	10_full	93	10_full_93
10_full_94	10_full	94	10_full_94
10_full_95	10_full	95	10_full_95
11_full_32	11_full	32	11_full_32
11_full_33	11_full	33	11_full_33
11_full_34	11_full	34	11_full_34
11_full_35	11_full	35	11_full_35
11_full_36	11_full	36	11_full_36
11_full_37	11_full	37	11_full_37
11_full_38	11_full	38	11_full_38
11_full_39	11_full	39	11_full_39
11_full_40	11_full	40	11_full_40
11_full_41	11_full	41	11_full_41
11_full_42	11_full	42	11_full_42
11_full_43	11_full	43	11_full_43
11_full_44	11_full	44	11_full_44
11_full_45	11_full	45	11_full_45
11_full_46	11_full	46	11_full_46
11_full_47	11_full	47	11_full_47
11_full_48	11_full	48	11_full_48
11_full_49	11_full	49	11_full_49
11_full_50	11_full	50	11_full_50
11_full_51	11_full	51	11_full_51
11_full_52	11_full	52	11_full_52
11_full_53	11_full	53	11_full_53
11_full_54	11_full	54	11_full_54
11_full_55	11_full	55	11_full_55
11_full_56	11_full	56	11_full_56
11_full_57	11_full	57	11_full_57
11_full_58	11_full	58	11_full_58
11_full_59	11_full	59	11_full_59
11_full_60	11_full	60	11_full_60
11_full_61	11_full	61	11_full_61
11_full_62	11_full	62	11_full_62
11_full_63	11_full	63	11_full_63
11_full_64	11_full	64	11_full_64
11_full_65	11_full	65	11_full_65
11_full_66	11_full	66	11_full_66
11_full_67	11_full	67	11_full_67
11_full_68	11_full	68	11_full_68
11_full_69	11_full	69	11_full_69
11_full_70	11_full	70	11_full_70
11_full_71	11_full	71	11_full_71
11_full_72	11_full	72	11_full_72
11_full_73	11_full	73	11_full_73
11_full_74	11_full	74	11_full_74
11_full_75	11_full	75	11_full_75
11_full_76	11_full	76	11_full_76
11_full_77	11_full	77	11_full_77
11_full_78	11_full	78	11_full_78
11_full_79	11_full	79	11_full_79
11_full_80	11_full	80	11_full_80
11_full_81	11_full	81	11_full_81
11_full_82	11_full	82	11_full_82
11_full_83	11_full	83	11_full_83
11_full_84	11_full	84	11_full_84
11_full_85	11_full	85	11_full_85
11_full_86	11_full	86	11_full_86
11_full_87	11_full	87	11_full_87
11_full_88	11_full	88	11_full_88
11_full_89	11_full	89	11_full_89
11_full_90	11_full	90	11_full_90
11_full_91	11_full	91	11_full_91
11_full_92	11_full	92	11_full_92
11_full_93	11_full	93	11_full_93
11_full_94	11_full	94	11_full_94
11_full_95	11_full	95	11_full_95
12_full_32	12_full	32	12_full_32
12_full_33	12_full	33	12_full_33
12_full_34	12_full	34	12_full_34
12_full_35	12_full	35	12_full_35
12_full_36	12_full	36	12_full_36
12_full_37	12_full	37	12_full_37
12_full_38	12_full	38	12_full_38
12_full_39	12_full	39	12_full_39
12_full_40	12_full	40	12_full_40
12_full_41	12_full	41	12_full_41
12_full_42	12_full	42	12_full_42
12_full_43	12_full	43	12_full_43
12_full_44	12_full	44	12_full_44
12_full_45	12_full	45	12_full_45
12_full_46	12_full	46	12_full_46
12_full_47	12_full	47	12_full_47
12_full_48	12_full	48	12_full_48
12_full_49	12_full	49	12_full_49
12_full_50	12_full	50	12_full_50
12_full_51	12_full	51	12_full_51
12_full_52	12_full	52	12_full_52
12_full_53	12_full	53	12_full_53
12_full_54	12_full	54	12_full_54
12_full_55	12_full	55	12_full_55
12_full_56	12_full	56	12_full_56
12_full_57	12_full	57	12_full_57
12_full_58	12_full	58	12_full_58
12_full_59	12_full	59	12_full_59
12_full_60	12_full	60	12_full_60
12_full_61	12_full	61	12_full_61
12_full_62	12_full	62	12_full_62
12_full_63	12_full	63	12_full_63
12_full_64	12_full	64	12_full_64
12_full_65	12_full	65	12_full_65
12_full_66	12_full	66	12_full_66
12_full_67	12_full	67	12_full_67
12_full_68	12_full	68	12_full_68
12_full_69	12_full	69	12_full_69
12_full_70	12_full	70	12_full_70
12_full_71	12_full	71	12_full_71
12_full_72	12_full	72	12_full_72
12_full_73	12_full	73	12_full_73
12_full_74	12_full	74	12_full_74
12_full_75	12_full	75	12_full_75
12_full_76	12_full	76	12_full_76
12_full_77	12_full	77	12_full_77
12_full_78	12_full	78	12_full_78
12_full_79	12_full	79	12_full_79
12_full_80	12_full	80	12_full_80
12_full_81	12_full	81	12_full_81
12_full_82	12_full	82	12_full_82
12_full_83	12_full	83	12_full_83
12_full_84	12_full	84	12_full_84
12_full_85	12_full	85	12_full_85
12_full_86	12_full	86	12_full_86
12_full_87	12_full	87	12_full_87
12_full_88	12_full	88	12_full_88
12_full_89	12_full	89	12_full_89
12_full_90	12_full	90	12_full_90
12_full_91	12_full	91	12_full_91
12_full_92	12_full	92	12_full_92
12_full_93	12_full	93	12_full_93
12_full_94	12_full	94	12_full_94
12_full_95	12_full	95	12_full_95
12_full_bold_32	12_full_bold	32	12_full_bold_32
12_full_bold_33	12_full_bold	33	12_full_bold_33
12_full_bold_34	12_full_bold	34	12_full_bold_34
12_full_bold_35	12_full_bold	35	12_full_bold_35
12_full_bold_36	12_full_bold	36	12_full_bold_36
12_full_bold_37	12_full_bold	37	12_full_bold_37
12_full_bold_38	12_full_bold	38	12_full_bold_38
12_full_bold_39	12_full_bold	39	12_full_bold_39
12_full_bold_40	12_full_bold	40	12_full_bold_40
12_full_bold_41	12_full_bold	41	12_full_bold_41
12_full_bold_42	12_full_bold	42	12_full_bold_42
12_full_bold_43	12_full_bold	43	12_full_bold_43
12_full_bold_44	12_full_bold	44	12_full_bold_44
12_full_bold_45	12_full_bold	45	12_full_bold_45
12_full_bold_46	12_full_bold	46	12_full_bold_46
12_full_bold_47	12_full_bold	47	12_full_bold_47
12_full_bold_48	12_full_bold	48	12_full_bold_48
12_full_bold_49	12_full_bold	49	12_full_bold_49
12_full_bold_50	12_full_bold	50	12_full_bold_50
12_full_bold_51	12_full_bold	51	12_full_bold_51
12_full_bold_52	12_full_bold	52	12_full_bold_52
12_full_bold_53	12_full_bold	53	12_full_bold_53
12_full_bold_54	12_full_bold	54	12_full_bold_54
12_full_bold_55	12_full_bold	55	12_full_bold_55
12_full_bold_56	12_full_bold	56	12_full_bold_56
12_full_bold_57	12_full_bold	57	12_full_bold_57
12_full_bold_58	12_full_bold	58	12_full_bold_58
12_full_bold_59	12_full_bold	59	12_full_bold_59
12_full_bold_60	12_full_bold	60	12_full_bold_60
12_full_bold_61	12_full_bold	61	12_full_bold_61
12_full_bold_62	12_full_bold	62	12_full_bold_62
12_full_bold_63	12_full_bold	63	12_full_bold_63
12_full_bold_64	12_full_bold	64	12_full_bold_64
12_full_bold_65	12_full_bold	65	12_full_bold_65
12_full_bold_66	12_full_bold	66	12_full_bold_66
12_full_bold_67	12_full_bold	67	12_full_bold_67
12_full_bold_68	12_full_bold	68	12_full_bold_68
12_full_bold_69	12_full_bold	69	12_full_bold_69
12_full_bold_70	12_full_bold	70	12_full_bold_70
12_full_bold_71	12_full_bold	71	12_full_bold_71
12_full_bold_72	12_full_bold	72	12_full_bold_72
12_full_bold_73	12_full_bold	73	12_full_bold_73
12_full_bold_74	12_full_bold	74	12_full_bold_74
12_full_bold_75	12_full_bold	75	12_full_bold_75
12_full_bold_76	12_full_bold	76	12_full_bold_76
12_full_bold_77	12_full_bold	77	12_full_bold_77
12_full_bold_78	12_full_bold	78	12_full_bold_78
12_full_bold_79	12_full_bold	79	12_full_bold_79
12_full_bold_80	12_full_bold	80	12_full_bold_80
12_full_bold_81	12_full_bold	81	12_full_bold_81
12_full_bold_82	12_full_bold	82	12_full_bold_82
12_full_bold_83	12_full_bold	83	12_full_bold_83
12_full_bold_84	12_full_bold	84	12_full_bold_84
12_full_bold_85	12_full_bold	85	12_full_bold_85
12_full_bold_86	12_full_bold	86	12_full_bold_86
12_full_bold_87	12_full_bold	87	12_full_bold_87
12_full_bold_88	12_full_bold	88	12_full_bold_88
12_full_bold_89	12_full_bold	89	12_full_bold_89
12_full_bold_90	12_full_bold	90	12_full_bold_90
12_full_bold_91	12_full_bold	91	12_full_bold_91
12_full_bold_92	12_full_bold	92	12_full_bold_92
12_full_bold_93	12_full_bold	93	12_full_bold_93
12_full_bold_94	12_full_bold	94	12_full_bold_94
12_full_bold_95	12_full_bold	95	12_full_bold_95
13_full_32	13_full	32	13_full_32
13_full_33	13_full	33	13_full_33
13_full_34	13_full	34	13_full_34
13_full_35	13_full	35	13_full_35
13_full_36	13_full	36	13_full_36
13_full_37	13_full	37	13_full_37
13_full_38	13_full	38	13_full_38
13_full_39	13_full	39	13_full_39
13_full_40	13_full	40	13_full_40
13_full_41	13_full	41	13_full_41
13_full_42	13_full	42	13_full_42
13_full_43	13_full	43	13_full_43
13_full_44	13_full	44	13_full_44
13_full_45	13_full	45	13_full_45
13_full_46	13_full	46	13_full_46
13_full_47	13_full	47	13_full_47
13_full_48	13_full	48	13_full_48
13_full_49	13_full	49	13_full_49
13_full_50	13_full	50	13_full_50
13_full_51	13_full	51	13_full_51
13_full_52	13_full	52	13_full_52
13_full_53	13_full	53	13_full_53
13_full_54	13_full	54	13_full_54
13_full_55	13_full	55	13_full_55
13_full_56	13_full	56	13_full_56
13_full_57	13_full	57	13_full_57
13_full_58	13_full	58	13_full_58
13_full_59	13_full	59	13_full_59
13_full_60	13_full	60	13_full_60
13_full_61	13_full	61	13_full_61
13_full_62	13_full	62	13_full_62
13_full_63	13_full	63	13_full_63
13_full_64	13_full	64	13_full_64
13_full_65	13_full	65	13_full_65
13_full_66	13_full	66	13_full_66
13_full_67	13_full	67	13_full_67
13_full_68	13_full	68	13_full_68
13_full_69	13_full	69	13_full_69
13_full_70	13_full	70	13_full_70
13_full_71	13_full	71	13_full_71
13_full_72	13_full	72	13_full_72
13_full_73	13_full	73	13_full_73
13_full_74	13_full	74	13_full_74
13_full_75	13_full	75	13_full_75
13_full_76	13_full	76	13_full_76
13_full_77	13_full	77	13_full_77
13_full_78	13_full	78	13_full_78
13_full_79	13_full	79	13_full_79
13_full_80	13_full	80	13_full_80
13_full_81	13_full	81	13_full_81
13_full_82	13_full	82	13_full_82
13_full_83	13_full	83	13_full_83
13_full_84	13_full	84	13_full_84
13_full_85	13_full	85	13_full_85
13_full_86	13_full	86	13_full_86
13_full_87	13_full	87	13_full_87
13_full_88	13_full	88	13_full_88
13_full_89	13_full	89	13_full_89
13_full_90	13_full	90	13_full_90
13_full_91	13_full	91	13_full_91
13_full_92	13_full	92	13_full_92
13_full_93	13_full	93	13_full_93
13_full_94	13_full	94	13_full_94
13_full_95	13_full	95	13_full_95
14_full_32	14_full	32	14_full_32
14_full_33	14_full	33	14_full_33
14_full_34	14_full	34	14_full_34
14_full_35	14_full	35	14_full_35
14_full_36	14_full	36	14_full_36
14_full_37	14_full	37	14_full_37
14_full_38	14_full	38	14_full_38
14_full_39	14_full	39	14_full_39
14_full_40	14_full	40	14_full_40
14_full_41	14_full	41	14_full_41
14_full_42	14_full	42	14_full_42
14_full_43	14_full	43	14_full_43
14_full_44	14_full	44	14_full_44
14_full_45	14_full	45	14_full_45
14_full_46	14_full	46	14_full_46
14_full_47	14_full	47	14_full_47
14_full_48	14_full	48	14_full_48
14_full_49	14_full	49	14_full_49
14_full_50	14_full	50	14_full_50
14_full_51	14_full	51	14_full_51
14_full_52	14_full	52	14_full_52
14_full_53	14_full	53	14_full_53
14_full_54	14_full	54	14_full_54
14_full_55	14_full	55	14_full_55
14_full_56	14_full	56	14_full_56
14_full_57	14_full	57	14_full_57
14_full_58	14_full	58	14_full_58
14_full_59	14_full	59	14_full_59
14_full_60	14_full	60	14_full_60
14_full_61	14_full	61	14_full_61
14_full_62	14_full	62	14_full_62
14_full_63	14_full	63	14_full_63
14_full_64	14_full	64	14_full_64
14_full_65	14_full	65	14_full_65
14_full_66	14_full	66	14_full_66
14_full_67	14_full	67	14_full_67
14_full_68	14_full	68	14_full_68
14_full_69	14_full	69	14_full_69
14_full_70	14_full	70	14_full_70
14_full_71	14_full	71	14_full_71
14_full_72	14_full	72	14_full_72
14_full_73	14_full	73	14_full_73
14_full_74	14_full	74	14_full_74
14_full_75	14_full	75	14_full_75
14_full_76	14_full	76	14_full_76
14_full_77	14_full	77	14_full_77
14_full_78	14_full	78	14_full_78
14_full_79	14_full	79	14_full_79
14_full_80	14_full	80	14_full_80
14_full_81	14_full	81	14_full_81
14_full_82	14_full	82	14_full_82
14_full_83	14_full	83	14_full_83
14_full_84	14_full	84	14_full_84
14_full_85	14_full	85	14_full_85
14_full_86	14_full	86	14_full_86
14_full_87	14_full	87	14_full_87
14_full_88	14_full	88	14_full_88
14_full_89	14_full	89	14_full_89
14_full_90	14_full	90	14_full_90
14_full_91	14_full	91	14_full_91
14_full_92	14_full	92	14_full_92
14_full_93	14_full	93	14_full_93
14_full_94	14_full	94	14_full_94
14_full_95	14_full	95	14_full_95
16_full_32	16_full	32	16_full_32
16_full_33	16_full	33	16_full_33
16_full_34	16_full	34	16_full_34
16_full_35	16_full	35	16_full_35
16_full_36	16_full	36	16_full_36
16_full_37	16_full	37	16_full_37
16_full_38	16_full	38	16_full_38
16_full_39	16_full	39	16_full_39
16_full_40	16_full	40	16_full_40
16_full_41	16_full	41	16_full_41
16_full_42	16_full	42	16_full_42
16_full_43	16_full	43	16_full_43
16_full_44	16_full	44	16_full_44
16_full_45	16_full	45	16_full_45
16_full_46	16_full	46	16_full_46
16_full_47	16_full	47	16_full_47
16_full_48	16_full	48	16_full_48
16_full_49	16_full	49	16_full_49
16_full_50	16_full	50	16_full_50
16_full_51	16_full	51	16_full_51
16_full_52	16_full	52	16_full_52
16_full_53	16_full	53	16_full_53
16_full_54	16_full	54	16_full_54
16_full_55	16_full	55	16_full_55
16_full_56	16_full	56	16_full_56
16_full_57	16_full	57	16_full_57
16_full_58	16_full	58	16_full_58
16_full_59	16_full	59	16_full_59
16_full_60	16_full	60	16_full_60
16_full_61	16_full	61	16_full_61
16_full_62	16_full	62	16_full_62
16_full_63	16_full	63	16_full_63
16_full_64	16_full	64	16_full_64
16_full_65	16_full	65	16_full_65
16_full_66	16_full	66	16_full_66
16_full_67	16_full	67	16_full_67
16_full_68	16_full	68	16_full_68
16_full_69	16_full	69	16_full_69
16_full_70	16_full	70	16_full_70
16_full_71	16_full	71	16_full_71
16_full_72	16_full	72	16_full_72
16_full_73	16_full	73	16_full_73
16_full_74	16_full	74	16_full_74
16_full_75	16_full	75	16_full_75
16_full_76	16_full	76	16_full_76
16_full_77	16_full	77	16_full_77
16_full_78	16_full	78	16_full_78
16_full_79	16_full	79	16_full_79
16_full_80	16_full	80	16_full_80
16_full_81	16_full	81	16_full_81
16_full_82	16_full	82	16_full_82
16_full_83	16_full	83	16_full_83
16_full_84	16_full	84	16_full_84
16_full_85	16_full	85	16_full_85
16_full_86	16_full	86	16_full_86
16_full_87	16_full	87	16_full_87
16_full_88	16_full	88	16_full_88
16_full_89	16_full	89	16_full_89
16_full_90	16_full	90	16_full_90
16_full_91	16_full	91	16_full_91
16_full_92	16_full	92	16_full_92
16_full_93	16_full	93	16_full_93
16_full_94	16_full	94	16_full_94
16_full_95	16_full	95	16_full_95
18_full_32	18_full	32	18_full_32
18_full_33	18_full	33	18_full_33
18_full_34	18_full	34	18_full_34
18_full_35	18_full	35	18_full_35
18_full_36	18_full	36	18_full_36
18_full_37	18_full	37	18_full_37
18_full_38	18_full	38	18_full_38
18_full_39	18_full	39	18_full_39
18_full_40	18_full	40	18_full_40
18_full_41	18_full	41	18_full_41
18_full_42	18_full	42	18_full_42
18_full_43	18_full	43	18_full_43
18_full_44	18_full	44	18_full_44
18_full_45	18_full	45	18_full_45
18_full_46	18_full	46	18_full_46
18_full_47	18_full	47	18_full_47
18_full_48	18_full	48	18_full_48
18_full_49	18_full	49	18_full_49
18_full_50	18_full	50	18_full_50
18_full_51	18_full	51	18_full_51
18_full_52	18_full	52	18_full_52
18_full_53	18_full	53	18_full_53
18_full_54	18_full	54	18_full_54
18_full_55	18_full	55	18_full_55
18_full_56	18_full	56	18_full_56
18_full_57	18_full	57	18_full_57
18_full_58	18_full	58	18_full_58
18_full_59	18_full	59	18_full_59
18_full_60	18_full	60	18_full_60
18_full_61	18_full	61	18_full_61
18_full_62	18_full	62	18_full_62
18_full_63	18_full	63	18_full_63
18_full_64	18_full	64	18_full_64
18_full_65	18_full	65	18_full_65
18_full_66	18_full	66	18_full_66
18_full_67	18_full	67	18_full_67
18_full_68	18_full	68	18_full_68
18_full_69	18_full	69	18_full_69
18_full_70	18_full	70	18_full_70
18_full_71	18_full	71	18_full_71
18_full_72	18_full	72	18_full_72
18_full_73	18_full	73	18_full_73
18_full_74	18_full	74	18_full_74
18_full_75	18_full	75	18_full_75
18_full_76	18_full	76	18_full_76
18_full_77	18_full	77	18_full_77
18_full_78	18_full	78	18_full_78
18_full_79	18_full	79	18_full_79
18_full_80	18_full	80	18_full_80
18_full_81	18_full	81	18_full_81
18_full_82	18_full	82	18_full_82
18_full_83	18_full	83	18_full_83
18_full_84	18_full	84	18_full_84
18_full_85	18_full	85	18_full_85
18_full_86	18_full	86	18_full_86
18_full_87	18_full	87	18_full_87
18_full_88	18_full	88	18_full_88
18_full_89	18_full	89	18_full_89
18_full_90	18_full	90	18_full_90
18_full_91	18_full	91	18_full_91
18_full_92	18_full	92	18_full_92
18_full_93	18_full	93	18_full_93
18_full_94	18_full	94	18_full_94
18_full_95	18_full	95	18_full_95
20_full_48	20_full	48	20_full_48
20_full_49	20_full	49	20_full_49
20_full_50	20_full	50	20_full_50
20_full_51	20_full	51	20_full_51
20_full_52	20_full	52	20_full_52
20_full_53	20_full	53	20_full_53
20_full_54	20_full	54	20_full_54
20_full_55	20_full	55	20_full_55
20_full_56	20_full	56	20_full_56
20_full_57	20_full	57	20_full_57
24_full_48	24_full	48	24_full_48
24_full_49	24_full	49	24_full_49
24_full_50	24_full	50	24_full_50
24_full_51	24_full	51	24_full_51
24_full_52	24_full	52	24_full_52
24_full_53	24_full	53	24_full_53
24_full_54	24_full	54	24_full_54
24_full_55	24_full	55	24_full_55
24_full_56	24_full	56	24_full_56
24_full_57	24_full	57	24_full_57
_09_full_12_32	_09_full_12	32	_09_full_12_32
_09_full_12_33	_09_full_12	33	_09_full_12_33
_09_full_12_34	_09_full_12	34	_09_full_12_34
_09_full_12_35	_09_full_12	35	_09_full_12_35
_09_full_12_36	_09_full_12	36	_09_full_12_36
_09_full_12_37	_09_full_12	37	_09_full_12_37
_09_full_12_38	_09_full_12	38	_09_full_12_38
_09_full_12_39	_09_full_12	39	_09_full_12_39
_09_full_12_40	_09_full_12	40	_09_full_12_40
_09_full_12_41	_09_full_12	41	_09_full_12_41
_09_full_12_42	_09_full_12	42	_09_full_12_42
_09_full_12_43	_09_full_12	43	_09_full_12_43
_09_full_12_44	_09_full_12	44	_09_full_12_44
_09_full_12_45	_09_full_12	45	_09_full_12_45
_09_full_12_46	_09_full_12	46	_09_full_12_46
_09_full_12_47	_09_full_12	47	_09_full_12_47
_09_full_12_48	_09_full_12	48	_09_full_12_48
_09_full_12_49	_09_full_12	49	_09_full_12_49
_09_full_12_50	_09_full_12	50	_09_full_12_50
_09_full_12_51	_09_full_12	51	_09_full_12_51
_09_full_12_52	_09_full_12	52	_09_full_12_52
_09_full_12_53	_09_full_12	53	_09_full_12_53
_09_full_12_54	_09_full_12	54	_09_full_12_54
_09_full_12_55	_09_full_12	55	_09_full_12_55
_09_full_12_56	_09_full_12	56	_09_full_12_56
_09_full_12_57	_09_full_12	57	_09_full_12_57
_09_full_12_58	_09_full_12	58	_09_full_12_58
_09_full_12_59	_09_full_12	59	_09_full_12_59
_09_full_12_60	_09_full_12	60	_09_full_12_60
_09_full_12_61	_09_full_12	61	_09_full_12_61
_09_full_12_62	_09_full_12	62	_09_full_12_62
_09_full_12_63	_09_full_12	63	_09_full_12_63
_09_full_12_64	_09_full_12	64	_09_full_12_64
_09_full_12_65	_09_full_12	65	_09_full_12_65
_09_full_12_66	_09_full_12	66	_09_full_12_66
_09_full_12_67	_09_full_12	67	_09_full_12_67
_09_full_12_68	_09_full_12	68	_09_full_12_68
_09_full_12_69	_09_full_12	69	_09_full_12_69
_09_full_12_70	_09_full_12	70	_09_full_12_70
_09_full_12_71	_09_full_12	71	_09_full_12_71
_09_full_12_72	_09_full_12	72	_09_full_12_72
_09_full_12_73	_09_full_12	73	_09_full_12_73
_09_full_12_74	_09_full_12	74	_09_full_12_74
_09_full_12_75	_09_full_12	75	_09_full_12_75
_09_full_12_76	_09_full_12	76	_09_full_12_76
_09_full_12_77	_09_full_12	77	_09_full_12_77
_09_full_12_78	_09_full_12	78	_09_full_12_78
_09_full_12_79	_09_full_12	79	_09_full_12_79
_09_full_12_80	_09_full_12	80	_09_full_12_80
_09_full_12_81	_09_full_12	81	_09_full_12_81
_09_full_12_82	_09_full_12	82	_09_full_12_82
_09_full_12_83	_09_full_12	83	_09_full_12_83
_09_full_12_84	_09_full_12	84	_09_full_12_84
_09_full_12_85	_09_full_12	85	_09_full_12_85
_09_full_12_86	_09_full_12	86	_09_full_12_86
_09_full_12_87	_09_full_12	87	_09_full_12_87
_09_full_12_88	_09_full_12	88	_09_full_12_88
_09_full_12_89	_09_full_12	89	_09_full_12_89
_09_full_12_90	_09_full_12	90	_09_full_12_90
_09_full_12_91	_09_full_12	91	_09_full_12_91
_09_full_12_92	_09_full_12	92	_09_full_12_92
_09_full_12_93	_09_full_12	93	_09_full_12_93
_09_full_12_94	_09_full_12	94	_09_full_12_94
_09_full_12_95	_09_full_12	95	_09_full_12_95
_09_full_12_96	_09_full_12	96	_09_full_12_96
_09_full_12_97	_09_full_12	97	_09_full_12_97
_09_full_12_98	_09_full_12	98	_09_full_12_98
_09_full_12_99	_09_full_12	99	_09_full_12_99
_09_full_12_100	_09_full_12	100	_09_full_12_100
_09_full_12_101	_09_full_12	101	_09_full_12_101
_09_full_12_102	_09_full_12	102	_09_full_12_102
_09_full_12_103	_09_full_12	103	_09_full_12_103
_09_full_12_104	_09_full_12	104	_09_full_12_104
_09_full_12_105	_09_full_12	105	_09_full_12_105
_09_full_12_106	_09_full_12	106	_09_full_12_106
_09_full_12_107	_09_full_12	107	_09_full_12_107
_09_full_12_108	_09_full_12	108	_09_full_12_108
_09_full_12_109	_09_full_12	109	_09_full_12_109
_09_full_12_110	_09_full_12	110	_09_full_12_110
_09_full_12_111	_09_full_12	111	_09_full_12_111
_09_full_12_112	_09_full_12	112	_09_full_12_112
_09_full_12_113	_09_full_12	113	_09_full_12_113
_09_full_12_114	_09_full_12	114	_09_full_12_114
_09_full_12_115	_09_full_12	115	_09_full_12_115
_09_full_12_116	_09_full_12	116	_09_full_12_116
_09_full_12_117	_09_full_12	117	_09_full_12_117
_09_full_12_118	_09_full_12	118	_09_full_12_118
_09_full_12_119	_09_full_12	119	_09_full_12_119
_09_full_12_120	_09_full_12	120	_09_full_12_120
_09_full_12_121	_09_full_12	121	_09_full_12_121
_09_full_12_122	_09_full_12	122	_09_full_12_122
_09_full_12_123	_09_full_12	123	_09_full_12_123
_09_full_12_124	_09_full_12	124	_09_full_12_124
_09_full_12_125	_09_full_12	125	_09_full_12_125
_7_full_32	_7_full	32	_7_full_32
_7_full_33	_7_full	33	_7_full_33
_7_full_34	_7_full	34	_7_full_34
_7_full_35	_7_full	35	_7_full_35
_7_full_36	_7_full	36	_7_full_36
_7_full_37	_7_full	37	_7_full_37
_7_full_38	_7_full	38	_7_full_38
_7_full_39	_7_full	39	_7_full_39
_7_full_40	_7_full	40	_7_full_40
_7_full_41	_7_full	41	_7_full_41
_7_full_42	_7_full	42	_7_full_42
_7_full_43	_7_full	43	_7_full_43
_7_full_44	_7_full	44	_7_full_44
_7_full_45	_7_full	45	_7_full_45
_7_full_46	_7_full	46	_7_full_46
_7_full_47	_7_full	47	_7_full_47
_7_full_48	_7_full	48	_7_full_48
_7_full_49	_7_full	49	_7_full_49
_7_full_50	_7_full	50	_7_full_50
_7_full_51	_7_full	51	_7_full_51
_7_full_52	_7_full	52	_7_full_52
_7_full_53	_7_full	53	_7_full_53
_7_full_54	_7_full	54	_7_full_54
_7_full_55	_7_full	55	_7_full_55
_7_full_56	_7_full	56	_7_full_56
_7_full_57	_7_full	57	_7_full_57
_7_full_58	_7_full	58	_7_full_58
_7_full_59	_7_full	59	_7_full_59
_7_full_60	_7_full	60	_7_full_60
_7_full_61	_7_full	61	_7_full_61
_7_full_62	_7_full	62	_7_full_62
_7_full_63	_7_full	63	_7_full_63
_7_full_64	_7_full	64	_7_full_64
_7_full_65	_7_full	65	_7_full_65
_7_full_66	_7_full	66	_7_full_66
_7_full_67	_7_full	67	_7_full_67
_7_full_68	_7_full	68	_7_full_68
_7_full_69	_7_full	69	_7_full_69
_7_full_70	_7_full	70	_7_full_70
_7_full_71	_7_full	71	_7_full_71
_7_full_72	_7_full	72	_7_full_72
_7_full_73	_7_full	73	_7_full_73
_7_full_74	_7_full	74	_7_full_74
_7_full_75	_7_full	75	_7_full_75
_7_full_76	_7_full	76	_7_full_76
_7_full_77	_7_full	77	_7_full_77
_7_full_78	_7_full	78	_7_full_78
_7_full_79	_7_full	79	_7_full_79
_7_full_80	_7_full	80	_7_full_80
_7_full_81	_7_full	81	_7_full_81
_7_full_82	_7_full	82	_7_full_82
_7_full_83	_7_full	83	_7_full_83
_7_full_84	_7_full	84	_7_full_84
_7_full_85	_7_full	85	_7_full_85
_7_full_86	_7_full	86	_7_full_86
_7_full_87	_7_full	87	_7_full_87
_7_full_88	_7_full	88	_7_full_88
_7_full_89	_7_full	89	_7_full_89
_7_full_90	_7_full	90	_7_full_90
_7_full_91	_7_full	91	_7_full_91
_7_full_92	_7_full	92	_7_full_92
_7_full_93	_7_full	93	_7_full_93
_7_full_94	_7_full	94	_7_full_94
_7_full_95	_7_full	95	_7_full_95
\.
