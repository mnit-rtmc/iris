SET SESSION AUTHORIZATION 'tms';

DROP TABLE dms_parameters;

CREATE TABLE system_policy (
    name character varying NOT NULL,
    value integer NOT NULL
);
REVOKE ALL ON TABLE system_policy FROM PUBLIC;
GRANT SELECT ON TABLE system_policy TO PUBLIC;

COPY system_policy (name, value) FROM stdin;
meter_green_time	13
meter_yellow_time	7
meter_min_red_time	1
dms_page_on_time	20
dms_page_off_time	0
ring_radius_0	2
ring_radius_1	5
ring_radius_2	10
\.
