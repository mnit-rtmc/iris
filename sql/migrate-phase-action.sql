\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add phase actions
INSERT INTO iris.resource_type (name, base)
    VALUES ('phase_action', 'action_plan');

CREATE TABLE iris.action_condition (
    id INTEGER PRIMARY KEY,
    description VARCHAR NOT NULL
);

INSERT INTO iris.action_condition (id, description)
VALUES
    (0, 'hold time'),
    (1, 'clock time'),
    (2, 'slow traffic'),
    (3, 'high occupancy'),
    (4, 'toll mode'),
    (5, 'RWIS reading'),
    (6, 'alert period');

CREATE TABLE iris.phase_action (
    name VARCHAR(30) PRIMARY KEY,
    action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
    from_phase VARCHAR(12) REFERENCES iris.plan_phase,
    to_phase VARCHAR(12) NOT NULL REFERENCES iris.plan_phase,
    day_plan VARCHAR(10) REFERENCES iris.day_plan,
    condition INTEGER NOT NULL REFERENCES iris.action_condition,
    parameters VARCHAR(16)
);

CREATE FUNCTION iris.phase_action_notify() RETURNS TRIGGER AS
    $phase_action_notify$
BEGIN
    PERFORM pg_notify('phase_action', NEW.name);
    RETURN NULL; -- AFTER trigger return is ignored
END;
$phase_action_notify$ LANGUAGE plpgsql;

CREATE TRIGGER phase_action_notify_trig
    AFTER UPDATE ON iris.phase_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.phase_action_notify();

CREATE TRIGGER phase_action_table_notify_trig
    AFTER INSERT OR DELETE ON iris.phase_action
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

CREATE VIEW phase_action_view AS
    SELECT name, action_plan, from_phase, to_phase, day_plan, condition,
           parameters
    FROM iris.phase_action;
GRANT SELECT ON phase_action_view TO PUBLIC;

COMMIT;
