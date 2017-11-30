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
	       ('PRV_003C', 'camera_tab', 'play_list', '', '', '', false);

COMMIT;
