\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.8.0', '5.9.0');

-- Change Junction to Jct in road_modifier table
UPDATE iris.road_modifier SET modifier = 'N Jct' WHERE id = 5;
UPDATE iris.road_modifier SET modifier = 'S Jct' WHERE id = 6;
UPDATE iris.road_modifier SET modifier = 'E Jct' WHERE id = 7;
UPDATE iris.road_modifier SET modifier = 'W Jct' WHERE id = 8;

CREATE OR REPLACE FUNCTION iris.geo_location(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT)
	RETURNS TEXT AS $geo_location$
DECLARE
	roadway ALIAS FOR $1;
	road_dir ALIAS FOR $2;
	cross_mod ALIAS FOR $3;
	cross_street ALIAS FOR $4;
	cross_dir ALIAS FOR $5;
	landmark ALIAS FOR $6;
	corridor TEXT;
	xloc TEXT;
	lmrk TEXT;
BEGIN
	corridor = trim(roadway || concat(' ', road_dir));
	xloc = trim(concat(cross_mod, ' ') || cross_street
	    || concat(' ', cross_dir));
	lmrk = replace('(' || landmark || ')', '()', '');
	RETURN NULLIF(trim(concat(corridor, ' ' || xloc, ' ' || lmrk)), '');
END;
$geo_location$ LANGUAGE plpgsql;

-- Add hidden column to dms table / views
ALTER TABLE iris._dms ADD COLUMN hidden BOOLEAN;
UPDATE iris._dms SET hidden = false;
ALTER TABLE iris._dms ALTER COLUMN hidden SET NOT NULL;

DROP VIEW dms_message_view;
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, gps, static_graphic,
	       purpose, hidden, beacon, preset, sign_config, sign_detail,
	       override_font, override_foreground, override_background,
	       msg_sched, msg_current, expire_time
	FROM iris._dms dms
	JOIN iris._device_io d ON dms.name = d.name
	JOIN iris._device_preset p ON dms.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
	$dms_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._device_preset (name, preset)
	     VALUES (NEW.name, NEW.preset);
	INSERT INTO iris._dms (name, geo_loc, notes, gps, static_graphic,
	                       purpose, hidden, beacon, sign_config, sign_detail,
	                       override_font, override_foreground,
	                       override_background, msg_sched, msg_current,
	                       expire_time)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.gps,
	             NEW.static_graphic, NEW.purpose, NEW.hidden, NEW.beacon,
	             NEW.sign_config, NEW.sign_detail, NEW.override_font,
	             NEW.override_foreground, NEW.override_background,
	             NEW.msg_sched, NEW.msg_current, NEW.expire_time);
	RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
	$dms_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._device_preset
	   SET preset = NEW.preset
	 WHERE name = OLD.name;
	UPDATE iris._dms
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       gps = NEW.gps,
	       static_graphic = NEW.static_graphic,
	       purpose = NEW.purpose,
	       hidden = NEW.hidden,
	       beacon = NEW.beacon,
	       sign_config = NEW.sign_config,
	       sign_detail = NEW.sign_detail,
	       override_font = NEW.override_font,
	       override_foreground = NEW.override_foreground,
	       override_background = NEW.override_background,
	       msg_sched = NEW.msg_sched,
	       msg_current = NEW.msg_current,
	       expire_time = NEW.expire_time
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE FUNCTION iris.dms_delete() RETURNS TRIGGER AS
	$dms_delete$
BEGIN
	DELETE FROM iris._device_preset WHERE name = OLD.name;
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$dms_delete$ LANGUAGE plpgsql;

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_delete();

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.gps,
	       d.static_graphic, dp.description AS purpose, d.hidden, d.beacon,
	       p.camera, p.preset_num, d.sign_config, d.sign_detail,
	       default_font, override_font, override_foreground,
	       override_background, msg_sched, msg_current, expire_time,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN iris.device_purpose dp ON d.purpose = dp.id
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW dms_message_view AS
	SELECT d.name, msg_current, cc.description AS condition, multi,
	       beacon_enabled, prefix_page, msg_priority,
	       iris.sign_msg_sources(source) AS sources, duration, expire_time
	FROM iris.dms d
	LEFT JOIN iris.controller c ON d.controller = c.name
	LEFT JOIN iris.condition cc ON c.condition = cc.id
	LEFT JOIN iris.sign_message s ON d.msg_current = s.name;
GRANT SELECT ON dms_message_view TO PUBLIC;

-- Remove hidden from sign_group
DROP VIEW dms_sign_group_view;
DROP VIEW sign_group_view;

ALTER TABLE iris.sign_group DROP COLUMN hidden;

CREATE VIEW sign_group_view AS
	SELECT name, local
	FROM iris.sign_group;
GRANT SELECT ON sign_group_view TO PUBLIC;

CREATE VIEW dms_sign_group_view AS
	SELECT d.name, dms, sign_group, local
	FROM iris.dms_sign_group d
	JOIN iris.sign_group sg ON d.sign_group = sg.name;
GRANT SELECT ON dms_sign_group_view TO PUBLIC;

-- Add encoder_stream sonar_type
INSERT INTO iris.sonar_type (name) VALUES ('encoder_stream');

-- Add privileges for encoder_stream
INSERT INTO iris.privilege (name, capability, type_n, write)
	VALUES ('PRV_es0', 'camera_admin', 'encoder_stream', true),
	       ('PRV_es1', 'camera_tab', 'encoder_stream', false),
	       ('PRV_es2', 'gate_arm_tab', 'encoder_stream', false);

-- Add AV1 encoding
INSERT INTO iris.encoding (id, description) VALUES (6, 'AV1');

-- Add encoding_quality LUT
CREATE TABLE iris.encoding_quality (
	id INTEGER PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

COPY iris.encoding_quality (id, description) FROM stdin;
0	Low
1	Medium
2	High
\.

-- Set unused encoder channels to 0
WITH cte AS (
	SELECT name, uri_path
	FROM iris.encoder_type
)
UPDATE iris.camera SET encoder_channel = 0
  FROM cte
 WHERE cte.name = encoder_type AND cte.uri_path NOT LIKE '%{chan}%';

DROP VIEW camera_view;
DROP VIEW iris.camera;
DROP FUNCTION iris.camera_insert();
DROP FUNCTION iris.camera_update();
DROP FUNCTION iris.camera_delete();
DROP VIEW encoder_type_view;

-- Add make, model and config to encoder_type
ALTER TABLE iris.encoder_type ADD COLUMN make VARCHAR(16);
ALTER TABLE iris.encoder_type ADD COLUMN model VARCHAR(16);
ALTER TABLE iris.encoder_type ADD COLUMN config VARCHAR(8);

UPDATE iris.encoder_type
	SET make = split_part(name, ' ', 1),
	    model = trim(split_part(name, ' ', 2) || ' ' ||
	                 split_part(name, ' ', 3)),
	    config = split_part(name, ' ', 4);

ALTER TABLE iris.encoder_type ALTER COLUMN make SET NOT NULL;
ALTER TABLE iris.encoder_type ALTER COLUMN model SET NOT NULL;
ALTER TABLE iris.encoder_type ALTER COLUMN config SET NOT NULL;

ALTER TABLE ONLY iris.encoder_type
    ADD CONSTRAINT encoder_type_make_model_config_key UNIQUE (make, model, config);

-- Add temporary column for new pkey
ALTER TABLE iris.encoder_type ADD COLUMN pkey VARCHAR(8);

WITH cte AS (
	SELECT name, ROW_NUMBER() OVER (ORDER BY name) AS rn
	FROM iris.encoder_type
)
UPDATE iris.encoder_type SET pkey = 'etp_' || (
	SELECT LPAD(CAST(rn AS TEXT), 4, '0')
	FROM cte
	WHERE cte.name = iris.encoder_type.name
);

ALTER TABLE iris.encoder_type ALTER COLUMN pkey SET NOT NULL;

ALTER TABLE iris._camera DROP CONSTRAINT _camera_encoder_type_fkey;

-- Update camera encoder_types to new pkey
WITH cte AS (SELECT name, pkey FROM iris.encoder_type)
UPDATE iris._camera SET encoder_type = (
	SELECT pkey
	FROM cte
	WHERE encoder_type = cte.name
);
ALTER TABLE iris._camera ALTER COLUMN encoder_type TYPE VARCHAR(8);

-- Replace name with temporary pkey
UPDATE iris.encoder_type SET name = pkey;
ALTER TABLE iris.encoder_type DROP COLUMN pkey;
ALTER TABLE iris.encoder_type ALTER COLUMN name TYPE VARCHAR(8);

-- Restore foreign key constraint on camera encoder_type
ALTER TABLE iris._camera
    ADD CONSTRAINT _camera_encoder_type_fkey FOREIGN KEY (encoder_type) REFERENCES iris.encoder_type(name);

-- Add encoder_stream table
CREATE TABLE iris.encoder_stream (
	name VARCHAR(8) PRIMARY KEY,
	encoder_type VARCHAR(8) NOT NULL REFERENCES iris.encoder_type,
	view_num INTEGER CHECK (view_num > 0 AND view_num <= 12),
	encoding INTEGER NOT NULL REFERENCES iris.encoding,
	quality INTEGER NOT NULL REFERENCES iris.encoding_quality,
	uri_scheme VARCHAR(8),
	uri_path VARCHAR(64),
	mcast_port INTEGER CHECK (mcast_port > 0 AND mcast_port <= 65535),
	latency INTEGER NOT NULL,
	UNIQUE(encoder_type, mcast_port)
);

INSERT INTO iris.encoder_stream (
	SELECT 'est_' || LPAD(CAST(ROW_NUMBER() OVER (ORDER BY name) AS TEXT), 4, '0'),
	        name, NULL, encoding, 1, uri_scheme, uri_path, NULL, latency
	FROM iris.encoder_type
);

ALTER TABLE iris.encoder_stream
	ADD CONSTRAINT unicast_or_multicast_ck
	CHECK ((uri_scheme IS NULL AND uri_path IS NULL) OR mcast_port IS NULL);

-- DROP columns we moved to encoder_stream
ALTER TABLE iris.encoder_type DROP COLUMN encoding;
ALTER TABLE iris.encoder_type DROP COLUMN uri_scheme;
ALTER TABLE iris.encoder_type DROP COLUMN uri_path;
ALTER TABLE iris.encoder_type DROP COLUMN latency;

CREATE VIEW encoder_stream_view AS
	SELECT es.name, encoder_type, make, model, config, view_num,
	       enc.description AS encoding, eq.description AS quality,
	       uri_scheme, uri_path, mcast_port, latency
	FROM iris.encoder_stream es
	LEFT JOIN iris.encoder_type et ON es.encoder_type = et.name
	LEFT JOIN iris.encoding enc ON es.encoding = enc.id
	LEFT JOIN iris.encoding_quality eq ON es.quality = eq.id;
GRANT SELECT ON encoder_stream_view TO PUBLIC;

-- Alter columns of _camera table
ALTER TABLE iris._camera ALTER COLUMN notes TYPE VARCHAR(256);
ALTER TABLE iris._camera ADD COLUMN enc_address INET;
UPDATE iris._camera
   SET enc_address = CAST(NULLIF(trim(split_part(encoder, ':', 1)), '') AS INET);
ALTER TABLE iris._camera ADD COLUMN enc_port INTEGER
	CHECK (enc_port > 0 AND enc_port <= 65535);
UPDATE iris._camera
   SET enc_port = CAST(NULLIF(split_part(encoder, ':', 2), '') AS INTEGER);
ALTER TABLE iris._camera DROP COLUMN encoder;
ALTER TABLE iris._camera RENAME COLUMN enc_mcast TO tmp_mcast;
ALTER TABLE iris._camera ADD COLUMN enc_mcast INET;
UPDATE iris._camera
   SET enc_mcast = CAST(NULLIF(split_part(tmp_mcast, ':', 1), '') AS INET);
ALTER TABLE iris._camera DROP COLUMN tmp_mcast;
ALTER TABLE iris._camera RENAME COLUMN encoder_channel TO enc_channel;
ALTER TABLE iris._camera ALTER COLUMN enc_channel DROP NOT NULL;

-- Set all 0 channels to NULL
UPDATE iris._camera SET enc_channel = NULL WHERE enc_channel = 0;

-- Add check constraint on enc_channel
ALTER TABLE iris._camera
	ADD CONSTRAINT _camera_enc_channel_check
	CHECK (enc_channel > 0 AND enc_channel <= 16);

-- Add camera views and trigger functions
CREATE VIEW iris.camera AS
	SELECT c.name, geo_loc, controller, pin, notes, cam_num, encoder_type,
	       enc_address, enc_port, enc_mcast, enc_channel, publish,
	       video_loss
	FROM iris._camera c
	JOIN iris._device_io d ON c.name = d.name;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, cam_num, encoder_type,
	            enc_address, enc_port, enc_mcast, enc_channel, publish,
	            video_loss)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.cam_num,
	             NEW.encoder_type, NEW.enc_address, NEW.enc_port,
	             NEW.enc_mcast, NEW.enc_channel, NEW.publish,
	             NEW.video_loss);
	RETURN NEW;
END;
$camera_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_insert();

CREATE FUNCTION iris.camera_update() RETURNS TRIGGER AS
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
	       video_loss = NEW.video_loss
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE FUNCTION iris.camera_delete() RETURNS TRIGGER AS
	$camera_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$camera_delete$ LANGUAGE plpgsql;

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_delete();

CREATE VIEW camera_view AS
	SELECT c.name, cam_num, encoder_type, et.make, et.model, et.config,
	       c.enc_address, c.enc_port, c.enc_mcast, c.enc_channel, c.publish,
	       c.video_loss, c.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.landmark, l.lat, l.lon, l.corridor, l.location,
	       c.controller, ctr.comm_link, ctr.drop_id, ctr.condition, c.notes
	FROM iris.camera c
	LEFT JOIN iris.encoder_type et ON c.encoder_type = et.name
	LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller_view ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

-- Add NOTIFY for camera publish
CREATE OR REPLACE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
	$camera_notify$
BEGIN
	IF (NEW.publish IS DISTINCT FROM OLD.publish) THEN
		NOTIFY camera, 'publish';
	ELSIF (NEW.video_loss IS DISTINCT FROM OLD.video_loss) THEN
		NOTIFY camera, 'video_loss';
	ELSE
		NOTIFY camera;
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

COMMIT;
