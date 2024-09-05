\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add video monitor hashtag trigger
CREATE FUNCTION iris.video_monitor_hashtag() RETURNS TRIGGER AS
    $video_monitor_hashtag$
BEGIN
    IF (NEW.notes IS DISTINCT FROM OLD.notes) THEN
        IF (TG_OP != 'INSERT') THEN
            DELETE FROM iris.hashtag
            WHERE resource_n = 'video_monitor' AND name = OLD.name;
        END IF;
        IF (TG_OP != 'DELETE') THEN
            INSERT INTO iris.hashtag (resource_n, name, hashtag)
            SELECT 'video_monitor', NEW.name, iris.parse_tags(NEW.notes);
        END IF;
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$video_monitor_hashtag$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._video_monitor
    FOR EACH ROW EXECUTE FUNCTION iris.video_monitor_hashtag();

-- Force hashtags to be created by trigger
UPDATE iris.video_monitor SET notes = notes || ' ';
UPDATE iris.video_monitor SET notes = trim(notes);

COMMIT;
