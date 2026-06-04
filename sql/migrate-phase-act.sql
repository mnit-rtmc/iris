\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Update phase_action_view condition column
DROP VIEW phase_action_view;
CREATE VIEW phase_action_view AS
    SELECT name, action_plan, day_plan, from_phase, to_phase,
           c.description AS condition, params
    FROM iris.phase_action pa
    JOIN iris.action_condition c ON pa.condition = c.id;
GRANT SELECT ON phase_action_view TO PUBLIC;

COMMIT;
