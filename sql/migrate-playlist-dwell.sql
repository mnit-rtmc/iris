\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add dwell_sec column to play_list
ALTER TABLE iris.play_list ADD COLUMN dwell_sec INTEGER;
UPDATE iris.play_list SET dwell_sec = (
    SELECT value FROM iris.system_attribute
    WHERE name = 'camera_playlist_dwell_sec'
)::INTEGER;
ALTER TABLE iris.play_list ALTER COLUMN dwell_sec SET NOT NULL;
DELETE FROM iris.system_attribute
    WHERE name = 'camera_playlist_dwell_sec';

COMMIT;
