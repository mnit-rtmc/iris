\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.112.0'
	WHERE name = 'database_version';

ALTER TABLE iris._lcs_array ADD COLUMN shift INTEGER;
UPDATE iris._lcs_array SET shift = 0;
ALTER TABLE iris._lcs_array ALTER COLUMN shift SET NOT NULL;

DROP VIEW iris.lcs_array;
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

UPDATE iris.system_attribute SET name = 'dms_poll_period_secs'
        WHERE name = 'dms_poll_freq_secs';
