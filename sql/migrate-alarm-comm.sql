\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Change alarm base resource **BACK** to comm_config
UPDATE iris.resource_type SET base = 'comm_config' WHERE name = 'alarm';

INSERT INTO iris.action_condition (id, description)
VALUES (6, 'alarm');

COMMIT;
