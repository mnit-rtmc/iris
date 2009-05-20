\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.86.0' WHERE name = 'database_version';

DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.controller_device;
DROP VIEW device_loc_view;
DROP TABLE lcs;
DROP TABLE lcs_module;
DROP TABLE "java_util_TreeMap";
DROP TABLE "java_util_AbstractMap";
DROP TABLE vault_map;
DROP TABLE traffic_device;
DROP TABLE device;
DROP TABLE abstract_list CASCADE;
DROP TABLE tms_object;
DROP TABLE "java_util_ArrayList";
DROP TABLE "java_lang_Integer";
DROP TABLE "java_lang_Number";
DROP TABLE vault_transaction;
DROP TABLE "java_util_AbstractList";
DROP TABLE "java_util_AbstractCollection";
DROP TABLE vault_list;
DROP TABLE vault_log_entry;
DROP TABLE vault_counter;
DROP TABLE vault_types;
DROP TABLE vault_object;
DROP FUNCTION get_next_oid();
 
CREATE TABLE iris.lcs_lock (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE iris._lcs_array (
	name VARCHAR(10) PRIMARY KEY,
	notes text NOT NULL,
	lcs_lock INTEGER REFERENCES iris.lcs_lock(id)
);

ALTER TABLE iris._lcs_array ADD CONSTRAINT _lcs_array_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lcs_array AS SELECT
	d.name, controller, pin, notes, lcs_lock
	FROM iris._lcs_array la JOIN iris._device_io d ON la.name = d.name;

CREATE RULE lcs_array_insert AS ON INSERT TO iris.lcs_array DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_array VALUES (NEW.name, NEW.notes, NEW.lcs_lock);
);

CREATE RULE lcs_array_update AS ON UPDATE TO iris.lcs_array DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._lcs_array SET
		notes = NEW.notes,
		lcs_lock = NEW.lcs_lock
	WHERE name = OLD.name;
);

CREATE RULE lcs_array_delete AS ON DELETE TO iris.lcs_array DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris.lcs (
	name VARCHAR(10) PRIMARY KEY REFERENCES iris._dms,
	lcs_array VARCHAR(10) NOT NULL REFERENCES iris._lcs_array,
	lane INTEGER NOT NULL
);

CREATE UNIQUE INDEX lcs_array_lane ON iris.lcs USING btree (lcs_array, lane);

CREATE TABLE iris.lane_use_indication (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

CREATE TABLE iris._lcs_indication (
	name VARCHAR(10) PRIMARY KEY,
	lcs VARCHAR(10) NOT NULL REFERENCES iris.lcs,
	indication INTEGER NOT NULL REFERENCES iris.lane_use_indication
);

ALTER TABLE iris._lcs_indication ADD CONSTRAINT _lcs_indication_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lcs_indication AS SELECT
	d.name, controller, pin, lcs, indication
	FROM iris._lcs_indication li JOIN iris._device_io d ON li.name = d.name;

CREATE RULE lcs_indication_insert AS ON INSERT TO iris.lcs_indication DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_indication VALUES (NEW.name, NEW.lcs,
		NEW.indication);
);

CREATE RULE lcs_indication_update AS ON UPDATE TO iris.lcs_indication DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._lcs_indication SET
		lcs = NEW.lcs,
		indication = NEW.indication
	WHERE name = OLD.name;
);
 
CREATE RULE lcs_indication_delete AS ON DELETE TO iris.lcs_indication DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE VIEW iris.controller_lcs AS
	SELECT dio.name, dio.controller, dio.pin, d.geo_loc
	FROM iris._device_io dio
	JOIN iris.dms d ON dio.name = d.name;

CREATE VIEW iris.controller_device AS
	SELECT * FROM iris.controller_dms UNION ALL
	SELECT * FROM iris.controller_lcs UNION ALL
	SELECT * FROM iris.controller_meter UNION ALL
	SELECT * FROM iris.controller_warning_sign UNION ALL
	SELECT * FROM iris.controller_camera;

CREATE VIEW controller_device_view AS
	SELECT d.name, d.controller, d.pin, d.geo_loc,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM iris.controller_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.mile, cab.geo_loc,
	trim(l.freeway || ' ' || l.free_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_street AS cross_street, d.freeway AS freeway, c.notes
	FROM controller c
	LEFT JOIN cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

COPY iris.lane_use_indication (id, description) FROM stdin;
0	Dark
1	Lane open
2	Use caution
3	Lane closed ahead
4	Lane closed
5	HOV / HOT
6	Merge right
7	Merge left
8	Merge left or right
9	Must exit right
10	Must exit left
11	Advisory variable speed limit
12	Variable speed limit
\.

COPY iris.lcs_lock (id, description) FROM stdin;
1	Incident
2	Maintenance
3	Testing
4	Other reason
\.
