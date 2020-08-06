\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.14.0', '5.15.0');

-- Add station_id column to travel_time_event table
ALTER TABLE event.travel_time_event ADD COLUMN station_id VARCHAR(10);

DROP VIEW travel_time_event_view;
CREATE VIEW travel_time_event_view AS
	SELECT event_id, event_date, event_description.description, device_id,
	       station_id
	FROM event.travel_time_event
	JOIN event.event_description
	ON travel_time_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON travel_time_event_view TO PUBLIC;

-- Add client video system attributes
INSERT INTO iris.system_attribute (name, value)
VALUES ('vid_connect_autostart', true),
	('vid_connect_fail_next_source', true),
	('vid_connect_fail_sec', 20),
	('vid_lost_timeout_sec', 10),
	('vid_reconnect_auto', true),
	('vid_reconnect_timeout_sec', 10),
	('gstreamer_version_windows','1.16.2');

-- Drop old views
DROP VIEW camera_view;
DROP VIEW iris.camera;

-- Add camera template
CREATE TABLE iris.camera_template (
	name VARCHAR(20) PRIMARY KEY,
	notes text,
	label text
);

-- Add video source template
CREATE TABLE iris.vid_src_template (
	name VARCHAR(20) PRIMARY KEY,
	label text,
	config text,
	default_port INTEGER,
	subnets text,
	latency INTEGER,
	encoder VARCHAR(64),
	scheme text,
	codec text,
	rez_width INTEGER,
	rez_height INTEGER,
	multicast BOOLEAN,
	notes text
);

-- Add camera video source ord
CREATE TABLE iris.cam_vid_src_ord (
	name VARCHAR(24) PRIMARY KEY,
	camera_template VARCHAR(20) REFERENCES iris.camera_template,
	src_order INTEGER,
	src_template VARCHAR(20) REFERENCES iris.vid_src_template
);

ALTER TABLE iris._camera ADD COLUMN cam_template VARCHAR(20);
ALTER TABLE iris._camera ADD CONSTRAINT _camera_cam_template_fkey
	FOREIGN KEY (cam_template) REFERENCES iris.camera_template;

CREATE VIEW iris.camera AS
	SELECT c.name, geo_loc, controller, pin, notes, cam_num, cam_template,
	       encoder_type, enc_address, enc_port, enc_mcast, enc_channel,
	       publish, streamable, video_loss
	FROM iris._camera c
	JOIN iris._device_io d ON c.name = d.name;

CREATE OR REPLACE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, cam_num, cam_template,
	            encoder_type, enc_address, enc_port, enc_mcast, enc_channel,
	            publish, streamable, video_loss)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num,
	             NEW.cam_template, NEW.encoder_type, NEW.enc_address,
	             NEW.enc_port, NEW.enc_mcast, NEW.enc_channel, NEW.publish,
	             NEW.streamable, NEW.video_loss);
	RETURN NEW;
END;
$camera_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_insert();

CREATE OR REPLACE FUNCTION iris.camera_update() RETURNS TRIGGER AS
	$camera_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._camera
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       cam_num = NEW.cam_num,
	       cam_template = NEW.cam_template,
	       encoder_type = NEW.encoder_type,
	       enc_address = NEW.enc_address,
	       enc_port = NEW.enc_port,
	       enc_mcast = NEW.enc_mcast,
	       enc_channel = NEW.enc_channel,
	       publish = NEW.publish,
	       streamable = NEW.streamable,
	       video_loss = NEW.video_loss
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

CREATE VIEW camera_view AS
	SELECT c.name, cam_num, c.cam_template, encoder_type, et.make, et.model,
	       et.config, c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel,
	       c.publish, c.streamable, c.video_loss, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
	FROM iris.camera c
	LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

INSERT INTO iris.sonar_type (name) VALUES ('camera_template');
INSERT INTO iris.sonar_type (name) VALUES ('vid_src_template');
INSERT INTO iris.sonar_type (name) VALUES ('cam_vid_src_ord');

INSERT INTO iris.privilege (name, capability, type_n, obj_n, attr_n, group_n, write)
VALUES ('PRV_003F', 'camera_tab', 'camera_template', '', '', '', false),
       ('PRV_003G', 'camera_tab', 'cam_vid_src_ord', '', '', '', false),
       ('PRV_003H', 'camera_tab', 'vid_src_template', '', '', '', false),
       ('PRV_004A', 'camera_admin', 'camera_template', '', '', '', true),
       ('PRV_004B', 'camera_admin', 'cam_vid_src_ord', '', '', '', true),
       ('PRV_004C', 'camera_admin', 'vid_src_template', '', '', '', true);

COMMIT;
