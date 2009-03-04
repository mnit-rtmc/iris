\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

-- Drop old relations
DROP VIEW simple;
DROP VIEW stratified;
DROP VIEW ramp_meter_view;
DROP VIEW dms_view;
DROP VIEW time_plan_log_view;
DROP SEQUENCE tms_log_seq;
DROP FUNCTION time_plan_log() CASCADE;
DROP TABLE time_plan_log;
DROP TABLE traffic_device_attribute;

-- Add f_number column to font table
CREATE TEMP SEQUENCE temp_font_number_seq;
ALTER TABLE font ADD COLUMN f_number INTEGER;
UPDATE font SET f_number = nextval('temp_font_number_seq');
ALTER TABLE font ALTER COLUMN f_number SET NOT NULL;
ALTER TABLE font ADD CONSTRAINT font_number_key UNIQUE (f_number);

-- Add new ramp meter stuff
CREATE TABLE iris.meter_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL,
	lanes INTEGER NOT NULL
);

COPY iris.meter_type (id, description, lanes) FROM stdin;
0	One Lane	1
1	Two Lane, Alternate Release	2
2	Two Lane, Simultaneous Release	2
\.

CREATE TABLE iris.meter_lock (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

COPY iris.meter_lock (id, description) FROM stdin;
1	Knocked down
2	Incident
3	Testing
4	Police panel
5	Manual mode
6	Other reason
\.

CREATE TABLE iris._ramp_meter (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES geo_loc(name),
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

ALTER TABLE ramp_meter ALTER COLUMN camera DROP NOT NULL;
UPDATE ramp_meter SET camera = null WHERE camera = '';
UPDATE ramp_meter SET controller = null WHERE controller = '';

CREATE FUNCTION temp_meter_type(boolean) RETURNS INTEGER AS
'	DECLARE
		single ALIAS FOR $1;
	BEGIN
		IF single = ''t'' THEN
			RETURN 0;
		END IF;
		RETURN 1;
	END;'
LANGUAGE plpgsql;

INSERT INTO iris.ramp_meter
	SELECT id, geo_loc, controller, pin, notes,
		temp_meter_type("singleRelease"), storage, "maxWait", camera,
		null
	FROM ramp_meter;

DROP FUNCTION temp_meter_type(boolean);
DROP TABLE ramp_meter;

CREATE VIEW ramp_meter_view AS
	SELECT m.name, geo_loc, controller, pin, notes,
	mt.description AS meter_type, storage, max_wait, camera,
	ml.description AS meter_lock,
	l.fwy, l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM iris.ramp_meter m
	LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
	LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

-- Add new DMS stuff
CREATE TABLE iris._dms (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES geo_loc,
	notes text NOT NULL,
	travel text NOT NULL,
	camera VARCHAR(10) REFERENCES iris._camera,
	aws_allowed BOOLEAN NOT NULL,
	aws_controlled BOOLEAN NOT NULL
);

ALTER TABLE iris._dms ADD CONSTRAINT _dms_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.dms AS SELECT
	d.name, geo_loc, controller, pin, notes, travel, camera, aws_allowed,
	aws_controlled
	FROM iris._dms dms JOIN iris._device_io d ON dms.name = d.name;

CREATE RULE dms_insert AS ON INSERT TO iris.dms DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._dms VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.travel, NEW.camera, NEW.aws_allowed, NEW.aws_controlled);
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
		travel = NEW.travel,
		camera = NEW.camera,
		aws_allowed = NEW.aws_allowed,
		aws_controlled = NEW.aws_controlled
	WHERE name = OLD.name;
);

CREATE RULE dms_delete AS ON DELETE TO iris.dms DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

ALTER TABLE dms ALTER COLUMN camera DROP NOT NULL;
UPDATE dms SET camera = null WHERE camera = '';
UPDATE dms SET controller = null WHERE controller = '';
INSERT INTO iris.dms
	SELECT id, geo_loc, controller, pin, notes,
		travel, camera, false, false
	FROM dms;

ALTER TABLE dms_sign_group DROP CONSTRAINT dms_sign_group_dms_fkey;
DROP TABLE dms;
ALTER TABLE dms_sign_group ADD FOREIGN KEY (dms) REFERENCES iris._dms;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.travel,
	d.camera, d.aws_allowed, d.aws_controlled,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM iris.dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE TABLE iris.sign_message (
	name VARCHAR(20) PRIMARY KEY,
	multi VARCHAR(256) NOT NULL,
	bitmaps text NOT NULL,
	priority INTEGER NOT NULL,
	duration INTEGER
);

-- Add new timing plan stuff
CREATE TABLE iris.timing_plan_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

COPY iris.timing_plan_type (id, description) FROM stdin;
0	Travel Time
1	Simple Metering
2	Stratified Metering
\.

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

CREATE TEMP SEQUENCE temp_plan_number_seq;
INSERT INTO iris.timing_plan
	SELECT traffic_device || '_' || nextval('temp_plan_number_seq'), 0,
		traffic_device, "startTime", "stopTime", active, false, 0
	FROM traffic_device_timing_plan
	JOIN iris.dms d ON traffic_device = d.name
	JOIN timing_plan p ON timing_plan = p.vault_oid;
DROP SEQUENCE temp_plan_number_seq;
CREATE TEMP SEQUENCE temp_plan_number_seq;
INSERT INTO iris.timing_plan
	SELECT traffic_device || '_' || nextval('temp_plan_number_seq'), 1,
		traffic_device, "startTime", "stopTime", active, false, target
	FROM traffic_device_timing_plan
	JOIN iris.ramp_meter m ON traffic_device = m.name
	JOIN simple_plan p ON timing_plan = p.vault_oid;
INSERT INTO iris.timing_plan
	SELECT traffic_device || '_' || nextval('temp_plan_number_seq'), 2,
		traffic_device, "startTime", "stopTime", active, false, 0
	FROM traffic_device_timing_plan
	JOIN iris.ramp_meter m ON traffic_device = m.name
	JOIN stratified_plan p ON timing_plan = p.vault_oid;
DROP SEQUENCE temp_plan_number_seq;

UPDATE iris.timing_plan stp SET target = tp.target
	FROM iris.timing_plan tp
	WHERE stp.device = tp.device
		AND stp.plan_type = 2
		AND tp.plan_type = 1
		AND stp.start_min = tp.start_min
		AND stp.stop_min = tp.stop_min;

CREATE VIEW timing_plan_view AS
	SELECT name, pt.description AS plan_type, device,
	hour_min(start_min) AS start_time, hour_min(stop_min) AS stop_time,
	active, testing, target
	FROM iris.timing_plan
	LEFT JOIN iris.timing_plan_type pt ON plan_type = pt.id;
GRANT SELECT ON timing_plan_view TO PUBLIC;

DROP TABLE traffic_device_timing_plan;
DROP TABLE stratified_plan;
DROP TABLE simple_plan;
DROP TABLE meter_plan;
DROP TABLE timing_plan;

-- Add new system attributes
INSERT INTO system_attribute VALUES('dms_max_lines', '3');
INSERT INTO system_attribute VALUES('dms_default_justification_line', '3');
INSERT INTO system_attribute VALUES('dms_default_justification_page', '2');
INSERT INTO system_attribute VALUES('dms_message_min_pages', '1');
INSERT INTO system_attribute VALUES('dms_message_blank_line_enable', 'true');
INSERT INTO system_attribute VALUES('dms_status_enable', 'false');
INSERT INTO system_attribute VALUES('dms_aws_enable', 'false');
INSERT INTO system_attribute VALUES('dms_duration_enable', 'true');
INSERT INTO system_attribute VALUES('dms_font_selection_enable', 'false');
INSERT INTO system_attribute VALUES('dms_pixel_status_enable', 'true');
INSERT INTO system_attribute VALUES('dms_brightness_enable', 'true');
INSERT INTO system_attribute VALUES('dms_manufacturer_enable', 'true');
INSERT INTO system_attribute VALUES('dms_reest_enable', 'true');
INSERT INTO system_attribute VALUES('meter_max_red_secs', '13.0');
INSERT INTO system_attribute VALUES('travel_time_min_mph', '15');
INSERT INTO system_attribute VALUES('travel_time_max_legs', '8');
INSERT INTO system_attribute VALUES('travel_time_max_miles', '16');
INSERT INTO system_attribute VALUES('camera_ptz_panel_enable', 'false');

-- Remove old system attributes
DELETE FROM system_attribute WHERE name = 'dms_preferred_font';
DELETE FROM system_attribute WHERE name = 'dmsdispatcher_getstatus_btn';
DELETE FROM system_attribute WHERE name = 'dmsdispatcher_aws_ckbox';
DELETE FROM system_attribute WHERE name = 'cameraviewer_onscrn_ptzctrls';


UPDATE system_attribute SET value = '3.81.0' WHERE name = 'database_version';

