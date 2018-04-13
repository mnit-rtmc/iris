\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.70.0', '4.71.0');

-- Add group_n to action_plan
ALTER TABLE iris.action_plan ADD COLUMN group_n VARCHAR(16);

DROP VIEW action_plan_view;
CREATE VIEW action_plan_view AS
	SELECT name, description, group_n, sync_actions, sticky, active,
	       default_phase, phase
	FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

-- Rename camera_playlist_dwell_sec to camera_sequence_dwell_sec
UPDATE iris.system_attribute
   SET name = 'camera_sequence_dwell_sec'
 WHERE name = 'camera_playlist_dwell_sec';

-- Create temporary play list tables
CREATE TEMP TABLE play_list_temp AS
	SELECT name, num
	FROM iris.play_list;
CREATE TEMP TABLE play_list_camera_temp AS
	SELECT play_list, ordinal, camera
	FROM iris.play_list_camera;

-- Drop old tables/view
DROP VIEW play_list_view;
DROP TABLE iris.play_list_camera;
DROP TABLE iris.play_list;

-- Create new play_list tables/views
CREATE TABLE iris._cam_sequence (
	seq_num INTEGER PRIMARY KEY
);

CREATE TABLE iris._play_list (
	name VARCHAR(20) PRIMARY KEY,
	seq_num INTEGER REFERENCES iris._cam_sequence
);

CREATE VIEW iris.play_list AS
	SELECT name, seq_num
	FROM iris._play_list;

CREATE FUNCTION iris.play_list_insert() RETURNS TRIGGER AS
	$play_list_insert$
BEGIN
	IF NEW.seq_num IS NOT NULL THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	INSERT INTO iris._play_list (name, seq_num)
	     VALUES (NEW.name, NEW.seq_num);
	RETURN NEW;
END;
$play_list_insert$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_insert_trig
    INSTEAD OF INSERT ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_insert();

CREATE FUNCTION iris.play_list_update() RETURNS TRIGGER AS
	$play_list_update$
BEGIN
	IF NEW.seq_num IS NOT NULL AND (OLD.seq_num IS NULL OR
	                                NEW.seq_num != OLD.seq_num)
	THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	UPDATE iris._play_list
	   SET seq_num = NEW.seq_num
	 WHERE name = OLD.name;
	IF OLD.seq_num IS NOT NULL AND (NEW.seq_num IS NULL OR
	                                NEW.seq_num != OLD.seq_num)
	THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
	END IF;
	RETURN NEW;
END;
$play_list_update$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_update_trig
    INSTEAD OF UPDATE ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_update();

CREATE FUNCTION iris.play_list_delete() RETURNS TRIGGER AS
	$play_list_delete$
BEGIN
	DELETE FROM iris._play_list WHERE name = OLD.name;
	IF FOUND THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$play_list_delete$ LANGUAGE plpgsql;

CREATE TRIGGER play_list_delete_trig
    INSTEAD OF DELETE ON iris.play_list
    FOR EACH ROW EXECUTE PROCEDURE iris.play_list_delete();

CREATE TABLE iris.play_list_camera (
	play_list VARCHAR(20) NOT NULL REFERENCES iris._play_list,
	ordinal INTEGER NOT NULL,
	camera VARCHAR(20) NOT NULL REFERENCES iris._camera
);
ALTER TABLE iris.play_list_camera ADD PRIMARY KEY (play_list, ordinal);

-- Copy old play list rows to new tables
INSERT INTO iris.play_list (name, seq_num)
	SELECT name, num FROM play_list_temp;
INSERT INTO iris.play_list_camera (play_list, ordinal, camera)
	SELECT play_list, ordinal, camera FROM play_list_camera_temp;

-- Create catalog stuff
CREATE TABLE iris._catalog (
	name VARCHAR(20) PRIMARY KEY,
	seq_num INTEGER NOT NULL REFERENCES iris._cam_sequence
);

CREATE VIEW iris.catalog AS
	SELECT name, seq_num
	FROM iris._catalog;

CREATE FUNCTION iris.catalog_insert() RETURNS TRIGGER AS
	$catalog_insert$
BEGIN
	INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	INSERT INTO iris._catalog (name, seq_num) VALUES (NEW.name,NEW.seq_num);
	RETURN NEW;
END;
$catalog_insert$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_insert_trig
    INSTEAD OF INSERT ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_insert();

CREATE FUNCTION iris.catalog_update() RETURNS TRIGGER AS
	$catalog_update$
BEGIN
	IF NEW.seq_num != OLD.seq_num THEN
		INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
	END IF;
	UPDATE iris._catalog
	   SET seq_num = NEW.seq_num
	 WHERE name = OLD.name;
	IF NEW.seq_num != OLD.seq_num THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
	END IF;
	RETURN NEW;
END;
$catalog_update$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_update_trig
    INSTEAD OF UPDATE ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_update();

CREATE FUNCTION iris.catalog_delete() RETURNS TRIGGER AS
	$catalog_delete$
BEGIN
	DELETE FROM iris._catalog WHERE name = OLD.name;
	IF FOUND THEN
		DELETE FROM iris._cam_sequence WHERE seq_num = OLD.seq_num;
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$catalog_delete$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_delete_trig
    INSTEAD OF DELETE ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_delete();

CREATE TABLE iris.catalog_play_list (
	catalog VARCHAR(20) NOT NULL REFERENCES iris._catalog,
	ordinal INTEGER NOT NULL,
	play_list VARCHAR(20) NOT NULL REFERENCES iris._play_list
);
ALTER TABLE iris.catalog_play_list ADD PRIMARY KEY (catalog, ordinal);

-- Create new play_list_view
CREATE VIEW play_list_view AS
	SELECT play_list, ordinal, seq_num, camera
	FROM iris.play_list_camera
	JOIN iris.play_list ON play_list_camera.play_list = play_list.name;
GRANT SELECT ON play_list_view TO PUBLIC;

-- Add catalog to sonar_type table
INSERT INTO iris.sonar_type (name) VALUES ('catalog');

-- Add privileges for catalog
INSERT INTO iris.privilege (name, capability, type_n, obj_n, group_n, attr_n,
                            write)
	VALUES ('PRV_002D', 'camera_admin', 'catalog', '', '', '', true),
	       ('PRV_003E', 'camera_tab', 'catalog', '', '', '', false);

COMMIT;
