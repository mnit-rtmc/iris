\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.12.0', '5.13.0');

CREATE VIEW sign_group_text_view AS
	SELECT sign_group, line, multi, rank
	FROM iris.sign_group sg
	JOIN iris.sign_text st ON sg.name = st.sign_group;
GRANT SELECT ON sign_group_text_view TO PUBLIC;

CREATE OR REPLACE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
	$camera_notify$
BEGIN
	IF (NEW.publish IS DISTINCT FROM OLD.publish) THEN
		PERFORM pg_notify('camera', 'publish ' || NEW.name);
	ELSIF (NEW.video_loss IS DISTINCT FROM OLD.video_loss) THEN
		NOTIFY camera, 'video_loss';
	ELSE
		NOTIFY camera;
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

COMMIT;
