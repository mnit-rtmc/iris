\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.51.0', '5.52.0');

-- Improve security stuff
ALTER FUNCTION iris.multi_tags_str(INTEGER)
    SET search_path = pg_catalog, pg_temp;

-- Rename user resource type to user_id
INSERT INTO iris.resource_type (name) VALUES ('user_id');
UPDATE iris.permission SET resource_n = 'user_id' WHERE resource_n = 'user';
UPDATE iris.privilege SET type_n = 'user_id' WHERE type_n = 'user';
DELETE FROM iris.resource_type WHERE name = 'user';

-- Rename i_user to user_id
DROP VIEW i_user_view;

CREATE TABLE iris.user_id (
    name VARCHAR(15) PRIMARY KEY,
    full_name VARCHAR(31) NOT NULL,
    password VARCHAR(64) NOT NULL,
    dn VARCHAR(128) NOT NULL,
    role VARCHAR(15) REFERENCES iris.role,
    enabled BOOLEAN NOT NULL
);

INSERT INTO iris.user_id (name, full_name, password, dn, role, enabled) (
    SELECT name, full_name, password, dn, role, enabled
    FROM iris.i_user
);

CREATE TRIGGER user_id_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.user_id
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW user_id_view AS
    SELECT name, full_name, dn, role, enabled
    FROM iris.user_id;
GRANT SELECT ON user_id_view TO PUBLIC;

CREATE TABLE iris.user_id_domain (
    user_id VARCHAR(15) NOT NULL REFERENCES iris.user_id,
    domain VARCHAR(15) NOT NULL REFERENCES iris.domain
);
ALTER TABLE iris.user_id_domain ADD PRIMARY KEY (user_id, domain);

INSERT INTO iris.user_id_domain (user_id, domain) (
    SELECT i_user, domain
    FROM iris.i_user_domain
);

DROP TABLE iris.i_user_domain;
DROP TABLE iris.i_user;

-- Add camera_publish channel
CREATE OR REPLACE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
    $camera_notify$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) OR
       (NEW.cam_num IS DISTINCT FROM OLD.cam_num) OR
       (NEW.publish IS DISTINCT FROM OLD.publish)
    THEN
        NOTIFY camera;
    ELSE
        PERFORM pg_notify('camera', NEW.name);
    END IF;
    IF (NEW.publish IS DISTINCT FROM OLD.publish) THEN
        PERFORM pg_notify('camera_publish', NEW.name);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

-- Make device notes nullable
ALTER TABLE iris._camera ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._beacon ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._dms ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._gate_arm_array ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._gate_arm ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._lane_marking ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._ramp_meter ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._tag_reader ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._video_monitor ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._weather_sensor ALTER COLUMN notes DROP NOT NULL;

DROP VIEW lcs_array_view;
DROP VIEW iris.lcs_array;

ALTER TABLE iris._lcs_array ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris._lcs_array ALTER COLUMN notes TYPE VARCHAR(128);

CREATE VIEW iris.lcs_array AS
    SELECT la.name, controller, pin, notes, shift, lcs_lock
    FROM iris._lcs_array la JOIN iris.controller_io cio ON la.name = cio.name;

CREATE TRIGGER lcs_array_insert_trig
    INSTEAD OF INSERT ON iris.lcs_array
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_array_insert();

CREATE TRIGGER lcs_array_update_trig
    INSTEAD OF UPDATE ON iris.lcs_array
    FOR EACH ROW EXECUTE FUNCTION iris.lcs_array_update();

CREATE TRIGGER lcs_array_delete_trig
    INSTEAD OF DELETE ON iris.lcs_array
    FOR EACH ROW EXECUTE FUNCTION iris.controller_io_delete();

CREATE VIEW lcs_array_view AS
    SELECT name, shift, notes, lcs_lock
    FROM iris.lcs_array;
GRANT SELECT ON lcs_array_view TO PUBLIC;

-- Set blank device notes to NULL
UPDATE iris._camera SET notes = NULL WHERE notes = '';
UPDATE iris._beacon SET notes = NULL WHERE notes = '';
UPDATE iris._detector SET notes = NULL WHERE notes = '';
UPDATE iris._dms SET notes = NULL WHERE notes = '';
UPDATE iris._gate_arm_array SET notes = NULL WHERE notes = '';
UPDATE iris._gate_arm SET notes = NULL WHERE notes = '';
UPDATE iris._gps SET notes = NULL WHERE notes = '';
UPDATE iris._lane_marking SET notes = NULL WHERE notes = '';
UPDATE iris._lcs_array SET notes = NULL WHERE notes = '';
UPDATE iris._ramp_meter SET notes = NULL WHERE notes = '';
UPDATE iris._tag_reader SET notes = NULL WHERE notes = '';
UPDATE iris._video_monitor SET notes = NULL WHERE notes = '';
UPDATE iris._weather_sensor SET notes = NULL WHERE notes = '';

-- Make r_node notes nullable
DROP VIEW r_node_view;

ALTER TABLE iris.r_node ALTER COLUMN notes DROP NOT NULL;
ALTER TABLE iris.r_node ALTER COLUMN notes TYPE VARCHAR(160);
UPDATE iris.r_node SET notes = NULL WHERE notes = '';

CREATE VIEW r_node_view AS
    SELECT n.name, n.geo_loc,
           l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
           l.landmark, l.lat, l.lon, l.corridor, l.location,
           nt.name AS node_type, n.pickable, n.above, tr.name AS transition,
           n.lanes, n.attach_side, n.shift, n.active,
           n.station_id, n.speed_limit, n.notes
    FROM iris.r_node n
    JOIN geo_loc_view l ON n.geo_loc = l.name
    JOIN iris.r_node_type nt ON n.node_type = nt.n_type
    JOIN iris.r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

-- Make controller notes nullable
ALTER TABLE iris.controller ALTER COLUMN notes DROP NOT NULL;
UPDATE iris.controller SET notes = NULL WHERE notes = '';

COMMIT;
