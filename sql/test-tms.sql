-- Test script for tms database
--
-- run with: psql tms -f test-tms.sql

\set QUIET
\set ON_ERROR_STOP

SET client_encoding = 'UTF8';
SET client_min_messages TO WARNING;

BEGIN;

CREATE SCHEMA IF NOT EXISTS test AUTHORIZATION tms;

SET SESSION AUTHORIZATION 'tms';

SET search_path = test;

CREATE TABLE results (
	test VARCHAR(64),
	result VARCHAR(8)
);

CREATE FUNCTION assert(tst BOOLEAN) RETURNS VARCHAR(8) AS $$
BEGIN
	IF tst IS NULL THEN
		RETURN 'NULL';
	END IF;
	IF tst THEN
		RETURN 'PASS';
	ELSE
		RETURN 'FAIL';
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION assert(tst BOOLEAN, name VARCHAR(64)) RETURNS VOID AS $$
BEGIN
	INSERT INTO results VALUES (name, assert(tst));
END;
$$ LANGUAGE plpgsql;

-- Insert controller stuff
INSERT INTO iris.comm_link (name, description, uri, protocol, poll_enabled,
                            poll_period, timeout)
	VALUES ('LNK_TEST_1', 'Test comm link', 'Test URI', 0, false, 30, 750);
INSERT INTO iris.geo_loc (name) VALUES ('LOC_TEST_1');
INSERT INTO iris.cabinet (name, geo_loc)
	VALUES ('CAB_TEST_1', 'LOC_TEST_1');
INSERT INTO iris.controller (name, drop_id, comm_link, cabinet, active, notes)
	VALUES ('CTL_TEST_1', 1, 'LNK_TEST_1', 'CAB_TEST_1', false,
	        'Test controller');

-- Test alarm view
INSERT INTO iris.alarm (name, description, pin, state)
	VALUES ('A_TEST_1', 'Test alarm', 0, false);

\o /dev/null
SELECT assert ('A_TEST_1' = (SELECT name FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm insert name');
SELECT assert ('Test alarm' = (SELECT description FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm insert description');
SELECT assert ((SELECT controller FROM iris.alarm WHERE name = 'A_TEST_1')
               IS NULL, 'alarm insert controller');
SELECT assert (0 = (SELECT pin FROM iris.alarm WHERE name = 'A_TEST_1'),
               'alarm insert pin');
SELECT assert (false = (SELECT state FROM iris.alarm WHERE name = 'A_TEST_1'),
               'alarm insert state');
\o

UPDATE iris.alarm SET description = 'Altered desc' WHERE name = 'A_TEST_1';
UPDATE iris.alarm SET controller = 'CTL_TEST_1' WHERE name = 'A_TEST_1';
UPDATE iris.alarm SET pin = 2 WHERE name = 'A_TEST_1';
UPDATE iris.alarm SET state = true WHERE name = 'A_TEST_1';

\o /dev/null
SELECT assert ('A_TEST_1' = (SELECT name FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm update name');
SELECT assert ('Altered desc' = (SELECT description FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm update description');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm update controller');
SELECT assert (2 = (SELECT pin FROM iris.alarm WHERE name = 'A_TEST_1'),
               'alarm update pin');
SELECT assert (true = (SELECT state FROM iris.alarm WHERE name = 'A_TEST_1'),
               'alarm update state');
\o

INSERT INTO iris.alarm (name, description, controller, pin, state)
	VALUES ('A_TEST_2', '', 'CTL_TEST_1', 1, true);

\o /dev/null
SELECT assert ('A_TEST_2' = (SELECT name FROM iris.alarm
               WHERE name = 'A_TEST_2'), 'alarm insert name 2');
SELECT assert ('' = (SELECT description FROM iris.alarm
               WHERE name = 'A_TEST_2'), 'alarm insert description 2');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.alarm
               WHERE name = 'A_TEST_2'), 'alarm insert controller 2');
SELECT assert (1 = (SELECT pin FROM iris.alarm WHERE name = 'A_TEST_2'),
               'alarm insert pin 2');
SELECT assert (true = (SELECT state FROM iris.alarm WHERE name = 'A_TEST_2'),
               'alarm insert state 2');
\o

DELETE FROM iris.alarm WHERE name = 'A_TEST_2';
DELETE FROM iris.alarm WHERE name = 'A_TEST_1';

-- Test detector view
INSERT INTO iris.r_node (name, geo_loc, node_type, pickable, above, transition,
                         lanes, attach_side, shift, active, abandoned,
                         station_id, speed_limit, notes)
	VALUES ('NOD_TEST_1', 'LOC_TEST_1', 0, true, false, 6, 4, false, 5,
                true, false, 'S_TEST_1', 55, 'notes');
INSERT INTO iris.r_node (name, geo_loc, node_type, pickable, above, transition,
                         lanes, attach_side, shift, active, abandoned,
                         station_id, speed_limit, notes)
	VALUES ('NOD_TEST_2', 'LOC_TEST_1', 0, true, false, 6, 4, false, 5,
                true, false, 'S_TEST_2', 55, 'notes');
INSERT INTO iris.detector (name, pin, r_node, lane_type, lane_number, abandoned,
                           force_fail, field_length)
	VALUES ('DET_TEST_1', 19, 'NOD_TEST_1', 1, 2, false, true, 37.0);

\o /dev/null
SELECT assert ('DET_TEST_1' = (SELECT name FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det insert name');
SELECT assert ((SELECT controller FROM iris.detector
               WHERE name = 'DET_TEST_1') IS NULL, 'det insert controller');
SELECT assert (19 = (SELECT pin FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det insert pin');
SELECT assert ('NOD_TEST_1' = (SELECT r_node FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det insert r_node');
SELECT assert (1 = (SELECT lane_type FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det insert lane_type');
SELECT assert (2 = (SELECT lane_number FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det insert lane_number');
SELECT assert (false = (SELECT abandoned FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det insert abandoned');
SELECT assert (true = (SELECT force_fail FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det insert force_fail');
SELECT assert (37.0 = (SELECT field_length FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det insert field_length');
SELECT assert ((SELECT fake FROM iris.detector
               WHERE name = 'DET_TEST_1') IS NULL, 'det insert fake');
SELECT assert ((SELECT notes FROM iris.detector
               WHERE name = 'DET_TEST_1') IS NULL, 'det insert notes');
\o

UPDATE iris.detector SET controller = 'CTL_TEST_1' WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET pin = 20 WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET r_node = 'NOD_TEST_2' WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET lane_type = 10 WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET lane_number = 3 WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET abandoned = true WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET force_fail = false WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET field_length = 15.0 WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET fake = 'fake exp' WHERE name = 'DET_TEST_1';
UPDATE iris.detector SET notes = 'notes text' WHERE name = 'DET_TEST_1';

\o /dev/null
SELECT assert ('DET_TEST_1' = (SELECT name FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update name');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update controller');
SELECT assert (20 = (SELECT pin FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update pin');
SELECT assert ('NOD_TEST_2' = (SELECT r_node FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update r_node');
SELECT assert (10 = (SELECT lane_type FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update lane_type');
SELECT assert (3 = (SELECT lane_number FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update lane_number');
SELECT assert (true = (SELECT abandoned FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update abandoned');
SELECT assert (false = (SELECT force_fail FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update force_fail');
SELECT assert (15.0 = (SELECT field_length FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update field_length');
SELECT assert ('fake exp' = (SELECT fake FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update fake');
SELECT assert ('notes text' = (SELECT notes FROM iris.detector
               WHERE name = 'DET_TEST_1'), 'det update notes');
\o

DELETE FROM iris.detector WHERE name = 'DET_TEST_1';
DELETE FROM iris.r_node WHERE name = 'NOD_TEST_2';
DELETE FROM iris.r_node WHERE name = 'NOD_TEST_1';

-- Test camera view
INSERT INTO iris.camera (name, pin, notes, encoder, encoder_channel,
                         encoder_type, publish)
	VALUES ('CAM_TEST_1', 0, 'notes', 'uri', 3, 1, false);

\o /dev/null
SELECT assert ('CAM_TEST_1' = (SELECT name FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam insert name');
SELECT assert ((SELECT controller FROM iris.camera
               WHERE name = 'CAM_TEST_1') IS NULL, 'cam insert controller');
SELECT assert (0 = (SELECT pin FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam insert pin');
SELECT assert ((SELECT geo_loc FROM iris.camera
               WHERE name = 'CAM_TEST_1') IS NULL, 'camera insert geo_loc');
SELECT assert ('notes' = (SELECT notes FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam insert notes');
SELECT assert ('uri' = (SELECT encoder FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam insert encoder');
SELECT assert (3 = (SELECT encoder_channel FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam insert encoder_channel');
SELECT assert (1 = (SELECT encoder_type FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam insert encoder_type');
SELECT assert (false = (SELECT publish FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam insert publish');
\o

UPDATE iris.camera SET controller = 'CTL_TEST_1' WHERE name = 'CAM_TEST_1';
UPDATE iris.camera SET pin = 10 WHERE name = 'CAM_TEST_1';
UPDATE iris.camera SET geo_loc = 'LOC_TEST_1' WHERE name = 'CAM_TEST_1';
UPDATE iris.camera SET notes = 'more notes' WHERE name = 'CAM_TEST_1';
UPDATE iris.camera SET encoder = 'ip addr' WHERE name = 'CAM_TEST_1';
UPDATE iris.camera SET encoder_channel = 4 WHERE name = 'CAM_TEST_1';
UPDATE iris.camera SET encoder_type = 3 WHERE name = 'CAM_TEST_1';
UPDATE iris.camera SET publish = true WHERE name = 'CAM_TEST_1';

\o /dev/null
SELECT assert ('CAM_TEST_1' = (SELECT name FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update name');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update controller');
SELECT assert (10 = (SELECT pin FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update pin');
SELECT assert ('LOC_TEST_1' = (SELECT geo_loc FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update geo_loc');
SELECT assert ('more notes' = (SELECT notes FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update notes');
SELECT assert ('ip addr' = (SELECT encoder FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update encoder');
SELECT assert (4 = (SELECT encoder_channel FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update encoder_channel');
SELECT assert (3 = (SELECT encoder_type FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update encoder_type');
SELECT assert (true = (SELECT publish FROM iris.camera
               WHERE name = 'CAM_TEST_1'), 'cam update publish');
\o

INSERT INTO iris.camera_preset (name, camera, preset_num, direction)
	VALUES ('PRE_TEST_1', 'CAM_TEST_1', 1, 3);

-- Test ramp meter view
INSERT INTO iris.ramp_meter (name, pin, notes, meter_type, storage, max_wait,
                             algorithm, am_target, pm_target, m_lock)
	VALUES ('RM_TEST_1', 5, 'notes', 1, 400, 240, 3, 500, 600, 3);

\o /dev/null
SELECT assert ('RM_TEST_1' = (SELECT name FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert name');
SELECT assert ((SELECT controller FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1') IS NULL, 'meter insert controller');
SELECT assert (5 = (SELECT pin FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert pin');
SELECT assert ((SELECT geo_loc FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1') IS NULL, 'meter insert geo_loc');
SELECT assert ('notes' = (SELECT notes FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert notes');
SELECT assert (1 = (SELECT meter_type FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert meter_type');
SELECT assert (400 = (SELECT storage FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert storage');
SELECT assert (240 = (SELECT max_wait FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert max_wait');
SELECT assert (3 = (SELECT algorithm FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert algorithm');
SELECT assert (500 = (SELECT am_target FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert am_target');
SELECT assert (600 = (SELECT pm_target FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert pm_target');
SELECT assert ((SELECT preset FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1') IS NULL, 'meter insert preset');
SELECT assert (3 = (SELECT m_lock FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter insert m_lock');
\o

UPDATE iris.ramp_meter SET controller = 'CTL_TEST_1' WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET pin = 11 WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET geo_loc = 'LOC_TEST_1' WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET notes = 'mtr note' WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET meter_type = 0 WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET storage = 850 WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET max_wait = 120 WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET algorithm = 1 WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET am_target = 1100 WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET pm_target = 1200 WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET preset = 'PRE_TEST_1' WHERE name = 'RM_TEST_1';
UPDATE iris.ramp_meter SET m_lock = 1 WHERE name = 'RM_TEST_1';

\o /dev/null
SELECT assert ('RM_TEST_1' = (SELECT name FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update name');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update controller');
SELECT assert (11 = (SELECT pin FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update pin');
SELECT assert ('LOC_TEST_1' = (SELECT geo_loc FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update geo_loc');
SELECT assert ('mtr note' = (SELECT notes FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update notes');
SELECT assert (0 = (SELECT meter_type FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update meter_type');
SELECT assert (850 = (SELECT storage FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update storage');
SELECT assert (120 = (SELECT max_wait FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update max_wait');
SELECT assert (1 = (SELECT algorithm FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update algorithm');
SELECT assert (1100 = (SELECT am_target FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update am_target');
SELECT assert (1200 = (SELECT pm_target FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update pm_target');
SELECT assert ('PRE_TEST_1' = (SELECT preset FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update preset');
SELECT assert (1 = (SELECT m_lock FROM iris.ramp_meter
               WHERE name = 'RM_TEST_1'), 'meter update m_lock');
\o

DELETE FROM iris.ramp_meter WHERE name = 'RM_TEST_1';

-- Test dms view
INSERT INTO iris.dms (name, pin, notes, aws_allowed, aws_controlled)
	VALUES ('DMS_TEST_1', 7, 'Notes', true, false);

\o /dev/null
SELECT assert ('DMS_TEST_1' = (SELECT name FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms insert name');
SELECT assert ((SELECT controller FROM iris.dms
               WHERE name = 'DMS_TEST_1') IS NULL, 'dms insert controller');
SELECT assert (7 = (SELECT pin FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms insert pin');
SELECT assert ((SELECT geo_loc FROM iris.dms
               WHERE name = 'DMS_TEST_1') IS NULL, 'dms insert geo_loc');
SELECT assert ('Notes' = (SELECT notes FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms insert notes');
SELECT assert ((SELECT preset FROM iris.dms
               WHERE name = 'DMS_TEST_1') IS NULL, 'dms insert preset');
SELECT assert (true = (SELECT aws_allowed FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms insert aws_allowed');
SELECT assert (false = (SELECT aws_controlled FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms insert aws_controlled');
SELECT assert ((SELECT default_font FROM iris.dms
               WHERE name = 'DMS_TEST_1') IS NULL, 'dms insert default_font');
\o

UPDATE iris.dms SET controller = 'CTL_TEST_1' WHERE name = 'DMS_TEST_1';
UPDATE iris.dms SET pin = 17 WHERE name = 'DMS_TEST_1';
UPDATE iris.dms SET geo_loc = 'LOC_TEST_1' WHERE name = 'DMS_TEST_1';
UPDATE iris.dms SET notes = 'no' WHERE name = 'DMS_TEST_1';
UPDATE iris.dms SET preset = 'PRE_TEST_1' WHERE name = 'DMS_TEST_1';
UPDATE iris.dms SET aws_allowed = false WHERE name = 'DMS_TEST_1';
UPDATE iris.dms SET aws_controlled = true WHERE name = 'DMS_TEST_1';

\o /dev/null
SELECT assert ('DMS_TEST_1' = (SELECT name FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms update name');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms update controller');
SELECT assert (17 = (SELECT pin FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms update pin');
SELECT assert ('LOC_TEST_1' = (SELECT geo_loc FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms update geo_loc');
SELECT assert ('no' = (SELECT notes FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms update notes');
SELECT assert ('PRE_TEST_1' = (SELECT preset FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms update preset');
SELECT assert (false = (SELECT aws_allowed FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms update aws_allowed');
SELECT assert (true = (SELECT aws_controlled FROM iris.dms
               WHERE name = 'DMS_TEST_1'), 'dms update aws_controlled');
SELECT assert ((SELECT default_font FROM iris.dms
               WHERE name = 'DMS_TEST_1') IS NULL, 'dms update default_font');
\o

DELETE FROM iris.dms WHERE name = 'DMS_TEST_1';

-- Test lane_marking view
INSERT INTO iris.lane_marking (name, pin, notes)
	VALUES ('LM_TEST_1', 9, 'Notes');

\o /dev/null
SELECT assert ('LM_TEST_1' = (SELECT name FROM iris.lane_marking
               WHERE name = 'LM_TEST_1'), 'lm insert name');
SELECT assert ((SELECT controller FROM iris.lane_marking
               WHERE name = 'LM_TEST_1') IS NULL, 'lm insert controller');
SELECT assert (9 = (SELECT pin FROM iris.lane_marking
               WHERE name = 'LM_TEST_1'), 'lm insert pin');
SELECT assert ((SELECT geo_loc FROM iris.lane_marking
               WHERE name = 'LM_TEST_1') IS NULL, 'lm insert geo_loc');
SELECT assert ('Notes' = (SELECT notes FROM iris.lane_marking
               WHERE name = 'LM_TEST_1'), 'lm insert notes');
\o

UPDATE iris.lane_marking SET controller = 'CTL_TEST_1' WHERE name = 'LM_TEST_1';
UPDATE iris.lane_marking SET pin = 3 WHERE name = 'LM_TEST_1';
UPDATE iris.lane_marking SET geo_loc = 'LOC_TEST_1' WHERE name = 'LM_TEST_1';
UPDATE iris.lane_marking SET notes = 'yes' WHERE name = 'LM_TEST_1';

\o /dev/null
SELECT assert ('LM_TEST_1' = (SELECT name FROM iris.lane_marking
               WHERE name = 'LM_TEST_1'), 'lm update name');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.lane_marking
               WHERE name = 'LM_TEST_1'), 'lm update controller');
SELECT assert (3 = (SELECT pin FROM iris.lane_marking
               WHERE name = 'LM_TEST_1'), 'lm update pin');
SELECT assert ('LOC_TEST_1' = (SELECT geo_loc FROM iris.lane_marking
               WHERE name = 'LM_TEST_1'), 'lm update geo_loc');
SELECT assert ('yes' = (SELECT notes FROM iris.lane_marking
               WHERE name = 'LM_TEST_1'), 'lm update notes');
\o

DELETE FROM iris.lane_marking WHERE name = 'LM_TEST_1';

-- Test weather_sensor view
INSERT INTO iris.weather_sensor (name, pin, notes)
	VALUES ('WS_TEST_1', 4, 'Some Notes');

\o /dev/null
SELECT assert ('WS_TEST_1' = (SELECT name FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1'), 'ws insert name');
SELECT assert ((SELECT controller FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1') IS NULL, 'ws insert controller');
SELECT assert (4 = (SELECT pin FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1'), 'ws insert pin');
SELECT assert ((SELECT geo_loc FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1') IS NULL, 'ws insert geo_loc');
SELECT assert ('Some Notes' = (SELECT notes FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1'), 'ws insert notes');
\o

UPDATE iris.weather_sensor SET controller = 'CTL_TEST_1'
	WHERE name = 'WS_TEST_1';
UPDATE iris.weather_sensor SET pin = 5 WHERE name = 'WS_TEST_1';
UPDATE iris.weather_sensor SET geo_loc = 'LOC_TEST_1' WHERE name = 'WS_TEST_1';
UPDATE iris.weather_sensor SET notes = 'yeshh' WHERE name = 'WS_TEST_1';

\o /dev/null
SELECT assert ('WS_TEST_1' = (SELECT name FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1'), 'ws update name');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1'), 'ws update controller');
SELECT assert (5 = (SELECT pin FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1'), 'ws update pin');
SELECT assert ('LOC_TEST_1' = (SELECT geo_loc FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1'), 'ws update geo_loc');
SELECT assert ('yeshh' = (SELECT notes FROM iris.weather_sensor
               WHERE name = 'WS_TEST_1'), 'ws update notes');
\o

DELETE FROM iris.weather_sensor WHERE name = 'WS_TEST_1';

-- Test lcs_array view
INSERT INTO iris.lcs_array (name, pin, notes, shift)
	VALUES ('LCS_TEST_1', 8, 'a Note', 5);

\o /dev/null
SELECT assert ('LCS_TEST_1' = (SELECT name FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs insert name');
SELECT assert ((SELECT controller FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1') IS NULL, 'lcs insert controller');
SELECT assert (8 = (SELECT pin FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs insert pin');
SELECT assert ('a Note' = (SELECT notes FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs insert notes');
SELECT assert (5 = (SELECT shift FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs insert shift');
SELECT assert ((SELECT lcs_lock FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1') IS NULL, 'lcs insert lcs_lock');
\o

UPDATE iris.lcs_array SET controller = 'CTL_TEST_1' WHERE name = 'LCS_TEST_1';
UPDATE iris.lcs_array SET pin = 11 WHERE name = 'LCS_TEST_1';
UPDATE iris.lcs_array SET notes = 'nope' WHERE name = 'LCS_TEST_1';
UPDATE iris.lcs_array SET shift = 4 WHERE name = 'LCS_TEST_1';
UPDATE iris.lcs_array SET lcs_lock = 3 WHERE name = 'LCS_TEST_1';

\o /dev/null
SELECT assert ('LCS_TEST_1' = (SELECT name FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs update name');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs update controller');
SELECT assert (11 = (SELECT pin FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs update pin');
SELECT assert ('nope' = (SELECT notes FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs update notes');
SELECT assert (4 = (SELECT shift FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs update shift');
SELECT assert (3 = (SELECT lcs_lock FROM iris.lcs_array
               WHERE name = 'LCS_TEST_1'), 'lcs update lcs_lock');
\o

-- Test lcs_indication view
INSERT INTO iris.dms (name, pin, notes, aws_allowed, aws_controlled)
	VALUES ('L_TEST_1', 1, '', false, false);
INSERT INTO iris.lcs (name, lcs_array, lane)
	VALUES ('L_TEST_1', 'LCS_TEST_1', 1);
INSERT INTO iris.dms (name, pin, notes, aws_allowed, aws_controlled)
	VALUES ('L_TEST_2', 2, '', false, false);
INSERT INTO iris.lcs (name, lcs_array, lane)
	VALUES ('L_TEST_2', 'LCS_TEST_1', 2);
INSERT INTO iris.lcs_indication (name, pin, lcs, indication)
	VALUES ('LI_TEST_1', 13, 'L_TEST_1', 1);

\o /dev/null
SELECT assert ('LI_TEST_1' = (SELECT name FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li insert name');
SELECT assert ((SELECT controller FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1') IS NULL, 'li insert controller');
SELECT assert (13 = (SELECT pin FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li insert pin');
SELECT assert ('L_TEST_1' = (SELECT lcs FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li insert lcs');
SELECT assert (1 = (SELECT indication FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li insert indication');
\o

UPDATE iris.lcs_indication SET controller = 'CTL_TEST_1'
	WHERE name = 'LI_TEST_1';
UPDATE iris.lcs_indication SET pin = 14 WHERE name = 'LI_TEST_1';
UPDATE iris.lcs_indication SET lcs = 'L_TEST_2' WHERE name = 'LI_TEST_1';
UPDATE iris.lcs_indication SET indication = 2 WHERE name = 'LI_TEST_1';

\o /dev/null
SELECT assert ('LI_TEST_1' = (SELECT name FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li update name');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li update controller');
SELECT assert (14 = (SELECT pin FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li update pin');
SELECT assert ('L_TEST_2' = (SELECT lcs FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li update lcs');
SELECT assert (2 = (SELECT indication FROM iris.lcs_indication
               WHERE name = 'LI_TEST_1'), 'li update indication');
\o

DELETE FROM iris.lcs_indication WHERE name = 'LI_TEST_1';
DELETE FROM iris.lcs WHERE name = 'L_TEST_2';
DELETE FROM iris.dms WHERE name = 'L_TEST_2';
DELETE FROM iris.lcs WHERE name = 'L_TEST_1';
DELETE FROM iris.dms WHERE name = 'L_TEST_1';
DELETE FROM iris.lcs_array WHERE name = 'LCS_TEST_1';

-- Delete controller stuff
DELETE FROM iris.camera_preset WHERE name = 'PRE_TEST_1';
DELETE FROM iris.camera WHERE name = 'CAM_TEST_1';
DELETE FROM iris.controller WHERE name = 'CTL_TEST_1';
DELETE FROM iris.cabinet WHERE name = 'CAB_TEST_1';
DELETE FROM iris.geo_loc WHERE name = 'LOC_TEST_1';
DELETE FROM iris.comm_link WHERE name = 'LNK_TEST_1';

-- Display results
SELECT * FROM test.results ORDER BY result DESC, test;
SELECT count(*), result FROM test.results GROUP BY result;

-- Drop test schema
DROP SCHEMA test CASCADE;

ROLLBACK;
