-- Test script for tms database
--
-- run with: psql tms -f test-tms.sql

\set QUIET
\set ON_ERROR_STOP

SET client_encoding = 'UTF8';
SET client_min_messages TO WARNING;

CREATE SCHEMA IF NOT EXISTS test AUTHORIZATION tms;

SET SESSION AUTHORIZATION 'tms';

SET search_path = test;

CREATE TABLE results (
	test VARCHAR(64),
	result VARCHAR(8)
);

CREATE FUNCTION assert(tst BOOLEAN) RETURNS VARCHAR(8) AS $$
BEGIN
	IF tst IS NULL THEN
		RETURN 'NULL';
	END IF;
	IF tst THEN
		RETURN 'PASS';
	ELSE
		RETURN 'FAIL';
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION assert(tst BOOLEAN, name VARCHAR(64)) RETURNS VOID AS $$
BEGIN
	INSERT INTO results VALUES (name, assert(tst));
END;
$$ LANGUAGE plpgsql;

-- Insert controller stuff
INSERT INTO iris.comm_link (name, description, uri, protocol, poll_enabled,
                            poll_period, timeout)
	VALUES ('LNK_TEST_1', 'Test comm link', 'Test URI', 0, false, 30, 750);
INSERT INTO iris.geo_loc (name) VALUES ('LOC_TEST_1');
INSERT INTO iris.cabinet (name, geo_loc)
	VALUES ('CAB_TEST_1', 'LOC_TEST_1');
INSERT INTO iris.controller (name, drop_id, comm_link, cabinet, active, notes)
	VALUES ('CTL_TEST_1', 1, 'LNK_TEST_1', 'CAB_TEST_1', false,
	        'Test controller');

-- Test alarm view
INSERT INTO iris.alarm (name, description, pin, state)
	VALUES ('A_TEST_1', 'Test alarm', 0, false);

\o /dev/null
SELECT assert ('A_TEST_1' = (SELECT name FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm insert name');
SELECT assert ('Test alarm' = (SELECT description FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm insert description');
SELECT assert ((SELECT controller FROM iris.alarm WHERE name = 'A_TEST_1')
               IS NULL, 'alarm insert controller');
SELECT assert (0 = (SELECT pin FROM iris.alarm WHERE name = 'A_TEST_1'),
               'alarm insert pin');
SELECT assert (false = (SELECT state FROM iris.alarm WHERE name = 'A_TEST_1'),
               'alarm insert state');
\o

UPDATE iris.alarm SET description = 'Altered desc' WHERE name = 'A_TEST_1';
UPDATE iris.alarm SET controller = 'CTL_TEST_1' WHERE name = 'A_TEST_1';
UPDATE iris.alarm SET pin = 2 WHERE name = 'A_TEST_1';
UPDATE iris.alarm SET state = true WHERE name = 'A_TEST_1';

\o /dev/null
SELECT assert ('A_TEST_1' = (SELECT name FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm update name');
SELECT assert ('Altered desc' = (SELECT description FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm update description');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.alarm
               WHERE name = 'A_TEST_1'), 'alarm update controller');
SELECT assert (2 = (SELECT pin FROM iris.alarm WHERE name = 'A_TEST_1'),
               'alarm update pin');
SELECT assert (true = (SELECT state FROM iris.alarm WHERE name = 'A_TEST_1'),
               'alarm update state');
\o

INSERT INTO iris.alarm (name, description, controller, pin, state)
	VALUES ('A_TEST_2', '', 'CTL_TEST_1', 1, true);

\o /dev/null
SELECT assert ('A_TEST_2' = (SELECT name FROM iris.alarm
               WHERE name = 'A_TEST_2'), 'alarm insert name 2');
SELECT assert ('' = (SELECT description FROM iris.alarm
               WHERE name = 'A_TEST_2'), 'alarm insert description 2');
SELECT assert ('CTL_TEST_1' = (SELECT controller FROM iris.alarm
               WHERE name = 'A_TEST_2'), 'alarm insert controller 2');
SELECT assert (1 = (SELECT pin FROM iris.alarm WHERE name = 'A_TEST_2'),
               'alarm insert pin 2');
SELECT assert (true = (SELECT state FROM iris.alarm WHERE name = 'A_TEST_2'),
               'alarm insert state 2');
\o

DELETE FROM iris.alarm WHERE name = 'A_TEST_2';
DELETE FROM iris.alarm WHERE name = 'A_TEST_1';

-- Delete controller stuff
DELETE FROM iris.controller WHERE name = 'CTL_TEST_1';
DELETE FROM iris.cabinet WHERE name = 'CAB_TEST_1';
DELETE FROM iris.geo_loc WHERE name = 'LOC_TEST_1';
DELETE FROM iris.comm_link WHERE name = 'LNK_TEST_1';

-- Display results
SELECT * FROM test.results ORDER BY result DESC, test;
SELECT count(*), result FROM test.results GROUP BY result;

-- Drop test schema
DROP SCHEMA test CASCADE;
