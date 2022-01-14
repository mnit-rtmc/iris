\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.27.0', '5.28.0');

-- Add "SECURITY DEFINER" to fix permission problems
CREATE OR REPLACE FUNCTION iris.multi_tags_str(INTEGER)
    RETURNS text AS $multi_tags_str$
DECLARE
    bits ALIAS FOR $1;
BEGIN
    RETURN (
        SELECT string_agg(mt.tag, ', ') FROM (
            SELECT bit, tag FROM iris.multi_tags(bits) ORDER BY bit
        ) AS mt
    );
END;
$multi_tags_str$ LANGUAGE plpgsql SECURITY DEFINER;

-- Add NOTIFY triggers for more tables
CREATE TRIGGER comm_config_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.comm_config
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER comm_link_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.comm_link
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER modem_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.modem
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER cabinet_style_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.cabinet_style
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER controller_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.controller
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

COMMIT;
