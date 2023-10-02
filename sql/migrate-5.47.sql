\set ON_ERROR_STOP

BEGIN;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.46.0', '5.47.0');

-- Create trigger for word changes
CREATE TRIGGER word_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.word
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Add hashtag/access constraint
ALTER TABLE iris.permission
    ADD CONSTRAINT hashtag_access_ck
    CHECK (hashtag IS NULL OR access_n != 1);

COMMIT;
