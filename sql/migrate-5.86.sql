\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.85.0', '5.86.0');

-- Add attributes to disable legacy UI
INSERT INTO iris.system_attribute (name, value) VALUES
    ('legacy_ui_system_enable', 'true'),
    ('legacy_ui_users_enable', 'true')
ON CONFLICT (name) DO NOTHING;

-- Update phase_action_view condition column
DROP VIEW phase_action_view;
CREATE VIEW phase_action_view AS
    SELECT name, action_plan, day_plan, from_phase, to_phase,
           c.description AS condition, params
    FROM iris.phase_action pa
    JOIN iris.action_condition c ON pa.condition = c.id;
GRANT SELECT ON phase_action_view TO PUBLIC;

-- Add max-pressure meter algorithm value
INSERT INTO iris.meter_algorithm (id, description) VALUES
    (4, 'Max-Pressure Metering');

COMMIT;
