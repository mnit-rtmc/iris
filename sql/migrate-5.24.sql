\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.23.0', '5.24.0');

-- Add msg_combining look-up table
CREATE TABLE iris.msg_combining (
	id INTEGER PRIMARY KEY,
	description VARCHAR(8) NOT NULL
);

COPY iris.msg_combining (id, description) FROM stdin;
0	disable
1	first
2	second
3	either
\.

DROP VIEW quick_message_view;
DROP VIEW dms_message_view;
DROP VIEW sign_message_view;

-- Replace prefix_page with msg_combining on sign_message
ALTER TABLE iris.sign_message
	ADD COLUMN msg_combining INTEGER REFERENCES iris.msg_combining;
UPDATE iris.sign_message SET msg_combining = 0 WHERE prefix_page = false;
UPDATE iris.sign_message SET msg_combining = 1 WHERE prefix_page = true;
ALTER TABLE iris.sign_message ALTER COLUMN msg_combining SET NOT NULL;
ALTER TABLE iris.sign_message DROP COLUMN prefix_page;

-- Replace prefix_page with msg_combining on quick_message
ALTER TABLE iris.quick_message
	ADD COLUMN msg_combining INTEGER REFERENCES iris.msg_combining;
UPDATE iris.quick_message SET msg_combining = 0 WHERE prefix_page = false;
UPDATE iris.quick_message SET msg_combining = 1 WHERE prefix_page = true;
ALTER TABLE iris.quick_message ALTER COLUMN msg_combining SET NOT NULL;
ALTER TABLE iris.quick_message DROP COLUMN prefix_page;

CREATE VIEW sign_message_view AS
	SELECT name, sign_config, incident, multi, beacon_enabled,
	       mc.description AS msg_combining, msg_priority,
	       iris.sign_msg_sources(source) AS sources, owner, duration
	FROM iris.sign_message sm
	LEFT JOIN iris.msg_combining mc ON sm.msg_combining = mc.id;
GRANT SELECT ON sign_message_view TO PUBLIC;

CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition,
	       fail_time IS NOT NULL AS failed, multi, beacon_enabled,
	       mc.description AS msg_combining, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, expire_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message sm ON d.msg_current = sm.name
	LEFT JOIN iris.msg_combining mc ON sm.msg_combining = mc.id;
GRANT SELECT ON dms_message_view TO PUBLIC;

CREATE VIEW quick_message_view AS
	SELECT name, sign_group, sign_config, mc.description AS msg_combining,
	       multi
	FROM iris.quick_message qm
	LEFT JOIN iris.msg_combining mc ON qm.msg_combining = mc.id;
GRANT SELECT ON quick_message_view TO PUBLIC;

ALTER TABLE iris.cabinet_style ADD COLUMN police_panel_pin_1 INTEGER;
ALTER TABLE iris.cabinet_style ADD COLUMN police_panel_pin_2 INTEGER;
ALTER TABLE iris.cabinet_style ADD COLUMN watchdog_reset_pin_1 INTEGER;
ALTER TABLE iris.cabinet_style ADD COLUMN watchdog_reset_pin_2 INTEGER;

UPDATE iris.cabinet_style SET police_panel_pin_1 = 82, police_panel_pin_2 = 82
	WHERE name IN ('334Z', '334DZ', 'S334Z');
UPDATE iris.cabinet_style SET police_panel_pin_1 = 81, police_panel_pin_2 = 82
	WHERE name IN ('334Z-94', '334Z-99', '334Z-00', '334ZP', '334Z-05',
	               '334Z-08', '334Z-14', 'MN340', 'MN340-14');
UPDATE iris.cabinet_style SET police_panel_pin_1 = 80
	WHERE name IN ('334', '334D', 'Prehistoric');

UPDATE iris.cabinet_style SET watchdog_reset_pin_1 = 37,
	                      watchdog_reset_pin_2 = 38
	WHERE name IN ('334Z', '334DZ', 'S334Z');
UPDATE iris.cabinet_style SET watchdog_reset_pin_1 = 37,
	                      watchdog_reset_pin_2 = 38
	WHERE name IN ('334Z-94', '334Z-99', '334Z-00', '334ZP', '334Z-05',
	               '334Z-08', '334Z-14', 'MN340', 'MN340-14');
UPDATE iris.cabinet_style SET watchdog_reset_pin_1 = 38
	WHERE name IN ('334', '334D', 'Prehistoric');

CREATE VIEW cabinet_style_view AS
	SELECT name, police_panel_pin_1, police_panel_pin_2,
	       watchdog_reset_pin_1, watchdog_reset_pin_2, dip
	FROM iris.cabinet_style;
GRANT SELECT ON cabinet_style_view TO PUBLIC;

-- Replace occ spike enable attribute
UPDATE iris.system_attribute SET name = 'detector_occ_spike_secs', value = '60'
	WHERE name = 'detector_occ_spike_enable' AND value = 'true';
UPDATE iris.system_attribute SET name = 'detector_occ_spike_secs', value = '0'
	WHERE name = 'detector_occ_spike_enable';

COMMIT;
