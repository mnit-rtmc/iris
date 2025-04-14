\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.69.0', '5.70.0');

-- Change comm protocol 26 to Campbell Cloud
UPDATE iris.comm_protocol
    SET description = 'CampbellCloud'
    WHERE id = 26;

-- Add LCS events
INSERT INTO iris.event_config (name, enable_store, enable_purge, purge_days)
VALUES ('lcs_event', true, false, 0);

CREATE TABLE event.lcs_event (
    id SERIAL PRIMARY KEY,
    event_date TIMESTAMP WITH time zone DEFAULT NOW() NOT NULL,
    event_desc INTEGER NOT NULL REFERENCES event.event_description,
    lcs VARCHAR(20) NOT NULL REFERENCES iris._lcs ON DELETE CASCADE,
    lock JSONB,
    status JSONB
);

CREATE VIEW lcs_event_view AS
    SELECT ev.id, event_date, ed.description, lcs, lock, status
    FROM event.lcs_event ev
    JOIN event.event_description ed ON ev.event_desc = ed.event_desc_id;
GRANT SELECT ON lcs_event_view TO PUBLIC;

INSERT INTO event.event_description (event_desc_id, description)
VALUES
    (87, 'LCS LOCKED'),
    (88, 'LCS UNLOCKED');

COMMIT;
