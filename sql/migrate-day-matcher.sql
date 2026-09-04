\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Update day_matcher_valid CHECK
ALTER TABLE iris.day_matcher
    DROP CONSTRAINT day_matcher_valid;
ALTER TABLE iris.day_matcher
    ADD CONSTRAINT day_matcher_valid CHECK (
        (month IS NOT NULL OR day IS NOT NULL OR weekday IS NOT NULL) AND
        (week IS NULL OR (day IS NULL AND weekday IS NOT NULL)) AND
        (shift IS NULL OR
            (day IS NULL AND weekday IS NOT NULL AND week IS NOT NULL)
        )
    );

COMMIT;
