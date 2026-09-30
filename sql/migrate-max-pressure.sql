\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add max-pressure event table
CREATE TABLE event.max_pressure_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    ramp_meter VARCHAR(20) NOT NULL REFERENCES iris._ramp_meter
        ON DELETE CASCADE,
    phase INTEGER NOT NULL REFERENCES iris.metering_phase,
    q_state INTEGER NOT NULL REFERENCES iris.meter_queue_state,
    q_len REAL NOT NULL,
    dem_adj REAL NOT NULL,
    wait_secs INTEGER NOT NULL,
    limit_ctrl INTEGER NOT NULL REFERENCES iris.meter_limit_control,
    min_rate INTEGER NOT NULL,
    rel_rate INTEGER NOT NULL,
    max_rate INTEGER NOT NULL,
    d_node VARCHAR(10),
    us_density_detected REAL NOT NULL,
    us_density_estimated REAL NOT NULL,
    ds_density_detected REAL NOT NULL,
    ds_density_estimated REAL NOT NULL,
    us_weight REAL NOT NULL,
    ds_weight REAL NOT NULL,
    rmp_weight REAL NOT NULL,
    pressure_ud REAL NOT NULL,
    pressure_rd REAL NOT NULL,
    receiving_flow REAL NOT NULL,
    sending_flow_us REAL NOT NULL,
    sending_flow_rmp REAL NOT NULL,
    q_len_from_cc REAL NOT NULL
);

-- DELETE of iris.ramp_meter *very* slow without this index
CREATE INDEX ON event.max_pressure_event (ramp_meter);

CREATE VIEW max_pressure_event_view AS
    SELECT me.id, event_date, ed.description, ramp_meter,
           mp.description AS phase, qs.description AS q_state, q_len, dem_adj,
           wait_secs, lc.description AS limit_ctrl, min_rate, rel_rate,
           max_rate, d_node, us_density_detected, us_density_estimated,
           ds_density_detected, ds_density_estimated, us_weight, ds_weight,
           rmp_weight, pressure_ud, pressure_rd, receiving_flow,
           sending_flow_us, sending_flow_rmp, q_len_from_cc
    FROM event.max_pressure_event me
    JOIN event.event_description ed ON me.event_desc = ed.event_desc_id
    JOIN iris.metering_phase mp ON phase = mp.id
    JOIN iris.meter_queue_state qs ON q_state = qs.id
    JOIN iris.meter_limit_control lc ON limit_ctrl = lc.id;
GRANT SELECT ON max_pressure_event_view TO PUBLIC;

-- Add config for max-pressure events
INSERT INTO iris.event_config (name, enable_store, enable_purge, purge_days)
VALUES ('max_pressure_event', true, true, 14);

COMMIT;
