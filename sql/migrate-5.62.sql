\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.61.0', '5.62.0');

-- Add encoder type notify channel
CREATE TRIGGER encoder_type_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.encoder_type
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

-- Add encoder stream notify channel
CREATE TRIGGER encoder_stream_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.encoder_stream
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

COMMIT;
