\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

DROP FUNCTION add_detector() CASCADE;
DROP FUNCTION remove_detector() CASCADE;
DROP FUNCTION add_dms() CASCADE;
DROP FUNCTION remove_dms() CASCADE;
DROP FUNCTION add_meter() CASCADE;
DROP FUNCTION remove_meter() CASCADE;
DROP FUNCTION add_camera() CASCADE;
DROP FUNCTION remove_camera() CASCADE;

DROP TABLE add_remove_device_log;

DROP VIEW camera_view;
DROP VIEW dms_view;
DROP VIEW ramp_meter_view;

CREATE TEMP TABLE camera_temp AS
	SELECT vault_oid, id, geo_loc, controller, pin, notes, encoder,
	encoder_channel, nvr, publish FROM camera;
UPDATE camera_temp SET controller = NULL WHERE controller = '';

DROP TABLE camera;

CREATE TABLE camera (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES geo_loc(name),
	controller VARCHAR(20) REFERENCES controller(name),
	pin INTEGER NOT NULL,
	notes text NOT NULL,
	encoder text NOT NULL,
	encoder_channel integer NOT NULL,
	nvr text NOT NULL,
	publish boolean NOT NULL
);

INSERT INTO camera (name, geo_loc, controller, pin, notes, encoder,
	encoder_channel, nvr, publish)
	(SELECT id, geo_loc, controller, pin, notes, encoder, encoder_channel,
	nvr, publish FROM camera_temp);

CREATE TEMP TABLE device_camera (
	vault_oid INTEGER PRIMARY KEY,
	camera VARCHAR(10) NOT NULL
);

INSERT INTO device_camera (vault_oid, camera)
	(SELECT dms.vault_oid, c.id FROM dms, camera_temp c
	WHERE dms.camera = c.vault_oid);
ALTER TABLE dms DROP COLUMN camera;
ALTER TABLE dms ADD COLUMN camera VARCHAR(10);
UPDATE dms SET camera = '';
ALTER TABLE dms ALTER COLUMN camera SET NOT NULL;
UPDATE dms SET camera = device_camera.camera
	FROM device_camera WHERE dms.vault_oid = device_camera.vault_oid;

INSERT INTO device_camera (vault_oid, camera)
	(SELECT ramp_meter.vault_oid, c.id FROM ramp_meter, camera_temp c
	WHERE ramp_meter.camera = c.vault_oid);
ALTER TABLE ramp_meter DROP COLUMN camera;
ALTER TABLE ramp_meter ADD COLUMN camera VARCHAR(10);
UPDATE ramp_meter SET camera = '';
ALTER TABLE ramp_meter ALTER COLUMN camera SET NOT NULL;
UPDATE ramp_meter SET camera = device_camera.camera
	FROM device_camera WHERE ramp_meter.vault_oid = device_camera.vault_oid;

INSERT INTO device_camera (vault_oid, camera)
	(SELECT warning_sign.vault_oid, c.id FROM warning_sign, camera_temp c
	WHERE warning_sign.camera = c.vault_oid);
ALTER TABLE warning_sign DROP COLUMN camera;
ALTER TABLE warning_sign ADD COLUMN camera VARCHAR(10);
UPDATE warning_sign SET camera = '';
ALTER TABLE warning_sign ALTER COLUMN camera SET NOT NULL;
UPDATE warning_sign SET camera = device_camera.camera
	FROM device_camera WHERE warning_sign.vault_oid = device_camera.vault_oid;

INSERT INTO device_camera (vault_oid, camera)
	(SELECT lcs.vault_oid, c.id FROM lcs, camera_temp c
	WHERE lcs.camera = c.vault_oid);
ALTER TABLE lcs DROP COLUMN camera;
ALTER TABLE lcs ADD COLUMN camera VARCHAR(10);
UPDATE lcs SET camera = '';
ALTER TABLE lcs ALTER COLUMN camera SET NOT NULL;
UPDATE lcs SET camera = device_camera.camera
	FROM device_camera WHERE lcs.vault_oid = device_camera.vault_oid;

CREATE VIEW dms_view AS
	SELECT d.id, d.notes, d.camera, d.mile, d.travel,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off
	FROM dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW ramp_meter_view AS
	SELECT m.vault_oid, m.id, m.notes,
	m."controlMode" AS control_mode, m."singleRelease" AS single_release,
	m."storage", m."maxWait" AS max_wait, m.camera,
	l.fwy, l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM ramp_meter m
	JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, ctr.comm_link, ctr.drop_id, ctr.active, c.notes,
	c.encoder, c.encoder_channel, c.nvr, c.publish,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM camera c
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;
