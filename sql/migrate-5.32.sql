\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.31.0', '5.32.0');

INSERT INTO iris.system_attribute (name, value)
    VALUES ('weather_sensor_event_purge_days', '14');

-- Remove batch from permission index
DROP INDEX iris.permission_role_resource_n_batch_idx;
CREATE UNIQUE INDEX permission_role_resource_n_idx
    ON iris.permission (role, resource_n);

-- Add new permissions for administrator
COPY iris.permission (role, resource_n, access_n) FROM stdin;
administrator	gate_arm	4
\.

CREATE TRIGGER gate_arm_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm
    FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

COMMIT;
