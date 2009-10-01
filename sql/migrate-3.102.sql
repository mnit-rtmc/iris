\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.102.0'
	WHERE name = 'database_version';

INSERT INTO iris.lane_type (id, description, dcode)
	VALUES (16, 'Shoulder', 'D');

CREATE TABLE iris.comm_protocol (
	id smallint PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);

COPY iris.comm_protocol (id, description) FROM stdin;
0	NTCIP Class B
1	MnDOT 170 (4-bit)
2	MnDOT 170 (5-bit)
3	SmartSensor 105
4	Canoga
5	Vicon Switcher
6	Pelco D PTZ
7	NTCIP Class C
8	Manchester PTZ
9	DMS Lite
10	AWS
11	NTCIP Class A
12	Pelco Switcher
13	Vicon PTZ
\.

ALTER TABLE iris.comm_link ADD CONSTRAINT comm_link_protocol_fkey
	FOREIGN KEY (protocol) REFERENCES iris.comm_protocol;

CREATE VIEW comm_link_view AS
	SELECT cl.name, cl.description, cl.url, cp.description AS protocol,
	cl.timeout
	FROM iris.comm_link cl
	JOIN iris.comm_protocol cp ON cl.protocol = cp.id;
GRANT SELECT ON comm_link_view TO PUBLIC;
