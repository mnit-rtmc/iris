\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add attributes to disable legacy UI
INSERT INTO iris.system_attribute (name, value) VALUES
    ('legacy_ui_comm_enable', 'true')
ON CONFLICT (name) DO NOTHING;

COMMIT;
