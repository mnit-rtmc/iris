--
-- PostgreSQL database dump
--

SET client_encoding = 'SQL_ASCII';
SET check_function_bodies = false;

SET SESSION AUTHORIZATION 'tms';

SET search_path = public, pg_catalog;

--
-- TOC entry 75 (OID 7951369)
-- Name: plpgsql_call_handler(); Type: FUNC PROCEDURAL LANGUAGE; Schema: public; Owner: tms
--

CREATE FUNCTION plpgsql_call_handler() RETURNS language_handler
    AS '/usr/lib/pgsql/plpgsql.so', 'plpgsql_call_handler'
    LANGUAGE c;


SET SESSION AUTHORIZATION DEFAULT;

--
-- TOC entry 74 (OID 7951370)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: public; Owner: 
--

CREATE TRUSTED PROCEDURAL LANGUAGE plpgsql HANDLER plpgsql_call_handler;


SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 4 (OID 2200)
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 36 (OID 7951371)
-- Name: system_event; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE system_event (
    event_id integer DEFAULT nextval('event_id_seq'::text) NOT NULL,
    event_date timestamp with time zone NOT NULL,
    event_desc_id smallint NOT NULL
);


--
-- TOC entry 37 (OID 7951371)
-- Name: system_event; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE system_event FROM PUBLIC;
GRANT SELECT ON TABLE system_event TO PUBLIC;
GRANT SELECT ON TABLE system_event TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 38 (OID 7951374)
-- Name: comm_line_event; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE comm_line_event (
    event_id integer DEFAULT nextval('event_id_seq'::text) NOT NULL,
    event_date timestamp with time zone NOT NULL,
    event_desc_id integer,
    line smallint,
    "drop" smallint,
    device_id character varying(30),
    remarks text
);


--
-- TOC entry 39 (OID 7951374)
-- Name: comm_line_event; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE comm_line_event FROM PUBLIC;
GRANT SELECT ON TABLE comm_line_event TO PUBLIC;
GRANT SELECT ON TABLE comm_line_event TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 40 (OID 7951384)
-- Name: event_description; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE event_description (
    event_desc_id smallint DEFAULT nextval('event_desc_id_seq'::text) NOT NULL,
    description text NOT NULL,
    device_type_id smallint,
    active boolean DEFAULT true NOT NULL,
    priority_level smallint DEFAULT 1
);


--
-- TOC entry 41 (OID 7951384)
-- Name: event_description; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE event_description FROM PUBLIC;
GRANT SELECT ON TABLE event_description TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 42 (OID 7951408)
-- Name: device_type; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE device_type (
    device_type_id smallint DEFAULT nextval('device_type_id_seq'::text) NOT NULL,
    description text NOT NULL,
    active boolean DEFAULT true
);


--
-- TOC entry 43 (OID 7951408)
-- Name: device_type; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE device_type FROM PUBLIC;
GRANT SELECT ON TABLE device_type TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 5 (OID 7951422)
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE event_id_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1
    CYCLE;


--
-- TOC entry 7 (OID 7951422)
-- Name: event_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE event_id_seq FROM PUBLIC;
GRANT SELECT ON TABLE event_id_seq TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 8 (OID 7951424)
-- Name: event_desc_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE event_desc_id_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1
    CYCLE;


--
-- TOC entry 10 (OID 7951424)
-- Name: event_desc_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE event_desc_id_seq FROM PUBLIC;
GRANT SELECT ON TABLE event_desc_id_seq TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 11 (OID 7951426)
-- Name: delay_reason_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE delay_reason_id_seq
    START WITH 7
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1
    CYCLE;


--
-- TOC entry 13 (OID 7951426)
-- Name: delay_reason_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE delay_reason_id_seq FROM PUBLIC;
GRANT SELECT ON TABLE delay_reason_id_seq TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 14 (OID 7951428)
-- Name: device_type_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE device_type_id_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1
    CYCLE;


--
-- TOC entry 16 (OID 7951428)
-- Name: device_type_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE device_type_id_seq FROM PUBLIC;
GRANT SELECT ON TABLE device_type_id_seq TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 17 (OID 7951430)
-- Name: close_reason_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE close_reason_id_seq
    START WITH 6
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1
    CYCLE;


--
-- TOC entry 19 (OID 7951430)
-- Name: close_reason_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE close_reason_id_seq FROM PUBLIC;
GRANT SELECT ON TABLE close_reason_id_seq TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 20 (OID 7951432)
-- Name: device_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE device_id_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1
    CYCLE;


--
-- TOC entry 22 (OID 7951432)
-- Name: device_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE device_id_seq FROM PUBLIC;
GRANT SELECT ON TABLE device_id_seq TO PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 44 (OID 7951447)
-- Name: communication_line_events_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW communication_line_events_view AS
    SELECT c.event_id, c.event_date, c.line, c."drop", ed.description AS event_description FROM comm_line_event c, event_description ed WHERE (c.event_desc_id = ed.event_desc_id);


--
-- TOC entry 45 (OID 7951447)
-- Name: communication_line_events_view; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE communication_line_events_view FROM PUBLIC;
GRANT SELECT ON TABLE communication_line_events_view TO PUBLIC;
GRANT SELECT ON TABLE communication_line_events_view TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 46 (OID 7951450)
-- Name: event_descriptions; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW event_descriptions AS
    SELECT ed.priority_level, ed.description AS event_description, ed.event_desc_id AS event_description_id, dt.description AS device_type FROM event_description ed, device_type dt WHERE (ed.device_type_id = dt.device_type_id);


--
-- TOC entry 47 (OID 7951450)
-- Name: event_descriptions; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE event_descriptions FROM PUBLIC;

CREATE TABLE "notify" (
    event_desc_id smallint DEFAULT nextval('event_desc_id_seq'::text) NOT NULL,
    notify_os boolean,
    notify_dt_day boolean,
    notify_dt_evening boolean
);

CREATE VIEW active_event_descriptions AS
    SELECT ed.priority_level, ed.event_desc_id AS event_description_id, ed.description AS event_description, dt.description AS device_type, n.notify_dt_day, n.notify_dt_evening, n.notify_os FROM device_type dt, (event_description ed LEFT JOIN "notify" n ON ((ed.event_desc_id = n.event_desc_id))) WHERE (((ed.device_type_id = dt.device_type_id) AND (ed.active = true)) AND (dt.active = true));

SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 48 (OID 7951462)
-- Name: active_device_types; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW active_device_types AS
    SELECT dt.description AS device_type, dt.device_type_id FROM device_type dt WHERE (dt.active = true);


--
-- TOC entry 23 (OID 7951469)
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE user_id_seq
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 49 (OID 7951471)
-- Name: tms_user; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE tms_user (
    id integer DEFAULT nextval('tms_user_id_seq'::text) NOT NULL,
    description text
);


--
-- TOC entry 25 (OID 7951477)
-- Name: tms_user_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE tms_user_id_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 50 (OID 7951479)
-- Name: settings; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE settings (
    parameter text NOT NULL,
    value smallint
);


--
-- TOC entry 51 (OID 7951490)
-- Name: sign_status_event; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE sign_status_event (
    event_id integer DEFAULT nextval('event_id_seq'::text),
    event_date timestamp with time zone,
    event_desc_id smallint,
    device_id character varying,
    message text,
    user_id smallint
);


--
-- TOC entry 52 (OID 7951490)
-- Name: sign_status_event; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE sign_status_event FROM PUBLIC;
GRANT SELECT ON TABLE sign_status_event TO PUBLIC;
GRANT SELECT ON TABLE sign_status_event TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 53 (OID 7951498)
-- Name: sign_status_events_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW sign_status_events_view AS
    SELECT sse.event_id, sse.event_date, tms_user.description AS "user", sse.device_id, sse.message, ed.description AS event_description, dt.description AS device_type FROM sign_status_event sse, event_description ed, device_type dt, tms_user WHERE (((dt.device_type_id = ed.device_type_id) AND (ed.event_desc_id = sse.event_desc_id)) AND (tms_user.id = sse.user_id));


--
-- TOC entry 54 (OID 7951501)
-- Name: system_log; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW system_log AS
    SELECT c.event_date, e.description, c.line, c."drop", c.device_id, c.remarks FROM comm_line_event c, event_description e WHERE (c.event_desc_id = e.event_desc_id);


--
-- TOC entry 55 (OID 7951501)
-- Name: system_log; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE system_log FROM PUBLIC;
GRANT SELECT ON TABLE system_log TO PUBLIC;
GRANT SELECT ON TABLE system_log TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 56 (OID 7951511)
-- Name: detector_malfunction_event; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE detector_malfunction_event (
    event_id integer DEFAULT nextval('event_id_seq'::text) NOT NULL,
    event_date timestamp with time zone,
    event_desc_id smallint,
    device_id integer,
    logged_by smallint
);


--
-- TOC entry 57 (OID 7951511)
-- Name: detector_malfunction_event; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE detector_malfunction_event FROM PUBLIC;
GRANT SELECT ON TABLE detector_malfunction_event TO PUBLIC;
GRANT SELECT ON TABLE detector_malfunction_event TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 58 (OID 7951516)
-- Name: det_malfunction; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW det_malfunction AS
    SELECT d.event_date, e.description, d.device_id AS det_num FROM detector_malfunction_event d, event_description e WHERE (e.event_desc_id = d.event_desc_id);


--
-- TOC entry 59 (OID 7951516)
-- Name: det_malfunction; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE det_malfunction FROM PUBLIC;
GRANT SELECT ON TABLE det_malfunction TO PUBLIC;
GRANT SELECT ON TABLE det_malfunction TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 60 (OID 7951519)
-- Name: det_malfunction_events_view; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW det_malfunction_events_view AS
    SELECT dm.event_id, dm.event_date, ed.description AS event_description, dm.device_id FROM detector_malfunction_event dm, event_description ed WHERE (dm.event_desc_id = ed.event_desc_id);


--
-- TOC entry 61 (OID 7951519)
-- Name: det_malfunction_events_view; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE det_malfunction_events_view FROM PUBLIC;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 62 (OID 7951522)
-- Name: communication_line_log; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW communication_line_log AS
    SELECT c.event_date, e.description, c.line, c."drop", c.device_id, c.remarks FROM comm_line_event c, event_description e WHERE (e.event_desc_id = c.event_desc_id);


--
-- TOC entry 63 (OID 7951522)
-- Name: communication_line_log; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE communication_line_log FROM PUBLIC;
GRANT SELECT ON TABLE communication_line_log TO PUBLIC;
GRANT SELECT ON TABLE communication_line_log TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 64 (OID 7951523)
-- Name: device_failure_event; Type: TABLE; Schema: public; Owner: tms
--

CREATE TABLE device_failure_event (
    event_date timestamp with time zone NOT NULL,
    event_desc_id smallint NOT NULL,
    logged_by text NOT NULL,
    remarks text,
    new_id text,
    old_id character varying(30),
    close_date timestamp with time zone,
    close_reason_id smallint,
    close_by text,
    os_notified boolean,
    dt_notified boolean,
    event_id integer DEFAULT nextval('event_id_seq'::text) NOT NULL,
    priority_level smallint
);


--
-- TOC entry 27 (OID 7951548)
-- Name: incident_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE incident_id_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    MINVALUE 219694
    CACHE 1
    CYCLE;


--
-- TOC entry 29 (OID 7951548)
-- Name: incident_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE incident_id_seq FROM PUBLIC;
GRANT UPDATE ON TABLE incident_id_seq TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 30 (OID 7951554)
-- Name: atp_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE atp_id_seq
    INCREMENT BY 1
    MAXVALUE 2147483647
    MINVALUE 4000
    CACHE 1
    CYCLE;


--
-- TOC entry 32 (OID 7951554)
-- Name: atp_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE atp_id_seq FROM PUBLIC;
GRANT UPDATE ON TABLE atp_id_seq TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 33 (OID 7951556)
-- Name: road_link_id_seq; Type: SEQUENCE; Schema: public; Owner: tms
--

CREATE SEQUENCE road_link_id_seq
    START WITH 1000
    INCREMENT BY 1
    MAXVALUE 2147483647
    MINVALUE 1000
    CACHE 1
    CYCLE;


--
-- TOC entry 35 (OID 7951556)
-- Name: road_link_id_seq; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE road_link_id_seq FROM PUBLIC;
GRANT UPDATE ON TABLE road_link_id_seq TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- TOC entry 76 (OID 7951570)
-- Name: message_line(text, integer); Type: FUNCTION; Schema: public; Owner: tms
--

CREATE FUNCTION message_line(text, integer) RETURNS text
    AS '
	DECLARE
		message ALIAS FOR $1;
		line ALIAS FOR $2;
		word text;
		wstop int2;
	BEGIN
		word := message;

		FOR w in 1..(line-1) LOOP
			wstop := strpos(word, ''[nl]'');
			IF wstop > 0 THEN
				word := SUBSTR(word, wstop + 4);
			ELSE
				word := '''';
			END IF;
		END LOOP;
		wstop := strpos(word, ''[nl]'');
		IF wstop > 0 THEN
			word := SUBSTR(word, 0, wstop);
		END IF;
		RETURN word;
	END;
'
    LANGUAGE plpgsql;


--
-- TOC entry 65 (OID 7951573)
-- Name: sign_message_log; Type: VIEW; Schema: public; Owner: tms
--

CREATE VIEW sign_message_log AS
    SELECT s.event_date, e.description, s.device_id, message_line(s.message, 1) AS line1, message_line(s.message, 2) AS line2, message_line(s.message, 3) AS line3, u.description AS "user" FROM sign_status_event s, event_description e, tms_user u WHERE ((s.event_desc_id = e.event_desc_id) AND (s.user_id = u.id));


--
-- TOC entry 66 (OID 7951573)
-- Name: sign_message_log; Type: ACL; Schema: public; Owner: tms
--

REVOKE ALL ON TABLE sign_message_log FROM PUBLIC;
GRANT SELECT ON TABLE sign_message_log TO PUBLIC;
GRANT SELECT ON TABLE sign_message_log TO dataentry;


SET SESSION AUTHORIZATION 'tms';

--
-- Data for TOC entry 77 (OID 7951371)
-- Name: system_event; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY system_event (event_id, event_date, event_desc_id) FROM stdin;
\.


--
-- Data for TOC entry 78 (OID 7951374)
-- Name: comm_line_event; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY comm_line_event (event_id, event_date, event_desc_id, line, "drop", device_id, remarks) FROM stdin;
\.


--
-- Data for TOC entry 79 (OID 7951384)
-- Name: event_description; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY event_description (event_desc_id, description, device_type_id, active, priority_level) FROM stdin;
64	Status ERROR	2	t	1
65	Comm FAILED	10	t	1
66	Warning Flasher Knockdown	7	t	1
5	Bulb Out	7	f	1
67	Projection Screen Problem	4	t	1
68	Serial I/O Timeout Error	2	t	1
69	Slow Opening	3	f	1
70	Slow Opening Arm	3	t	1
71	Unstable Text	6	t	1
72	Text Mislabled	6	t	1
73	Base Damage	7	t	1
74	PPF Problem	7	t	1
78	MaintDsp	12	f	1
82	MTransit	12	f	1
77	HennCo	12	f	1
75	Mnpls	12	f	1
79	Patrol	12	f	1
76	StPaul	12	f	1
81	Edina	12	f	1
80	Blooming	12	f	1
83	Keyboard Problem	12	t	1
84	No Video	12	f	1
85	Video Problem	12	t	1
86	Keyboard/Video Problem	12	t	1
87	Other	12	t	1
88	Heater Failure	6	t	1
89	Sign DEPLOYED	8	t	1
90	Sign CLEARED	8	t	1
91	Sign DEPLOYED	2	t	1
92	Sign CLEARED	2	t	1
93	Status Event	8	t	1
94	NO HITS	11	t	1
95	LOCKED ON	11	t	1
96	CHATTER	11	t	1
97	Stuck On	8	t	1
98	Data Extraction	1	t	1
1	Lights Not Working	3	t	1
2	Communication Failure	7	t	1
3	Audio Problem	4	t	1
4	Misc.	1	t	1
8	Comm ERROR	10	t	1
9	Comm RESTORED	10	t	1
10	System RESTARTED	1	t	1
11	Bulb Out	8	t	1
12	Other	1	t	1
13	No Data	11	t	1
14	Occupancy Spiking	11	t	1
15	Locked On	11	t	1
16	Incorrect Information	11	t	1
17	Other	11	t	1
18	Failure	10	t	1
19	Software Problem	4	t	1
20	Visor Damage	7	t	1
21	Sign Problem	7	t	1
22	Other	8	t	1
23	Other	5	t	1
24	Improper Message	5	t	1
25	Communication Failure	5	t	1
26	Other	3	t	1
27	Monitor Problem	4	t	1
28	Keyboard Malfunction	4	t	1
29	Other	4	t	1
30	In Flash	7	t	1
31	Lens Cracked	7	t	1
32	No Wiper Fluid	6	t	1
33	Unstable Picture	6	t	1
34	Other	2	t	1
35	Seek Error	2	t	1
36	Low LED Light Levels	2	t	1
37	Pixel Errors	2	t	1
38	Communication Failure	2	t	1
39	Other	7	t	1
40	Shield Damage	7	t	1
41	Warning Flasher Problem	7	t	1
42	Head Turned	7	t	1
43	Other	6	t	1
44	Focus Malfunction	6	t	1
45	Wiper Malfunction	6	t	1
46	Poor Picture Quality	6	t	1
47	P-T-Z Malfunction	6	t	1
48	Removed	7	t	1
49	Indication Failure	7	t	1
50	Power Outage	5	t	1
51	Power Outage	2	t	1
52	Power Outage	8	t	1
53	Power Outage	7	t	1
54	Power Outage	6	t	1
55	Other	10	t	1
56	Stuck Closed	3	t	1
57	Stuck Open	3	t	1
58	Communication Failure	8	t	1
59	Knock Down	3	t	1
60	Rolling Message	2	t	1
61	Message Stuck	2	t	1
62	No Picture	6	t	1
63	Knock Down	7	t	1
99	Workstation Problem	4	t	1
100	PEM Problem	4	t	1
\.


--
-- Data for TOC entry 80 (OID 7951408)
-- Name: device_type; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY device_type (device_type_id, description, active) FROM stdin;
2	DMS	t
3	Gate Arm	t
4	Control Room	t
5	Led HOV	t
6	Camera	t
7	Meter	t
8	Lane Control Sign	t
10	Communication Line	t
11	Detector	t
1	System	t
12	Remote Video	t
\.


--
-- Data for TOC entry 81 (OID 7951471)
-- Name: tms_user; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY tms_user (id, description) FROM stdin;
\.


--
-- Data for TOC entry 82 (OID 7951479)
-- Name: settings; Type: TABLE DATA; Schema: public; Owner: tms
--

COPY settings (parameter, value) FROM stdin;
comm line purge threshold	7
sign status purge threshold	-1
device failure purge threshold	-1
\.



--
-- TOC entry 72 (OID 9114583)
-- Name: sign_status_event_pkey; Type: INDEX; Schema: public; Owner: tms
--

CREATE INDEX sign_status_event_pkey ON sign_status_event USING btree (event_id);


--
-- TOC entry 71 (OID 9114584)
-- Name: sign_status_event_date; Type: INDEX; Schema: public; Owner: tms
--

CREATE INDEX sign_status_event_date ON sign_status_event USING btree (event_date);


--
-- TOC entry 67 (OID 9114586)
-- Name: system_event_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY system_event
    ADD CONSTRAINT system_event_pkey PRIMARY KEY (event_id);


--
-- TOC entry 68 (OID 9114588)
-- Name: comm_line_event_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY comm_line_event
    ADD CONSTRAINT comm_line_event_pkey PRIMARY KEY (event_id);


--
-- TOC entry 69 (OID 9114592)
-- Name: event_description_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY event_description
    ADD CONSTRAINT event_description_pkey PRIMARY KEY (event_desc_id);


--
-- TOC entry 70 (OID 9114600)
-- Name: device_type_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY device_type
    ADD CONSTRAINT device_type_pkey PRIMARY KEY (device_type_id);


--
-- TOC entry 73 (OID 9114606)
-- Name: device_pkey; Type: CONSTRAINT; Schema: public; Owner: tms
--

ALTER TABLE ONLY device_failure_event
    ADD CONSTRAINT device_pkey PRIMARY KEY (event_id);


--
-- TOC entry 86 (OID 9114614)
-- Name: RI_ConstraintTrigger_9114614; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON system_event
    FROM event_description
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'system_event', 'event_description', 'UNSPECIFIED', 'event_desc_id', 'event_desc_id');


--
-- TOC entry 88 (OID 9114615)
-- Name: RI_ConstraintTrigger_9114615; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON event_description
    FROM system_event
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'system_event', 'event_description', 'UNSPECIFIED', 'event_desc_id', 'event_desc_id');


--
-- TOC entry 89 (OID 9114616)
-- Name: RI_ConstraintTrigger_9114616; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON event_description
    FROM system_event
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'system_event', 'event_description', 'UNSPECIFIED', 'event_desc_id', 'event_desc_id');


--
-- TOC entry 87 (OID 9114617)
-- Name: RI_ConstraintTrigger_9114617; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON comm_line_event
    FROM event_description
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'comm_line_event', 'event_description', 'UNSPECIFIED', 'event_desc_id', 'event_desc_id');


--
-- TOC entry 90 (OID 9114618)
-- Name: RI_ConstraintTrigger_9114618; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON event_description
    FROM comm_line_event
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'comm_line_event', 'event_description', 'UNSPECIFIED', 'event_desc_id', 'event_desc_id');


--
-- TOC entry 91 (OID 9114619)
-- Name: RI_ConstraintTrigger_9114619; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON event_description
    FROM comm_line_event
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'comm_line_event', 'event_description', 'UNSPECIFIED', 'event_desc_id', 'event_desc_id');


--
-- TOC entry 92 (OID 9114623)
-- Name: RI_ConstraintTrigger_9114623; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER INSERT OR UPDATE ON event_description
    FROM device_type
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_check_ins"('<unnamed>', 'event_description', 'device_type', 'UNSPECIFIED', 'device_type_id', 'device_type_id');


--
-- TOC entry 93 (OID 9114624)
-- Name: RI_ConstraintTrigger_9114624; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER DELETE ON device_type
    FROM event_description
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_del"('<unnamed>', 'event_description', 'device_type', 'UNSPECIFIED', 'device_type_id', 'device_type_id');


--
-- TOC entry 94 (OID 9114625)
-- Name: RI_ConstraintTrigger_9114625; Type: TRIGGER; Schema: public; Owner: tms
--

CREATE CONSTRAINT TRIGGER "<unnamed>"
    AFTER UPDATE ON device_type
    FROM event_description
    NOT DEFERRABLE INITIALLY IMMEDIATE
    FOR EACH ROW
    EXECUTE PROCEDURE "RI_FKey_noaction_upd"('<unnamed>', 'event_description', 'device_type', 'UNSPECIFIED', 'device_type_id', 'device_type_id');


--
-- TOC entry 6 (OID 7951422)
-- Name: event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('event_id_seq', 9951119, true);


--
-- TOC entry 9 (OID 7951424)
-- Name: event_desc_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('event_desc_id_seq', 100, true);


--
-- TOC entry 12 (OID 7951426)
-- Name: delay_reason_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('delay_reason_id_seq', 7, false);


--
-- TOC entry 15 (OID 7951428)
-- Name: device_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('device_type_id_seq', 12, true);


--
-- TOC entry 18 (OID 7951430)
-- Name: close_reason_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('close_reason_id_seq', 6, false);


--
-- TOC entry 21 (OID 7951432)
-- Name: device_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('device_id_seq', 97, true);


--
-- TOC entry 24 (OID 7951469)
-- Name: user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('user_id_seq', 1, false);


--
-- TOC entry 26 (OID 7951477)
-- Name: tms_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('tms_user_id_seq', 239, true);


--
-- TOC entry 28 (OID 7951548)
-- Name: incident_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('incident_id_seq', 305222, true);


--
-- TOC entry 31 (OID 7951554)
-- Name: atp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('atp_id_seq', 9270, true);


--
-- TOC entry 34 (OID 7951556)
-- Name: road_link_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tms
--

SELECT pg_catalog.setval('road_link_id_seq', 1000, false);


SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 3 (OID 2200)
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';


