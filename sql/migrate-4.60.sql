\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.59.0', '4.60.0');

-- delete camera_blank_url system attribute
DELETE FROM iris.system_attribute WHERE name = 'camera_blank_url';

-- Add title_bar column to monitor_style table
ALTER TABLE iris.monitor_style ADD COLUMN title_bar BOOLEAN;
UPDATE iris.monitor_style SET title_bar = 't';
ALTER TABLE iris.monitor_style ALTER COLUMN title_bar SET NOT NULL;

-- Update monitor_style_view
DROP VIEW monitor_style_view;
CREATE VIEW monitor_style_view AS
	SELECT name, force_aspect, accent, font_sz, title_bar
	FROM iris.monitor_style;
GRANT SELECT ON monitor_style_view TO PUBLIC;

-- Add primary keys to relation tables
ALTER TABLE iris.role_capability ADD PRIMARY KEY (role, capability);
ALTER TABLE iris.day_plan_day_matcher ADD PRIMARY KEY (day_plan, day_matcher);
ALTER TABLE iris.tag_reader_dms ADD PRIMARY KEY (tag_reader, dms);

-- Add play list tables
CREATE TABLE iris.play_list (
	name VARCHAR(20) PRIMARY KEY,
	num INTEGER UNIQUE
);

CREATE TABLE iris.play_list_camera (
	play_list VARCHAR(20) NOT NULL REFERENCES iris.play_list,
	ordinal INTEGER NOT NULL,
	camera VARCHAR(20) NOT NULL REFERENCES iris._camera
);
ALTER TABLE iris.play_list_camera ADD PRIMARY KEY (play_list, ordinal);

CREATE VIEW play_list_view AS
	SELECT play_list, ordinal, num, camera
	FROM iris.play_list_camera
	JOIN iris.play_list ON play_list_camera.play_list = play_list.name;
GRANT SELECT ON play_list_view TO PUBLIC;

-- Add play_list to sonar_type table
INSERT INTO iris.sonar_type (name) VALUES ('play_list');

-- Add privileges for play_list
INSERT INTO iris.privilege (name, capability, type_n, obj_n, group_n, attr_n,
                            write)
	VALUES ('PRV_002C', 'camera_admin', 'play_list', '', '', '', true),
	       ('PRV_003C', 'camera_tab', 'play_list', '', '', '', false),
	       ('PRV_003D', 'camera_tab', 'play_list', '', 'user', '', true);

-- Remove direct column from video monitor table
DROP VIEW video_monitor_view;
DROP VIEW iris.video_monitor;

DROP FUNCTION iris.video_monitor_insert();
DROP FUNCTION iris.video_monitor_update();
DROP FUNCTION iris.video_monitor_delete();

ALTER TABLE iris._video_monitor DROP COLUMN direct;

CREATE VIEW iris.video_monitor AS SELECT
	m.name, controller, pin, notes, mon_num, restricted,
	monitor_style, camera
	FROM iris._video_monitor m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
	$video_monitor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._video_monitor (name, notes, mon_num, restricted,
	                                 monitor_style, camera)
	     VALUES (NEW.name, NEW.notes, NEW.mon_num, NEW.restricted,
	             NEW.monitor_style, NEW.camera);
	RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_insert();

CREATE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
	$video_monitor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._video_monitor
	   SET notes = NEW.notes,
	       mon_num = NEW.mon_num,
	       restricted = NEW.restricted,
	       monitor_style = NEW.monitor_style,
	       camera = NEW.camera
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_update();

CREATE FUNCTION iris.video_monitor_delete() RETURNS TRIGGER AS
	$video_monitor_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$video_monitor_delete$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_delete();

CREATE VIEW video_monitor_view AS
	SELECT m.name, m.notes, mon_num, restricted, monitor_style,
	       m.controller, m.pin, ctr.condition, ctr.comm_link, camera
	FROM iris.video_monitor m
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;

COMMIT;
