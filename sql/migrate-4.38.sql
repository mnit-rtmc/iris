\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.38.0'
	WHERE name = 'database_version';

-- Add sonar_type lookup table
CREATE TABLE iris.sonar_type (
	name VARCHAR(16) PRIMARY KEY
);

COPY iris.sonar_type (name) FROM stdin;
action_plan
alarm
beacon
beacon_action
cabinet
cabinet_style
camera
camera_preset
capability
comm_link
connection
controller
day_plan
detector
dms
dms_action
dms_sign_group
font
gate_arm
gate_arm_array
geo_loc
glyph
graphic
holiday
inc_advice
inc_descriptor
incident
incident_detail
inc_locator
lane_action
lane_marking
lane_use_multi
lcs
lcs_array
lcs_indication
map_extent
meter_action
modem
plan_phase
privilege
quick_message
ramp_meter
r_node
road
role
sign_group
sign_message
sign_text
station
system_attribute
tag_reader
time_action
toll_zone
user
video_monitor
weather_sensor
word
\.

-- Add type_n column to privilege table
ALTER TABLE iris.privilege ADD COLUMN type_n VARCHAR(16);
UPDATE iris.privilege
   SET type_n = split_part(split_part(pattern, '(', 1), '/', 1);
ALTER TABLE iris.privilege ALTER COLUMN type_n SET NOT NULL;
ALTER TABLE iris.privilege ADD CONSTRAINT _sonar_type_fkey
	FOREIGN KEY (type_n) REFERENCES iris.sonar_type;

-- Add obj_n column to privilege table
ALTER TABLE iris.privilege ADD COLUMN obj_n VARCHAR(16);
UPDATE iris.privilege SET obj_n = split_part(pattern, '/', 2);
UPDATE iris.privilege SET obj_n = '' WHERE obj_n = '.*' OR obj_n = '.*)?';
ALTER TABLE iris.privilege ALTER COLUMN obj_n SET NOT NULL;

-- Add attr_n column to privilege table
ALTER TABLE iris.privilege ADD COLUMN attr_n VARCHAR(16);
UPDATE iris.privilege SET attr_n = split_part(pattern, '/', 3);
ALTER TABLE iris.privilege ALTER COLUMN attr_n SET NOT NULL;

-- Add write column to privilege table
ALTER TABLE iris.privilege ADD COLUMN write BOOLEAN;
UPDATE iris.privilege SET write = priv_w OR priv_c OR priv_d;
ALTER TABLE iris.privilege ALTER COLUMN write SET NOT NULL;

-- Drop old columns from privilege table
ALTER TABLE iris.privilege DROP COLUMN pattern;
ALTER TABLE iris.privilege DROP COLUMN priv_r;
ALTER TABLE iris.privilege DROP COLUMN priv_w;
ALTER TABLE iris.privilege DROP COLUMN priv_c;
ALTER TABLE iris.privilege DROP COLUMN priv_d;

-- Create role_privilege_view
CREATE VIEW role_privilege_view AS
	SELECT role, type_n, obj_n, attr_n, write
	FROM iris.role
	JOIN iris.role_capability ON role.name = role_capability.role
	JOIN iris.capability ON role_capability.capability = capability.name
	JOIN iris.privilege ON privilege.capability = role_capability.capability
	WHERE role.enabled = 't' AND capability.enabled = 't';
GRANT SELECT ON role_privilege_view TO PUBLIC;

-- rename tesla_host sys attr to work_request_url
UPDATE iris.system_attribute SET name = 'work_request_url'
	WHERE name = 'tesla_host';

-- Add dms_update_font_table system attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('dms_update_font_table', true);
