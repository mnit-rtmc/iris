\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.20.0', '5.21.0');

INSERT INTO cap.event (code, description) VALUES
	('BHS', 'Beach Hazards Statement'),
	('CFY', 'Coastal Flood Advisory'),
	('DSY', 'Dust Advisory'),
	('DUY', 'Blowing Dust Advisory'),
	('FAA', 'Flood Watch'),
	('FAW', 'Flood Warning'),
	('FAY', 'Flood Advisory'),
	('FZA', 'Freeze Watch'),
	('HZA', 'Hard Freeze Watch'),
	('HZW', 'Hard Freeze Warning'),
	('ISW', 'Ice Storm Warning'),
	('LOY', 'Low Water Advisory'),
	('LWY', 'Lake Wind Advisory'),
	('MAW', 'Special Marine Warning'),
	('MFY', 'Dense Fog Advisory'),
	('SAB', 'Avalanche Advisory'),
	('SEA', 'Hazardous Seas Watch'),
	('SEW', 'Hazardous Seas Warning'),
	('SRW', 'Storm Warning'),
	('SVW', 'Severe Thunderstorm Warning'),
	('TOW', 'Tornado Warning'),
	('WCA', 'Wind Chill Watch'),
	('WCW', 'Wind Chill Warning'),
	('ZFY', 'Freezing Fog Advisory');

DELETE FROM cap.event WHERE code IN ('SMW', 'SVR', 'TOR');

DROP VIEW alert_config_view;
DROP TABLE iris.alert_config;

CREATE TABLE iris.alert_config (
	name VARCHAR(20) PRIMARY KEY,
	event VARCHAR(3) REFERENCES cap.event,
	response_shelter BOOLEAN NOT NULL,
	response_evacuate BOOLEAN NOT NULL,
	response_prepare BOOLEAN NOT NULL,
	response_execute BOOLEAN NOT NULL,
	response_avoid BOOLEAN NOT NULL,
	response_monitor BOOLEAN NOT NULL,
	response_all_clear BOOLEAN NOT NULL,
	response_none BOOLEAN NOT NULL,
	urgency_unknown BOOLEAN NOT NULL,
	urgency_past BOOLEAN NOT NULL,
	urgency_future BOOLEAN NOT NULL,
	urgency_expected BOOLEAN NOT NULL,
	urgency_immediate BOOLEAN NOT NULL,
	severity_unknown BOOLEAN NOT NULL,
	severity_minor BOOLEAN NOT NULL,
	severity_moderate BOOLEAN NOT NULL,
	severity_severe BOOLEAN NOT NULL,
	severity_extreme BOOLEAN NOT NULL,
	certainty_unknown BOOLEAN NOT NULL,
	certainty_unlikely BOOLEAN NOT NULL,
	certainty_possible BOOLEAN NOT NULL,
	certainty_likely BOOLEAN NOT NULL,
	certainty_observed BOOLEAN NOT NULL,
	pre_alert_hours INTEGER NOT NULL,
	post_alert_hours INTEGER NOT NULL,
	auto_deploy BOOLEAN NOT NULL
);

CREATE TABLE iris.alert_config_quick_message (
	alert_config VARCHAR(20) NOT NULL REFERENCES iris.alert_config,
	quick_message VARCHAR(20) NOT NULL REFERENCES iris.quick_message
);
ALTER TABLE iris.alert_config_quick_message
	ADD PRIMARY KEY (alert_config, quick_message);

COMMIT;
