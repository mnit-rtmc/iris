\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.140.0'
	WHERE name = 'database_version';

DROP RULE lcs_array_insert ON iris.lcs_array;

CREATE RULE lcs_array_insert AS ON INSERT TO iris.lcs_array DO INSTEAD
(
	INSERT INTO iris._device_io ("name", "controller", "pin")
	VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_array ("name", "notes", "shift", "lcs_lock")
	VALUES (NEW.name, NEW.notes, NEW.shift, NEW.lcs_lock);
);
