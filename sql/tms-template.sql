--
-- PostgreSQL database template for IRIS
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
	dn VARCHAR(64) NOT NULL,
	role VARCHAR(15) REFERENCES iris.role,
	enabled BOOLEAN NOT NULL
);

CREATE TABLE iris.capability (
	name VARCHAR(16) PRIMARY KEY,
	enabled BOOLEAN NOT NULL
);

CREATE TABLE iris.privilege (
	name VARCHAR(8) PRIMARY KEY,
	capability VARCHAR(16) NOT NULL REFERENCES iris.capability,
	pattern VARCHAR(31) DEFAULT ''::VARCHAR NOT NULL,
	priv_r boolean DEFAULT false NOT NULL,
	priv_w boolean DEFAULT false NOT NULL,
	priv_c boolean DEFAULT false NOT NULL,
	priv_d boolean DEFAULT false NOT NULL
);

CREATE TABLE iris.role_capability (
	role VARCHAR(15) NOT NULL REFERENCES iris.role,
	capability VARCHAR(16) NOT NULL REFERENCES iris.capability
);

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

CREATE FUNCTION graphic_bpp(VARCHAR(20)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		b INTEGER;
	BEGIN SELECT INTO b bpp FROM iris.graphic WHERE name = n;
		RETURN b;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_height(VARCHAR(20)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM iris.graphic WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION graphic_width(VARCHAR(20)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM iris.graphic WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION glyph_font(VARCHAR(20)) RETURNS VARCHAR(16) AS '
	DECLARE n ALIAS FOR $1;
		f VARCHAR(16);
	BEGIN SELECT INTO f font FROM iris.glyph WHERE graphic = n;
		RETURN f;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_height(VARCHAR(16)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		h INTEGER;
	BEGIN SELECT INTO h height FROM iris.font WHERE name = n;
		RETURN h;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_width(VARCHAR(16)) RETURNS INTEGER AS '
	DECLARE n ALIAS FOR $1;
		w INTEGER;
	BEGIN SELECT INTO w width FROM iris.font WHERE name = n;
		RETURN w;
	END;'
LANGUAGE PLPGSQL;

CREATE FUNCTION font_graphic(VARCHAR(16)) RETURNS VARCHAR(20) AS '
	DECLARE n ALIAS FOR $1;
		g TEXT;
	BEGIN SELECT INTO g graphic FROM iris.glyph WHERE font = n;
		RETURN g;
	END;'
LANGUAGE PLPGSQL;

ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_bpp_ck
	CHECK (bpp = 1 OR bpp = 8 OR bpp = 24);
ALTER TABLE iris.graphic
	ADD CONSTRAINT graphic_font_ck
	CHECK (glyph_font(name) IS NULL OR
		(font_height(glyph_font(name)) = height AND
		(font_width(glyph_font(name)) = 0 OR
		font_width(glyph_font(name)) = width)));

ALTER TABLE iris.glyph
	ADD CONSTRAINT glyph_bpp_ck
	CHECK (graphic_bpp(graphic) = 1);
ALTER TABLE iris.glyph
	ADD CONSTRAINT glyph_size_ck
	CHECK (font_height(font) = graphic_height(graphic) AND
		(font_width(font) = 0 OR
		font_width(font) = graphic_width(graphic)));

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
ALTER TABLE iris.font
	ADD CONSTRAINT font_graphic_ck
	CHECK (font_graphic(name) IS NULL OR
		(graphic_height(font_graphic(name)) = height AND
		(width = 0 OR graphic_width(font_graphic(name)) = width)));

CREATE TABLE iris.video_monitor (
	name VARCHAR(12) PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	restricted boolean NOT NULL
);

CREATE TABLE iris.holiday (
	name VARCHAR(32) PRIMARY KEY,
	month INTEGER NOT NULL,
	day INTEGER NOT NULL,
	week INTEGER NOT NULL,
	weekday INTEGER NOT NULL,
	shift INTEGER NOT NULL,
	period INTEGER NOT NULL
);

CREATE TABLE iris.day_plan (
	name VARCHAR(10) PRIMARY KEY
);

CREATE TABLE iris.day_plan_holiday (
	day_plan VARCHAR(10) NOT NULL REFERENCES iris.day_plan,
	holiday VARCHAR(32) NOT NULL REFERENCES iris.holiday
);

CREATE TABLE iris.geo_loc (
	name VARCHAR(20) PRIMARY KEY,
	roadway VARCHAR(20) REFERENCES iris.road(name),
	road_dir smallint REFERENCES iris.direction(id),
	cross_street VARCHAR(20) REFERENCES iris.road(name),
	cross_dir smallint REFERENCES iris.direction(id),
	cross_mod smallint REFERENCES iris.road_modifier(id),
	easting integer,
	northing integer
);

CREATE TABLE iris.map_extent (
	name VARCHAR(20) PRIMARY KEY,
	lon real NOT NULL,
	lat real NOT NULL,
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
	station_id VARCHAR(10),
	speed_limit integer NOT NULL,
	notes text NOT NULL
);

CREATE UNIQUE INDEX r_node_station_idx ON iris.r_node USING btree (station_id);

CREATE TABLE iris.comm_protocol (
	id smallint PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

CREATE TABLE iris.comm_link (
	name VARCHAR(20) PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	url VARCHAR(64) NOT NULL,
	protocol smallint NOT NULL REFERENCES iris.comm_protocol(id),
	timeout integer NOT NULL
);

CREATE TABLE iris.cabinet_style (
	name VARCHAR(20) PRIMARY KEY,
	dip integer
);

CREATE TABLE iris.cabinet (
	name VARCHAR(20) PRIMARY KEY,
	style VARCHAR(20) REFERENCES iris.cabinet_style(name),
	geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
	mile real
);

CREATE TABLE iris.controller (
	name VARCHAR(20) PRIMARY KEY,
	drop_id smallint NOT NULL,
	comm_link VARCHAR(20) NOT NULL REFERENCES iris.comm_link(name),
	cabinet VARCHAR(20) NOT NULL REFERENCES iris.cabinet(name),
	active boolean NOT NULL,
	password VARCHAR(16),
	notes VARCHAR(128) NOT NULL
);

CREATE UNIQUE INDEX ctrl_link_drop_idx ON iris.controller
	USING btree (comm_link, drop_id);

CREATE TABLE iris._device_io (
	name VARCHAR(10) PRIMARY KEY,
	controller VARCHAR(20) REFERENCES iris.controller(name),
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

CREATE TABLE iris._detector (
	name VARCHAR(10) PRIMARY KEY,
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

CREATE TABLE iris.encoder_type (
	id integer PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

CREATE TABLE iris._camera (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes text NOT NULL,
	encoder text NOT NULL,
	encoder_channel integer NOT NULL,
	encoder_type integer NOT NULL REFERENCES iris.encoder_type(id),
	publish boolean NOT NULL
);

ALTER TABLE iris._camera ADD CONSTRAINT _camera_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, encoder, encoder_channel,
		encoder_type, publish
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE RULE camera_insert AS ON INSERT TO iris.camera DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.encoder, NEW.encoder_channel, NEW.encoder_type,NEW.publish);
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
		encoder_type = NEW.encoder_type,
		publish = NEW.publish
	WHERE name = OLD.name;
);

CREATE RULE camera_delete AS ON DELETE TO iris.camera DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris._warning_sign (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
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

CREATE TABLE iris.meter_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	lanes INTEGER NOT NULL
);

CREATE TABLE iris.meter_lock (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE iris._ramp_meter (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes text NOT NULL,
	meter_type INTEGER NOT NULL REFERENCES iris.meter_type(id),
	storage INTEGER NOT NULL,
	max_wait INTEGER NOT NULL,
	camera VARCHAR(10) REFERENCES iris._camera(name),
	m_lock INTEGER REFERENCES iris.meter_lock(id)
);

ALTER TABLE iris._ramp_meter ADD CONSTRAINT _ramp_meter_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.ramp_meter AS SELECT
	m.name, geo_loc, controller, pin, notes, meter_type, storage,
	max_wait, camera, m_lock
	FROM iris._ramp_meter m JOIN iris._device_io d ON m.name = d.name;

CREATE RULE ramp_meter_insert AS ON INSERT TO iris.ramp_meter DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._ramp_meter VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.meter_type, NEW.storage, NEW.max_wait, NEW.camera,
		NEW.m_lock);
);

CREATE RULE ramp_meter_update AS ON UPDATE TO iris.ramp_meter DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._ramp_meter SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes,
		meter_type = NEW.meter_type,
		storage = NEW.storage,
		max_wait = NEW.max_wait,
		camera = NEW.camera,
		m_lock = NEW.m_lock
	WHERE name = OLD.name;
);

CREATE RULE ramp_meter_delete AS ON DELETE TO iris.ramp_meter DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris._dms (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc,
	notes text NOT NULL,
	camera VARCHAR(10) REFERENCES iris._camera,
	aws_allowed BOOLEAN NOT NULL,
	aws_controlled BOOLEAN NOT NULL,
	default_font VARCHAR(16) REFERENCES iris.font
);

ALTER TABLE iris._dms ADD CONSTRAINT _dms_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.dms AS SELECT
	d.name, geo_loc, controller, pin, notes, camera, aws_allowed,
	aws_controlled, default_font
	FROM iris._dms dms JOIN iris._device_io d ON dms.name = d.name;

CREATE RULE dms_insert AS ON INSERT TO iris.dms DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._dms VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.camera, NEW.aws_allowed, NEW.aws_controlled,
		NEW.default_font);
);

CREATE RULE dms_update AS ON UPDATE TO iris.dms DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._dms SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes,
		camera = NEW.camera,
		aws_allowed = NEW.aws_allowed,
		aws_controlled = NEW.aws_controlled,
		default_font = NEW.default_font
	WHERE name = OLD.name;
);

CREATE RULE dms_delete AS ON DELETE TO iris.dms DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris._lane_marking (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes VARCHAR(64) NOT NULL
);

ALTER TABLE iris._lane_marking ADD CONSTRAINT _lane_marking_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lane_marking AS SELECT
	m.name, geo_loc, controller, pin, notes
	FROM iris._lane_marking m JOIN iris._device_io d ON m.name = d.name;

CREATE RULE lane_marking_insert AS ON INSERT TO iris.lane_marking DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lane_marking VALUES (NEW.name, NEW.geo_loc,NEW.notes);
);

CREATE RULE lane_marking_update AS ON UPDATE TO iris.lane_marking DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._lane_marking SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes
	WHERE name = OLD.name;
);

CREATE RULE lane_marking_delete AS ON DELETE TO iris.lane_marking DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris._weather_sensor (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes VARCHAR(64) NOT NULL
);

ALTER TABLE iris._weather_sensor ADD CONSTRAINT _weather_sensor_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.weather_sensor AS SELECT
	m.name, geo_loc, controller, pin, notes
	FROM iris._weather_sensor m JOIN iris._device_io d ON m.name = d.name;

CREATE RULE weather_sensor_insert AS ON INSERT TO iris.weather_sensor DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._weather_sensor VALUES (NEW.name, NEW.geo_loc,
		NEW.notes);
);

CREATE RULE weather_sensor_update AS ON UPDATE TO iris.weather_sensor DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._weather_sensor SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes
	WHERE name = OLD.name;
);

CREATE RULE weather_sensor_delete AS ON DELETE TO iris.weather_sensor DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris.lcs_lock (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE iris._lcs_array (
	name VARCHAR(10) PRIMARY KEY,
	notes text NOT NULL,
	shift INTEGER NOT NULL,
	lcs_lock INTEGER REFERENCES iris.lcs_lock(id)
);

ALTER TABLE iris._lcs_array ADD CONSTRAINT _lcs_array_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lcs_array AS SELECT
	d.name, controller, pin, notes, shift, lcs_lock
	FROM iris._lcs_array la JOIN iris._device_io d ON la.name = d.name;

CREATE RULE lcs_array_insert AS ON INSERT TO iris.lcs_array DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_array VALUES (NEW.name, NEW.notes, NEW.shift,
		NEW.lcs_lock);
);

CREATE RULE lcs_array_update AS ON UPDATE TO iris.lcs_array DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._lcs_array SET
		notes = NEW.notes,
		shift = NEW.shift,
		lcs_lock = NEW.lcs_lock
	WHERE name = OLD.name;
);

CREATE RULE lcs_array_delete AS ON DELETE TO iris.lcs_array DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris.lcs (
	name VARCHAR(10) PRIMARY KEY REFERENCES iris._dms,
	lcs_array VARCHAR(10) NOT NULL REFERENCES iris._lcs_array,
	lane INTEGER NOT NULL
);

CREATE UNIQUE INDEX lcs_array_lane ON iris.lcs USING btree (lcs_array, lane);

CREATE TABLE iris.lane_use_indication (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

CREATE TABLE iris._lcs_indication (
	name VARCHAR(10) PRIMARY KEY,
	lcs VARCHAR(10) NOT NULL REFERENCES iris.lcs,
	indication INTEGER NOT NULL REFERENCES iris.lane_use_indication
);

ALTER TABLE iris._lcs_indication ADD CONSTRAINT _lcs_indication_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lcs_indication AS SELECT
	d.name, controller, pin, lcs, indication
	FROM iris._lcs_indication li JOIN iris._device_io d ON li.name = d.name;

CREATE RULE lcs_indication_insert AS ON INSERT TO iris.lcs_indication DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_indication VALUES (NEW.name, NEW.lcs,
		NEW.indication);
);

CREATE RULE lcs_indication_update AS ON UPDATE TO iris.lcs_indication DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._lcs_indication SET
		lcs = NEW.lcs,
		indication = NEW.indication
	WHERE name = OLD.name;
);

CREATE RULE lcs_indication_delete AS ON DELETE TO iris.lcs_indication DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris.sign_group (
	name VARCHAR(16) PRIMARY KEY,
	local BOOLEAN NOT NULL
);

CREATE TABLE iris.dms_sign_group (
	name VARCHAR(24) PRIMARY KEY,
	dms VARCHAR(10) NOT NULL REFERENCES iris._dms,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group
);

CREATE TABLE iris.sign_text (
	name VARCHAR(20) PRIMARY KEY,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	line smallint NOT NULL,
	multi VARCHAR(64) NOT NULL,
	priority smallint NOT NULL,
	CONSTRAINT sign_text_line CHECK ((line >= 1) AND (line <= 12)),
	CONSTRAINT sign_text_priority CHECK
		((priority >= 1) AND (priority <= 99))
);

CREATE TABLE iris.sign_message (
	name VARCHAR(20) PRIMARY KEY,
	multi VARCHAR(256) NOT NULL,
	bitmaps text NOT NULL,
	a_priority INTEGER NOT NULL,
	r_priority INTEGER NOT NULL,
	scheduled BOOLEAN NOT NULL,
	duration INTEGER
);

CREATE TABLE iris.quick_message (
	name VARCHAR(20) PRIMARY KEY,
	sign_group VARCHAR(16) REFERENCES iris.sign_group,
	multi VARCHAR(256) NOT NULL
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

CREATE TABLE iris.plan_state (
	id INTEGER PRIMARY KEY,
	description VARCHAR(12) NOT NULL
);

CREATE TABLE iris.action_plan (
	name VARCHAR(16) PRIMARY KEY,
	description VARCHAR(64) NOT NULL,
	sync_actions BOOLEAN NOT NULL,
	deploying_secs INTEGER NOT NULL,
	undeploying_secs INTEGER NOT NULL,
	active BOOLEAN NOT NULL,
	state INTEGER NOT NULL REFERENCES iris.plan_state
);

CREATE TABLE iris.time_action (
	name VARCHAR(20) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	day_plan VARCHAR(10) NOT NULL REFERENCES iris.day_plan,
	minute SMALLINT NOT NULL,
	deploy BOOLEAN NOT NULL
);

CREATE TABLE iris.dms_action (
	name VARCHAR(20) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	sign_group VARCHAR(16) NOT NULL REFERENCES iris.sign_group,
	state INTEGER NOT NULL REFERENCES iris.plan_state,
	quick_message VARCHAR(20) REFERENCES iris.quick_message,
	a_priority INTEGER NOT NULL,
	r_priority INTEGER NOT NULL
);

CREATE TABLE iris.lane_action (
	name VARCHAR(20) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	lane_marking VARCHAR(10) NOT NULL REFERENCES iris._lane_marking,
	state INTEGER NOT NULL REFERENCES iris.plan_state
);

CREATE TABLE iris.timing_plan_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

CREATE TABLE iris.timing_plan (
	name VARCHAR(16) PRIMARY KEY,
	plan_type INTEGER NOT NULL REFERENCES iris.timing_plan_type,
	device VARCHAR(10) NOT NULL REFERENCES iris._device_io,
	start_min INTEGER NOT NULL,
	stop_min INTEGER NOT NULL,
	active BOOLEAN NOT NULL,
	testing BOOLEAN NOT NULL,
	target INTEGER NOT NULL
);

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

CREATE VIEW timing_plan_view AS
	SELECT name, pt.description AS plan_type, device,
	hour_min(start_min) AS start_time, hour_min(stop_min) AS stop_time,
	active, testing, target
	FROM iris.timing_plan
	LEFT JOIN iris.timing_plan_type pt ON plan_type = pt.id;
GRANT SELECT ON timing_plan_view TO PUBLIC;

CREATE VIEW road_view AS
	SELECT name, abbrev, rcl.description AS r_class, dir.direction,
	adir.direction AS alt_dir
	FROM iris.road r
	LEFT JOIN iris.road_class rcl ON r.r_class = rcl.id
	LEFT JOIN iris.direction dir ON r.direction = dir.id
	LEFT JOIN iris.direction adir ON r.alt_dir = adir.id;
GRANT SELECT ON road_view TO PUBLIC;

CREATE VIEW geo_loc_view AS
	SELECT l.name, r.abbrev AS rd, l.roadway,
	r_dir.direction AS road_dir, r_dir.dir AS rdir,
	m.modifier AS cross_mod, m.mod AS xmod, c.abbrev as xst,
	l.cross_street, c_dir.direction AS cross_dir,
	l.easting, l.northing
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
	n.station_id, n.speed_limit, n.notes
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

CREATE VIEW controller_loc_view AS
	SELECT c.name, c.drop_id, c.comm_link, c.cabinet, c.active, c.notes,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.camera,
	d.aws_allowed, d.aws_controlled, d.default_font,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing
	FROM iris.dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;

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
	mt.description AS meter_type, storage, max_wait, camera,
	ml.description AS meter_lock,
	l.rd, l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing
	FROM iris.ramp_meter m
	LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
	LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW encoder_type_view AS
	SELECT id, description FROM iris.encoder_type;
GRANT SELECT ON encoder_type_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, c.encoder, c.encoder_channel,
	et.description AS encoder_type, c.publish, c.geo_loc, l.roadway,
	l.road_dir, l.cross_mod, l.cross_street,
	l.cross_dir, l.easting, l.northing,
	c.controller, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.camera c
	JOIN iris.encoder_type et ON c.encoder_type = et.id
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW warning_sign_view AS
	SELECT w.name, w.notes, w.message, w.camera, w.geo_loc,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing,
	w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.warning_sign w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON w.controller = ctr.name;
GRANT SELECT ON warning_sign_view TO PUBLIC;

CREATE VIEW lane_marking_view AS
	SELECT m.name, m.notes, m.geo_loc,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing,
	m.controller, m.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.lane_marking m
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON m.controller = ctr.name;
GRANT SELECT ON lane_marking_view TO PUBLIC;

CREATE VIEW weather_sensor_view AS
	SELECT w.name, w.notes, w.geo_loc,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing,
	w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.weather_sensor w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON w.controller = ctr.name;
GRANT SELECT ON weather_sensor_view TO PUBLIC;

CREATE VIEW lane_type_view AS
	SELECT id, description, dcode FROM iris.lane_type;
GRANT SELECT ON lane_type_view TO PUBLIC;

CREATE FUNCTION detector_label(text, varchar, text, varchar, text, smallint,
	smallint, boolean) RETURNS text AS
'	DECLARE
		rd ALIAS FOR $1;
		rdir ALIAS FOR $2;
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
		IF rd IS NULL OR xst IS NULL THEN
			RETURN ''FUTURE'';
		END IF;
		SELECT INTO ltyp dcode FROM lane_type_view WHERE id = l_type;
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
		RETURN rd || ''/'' || cross_dir || xmd || xst || rdir ||
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
	detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name AS det_id, d.r_node, d.controller, c.comm_link, c.drop_id,
	d.pin, detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label,
	rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	l.cross_dir, d.lane_number, d.field_length, ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d.force_fail) AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN iris.controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.controller, a.pin, c.comm_link,
		c.drop_id
	FROM iris.alarm a LEFT JOIN iris.controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, multi, priority
	FROM iris.dms_sign_group dsg
	JOIN iris.sign_group sg ON dsg.sign_group = sg.name
	JOIN iris.sign_text st ON sg.name = st.sign_group;
GRANT SELECT ON sign_text_view TO PUBLIC;

CREATE VIEW controller_view AS
	SELECT c.name, drop_id, comm_link, cabinet, active, notes, cab.geo_loc
	FROM iris.controller c
	JOIN iris.cabinet cab ON c.cabinet = cab.name;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW iris.controller_dms AS
	SELECT dio.name, dio.controller, dio.pin, d.geo_loc
	FROM iris._device_io dio
	JOIN iris.dms d ON dio.name = d.name;

CREATE VIEW iris.controller_lane_marking AS
	SELECT dio.name, dio.controller, dio.pin, m.geo_loc
	FROM iris._device_io dio
	JOIN iris.lane_marking m ON dio.name = m.name;

CREATE VIEW iris.controller_weather_sensor AS
	SELECT dio.name, dio.controller, dio.pin, m.geo_loc
	FROM iris._device_io dio
	JOIN iris.weather_sensor m ON dio.name = m.name;

CREATE VIEW iris.controller_lcs AS
	SELECT dio.name, dio.controller, dio.pin, d.geo_loc
	FROM iris._device_io dio
	JOIN iris.dms d ON dio.name = d.name;

CREATE VIEW iris.controller_meter AS
	SELECT dio.name, dio.controller, dio.pin, m.geo_loc
	FROM iris._device_io dio
	JOIN iris.ramp_meter m ON dio.name = m.name;

CREATE VIEW iris.controller_warning_sign AS
	SELECT dio.name, dio.controller, dio.pin, s.geo_loc
	FROM iris._device_io dio
	JOIN iris.warning_sign s ON dio.name = s.name;

CREATE VIEW iris.controller_camera AS
	SELECT dio.name, dio.controller, dio.pin, c.geo_loc
	FROM iris._device_io dio
	JOIN iris.camera c ON dio.name = c.name;

CREATE VIEW iris.controller_device AS
	SELECT * FROM iris.controller_dms UNION ALL
	SELECT * FROM iris.controller_lane_marking UNION ALL
	SELECT * FROM iris.controller_weather_sensor UNION ALL
	SELECT * FROM iris.controller_lcs UNION ALL
	SELECT * FROM iris.controller_meter UNION ALL
	SELECT * FROM iris.controller_warning_sign UNION ALL
	SELECT * FROM iris.controller_camera;

CREATE VIEW controller_device_view AS
	SELECT d.name, d.controller, d.pin, d.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) AS corridor,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_loc
	FROM iris.controller_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.mile, cab.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

CREATE VIEW comm_link_view AS
	SELECT cl.name, cl.description, cl.url, cp.description AS protocol,
	cl.timeout
	FROM iris.comm_link cl
	JOIN iris.comm_protocol cp ON cl.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;


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
5	Vicon Switcher
6	Pelco D PTZ
7	NTCIP Class C
8	Manchester PTZ
9	DMS XML
10	MSG_FEED
11	NTCIP Class A
12	Pelco Switcher
13	Vicon PTZ
14	SmartSensor 125 HD
15	OSi ORG-815
\.

COPY iris.cabinet_style (name, dip) FROM stdin;
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

COPY iris.meter_lock (id, description) FROM stdin;
1	Knocked down
2	Incident
3	Testing
4	Police panel
5	Manual mode
6	Other reason
\.

COPY iris.encoder_type (id, description) FROM stdin;
0	
1	Axis MJPEG
2	Axis MPEG4
3	Infinova MPEG4
\.

COPY iris.plan_state (id, description) FROM stdin;
0	undeployed
1	deploying
2	deployed
3	undeploying
\.

COPY iris.timing_plan_type (id, description) FROM stdin;
0	Travel Time
1	Simple Metering
2	Stratified Metering
\.

COPY iris.system_attribute (name, value) FROM stdin;
camera_id_blank	
camera_num_preset_btns	3
camera_ptz_panel_enable	false
camera_stream_duration_secs	60
database_version	3.131.0
detector_auto_fail_enable	true
dms_aws_enable	false
dms_aws_retry_threshold	6
dms_brightness_enable	true
dms_comm_loss_minutes	5
dms_composer_edit_mode	1
dms_default_justification_line	3
dms_default_justification_page	2
dms_duration_enable	true
dms_font_selection_enable	false
dms_form	1
dms_high_temp_cutoff	60
dms_lamp_test_timeout_secs	30
dms_manufacturer_enable	true
dms_max_lines	3
dms_message_min_pages	1
dms_op_status_enable	false
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
dms_poll_period_secs	30
dms_querymsg_enable	false
dms_reset_enable	false
dms_send_confirmation_enable	false
dmslite_modem_op_timeout_secs	305
dmslite_op_timeout_secs	65
email_sender_client	
email_sender_server	
email_smtp_host	
email_recipient_aws	
email_recipient_bugs	
help_trouble_ticket_enable	false
help_trouble_ticket_url	
incident_clear_secs	600
kml_file_enable	false
kml_filename	/var/www/html/iris-client/iris.kmz
map_icon_size_scale_max	30
map_northern_hemisphere	true
map_utm_zone	15
map_segment_max_meters	2000
meter_green_secs	1.3
meter_max_red_secs	13.0
meter_min_red_secs	0.1
meter_yellow_secs	0.7
operation_retry_threshold	3
sample_archive_enable	true
sample_archive_directory	/var/lib/iris/traffic
station_xml_enable	true
temp_fahrenheit_enable	true
tesla_host	
travel_time_max_legs	8
travel_time_max_miles	16
travel_time_min_mph	15
uptime_log_enable	false
uptime_log_filename	/var/www/html/irisuptimelog.csv
vsa_bottleneck_id_mph	55
vsa_control_threshold	-1000
vsa_downstream_miles	0.2
vsa_min_display_mph	30
vsa_min_station_miles	0.1
vsa_start_intervals	3
vsa_start_threshold	-1500
vsa_stop_threshold	-750
window_title	IRIS:
xml_output_directory	/var/www/html/iris_xml/
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

COPY iris.capability (name, enabled) FROM stdin;
login	t
camera_tab	t
camera_control	t
dms_tab	t
dms_control	t
incident_tab	t
incident_control	t
lcs_tab	t
lcs_control	t
meter_tab	t
meter_control	t
detection	t
det_control	t
plan_control	t
maintenance	t
publish	t
policy_admin	t
device_admin	t
system_admin	t
user_admin	t
\.

COPY iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c, priv_d) FROM stdin;
PRV_0001	login	user(/.*)?	t	f	f	f
PRV_0002	login	role(/.*)?	t	f	f	f
PRV_0003	login	capability(/.*)?	t	f	f	f
PRV_0004	login	privilege(/.*)?	t	f	f	f
PRV_0005	login	connection(/.*)?	t	f	f	f
PRV_0006	login	system_attribute(/.*)?	t	f	f	f
PRV_0007	login	map_extent(/.*)?	t	f	f	f
PRV_0008	login	road(/.*)?	t	f	f	f
PRV_0009	login	geo_loc(/.*)?	t	f	f	f
PRV_0010	login	incident_detail(/.*)?	t	f	f	f
PRV_0011	camera_tab	camera(/.*)?	t	f	f	f
PRV_0012	camera_tab	controller(/.*)?	t	f	f	f
PRV_0013	camera_tab	video_monitor(/.*)?	t	f	f	f
PRV_0014	incident_tab	incident(/.*)?	t	f	f	f
PRV_0015	dms_tab	cabinet(/.*)?	t	f	f	f
PRV_0016	dms_tab	controller(/.*)?	t	f	f	f
PRV_0017	dms_tab	dms(/.*)?	t	f	f	f
PRV_0018	dms_tab	dms_sign_group(/.*)?	t	f	f	f
PRV_0019	dms_tab	font(/.*)?	t	f	f	f
PRV_0020	dms_tab	glyph(/.*)?	t	f	f	f
PRV_0021	dms_tab	graphic(/.*)?	t	f	f	f
PRV_0022	dms_tab	quick_message(/.*)?	t	f	f	f
PRV_0023	dms_tab	sign_group(/.*)?	t	f	f	f
PRV_0024	dms_tab	sign_message(/.*)?	t	f	f	f
PRV_0025	dms_tab	sign_text(/.*)?	t	f	f	f
PRV_0026	dms_tab	warning_sign(/.*)?	t	f	f	f
PRV_0027	lcs_tab	cabinet(/.*)?	t	f	f	f
PRV_0028	lcs_tab	controller(/.*)?	t	f	f	f
PRV_0029	lcs_tab	dms(/.*)?	t	f	f	f
PRV_0030	lcs_tab	lane_use_multi(/.*)?	t	f	f	f
PRV_0031	lcs_tab	lcs(/.*)?	t	f	f	f
PRV_0032	lcs_tab	lcs_array(/.*)?	t	f	f	f
PRV_0033	lcs_tab	lcs_indication(/.*)?	t	f	f	f
PRV_0034	lcs_tab	quick_message(/.*)?	t	f	f	f
PRV_0035	meter_tab	ramp_meter(/.*)?	t	f	f	f
PRV_0036	meter_tab	timing_plan(/.*)?	t	f	f	f
PRV_0037	maintenance	alarm(/.*)?	t	f	f	f
PRV_0038	maintenance	cabinet(/.*)?	t	f	f	f
PRV_0039	maintenance	cabinet_style(/.*)?	t	f	f	f
PRV_0040	maintenance	comm_link(/.*)?	t	f	f	f
PRV_0041	maintenance	controller(/.*)?	t	f	f	f
PRV_0042	maintenance	controller/.*/active	f	t	f	f
PRV_0043	maintenance	controller/.*/download	f	t	f	f
PRV_0044	maintenance	controller/.*/counters	f	t	f	f
PRV_0045	maintenance	dms/.*/deviceRequest	f	t	f	f
PRV_0046	maintenance	lcs_array/.*/deviceRequest	f	t	f	f
PRV_0047	maintenance	ramp_meter/.*/deviceRequest	f	t	f	f
PRV_0048	camera_control	camera/.*/ptz	f	t	f	f
PRV_0049	camera_control	camera/.*/recallPreset	f	t	f	f
PRV_0050	publish	camera/.*/publish	f	t	f	f
PRV_0051	dms_control	dms/.*/messageNext	f	t	f	f
PRV_0052	dms_control	dms/.*/ownerNext	f	t	f	f
PRV_0053	dms_control	sign_message/.*	f	t	t	f
PRV_0054	dms_control	warning_sign/.*/deployed	f	t	f	f
PRV_0055	incident_control	incident/.*	f	t	t	t
PRV_0056	lcs_control	lcs_array/.*/indicationsNext	f	t	f	f
PRV_0057	lcs_control	lcs_array/.*/ownerNext	f	t	f	f
PRV_0058	lcs_control	lcs_array/.*/lcsLock	f	t	f	f
PRV_0059	meter_control	ramp_meter/.*/mLock	f	t	f	f
PRV_0060	meter_control	ramp_meter/.*/rateNext	f	t	f	f
PRV_0061	detection	detector(/.*)?	t	f	f	f
PRV_0062	detection	r_node(/.*)?	t	f	f	f
PRV_0063	detection	station(/.*)?	t	f	f	f
PRV_0064	plan_control	action_plan(/.*)?	t	f	f	f
PRV_0065	plan_control	action_plan/.*/deployed	f	t	f	f
PRV_0066	det_control	detector/.*/fieldLength	f	t	f	f
PRV_0067	det_control	detector/.*/forceFail	f	t	f	f
PRV_0068	policy_admin	action_plan(/.*)?	t	f	f	f
PRV_0069	policy_admin	action_plan/.*	f	t	t	t
PRV_0070	policy_admin	day_plan(/.*)?	t	f	f	f
PRV_0071	policy_admin	day_plan/.*	f	t	t	t
PRV_0072	policy_admin	dms_action(/.*)?	t	f	f	f
PRV_0073	policy_admin	dms_action/.*	f	t	t	t
PRV_0074	policy_admin	dms_sign_group/.*	f	t	t	t
PRV_0075	policy_admin	holiday(/.*)?	t	f	f	f
PRV_0076	policy_admin	holiday/.*	f	t	t	t
PRV_0077	policy_admin	incident_detail/.*	f	t	t	t
PRV_0078	policy_admin	lane_action(/.*)?	t	f	f	f
PRV_0079	policy_admin	lane_action/.*	f	t	t	t
PRV_0080	policy_admin	map_extent/.*	f	t	t	t
PRV_0081	policy_admin	quick_message/.*	f	t	t	t
PRV_0082	policy_admin	sign_group/.*	f	t	t	t
PRV_0083	policy_admin	sign_text/.*	f	t	t	t
PRV_0084	policy_admin	time_action(/.*)?	t	f	f	f
PRV_0085	policy_admin	time_action/.*	f	t	t	t
PRV_0086	policy_admin	timing_plan/.*	f	t	t	t
PRV_0087	device_admin	alarm/.*	f	t	t	t
PRV_0088	device_admin	cabinet/.*	f	t	t	t
PRV_0089	device_admin	camera/.*	f	t	t	t
PRV_0090	device_admin	comm_link/.*	f	t	t	t
PRV_0091	device_admin	controller/.*	f	t	t	t
PRV_0092	device_admin	detector/.*	f	t	t	t
PRV_0093	device_admin	dms/.*	f	t	t	t
PRV_0094	device_admin	geo_loc/.*	f	t	t	t
PRV_0095	device_admin	lane_marking(/.*)?	t	f	f	f
PRV_0096	device_admin	lane_marking/.*	f	t	t	t
PRV_0097	device_admin	lcs/.*	f	t	t	t
PRV_0098	device_admin	lcs_array/.*	f	t	t	t
PRV_0099	device_admin	lcs_indication/.*	f	t	t	t
PRV_0100	device_admin	r_node/.*	f	t	t	t
PRV_0101	device_admin	ramp_meter/.*	f	t	t	t
PRV_0102	device_admin	road/.*	f	t	t	t
PRV_0103	device_admin	video_monitor/.*	f	t	t	t
PRV_0104	device_admin	warning_sign/.*	f	t	t	t
PRV_0105	device_admin	weather_sensor(/.*)?	t	f	f	f
PRV_0106	device_admin	weather_sensor/.*	f	t	t	t
PRV_0107	system_admin	cabinet_style/.*	f	t	t	t
PRV_0108	system_admin	font/.*	f	t	t	t
PRV_0109	system_admin	glyph/.*	f	t	t	t
PRV_0110	system_admin	graphic/.*	f	t	t	t
PRV_0111	system_admin	lane_use_multi/.*	f	t	t	t
PRV_0112	system_admin	system_attribute/.*	f	t	t	t
PRV_0113	user_admin	user/.*	f	t	t	t
PRV_0114	user_admin	role/.*	f	t	t	t
PRV_0115	user_admin	privilege/.*	f	t	t	t
PRV_0116	user_admin	capability/.*	f	t	t	t
PRV_0117	user_admin	connection/.*	f	f	f	t
\.

COPY iris.role (name, enabled) FROM stdin;
administrator	t
operator	t
\.

COPY iris.role_capability (role, capability) FROM stdin;
administrator	login
administrator	incident_tab
administrator	camera_tab
administrator	camera_control
administrator	dms_tab
administrator	dms_control
administrator	lcs_tab
administrator	lcs_control
administrator	meter_tab
administrator	meter_control
administrator	detection
administrator	policy_admin
administrator	device_admin
administrator	maintenance
administrator	det_control
administrator	plan_control
administrator	system_admin
administrator	user_admin
operator	login
operator	incident_tab
operator	camera_tab
operator	camera_control
operator	dms_tab
operator	dms_control
operator	lcs_tab
operator	lcs_control
operator	meter_tab
operator	meter_control
operator	detection
\.

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

CREATE TABLE event.brightness_sample (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	dms VARCHAR(10) NOT NULL REFERENCES iris._dms(name)
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
	device_id VARCHAR(10) REFERENCES iris._detector(name)
);

CREATE TABLE event.sign_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(20),
	message text,
	iris_user VARCHAR(15) REFERENCES iris.i_user(name)
);

CREATE TABLE event.incident_detail (
	name VARCHAR(8) PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

CREATE TABLE event.incident (
	event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	name VARCHAR(16) NOT NULL UNIQUE,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	detail VARCHAR(8) REFERENCES event.incident_detail(name),
	lane_type smallint NOT NULL REFERENCES iris.lane_type(id),
	road VARCHAR(20) NOT NULL,
	dir SMALLINT NOT NULL REFERENCES iris.direction(id),
	easting INTEGER NOT NULL,
	northing INTEGER NOT NULL,
	camera VARCHAR(10),
	impact VARCHAR(20) NOT NULL,
	cleared BOOLEAN NOT NULL
);

CREATE TABLE event.incident_update (
	event_id INTEGER PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	incident VARCHAR(16) NOT NULL REFERENCES event.incident(name),
	event_date timestamp WITH time zone NOT NULL,
	impact VARCHAR(20) NOT NULL,
	cleared BOOLEAN NOT NULL
);

CREATE FUNCTION event.incident_update_trig() RETURNS "trigger" AS
'
BEGIN
	INSERT INTO event.incident_update
		(incident, event_date, impact, cleared)
	VALUES (NEW.name, now(), NEW.impact, NEW.cleared);
	RETURN NEW;
END;' LANGUAGE plpgsql;

CREATE TRIGGER incident_update_trigger
	AFTER UPDATE ON event.incident
	FOR EACH ROW EXECUTE PROCEDURE event.incident_update_trig();

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
	LEFT JOIN iris.controller c ON e.controller = c.name;
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
\.

COPY event.incident_detail (name, description) FROM stdin;
animal	Animal on Road
debris	Debris
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
veh_fire	Vehicle Fire
\.
