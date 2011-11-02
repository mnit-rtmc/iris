\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.136.0'
	WHERE name = 'database_version';

CREATE TABLE iris.meter_algorithm (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

COPY iris.meter_algorithm (id, description) FROM stdin;
0	No Metering
1	Simple Metering
2	Stratified Metering
\.

ALTER TABLE iris._ramp_meter ADD COLUMN algorithm INTEGER REFERENCES iris.meter_algorithm;
UPDATE iris._ramp_meter SET algorithm = plan_type FROM iris.timing_plan WHERE device = _ramp_meter.name AND active = true;
UPDATE iris._ramp_meter SET algorithm = 0 WHERE algorithm IS NULL;
ALTER TABLE iris._ramp_meter ALTER COLUMN algorithm SET NOT NULL;

ALTER TABLE iris._ramp_meter ADD COLUMN am_target INTEGER;
UPDATE iris._ramp_meter SET am_target = 0;
UPDATE iris._ramp_meter SET am_target = target FROM iris.timing_plan WHERE device = _ramp_meter.name AND start_min < 720;
ALTER TABLE iris._ramp_meter ALTER COLUMN am_target SET NOT NULL;

ALTER TABLE iris._ramp_meter ADD COLUMN pm_target INTEGER;
UPDATE iris._ramp_meter SET pm_target = 0;
UPDATE iris._ramp_meter SET pm_target = target FROM iris.timing_plan WHERE device = _ramp_meter.name AND start_min >= 720;
ALTER TABLE iris._ramp_meter ALTER COLUMN pm_target SET NOT NULL;

CREATE TABLE iris.meter_action (
	name VARCHAR(20) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	ramp_meter VARCHAR(10) NOT NULL REFERENCES iris._ramp_meter,
	state INTEGER NOT NULL REFERENCES iris.plan_state
);

INSERT INTO iris.action_plan (name, description, sync_actions, deploying_secs,
	undeploying_secs, active, state)
	(SELECT 'METER_' || replace(hour_min(start_min), ':', '') || '_' ||
	replace(hour_min(stop_min), ':', ''), 'Meters starting at ' ||
	hour_min(start_min), false, 0, 0, true, 0 FROM iris.timing_plan
	WHERE active = true GROUP BY start_min, stop_min);

INSERT INTO iris.time_action (name, action_plan, day_plan, minute, deploy)
	(SELECT 'METER_' || replace(hour_min(start_min), ':', '') || '_' ||
        replace(hour_min(stop_min), ':', '') || '_' ||
	replace(hour_min(start_min), ':', ''), 'METER_' ||
	replace(hour_min(start_min), ':', '') || '_' ||
        replace(hour_min(stop_min), ':', ''), 'DEFAULT_AM', start_min, true
	FROM iris.timing_plan WHERE active = true AND start_min < 720
	GROUP BY start_min, stop_min);

INSERT INTO iris.time_action (name, action_plan, day_plan, minute, deploy)
	(SELECT 'METER_' || replace(hour_min(start_min), ':', '') || '_' ||
        replace(hour_min(stop_min), ':', '') || '_' ||
	replace(hour_min(stop_min), ':', ''), 'METER_' ||
	replace(hour_min(start_min), ':', '') || '_' ||
        replace(hour_min(stop_min), ':', ''), 'DEFAULT_AM', stop_min, false
	FROM iris.timing_plan WHERE active = true AND start_min < 720
	GROUP BY start_min, stop_min);

INSERT INTO iris.time_action (name, action_plan, day_plan, minute, deploy)
	(SELECT 'METER_' || replace(hour_min(start_min), ':', '') || '_' ||
        replace(hour_min(stop_min), ':', '') || '_' ||
	replace(hour_min(start_min), ':', ''), 'METER_' ||
	replace(hour_min(start_min), ':', '') || '_' ||
        replace(hour_min(stop_min), ':', ''), 'DEFAULT_PM', start_min, true
	FROM iris.timing_plan WHERE active = true AND start_min >= 720
	GROUP BY start_min, stop_min);

INSERT INTO iris.time_action (name, action_plan, day_plan, minute, deploy)
	(SELECT 'METER_' || replace(hour_min(start_min), ':', '') || '_' ||
        replace(hour_min(stop_min), ':', '') || '_' ||
	replace(hour_min(stop_min), ':', ''), 'METER_' ||
	replace(hour_min(start_min), ':', '') || '_' ||
        replace(hour_min(stop_min), ':', ''), 'DEFAULT_PM', stop_min, false
	FROM iris.timing_plan WHERE active = true AND start_min >= 720
	GROUP BY start_min, stop_min);

INSERT INTO iris.meter_action (name, action_plan, ramp_meter, state)
	(SELECT device || '_' || replace(hour_min(start_min), ':', '') || '_' ||
	replace(hour_min(stop_min), ':', ''), 'METER_' ||
	replace(hour_min(start_min), ':', '') || '_' ||
	replace(hour_min(stop_min), ':', ''), device, 2
	FROM iris.timing_plan WHERE active = true);

DROP VIEW timing_plan_view;
DROP TABLE iris.timing_plan;
DROP TABLE iris.timing_plan_type;

DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.controller_device;
DROP VIEW iris.controller_meter;
DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;

CREATE VIEW iris.ramp_meter AS SELECT
	m.name, geo_loc, controller, pin, notes, meter_type, storage,
	max_wait, algorithm, am_target, pm_target, camera, m_lock
	FROM iris._ramp_meter m JOIN iris._device_io d ON m.name = d.name;

CREATE RULE ramp_meter_insert AS ON INSERT TO iris.ramp_meter DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._ramp_meter (name, geo_loc, notes, meter_type, storage,
		max_wait, algorithm, am_target, pm_target, camera, m_lock)
		VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type,
		NEW.storage, NEW.max_wait, NEW.algorithm, NEW.am_target,
		NEW.pm_target, NEW.camera, NEW.m_lock);
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
		algorithm = NEW.algorithm,
		am_target = NEW.am_target,
		pm_target = NEW.pm_target,
		camera = NEW.camera,
		m_lock = NEW.m_lock
	WHERE name = OLD.name;
);

CREATE RULE ramp_meter_delete AS ON DELETE TO iris.ramp_meter DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE VIEW ramp_meter_view AS
	SELECT m.name, geo_loc, controller, pin, notes,
	mt.description AS meter_type, storage, max_wait,
	alg.description AS algorithm, am_target, pm_target, camera,
	ml.description AS meter_lock,
	l.rd, l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing
	FROM iris.ramp_meter m
	LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
	LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
	LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW iris.controller_meter AS
	SELECT dio.name, dio.controller, dio.pin, m.geo_loc
	FROM iris._device_io dio
	JOIN iris.ramp_meter m ON dio.name = m.name;

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

CREATE VIEW action_plan_view AS
	SELECT name, ap.description, sync_actions, deploying_secs,
	undeploying_secs, active, ps.description AS state
	FROM iris.action_plan ap, iris.plan_state ps WHERE ap.state = ps.id;
GRANT SELECT ON action_plan_view TO PUBLIC;

CREATE VIEW time_action_view AS
	SELECT name, action_plan, day_plan, hour_min(minute) AS time_of_day,
	deploy
	FROM iris.time_action;
GRANT SELECT ON time_action_view TO PUBLIC;

INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d)
	VALUES ('prv_mac0', 'policy_admin', 'meter_action(/.*)?', true, false,
	false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
	priv_d)
	VALUES ('prv_mac1', 'policy_admin', 'meter_action/.*', false, true,
	true, true);

DELETE FROM iris.privilege WHERE pattern LIKE 'timing_plan%';
