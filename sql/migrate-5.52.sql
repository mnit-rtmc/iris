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

COMMIT;
