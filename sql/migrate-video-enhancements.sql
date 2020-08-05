INSERT INTO iris.system_attribute (name, value)
VALUES ('vid_connect_autostart', true),
		('vid_connect_fail_next_source', true),
		('vid_connect_fail_sec', 20),
		('vid_lost_timeout_sec', 10),
		('vid_reconnect_auto', true),
		('vid_reconnect_timeout_sec', 10),
		('gstreamer_version_windows','1.16.2');

CREATE TABLE iris._camera_template
(
	name character varying(20),
	notes text,
	label text,
	CONSTRAINT camera_template_pkey PRIMARY KEY (name)
);

ALTER TABLE iris._camera_template
    OWNER to tms;

CREATE OR REPLACE VIEW iris.camera_template
 AS
 SELECT name,
		notes,
		label
   FROM iris._camera_template;

ALTER TABLE iris.camera_template
    OWNER TO tms;

CREATE TABLE iris._vid_src_template
(
	name character varying(20),
	label text,
	config text,
	default_port integer,
	subnets text,
	latency integer,
	encoder character varying(64),
	scheme text,
	codec text,
	rez_width integer,
	rez_height integer,
	multicast boolean,
	notes text,
	CONSTRAINT vid_src_template_pkey PRIMARY KEY (name)
);

ALTER TABLE iris._vid_src_template
    OWNER to tms;

CREATE OR REPLACE VIEW iris.vid_src_template
 AS
 SELECT name,
		label,
		config,
		default_port,
		subnets,
		latency,
		encoder,
		scheme,
		codec,
		rez_width,
		rez_height,
		multicast,
		notes
   FROM iris._vid_src_template;

ALTER TABLE iris.vid_src_template
    OWNER TO tms;

ALTER TABLE iris._camera ADD COLUMN cam_template character varying(20);
 
ALTER TABLE iris._camera ADD CONSTRAINT _camera_cam_template_fkey FOREIGN KEY (cam_template)
        REFERENCES iris._camera_template (name) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

DROP VIEW public.camera_view;
DROP VIEW iris.camera;

CREATE OR REPLACE VIEW iris.camera
 AS
 SELECT c.name,
    c.geo_loc,
    d.controller,
    d.pin,
    c.notes,
    c.cam_num,
    c.encoder_type,
    c.enc_address,
    c.enc_port,
    c.enc_mcast,
    c.enc_channel,
    c.publish,
	c.streamable,
	c.cam_template,
    c.video_loss
   FROM iris._camera c
     JOIN iris._device_io d ON c.name::text = d.name::text;

ALTER VIEW iris.camera
    OWNER TO tms;
	
CREATE VIEW public.camera_view AS
	SELECT c.name, cam_num, encoder_type, et.make, et.model, et.config,
	       c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel, c.publish,
	       c.streamable, c.cam_template, c.video_loss, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
	FROM iris.camera c
	LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON public.camera_view TO PUBLIC;

ALTER VIEW public.camera_view
    OWNER TO tms;

CREATE OR REPLACE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, cam_num, encoder_type,
	            enc_address, enc_port, enc_mcast, enc_channel, publish,
	            streamable, cam_template, video_loss)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num,
	             NEW.encoder_type, NEW.enc_address, NEW.enc_port,
	             NEW.enc_mcast, NEW.enc_channel, NEW.publish,
	             NEW.streamable, NEW.cam_template, NEW.video_loss);
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
	       encoder_type = NEW.encoder_type,
	       enc_address = NEW.enc_address,
	       enc_port = NEW.enc_port,
	       enc_mcast = NEW.enc_mcast,
	       enc_channel = NEW.enc_channel,
	       publish = NEW.publish,
	       streamable = NEW.streamable,
	       cam_template = NEW.cam_template,
	       video_loss = NEW.video_loss
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE TABLE iris._cam_vid_src_ord
(
	name character varying(24),
	camera_template character varying(20),
	src_order integer,
	src_template character varying(20),
	CONSTRAINT camera_vid_src_order_pkey PRIMARY KEY (name)
);

ALTER TABLE iris._cam_vid_src_ord
    OWNER to tms;	
	
CREATE OR REPLACE VIEW iris.cam_vid_src_ord
 AS
 SELECT name,
		camera_template,
		src_order,
		src_template
   FROM iris._cam_vid_src_ord;

ALTER TABLE iris.cam_vid_src_ord
    OWNER TO tms;

ALTER TABLE iris._cam_vid_src_ord ADD CONSTRAINT cam_vid_src_ord_camera_template_fkey FOREIGN KEY (camera_template)
        REFERENCES iris._camera_template (name) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;
		
ALTER TABLE iris._cam_vid_src_ord ADD CONSTRAINT cam_vid_src_ord_src_template_fkey FOREIGN KEY (src_template)
       REFERENCES iris._vid_src_template (name) MATCH SIMPLE
       ON UPDATE NO ACTION
       ON DELETE NO ACTION;

INSERT INTO iris.sonar_type(name) VALUES ('cam_vid_src_ord');
INSERT INTO iris.sonar_type(name) VALUES ('camera_template');
INSERT INTO iris.sonar_type(name) VALUES ('vid_src_template');

INSERT INTO iris.privilege (name,capability,type_n,obj_n,attr_n,group_n,write) VALUES
						   ('PRV_003F','camera_tab','camera_template','','','',false),
						   ('PRV_003G','camera_tab','cam_vid_src_ord','','','',false),
						   ('PRV_003H','camera_tab','vid_src_template','','','',false),
						   ('PRV_004A','camera_admin','camera_template','','','',true),
						   ('PRV_004B','camera_admin','cam_vid_src_ord','','','',true),
						   ('PRV_004C','camera_admin','vid_src_template','','','',true);
