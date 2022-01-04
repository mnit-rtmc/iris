\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.26.0', '5.27.0');

-- Allow higher code points
ALTER TABLE iris.glyph DROP CONSTRAINT IF EXISTS glyph_code_point_ck;
ALTER TABLE iris.glyph
    ADD CONSTRAINT glyph_code_point_ck
    CHECK (code_point > 0 AND code_point < 65536);

-- Increase maximum glyph width to 32 pixels
ALTER TABLE iris.glyph DROP CONSTRAINT IF EXISTS glyph_width_ck;
ALTER TABLE iris.glyph
    ADD CONSTRAINT glyph_width_ck
    CHECK (width >= 0 AND width <= 32);

-- Add duration to sign events
DROP VIEW recent_sign_event_view;
DROP VIEW sign_event_view;

ALTER TABLE event.sign_event ADD COLUMN multi VARCHAR(1024);
ALTER TABLE event.sign_event ADD COLUMN duration INTEGER;
UPDATE event.sign_event SET multi = message::VARCHAR(1024);
ALTER TABLE event.sign_event DROP COLUMN message;

CREATE FUNCTION event.multi_message(VARCHAR(1024))
    RETURNS TEXT AS $multi_message$
DECLARE
    multi ALIAS FOR $1;
BEGIN
    RETURN regexp_replace(
        replace(
            replace(multi, '[nl]', E'\n'),
            '[np]', E'\n'
        ),
        '\[.+?\]', ' ', 'g'
    );
END;
$multi_message$ LANGUAGE plpgsql;

CREATE VIEW sign_event_view AS
    SELECT event_id, event_date, description, device_id,
           event.multi_message(multi) as message, multi, owner, duration
    FROM event.sign_event JOIN event.event_description
    ON sign_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON sign_event_view TO PUBLIC;

CREATE VIEW recent_sign_event_view AS
    SELECT event_id, event_date, description, device_id, message, multi,
           owner, duration
    FROM sign_event_view
    WHERE event_date > (CURRENT_TIMESTAMP - interval '90 days');
GRANT SELECT ON recent_sign_event_view TO PUBLIC;

COMMIT;
