\set ON_ERROR_STOP

BEGIN;

ALTER SCHEMA public OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.44.0', '5.45.0');

-- Change resource_notify trigger to use column name as payload
DROP TRIGGER resource_notify_trig ON iris.geo_loc;
DROP TRIGGER resource_notify_trig ON iris.controller_io;
DROP FUNCTION iris.resource_notify();

CREATE FUNCTION iris.resource_notify() RETURNS TRIGGER AS
    $resource_notify$
DECLARE
    arg TEXT;
BEGIN
    FOREACH arg IN ARRAY TG_ARGV LOOP
        IF (TG_OP = 'DELETE') THEN
            PERFORM pg_notify(OLD.resource_n, arg);
        ELSE
            PERFORM pg_notify(NEW.resource_n, arg);
        END IF;
    END LOOP;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$resource_notify$ LANGUAGE plpgsql;

CREATE TRIGGER resource_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.hashtag
    FOR EACH ROW EXECUTE PROCEDURE iris.resource_notify('hashtags');

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.geo_loc
    FOR EACH ROW EXECUTE PROCEDURE iris.resource_notify('geo_loc');

CREATE TRIGGER resource_notify_trig
    AFTER UPDATE ON iris.controller_io
    FOR EACH ROW EXECUTE PROCEDURE iris.resource_notify('controller_io');

COMMIT;
