\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add symbol column to lcs_indication
ALTER TABLE iris.lcs_indication ADD COLUMN symbol VARCHAR;

UPDATE iris.lcs_indication SET symbol = '?' WHERE id = 0;
UPDATE iris.lcs_indication SET symbol = '⍽' WHERE id = 1;
UPDATE iris.lcs_indication SET symbol = '↓' WHERE id = 2;
UPDATE iris.lcs_indication SET symbol = '⇣' WHERE id = 3;
UPDATE iris.lcs_indication SET symbol = '✕' WHERE id = 4;
UPDATE iris.lcs_indication SET symbol = '✖' WHERE id = 5;
UPDATE iris.lcs_indication SET symbol = '》' WHERE id = 6;
UPDATE iris.lcs_indication SET symbol = '《' WHERE id = 7;
UPDATE iris.lcs_indication SET symbol = '⤷' WHERE id = 8;
UPDATE iris.lcs_indication SET symbol = '⤶' WHERE id = 9;
UPDATE iris.lcs_indication SET symbol = '◊' WHERE id = 10;
UPDATE iris.lcs_indication SET symbol = 'A' WHERE id = 11;
UPDATE iris.lcs_indication SET symbol = 'L' WHERE id = 12;

ALTER TABLE iris.lcs_indication ALTER COLUMN symbol SET NOT NULL;

COMMIT;
