\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Replace action plan description with notes (+ hashtag_trig)
DROP VIEW action_plan_view;

ALTER TABLE iris.action_plan ADD COLUMN notes VARCHAR;
ALTER TABLE iris.action_plan ADD CONSTRAINT action_plan_notes_check
    CHECK (LENGTH(notes) < 256);

CREATE TRIGGER action_plan_hashtag_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.action_plan
    FOR EACH ROW EXECUTE FUNCTION iris.hashtag_trig('action_plan');

UPDATE iris.action_plan SET notes = description;

ALTER TABLE iris.action_plan DROP COLUMN description;
ALTER TABLE iris.action_plan DROP COLUMN group_n;

CREATE VIEW action_plan_view AS
    SELECT name, notes, sync_actions, sticky, ignore_auto_fail, active,
           default_phase, phase
    FROM iris.action_plan;
GRANT SELECT ON action_plan_view TO PUBLIC;

COMMIT;
