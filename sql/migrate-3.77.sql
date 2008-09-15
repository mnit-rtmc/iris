\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

SELECT id AS name, geo_loc, controller, pin, notes, "text" AS message,
	camera INTO TEMP temp_warning FROM warning_sign;

DROP TABLE warning_sign;

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

INSERT INTO iris.warning_sign SELECT * FROM temp_warning;

CREATE VIEW warning_sign_view AS
	SELECT w.name, w.notes, w.message, w.camera, w.geo_loc,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off,
	w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.warning_sign w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN controller ctr ON w.controller = ctr.name;
GRANT SELECT ON warning_sign_view TO PUBLIC;

DELETE FROM vault_types WHERE "table" = 'warning_sign';
