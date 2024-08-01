\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.58.0', '5.59.0');

-- Move parse_tags to iris schema
CREATE FUNCTION iris.parse_tags(notes TEXT) RETURNS SETOF TEXT AS
    $parse_tags$
BEGIN
    RETURN QUERY SELECT tag[1] FROM (
        SELECT regexp_matches(notes, '#([A-Za-z0-9]+)', 'g') AS tag
    ) AS tags;
END;
$parse_tags$ LANGUAGE plpgsql STABLE;

CREATE OR REPLACE FUNCTION iris.camera_hashtag() RETURNS TRIGGER AS
    $camera_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'camera' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'camera', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.beacon_hashtag() RETURNS TRIGGER AS
    $beacon_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'beacon' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'beacon', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.gps_hashtag() RETURNS TRIGGER AS
    $gps_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'gps' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'gps', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gps_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.dms_hashtag() RETURNS TRIGGER AS
    $dms_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'dms' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'dms', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.gate_arm_array_hashtag() RETURNS TRIGGER AS
    $gate_arm_array_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'gate_arm_array' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'gate_arm_array', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$gate_arm_array_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.ramp_meter_hashtag() RETURNS TRIGGER AS
    $ramp_meter_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'ramp_meter' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'ramp_meter', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$ramp_meter_hashtag$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION iris.weather_sensor_hashtag() RETURNS TRIGGER AS
    $weather_sensor_hashtag$
BEGIN
    IF (TG_OP != 'INSERT') THEN
        DELETE FROM iris.hashtag
        WHERE resource_n = 'weather_sensor' AND name = OLD.name;
    END IF;
    IF (TG_OP != 'DELETE') THEN
        INSERT INTO iris.hashtag (resource_n, name, hashtag)
        SELECT 'weather_sensor', NEW.name, iris.parse_tags(NEW.notes);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_hashtag$ LANGUAGE plpgsql;

DROP FUNCTION parse_tags(text);

COMMIT;
