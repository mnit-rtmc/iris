\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add simple meter event type
INSERT INTO event.event_description (event_desc_id, description)
    VALUES (404, 'Simple meter event');

CREATE TABLE event.simple_meter_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    ramp_meter VARCHAR(20) NOT NULL REFERENCES iris._ramp_meter
        ON DELETE CASCADE,
    rel_rate INTEGER NOT NULL
);

-- DELETE of iris.ramp_meter *very* slow without this index
CREATE INDEX ON event.simple_meter_event (ramp_meter);

CREATE VIEW simple_meter_event_view AS
    SELECT me.id, event_date, ed.description, ramp_meter, rel_rate
    FROM event.simple_meter_event me
    JOIN event.event_description ed ON me.event_desc = ed.event_desc_id;
GRANT SELECT ON simple_meter_event_view TO PUBLIC;

-- Add config for simple meter events
INSERT INTO iris.event_config (name, enable_store, enable_purge, purge_days)
VALUES ('simple_meter_event', true, true, 14);

COMMIT;
