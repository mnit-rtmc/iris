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

COMMIT;
