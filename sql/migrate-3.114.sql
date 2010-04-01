\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.114.0'
	WHERE name = 'database_version';

CREATE TABLE event.incident_detail (
	name VARCHAR(8) PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

COPY event.incident_detail (name, description) FROM stdin;
animal	Animal on Road
debris	Debris
emrg_veh	Emergency Vehicles
event	Event Congestion
flooding	Flash Flooding
gr_fire	Grass Fire
ice	Ice
jacknife	Jacknifed Trailer
pavement	Pavement Failure
ped	Pedestrian on Highway
rollover	Rollover
sgnl_out	Traffic Lights Out
snow_rmv	Snow Removal
spill	Spilled Load
veh_fire	Vehicle Fire
\.

ALTER TABLE event.incident ADD COLUMN detail VARCHAR(8);
ALTER TABLE event.incident ADD CONSTRAINT incident_detail_fkey
	FOREIGN KEY (detail) REFERENCES event.incident_detail;

INSERT INTO iris.privilege (name, role, pattern, priv_r, priv_w, priv_c, priv_d)
	VALUES('prv_idtl', 'login', 'incident_detail(/.)?', 't', 'f', 'f', 'f');
