SET SESSION AUTHORIZATION 'tms';

CREATE TABLE holiday (
	name TEXT PRIMARY KEY,
	month INTEGER NOT NULL,
	day INTEGER NOT NULL,
	week INTEGER NOT NULL,
	weekday INTEGER NOT NULL,
	shift INTEGER NOT NULL,
	period INTEGER NOT NULL
);

INSERT INTO holiday (name, month, day, week, weekday, shift, period)
	(SELECT name, month, day, week, weekday, shift, period
	FROM metering_holiday);

REVOKE ALL ON TABLE holiday FROM PUBLIC;
GRANT SELECT ON TABLE holiday TO PUBLIC;

DROP TABLE metering_holiday;
