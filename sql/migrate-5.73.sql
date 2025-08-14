\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.72.0', '5.73.0');

CREATE TRIGGER gate_arm_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('gate_arm');

-- Force trigger to execute
UPDATE iris.gate_arm SET notes = notes;

COMMIT;
