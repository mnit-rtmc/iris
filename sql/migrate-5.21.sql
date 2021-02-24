\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.20.0', '5.21.0');

INSERT INTO iris.sonar_type (name) VALUES ('alert_message');

INSERT INTO iris.privilege (name, capability, type_n, write) VALUES
	('PRV_009E', 'alert_admin', 'alert_message', true),
	('PRV_009F', 'alert_tab', 'alert_message', false);

INSERT INTO cap.event (code, description) VALUES
	('BHS', 'Beach Hazards Statement'),
	('CFY', 'Coastal Flood Advisory'),
	('DSY', 'Dust Advisory'),
	('DUY', 'Blowing Dust Advisory'),
	('FAA', 'Flood Watch'),
	('FAW', 'Flood Warning'),
	('FAY', 'Flood Advisory'),
	('FZA', 'Freeze Watch'),
	('HZA', 'Hard Freeze Watch'),
	('HZW', 'Hard Freeze Warning'),
	('ISW', 'Ice Storm Warning'),
	('LOY', 'Low Water Advisory'),
	('LWY', 'Lake Wind Advisory'),
	('MAW', 'Special Marine Warning'),
	('MFY', 'Dense Fog Advisory'),
	('SAB', 'Avalanche Advisory'),
	('SEA', 'Hazardous Seas Watch'),
	('SEW', 'Hazardous Seas Warning'),
	('SRW', 'Storm Warning'),
	('SVW', 'Severe Thunderstorm Warning'),
	('TOW', 'Tornado Warning'),
	('WCA', 'Wind Chill Watch'),
	('WCW', 'Wind Chill Warning'),
	('ZFY', 'Freezing Fog Advisory');

DELETE FROM cap.event WHERE code IN ('SMW', 'SVR', 'TOR');

DROP VIEW alert_config_view;
DROP TABLE iris.alert_config;

CREATE TABLE iris.alert_config (
	name VARCHAR(20) PRIMARY KEY,
	event VARCHAR(3) REFERENCES cap.event,
	response_shelter BOOLEAN NOT NULL,
	response_evacuate BOOLEAN NOT NULL,
	response_prepare BOOLEAN NOT NULL,
	response_execute BOOLEAN NOT NULL,
	response_avoid BOOLEAN NOT NULL,
	response_monitor BOOLEAN NOT NULL,
	response_all_clear BOOLEAN NOT NULL,
	response_none BOOLEAN NOT NULL,
	urgency_unknown BOOLEAN NOT NULL,
	urgency_past BOOLEAN NOT NULL,
	urgency_future BOOLEAN NOT NULL,
	urgency_expected BOOLEAN NOT NULL,
	urgency_immediate BOOLEAN NOT NULL,
	severity_unknown BOOLEAN NOT NULL,
	severity_minor BOOLEAN NOT NULL,
	severity_moderate BOOLEAN NOT NULL,
	severity_severe BOOLEAN NOT NULL,
	severity_extreme BOOLEAN NOT NULL,
	certainty_unknown BOOLEAN NOT NULL,
	certainty_unlikely BOOLEAN NOT NULL,
	certainty_possible BOOLEAN NOT NULL,
	certainty_likely BOOLEAN NOT NULL,
	certainty_observed BOOLEAN NOT NULL,
	auto_deploy BOOLEAN NOT NULL,
	before_period_hours INTEGER NOT NULL,
	after_period_hours INTEGER NOT NULL
);

CREATE TABLE iris.alert_period (
	id INTEGER PRIMARY KEY,
	description VARCHAR(10) NOT NULL
);

COPY iris.alert_period(id, description) FROM stdin;
0	before
1	during
2	after
\.

CREATE TABLE iris.alert_message (
	name VARCHAR(20) PRIMARY KEY,
	alert_config VARCHAR(20) NOT NULL REFERENCES iris.alert_config,
	alert_period INTEGER NOT NULL REFERENCES iris.alert_period,
	sign_group VARCHAR(20) REFERENCES iris.sign_group,
	quick_message VARCHAR(20) REFERENCES iris.quick_message
);

-- Delete unused sign configs
DELETE FROM iris.sign_config WHERE name NOT IN (
    SELECT sign_config FROM iris._dms WHERE sign_config IS NOT NULL
    UNION ALL
    SELECT sign_config FROM iris.quick_message WHERE sign_config IS NOT NULL
    UNION ALL
    SELECT sign_config FROM iris.sign_message WHERE sign_config IS NOT NULL
);

-- Change sign_config primary key values
DROP VIEW dms_view;
DROP VIEW dms_message_view;
DROP VIEW iris.dms;
DROP VIEW sign_message_view;
DROP VIEW quick_message_view;
DROP VIEW sign_config_view;

ALTER TABLE iris.sign_message DROP CONSTRAINT sign_message_sign_config_fkey;
ALTER TABLE iris.sign_message ALTER COLUMN sign_config TYPE VARCHAR(16);

ALTER TABLE iris._dms DROP CONSTRAINT _dms_sign_config_fkey;
ALTER TABLE iris._dms ALTER COLUMN sign_config TYPE VARCHAR(16);

ALTER TABLE iris.quick_message DROP CONSTRAINT quick_message_sign_config_fkey;
ALTER TABLE iris.quick_message ALTER COLUMN sign_config TYPE VARCHAR(16);

ALTER TABLE iris.sign_config DROP CONSTRAINT sign_config_pkey;
ALTER TABLE iris.sign_config ALTER COLUMN name TYPE VARCHAR(16);
ALTER TABLE iris.sign_config ADD COLUMN tname VARCHAR(16);
WITH cte AS (
	SELECT name,
               'sc_' || pixel_width || 'x' || pixel_height || '_' || row_number()
	       OVER (PARTITION BY pixel_width, pixel_height) AS tname
	FROM iris.sign_config
)
UPDATE iris.sign_config SET tname = cte.tname
FROM cte
WHERE iris.sign_config.name = cte.name;

UPDATE iris.sign_message sm
   SET sign_config = sc.tname
   FROM iris.sign_config sc
   WHERE sm.sign_config = sc.name;

UPDATE iris._dms d
   SET sign_config = sc.tname
   FROM iris.sign_config sc
   WHERE d.sign_config = sc.name;

UPDATE iris.quick_message qm
   SET sign_config = sc.tname
   FROM iris.sign_config sc
   WHERE qm.sign_config = sc.name;

UPDATE iris.sign_config SET name = tname;
ALTER TABLE iris.sign_config DROP COLUMN tname;
ALTER TABLE iris.sign_config ADD PRIMARY KEY (name);

ALTER TABLE iris.quick_message
    ADD FOREIGN KEY (sign_config)
    REFERENCES iris.sign_config;

ALTER TABLE iris._dms
    ADD FOREIGN KEY (sign_config)
    REFERENCES iris.sign_config;

ALTER TABLE iris.sign_message
    ADD FOREIGN KEY (sign_config)
    REFERENCES iris.sign_config;

CREATE VIEW sign_config_view AS
	SELECT name, face_width, face_height, border_horiz, border_vert,
	       pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width,
	       char_height, monochrome_foreground, monochrome_background,
	       cs.description AS color_scheme, default_font
	FROM iris.sign_config
	JOIN iris.color_scheme cs ON sign_config.color_scheme = cs.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

CREATE VIEW sign_message_view AS
	SELECT name, sign_config, incident, multi, beacon_enabled, prefix_page,
	       msg_priority, iris.sign_msg_sources(source) AS sources, owner,
	       duration
	FROM iris.sign_message;
GRANT SELECT ON sign_message_view TO PUBLIC;

CREATE VIEW quick_message_view AS
	SELECT name, sign_group, sign_config, prefix_page, multi
	FROM iris.quick_message;
GRANT SELECT ON quick_message_view TO PUBLIC;

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       purpose, hidden, beacon, preset, sign_config, sign_detail,
	       override_font, override_foreground, override_background,
	       msg_sched, msg_current, expire_time
	FROM iris._dms dms
	JOIN iris._device_io d ON dms.name = d.name
	JOIN iris._device_preset p ON dms.name = p.name;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition,
	       fail_time IS NOT NULL AS failed, multi, beacon_enabled,
	       prefix_page, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, expire_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, dp.description AS purpose, d.hidden, d.beacon,
	       p.camera, p.preset_num, d.sign_config, d.sign_detail,
	       default_font, override_font, override_foreground,
	       override_background, msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

COMMIT;
