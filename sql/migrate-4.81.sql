\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.80.0', '4.81.0');

-- Add GPS view
CREATE VIEW gps_view AS
	SELECT name, controller, pin, notes, latest_poll, latest_sample,
	       lat, lon
	FROM iris.gps;

-- Add notify_tag to geo_loc
ALTER TABLE iris.geo_loc ADD COLUMN notify_tag VARCHAR(20);

-- Add existing notify_tag values
UPDATE iris.geo_loc SET notify_tag = 'camera'
	WHERE name IN (SELECT geo_loc FROM iris._camera);
UPDATE iris.geo_loc SET notify_tag = 'dms'
	WHERE name IN (SELECT geo_loc FROM iris._dms);
UPDATE iris.geo_loc SET notify_tag = 'parking_area'
	WHERE name IN (SELECT geo_loc FROM iris.parking_area);

-- Add geo_loc_notify trigger
CREATE FUNCTION iris.geo_loc_notify() RETURNS TRIGGER AS
	$geo_loc_notify$
BEGIN
	IF (TG_OP = 'DELETE') THEN
		IF (OLD.notify_tag IS NOT NULL) THEN
			PERFORM pg_notify('tms', OLD.notify_tag);
		END IF;
	ELSIF (NEW.notify_tag IS NOT NULL) THEN
		PERFORM pg_notify('tms', NEW.notify_tag);
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$geo_loc_notify$ LANGUAGE plpgsql;

CREATE TRIGGER geo_loc_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.geo_loc
	FOR EACH ROW EXECUTE PROCEDURE iris.geo_loc_notify();

-- Add camera_notify trigger
CREATE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
	$camera_notify$
BEGIN
	IF (NEW.publish IS DISTINCT FROM OLD.publish) THEN
		NOTIFY tms, 'camera';
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

CREATE TRIGGER camera_notify_trig
    AFTER UPDATE ON iris._camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_notify();

-- Add font_notify trigger
CREATE FUNCTION iris.font_notify() RETURNS TRIGGER AS
	$font_notify$
BEGIN
	NOTIFY tms, 'font';
	RETURN NULL; -- AFTER trigger return is ignored
END;
$font_notify$ LANGUAGE plpgsql;

CREATE TRIGGER font_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.font
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.font_notify();

CREATE TRIGGER glyph_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.glyph
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.font_notify();

-- Add sign_config_notify trigger
CREATE FUNCTION iris.sign_config_notify() RETURNS TRIGGER AS
	$sign_config_notify$
BEGIN
	NOTIFY tms, 'sign_config';
	RETURN NULL; -- AFTER trigger return is ignored
END;
$sign_config_notify$ LANGUAGE plpgsql;

CREATE TRIGGER sign_config_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.sign_config
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.sign_config_notify();

-- Add sign_message_notify trigger
CREATE FUNCTION iris.sign_message_notify() RETURNS TRIGGER AS
	$sign_message_notify$
BEGIN
	NOTIFY tms, 'sign_message';
	RETURN NULL; -- AFTER trigger return is ignored
END;
$sign_message_notify$ LANGUAGE plpgsql;

CREATE TRIGGER sign_message_notify_trig
    AFTER INSERT OR DELETE ON iris.sign_message
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.sign_message_notify();

-- Add dms_notify trigger
CREATE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
	$dms_notify$
BEGIN
	IF (NEW.sign_config IS DISTINCT FROM OLD.sign_config) THEN
		NOTIFY tms, 'dms';
	END IF;
	IF (NEW.msg_current IS DISTINCT FROM OLD.msg_current) THEN
		NOTIFY tms, 'dms_message';
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE TRIGGER dms_notify_trig
    AFTER UPDATE ON iris._dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_notify();

-- Add incident_notify trigger
CREATE FUNCTION iris.incident_notify() RETURNS TRIGGER AS
	$incident_notify$
BEGIN
	NOTIFY tms, 'incident';
	RETURN NULL; -- AFTER trigger return is ignored
END;
$incident_notify$ LANGUAGE plpgsql;

CREATE TRIGGER incident_notify_trig
    AFTER INSERT OR UPDATE ON event.incident
    FOR EACH ROW EXECUTE PROCEDURE iris.incident_notify();

-- Add parking_area_notify trigger
CREATE FUNCTION iris.parking_area_notify() RETURNS TRIGGER AS
	$parking_area_notify$
BEGIN
	IF (NEW.time_stamp_static IS DISTINCT FROM OLD.time_stamp_static) THEN
		NOTIFY tms, 'parking_area';
	END IF;
	IF (NEW.time_stamp IS DISTINCT FROM OLD.time_stamp) THEN
		NOTIFY tms, 'parking_area_dynamic';
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$parking_area_notify$ LANGUAGE plpgsql;

CREATE TRIGGER parking_area_trig
	AFTER UPDATE ON iris.parking_area
	FOR EACH ROW EXECUTE PROCEDURE iris.parking_area_notify();

COMMIT;
