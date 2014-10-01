\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.17.0'
	WHERE name = 'database_version';

CREATE TABLE iris.camera_preset (
	name VARCHAR(10) PRIMARY KEY,
	camera VARCHAR(10) NOT NULL REFERENCES iris._camera,
	preset_num INTEGER NOT NULL CHECK (preset_num > 0 AND preset_num <= 12),
	direction SMALLINT REFERENCES iris.direction(id),
	UNIQUE(camera, preset_num)
);

CREATE TABLE iris._device_preset (
	name VARCHAR(10) PRIMARY KEY,
	preset VARCHAR(10) UNIQUE REFERENCES iris.camera_preset(name)
);
